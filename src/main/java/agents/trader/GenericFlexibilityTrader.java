package agents.trader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import agents.flexibility.BidSchedule;
import agents.flexibility.GenericDevice;
import agents.flexibility.dynamicProgramming.Strategist;
import agents.flexibility.dynamicProgramming.Strategist.Target;
import agents.flexibility.dynamicProgramming.assessment.AssessmentFunction;
import agents.flexibility.dynamicProgramming.assessment.AssessmentFunctionBuilder;
import agents.flexibility.dynamicProgramming.bidding.BidSchedulerBuilder;
import agents.flexibility.dynamicProgramming.states.StateManager;
import agents.flexibility.dynamicProgramming.states.StateManagerBuilder;
import agents.forecast.Forecaster;
import agents.markets.DayAheadMarket;
import agents.markets.DayAheadMarketTrader;
import agents.markets.meritOrder.Bid;
import communications.message.AwardData;
import communications.message.ClearingTimes;
import communications.message.PointInTime;
import communications.portable.BidsAtTime;
import communications.portable.MeritOrderMessage;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.Constants.Interval;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Trader that operates a generic flexibility device, e.g., a battery storage, pumped-hydro storage, load shifting, heat pumps...
 * It can use different dispatch optimisation strategies, optimisation targets, and strategy assessments. Optimisation is based on
 * dynamic programming.
 * 
 * @author Felix Nitsch, Christoph Schimeczek, Johannes Kochems */
public class GenericFlexibilityTrader extends Trader {
	@Input private static final Tree parameters = Make.newTree().addAs("Device", GenericDevice.parameters)
			.add(Strategist.TARGET_PARAM).addAs("Assessment", AssessmentFunctionBuilder.parameters)
			.addAs("StateManagement", StateManagerBuilder.parameters).addAs("Bidding", BidSchedulerBuilder.parameters)
			.buildTree();

	/** Products of {@link FlexibilityTrader}s */
	@Product
	public static enum Products {
		/** Requests for price-forecasts */
		PriceForecastRequest,
	}

	/** Output columns of {@link FlexibilityTrader}s */
	@Output
	protected static enum Outputs {
		/** Total received money in EUR */
		ReceivedMoneyInEUR,
		OfferedChargePriceInEURperMWH, OfferedDischargePriceInEURperMWH, AwardedChargeEnergyInMWH,
		AwardedDischargeEnergyInMWH, StoredEnergyInMWH, VariableCostsInEUR
	}

	private static final TimeSpan OPERATION_PERIOD = new TimeSpan(1, Interval.HOURS);

	private final GenericDevice device;
	private final AssessmentFunction assessmentFunction;
	private final StateManager stateManager;
	private final Strategist strategist;
	private BidSchedule bidSchedule;

	public GenericFlexibilityTrader(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);

		device = new GenericDevice(input.getGroup("Device"));
		assessmentFunction = AssessmentFunctionBuilder.build(input.getGroup("Assessment"));
		stateManager = StateManagerBuilder.build(device, assessmentFunction, input.getGroup("StateManagement"));
		var bidScheduler = BidSchedulerBuilder.build(input.getGroup("Bidding"));
		strategist = new Strategist(stateManager, bidScheduler, input.getEnum("OptimisationTarget", Target.class));

		call(this::requestElectricityForecast).on(Products.PriceForecastRequest)
				.use(DayAheadMarket.Products.GateClosureInfo);
		call(this::updateForecast).onAndUse(Forecaster.Products.PriceForecast);
		call(this::prepareBids).on(DayAheadMarketTrader.Products.Bids).use(DayAheadMarket.Products.GateClosureInfo);
		call(this::digestAwards).onAndUse(DayAheadMarket.Products.Awards);
	}

	/** Requests a forecast from a contracted Forecaster. The type of forecast (either {@link MeritOrderMessage} or PriceForecast)
	 * is determined by the contract.
	 * 
	 * @param input one ClearingTimes message from connected {@link DayAheadMarket}
	 * @param contracts single contracted Forecaster to request forecast from */
	protected void requestElectricityForecast(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		ClearingTimes clearingTimes = CommUtils.getExactlyOneEntry(input).getDataItemOfType(ClearingTimes.class);
		TimePeriod nextTime = new TimePeriod(clearingTimes.getTimes().get(0), OPERATION_PERIOD);
		var allPlanningTimes = stateManager.getPlanningTimes(nextTime);
		var missingForecastTimes = assessmentFunction.getMissingForecastTimes(allPlanningTimes);
		for (TimeStamp missingForecastTime : missingForecastTimes) {
			PointInTime pointInTime = new PointInTime(missingForecastTime);
			fulfilNext(contract, pointInTime);
		}
	}

	/** Store input forecasts for dispatch planning
	 * 
	 * @param input to be stored
	 * @param contracts not used */
	protected void updateForecast(ArrayList<Message> input, List<Contract> contracts) {
		assessmentFunction.storeForecast(input);
	}

	/** Prepares and sends Bids to the contracted partner
	 * 
	 * @param input one ClearingTimes message
	 * @param contracts one partner */
	private void prepareBids(ArrayList<Message> input, List<Contract> contracts) {
		Contract contractToFulfil = CommUtils.getExactlyOneEntry(contracts);
		ClearingTimes clearingTimes = CommUtils.getExactlyOneEntry(input).getDataItemOfType(ClearingTimes.class);
		List<TimeStamp> targetTimes = clearingTimes.getTimes();
		for (TimeStamp targetTime : targetTimes) {
			excuteBeforeBidPreparation(targetTime);
			Bid demandBid = prepareHourlyDemandBids(targetTime);
			Bid supplyBid = prepareHourlySupplyBids(targetTime);
			store(OutputColumns.OfferedEnergyInMWH, supplyBid.getEnergyAmountInMWH() - demandBid.getEnergyAmountInMWH());
			fulfilNext(contractToFulfil,
					new BidsAtTime(targetTime, getId(), Arrays.asList(supplyBid), Arrays.asList(demandBid)));
		}
	}

	/** Clears past sensitivities and creates new schedule based on current energy storage level
	 * 
	 * @param targetTime TimeStamp of bid to prepare */
	private void excuteBeforeBidPreparation(TimeStamp targetTime) {
		if (bidSchedule == null || !bidSchedule.isApplicable(targetTime, device.getCurrentInternalEnergyInMWH())) {
			assessmentFunction.clearBefore(now());
			bidSchedule = strategist.createSchedule(new TimePeriod(targetTime, OPERATION_PERIOD));
		}
	}

	/** Prepares hourly demand bid
	 * 
	 * @param requestedTime TimeStamp at which the demand bid should be defined
	 * @return demand bid for requestedTime */
	private Bid prepareHourlyDemandBids(TimeStamp requestedTime) {
		double demandPower = bidSchedule.getScheduledEnergyPurchaseInMWH(requestedTime);
		double price = bidSchedule.getScheduledBidInHourInEURperMWH(requestedTime);
		Bid demandBid = new Bid(demandPower, price, Double.NaN);
		store(Outputs.OfferedChargePriceInEURperMWH, price);
		return demandBid;
	}

	/** Prepares hourly supply bid
	 * 
	 * @param requestedTime TimeStamp at which the supply bid should be defined
	 * @return supply bid for requestedTime */
	private Bid prepareHourlySupplyBids(TimeStamp requestedTime) {
		double supplyPower = bidSchedule.getScheduledEnergySalesInMWH(requestedTime);
		double price = bidSchedule.getScheduledBidInHourInEURperMWH(requestedTime);
		Bid supplyBid = new Bid(supplyPower, price, Double.NaN);
		store(Outputs.OfferedDischargePriceInEURperMWH, price);
		return supplyBid;
	}

	/** Digests award information from {@link DayAheadMarket} and writes out award data
	 * 
	 * @param input award information received from {@link DayAheadMarket}
	 * @param contracts not used */
	private void digestAwards(ArrayList<Message> input, List<Contract> contracts) {
		AwardData awards = CommUtils.getExactlyOneEntry(input).getDataItemOfType(AwardData.class);
		double awardedChargeEnergyInMWH = awards.demandEnergyInMWH;
		double awardedDischargeEnergyInMWH = awards.supplyEnergyInMWH;
		double externalEnergyDeltaInMWH = awardedChargeEnergyInMWH - awardedDischargeEnergyInMWH;
		double powerPriceInEURperMWH = awards.powerPriceInEURperMWH;
		double revenuesInEUR = powerPriceInEURperMWH * awardedDischargeEnergyInMWH;
		double costsInEUR = powerPriceInEURperMWH * awardedChargeEnergyInMWH;

		device.transition(awards.beginOfDeliveryInterval, externalEnergyDeltaInMWH, OPERATION_PERIOD);

		store(Outputs.AwardedDischargeEnergyInMWH, awardedDischargeEnergyInMWH);
		store(Outputs.AwardedChargeEnergyInMWH, awardedChargeEnergyInMWH);
		store(OutputColumns.AwardedEnergyInMWH, externalEnergyDeltaInMWH);
		store(Outputs.StoredEnergyInMWH, device.getCurrentInternalEnergyInMWH());
		store(Outputs.ReceivedMoneyInEUR, revenuesInEUR);
		store(Outputs.VariableCostsInEUR, costsInEUR);
	}
}
