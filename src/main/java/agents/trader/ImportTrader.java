// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader;

import java.util.ArrayList;
import java.util.List;
import agents.forecast.MarketForecaster;
import agents.markets.DayAheadMarket;
import agents.markets.DayAheadMarketTrader;
import agents.markets.meritOrder.Bid.Type;
import communications.message.AwardData;
import communications.message.BidData;
import communications.message.ClearingTimes;
import communications.message.PointInTime;
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

/** Offers imported energy at {@link EnergyExchange} according to given {@link TimeSeries} of energy import as supply.
 *
 * @author Christoph Schimeczek, A. Achraf El Ghazi, Felix Nitsch */
public class ImportTrader extends Trader {
	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newGroup("Imports").list().add(Make.newSeries("AvailableEnergyForImport"),
					Make.newDouble("ImportCostInEURperMWH")))
			.buildTree();

	@Output
	private static enum OutputColumns {
		/** Energy offered to energy exchange */
		OfferedEnergyInMWH,
		/** Energy awarded by energy exchange */
		AwardedEnergyInMWH
	};

	/** Represents one energy import TimeSeries with a fixed associated value of import cost */
	private class EnergyImport {
		public final TimeSeries tsEnergyImportInMWH;
		public final double importCostInEURperMWH;

		public EnergyImport(TimeSeries importSeries, double importCost) {
			this.tsEnergyImportInMWH = importSeries;
			this.importCostInEURperMWH = importCost;
		}
	}

	private ArrayList<EnergyImport> imports = new ArrayList<>();

	/** Creates a {@link ImportTrader}
	 * 
	 * @param dataProvider provides input from config file
	 * @throws MissingDataException if any required data is not provided */
	public ImportTrader(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		for (ParameterData group : input.getGroupList("Imports")) {
			imports.add(
					new EnergyImport(group.getTimeSeries("AvailableEnergyForImport"), group.getDouble("ImportCostInEURperMWH")));
		}

		call(this::prepareForecasts).on(Trader.Products.BidsForecast).use(MarketForecaster.Products.ForecastRequest);
		call(this::prepareBids).on(DayAheadMarketTrader.Products.Bids).use(DayAheadMarket.Products.GateClosureInfo);
		call(this::evaluateAwardedSupplyBids).on(DayAheadMarket.Products.Awards).use(DayAheadMarket.Products.Awards);
	}

	/** Prepares forecasts and sends them to the {@link MarketForecaster} */
	private void prepareForecasts(ArrayList<Message> input, List<Contract> contracts) {
		prepareBidsMultipleTimes(input, contracts);
	}

	/** Calculates and submits supply bids
	 * 
	 * @param input exactly one ClearingTimes message
	 * @param contracts exactly one contracted partner to receive bids
	 * @return sum of supply bid energies */
	private double prepareBidsMultipleTimes(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		ClearingTimes clearingTimes = CommUtils.getExactlyOneEntry(input).getDataItemOfType(ClearingTimes.class);
		double totalSuppliedEnergyInMWH = 0;
		for (TimeStamp targetTime : clearingTimes.getTimes()) {
			List<BidData> bids = prepareBidsFor(targetTime);
			for (BidData bid : bids) {
				fulfilNext(contract, bid, new PointInTime(targetTime));
				totalSuppliedEnergyInMWH += bid.offeredEnergyInMWH;
			}
		}
		return totalSuppliedEnergyInMWH;
	}

	/** Prepares hourly supply bids */
	private ArrayList<BidData> prepareBidsFor(TimeStamp requestedTime) {
		ArrayList<BidData> bids = new ArrayList<>();
		for (EnergyImport energyImport : imports) {
			double offeredEnergyInMWH = energyImport.tsEnergyImportInMWH.getValueLinear(requestedTime);
			bids.add(new BidData(offeredEnergyInMWH, energyImport.importCostInEURperMWH, getId(), Type.Supply, requestedTime));
		}
		return bids;
	}

	/** Prepares supply bids and sends them to the {@link EnergyExchange} */
	private void prepareBids(ArrayList<Message> input, List<Contract> contracts) {
		double totalSupplyedEnergyInMWH = prepareBidsMultipleTimes(input, contracts);
		store(OutputColumns.OfferedEnergyInMWH, totalSupplyedEnergyInMWH);
	}

	/** Writes out the total awarded supply */
	private void evaluateAwardedSupplyBids(ArrayList<Message> input, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(input);
		double awardedPower = message.getDataItemOfType(AwardData.class).supplyEnergyInMWH;
		store(OutputColumns.AwardedEnergyInMWH, awardedPower);
	}
}
