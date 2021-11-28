package agents.trader;

import java.util.ArrayList;
import java.util.List;
import agents.forecast.MarketForecaster;
import agents.markets.EnergyExchange;
import agents.markets.meritOrder.Bid.Type;
import communications.message.AwardData;
import communications.message.BidData;
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

/** Purchases energy at {@link EnergyExchange} according to given {@link TimeSeries} of energy demand
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

		call(this::sendDemandForecast).on(Trader.Products.BidsForecast).use(MarketForecaster.Products.ForecastRequest);
		call(this::prepareBids).on(Trader.Products.Bids);
		call(this::evaluateAwardedDemandBids).on(EnergyExchange.Products.Awards).use(EnergyExchange.Products.Awards);
	}

	/** Prepares demand bids and sends them to the {@link EnergyExchange} */
	private void prepareBids(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		List<BidData> bids = prepareBidsFor(now().laterByOne()); // HACK!! Target Time has been hacked
		double totalRequestedEnergyInMWH = 0;
		for (BidData bid : bids) {
			fulfilNext(contract, bid);
			totalRequestedEnergyInMWH += bid.offeredEnergyInMWH;
		}
		store(OutputColumns.RequestedEnergyInMWH, totalRequestedEnergyInMWH);
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

	/** Writes out the total awarded demand */
	private void evaluateAwardedDemandBids(ArrayList<Message> input, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(input);
		double awardedPower = message.getDataItemOfType(AwardData.class).demandEnergyInMWH;
		store(OutputColumns.AwardedEnergyInMWH, awardedPower);
	}

	/** Sends demand forecast for requested time to contracted partner
	 * 
	 * @param input forecast request(s) that specify the time(s) at which forecast(s) are requested
	 * @param contracts single contract with Forecaster to receive forecasted demand */
	private void sendDemandForecast(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		for (Message message : input) {
			TimeStamp requestedTime = (message.getDataItemOfType(PointInTime.class)).timeStamp;
			List<BidData> bids = prepareBidsFor(requestedTime);
			for (BidData bid : bids) {
				fulfilNext(contract, bid);
			}
		}
	}
}