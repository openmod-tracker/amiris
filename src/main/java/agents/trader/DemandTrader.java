// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader;

import java.util.ArrayList;
import java.util.List;
import agents.forecast.MarketForecaster;
import agents.markets.DayAheadMarketSingleZone;
import agents.markets.meritOrder.Bid.Type;
import communications.message.AwardData;
import communications.message.BidData;
import communications.message.ClearingTimes;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Purchases energy at {@link DayAheadMarketSingleZone} according to given {@link TimeSeries} of energy demand
 *
 * @author Christoph Schimeczek, Ulrich Frey, Marc Deissenroth, Johannes Kochems */
public class DemandTrader extends Trader {
	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newGroup("Loads").list().add(Make.newSeries("DemandSeries"), Make.newDouble("ValueOfLostLoad")))
			.buildTree();

	@Output
	private static enum OutputColumns {
		/** Energy demanded from energy exchange */
		RequestedEnergyInMWH,
		/** Energy awarded by energy exchange */
		AwardedEnergyInMWH
	};

	/** Helper class that represents one load TimeSeries with a fixed associated value of lost load */
	private class Load {
		public final TimeSeries tsEnergyDemandInMWHperTimeSegment;
		public final double valueOfLostLoadInEURperMWH;

		public Load(TimeSeries demandSeries, double valueOfLostLoad) {
			this.tsEnergyDemandInMWHperTimeSegment = demandSeries;
			this.valueOfLostLoadInEURperMWH = valueOfLostLoad;
		}
	}

	private ArrayList<Load> loads = new ArrayList<>();

	/** Creates a {@link DemandTrader}
	 * 
	 * @param dataProvider provides input from config file
	 * @throws MissingDataException if any required data is not provided */
	public DemandTrader(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		for (ParameterData group : input.getGroupList("Loads")) {
			loads.add(new Load(group.getTimeSeries("DemandSeries"), group.getDouble("ValueOfLostLoad")));
		}

		call(this::prepareForecasts).on(Trader.Products.BidsForecast).use(MarketForecaster.Products.ForecastRequest);
		call(this::prepareBids).on(Trader.Products.Bids).use(DayAheadMarketSingleZone.Products.GateClosureInfo);
		call(this::evaluateAwardedDemandBids).on(DayAheadMarketSingleZone.Products.Awards).use(DayAheadMarketSingleZone.Products.Awards);
	}

	/** Prepares forecasts and sends them to the {@link MarketForecaster} */
	private void prepareForecasts(ArrayList<Message> input, List<Contract> contracts) {
		prepareBidsMultipleTimes(input, contracts);
	}

	/** Calculates and submits demand bids
	 * 
	 * @param input single ClearingTimes message
	 * @param contracts one contracted partner to receive bids
	 * @return sum of demand bid energies */
	private double prepareBidsMultipleTimes(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		ClearingTimes clearingTimes = CommUtils.getExactlyOneEntry(input).getDataItemOfType(ClearingTimes.class);
		double totalRequestedEnergyInMWH = 0;
		for (TimeStamp targetTime : clearingTimes.getTimes()) {
			List<BidData> bids = prepareBidsFor(targetTime);
			for (BidData bid : bids) {
				fulfilNext(contract, bid);
				totalRequestedEnergyInMWH += bid.offeredEnergyInMWH;
			}
		}
		return totalRequestedEnergyInMWH;
	}

	/** Prepares hourly demand bids */
	private ArrayList<BidData> prepareBidsFor(TimeStamp requestedTime) {
		ArrayList<BidData> bids = new ArrayList<>();
		for (Load load : loads) {
			double requestedEnergyInMWH = load.tsEnergyDemandInMWHperTimeSegment.getValueLinear(requestedTime);
			double voll = load.valueOfLostLoadInEURperMWH;
			bids.add(new BidData(requestedEnergyInMWH, voll, getId(), Type.Demand, requestedTime));
		}
		return bids;
	}

	/** Prepares demand bids and sends them to the {@link DayAheadMarketSingleZone} */
	private void prepareBids(ArrayList<Message> input, List<Contract> contracts) {
		double totalRequestedEnergyInMWH = prepareBidsMultipleTimes(input, contracts);
		store(OutputColumns.RequestedEnergyInMWH, totalRequestedEnergyInMWH);
	}

	/** Writes out the total awarded demand */
	private void evaluateAwardedDemandBids(ArrayList<Message> input, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(input);
		double awardedPower = message.getDataItemOfType(AwardData.class).demandEnergyInMWH;
		store(OutputColumns.AwardedEnergyInMWH, awardedPower);
	}
}