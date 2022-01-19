package agents.trader;

import java.util.ArrayList;
import java.util.List;
import agents.forecast.MeritOrderForecaster;
import agents.markets.EnergyExchange;
import agents.markets.meritOrder.Bid.Type;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.SupplyOrderBook;
import agents.storage.Device;
import agents.storage.DispatchSchedule;
import agents.storage.arbitrageStrategists.ArbitrageStrategist;
import agents.storage.arbitrageStrategists.ArbitrageStrategist.StrategistType;
import agents.storage.arbitrageStrategists.FileDispatcher;
import agents.storage.arbitrageStrategists.SystemCostMinimiser;
import communications.message.AwardData;
import communications.message.BidData;
import communications.message.ClearingTimes;
import communications.message.PointInTime;
import communications.portable.MeritOrderMessage;
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
import de.dlr.gitlab.fame.time.Constants.Interval;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Sells and buys energy utilising a Storage {@link Device} at the EnergyExchange
 * 
 * @author Christoph Schimeczek, Johannes Kochems, Farzad Sarfarazi, Felix Nitsch */
public class StorageTrader extends Trader {
	@Input private static final Tree parameters = Make.newTree().addAs("Device", Device.parameters)
			.add(Make.newInt("ForecastRequestOffsetInSeconds"),
					Make.newGroup("Strategy").add(Make.newInt("ForecastPeriodInHours"), Make.newInt("ScheduleDurationInHours"),
							Make.newEnum("StrategistType", StrategistType.class),
							Make.newGroup("SingleAgent").add(Make.newInt("ModelledChargingSteps").optional(),
									Make.newDouble("PurchaseLeviesAndTaxesInEURperMWH").optional()),
							Make.newGroup("MultiAgent").add(Make.newDouble("AssessmentFunctionPrefactors").optional().list()),
							Make.newGroup("FixedDispatch").add(Make.newSeries("Schedule").optional())))
			.buildTree();

	@Output
	private static enum OutputFields {
		OfferedPowerInMW, OfferedChargePrice, OfferedDischargePrice, AwardedChargePower, AwardedDischargePower,
		AwardedPower, StoredMWh, Revenues, Costs, Profit
	};

	private final TimeSpan forecastRequestOffset;
	private final Device storage;
	private final ArbitrageStrategist strategist;
	private DispatchSchedule schedule;
	private TimeSpan operationPeriod = new TimeSpan(1, Interval.HOURS);

	/** Creates a {@link StorageTrader}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public StorageTrader(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		this.storage = new Device(input.getGroup("Device"));
		this.strategist = createStrategist(input.getGroup("Strategy"));
		forecastRequestOffset = new TimeSpan(input.getInteger("ForecastRequestOffsetInSeconds"));

		call(this::requestMeritOrderForecast).on(Trader.Products.MeritOrderForecastRequest);
		call(this::updateMeritOrderForecast).on(MeritOrderForecaster.Products.MeritOrderForecast)
				.use(MeritOrderForecaster.Products.MeritOrderForecast);
		call(this::requestPriceForecast).on(Trader.Products.PriceForecastRequest);
		call(this::prepareBids).on(Trader.Products.Bids).use(EnergyExchange.Products.GateClosureInfo);
		call(this::digestAwards).on(EnergyExchange.Products.Awards).use(EnergyExchange.Products.Awards);
	}

	/** Creates new {@link ArbitrageStrategist} based on given Strategy information
	 * 
	 * @param data parameter group for "Strategy" parameters
	 * @return created Strategist
	 * @throws MissingDataException if any required data is not provided */
	private ArbitrageStrategist createStrategist(ParameterData data) throws MissingDataException {
		StrategistType strategistType = data.getEnum("StrategistType", StrategistType.class);
		int forecastPeriodInHours = data.getInteger("ForecastPeriodInHours");
		int scheduleDurationInHours = data.getInteger("ScheduleDurationInHours");

		switch (strategistType) {
			case SINGLE_AGENT_MIN_SYSTEM_COST: {
				int chargingSteps = data.getInteger("SingleAgent.ModelledChargingSteps");
				return new SystemCostMinimiser(forecastPeriodInHours, storage, scheduleDurationInHours, chargingSteps);
			}
			case DISPATCH_FILE: {
				TimeSeries dispatchSchedule = data.getTimeSeries("FixedDispatch.Schedule");
				return new FileDispatcher(forecastPeriodInHours, storage, scheduleDurationInHours, dispatchSchedule);
			}
			default:
				throw new RuntimeException("Storage Strategist not implemented: " + strategistType);
		}
	}

	/** Requests MeritOrderForecast from contracted partner (Forecaster)
	 * 
	 * @param input not used
	 * @param contracts single contracted Forecaster to request forecast from */
	private void requestMeritOrderForecast(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TimePeriod nextTime = new TimePeriod(now().laterBy(forecastRequestOffset), operationPeriod);
		ArrayList<TimeStamp> missingForecastTimes = strategist.getTimesMissingForecasts(nextTime);
		for (TimeStamp missingForecastTime : missingForecastTimes) {
			PointInTime pointInTime = new PointInTime(missingForecastTime);
			fulfilNext(contract, pointInTime);
		}
	}

	/** Digests incoming {@link MeritOrderMessage} forecasts
	 * 
	 * @param input one or multiple merit order forecast message(s)
	 * @param contracts not used */
	private void updateMeritOrderForecast(ArrayList<Message> input, List<Contract> contracts) {
		for (Message inputMessage : input) {
			MeritOrderMessage meritOrderMessage = inputMessage.getAllPortableItemsOfType(MeritOrderMessage.class).get(0);
			SupplyOrderBook supplyOrderBook = meritOrderMessage.getSupplyOrderBook();
			DemandOrderBook demandOrderBook = meritOrderMessage.getDemandOrderBook();
			TimePeriod timeSegment = new TimePeriod(meritOrderMessage.getTimeStamp(), operationPeriod);
			strategist.storeMeritOrderForesight(timeSegment, supplyOrderBook, demandOrderBook);
		}
	}

	/** Requests PriceForecast from contracted partner (Forecaster)
	 * 
	 * @param input not used
	 * @param contracts single contracted Forecaster to request forecast from */
	private void requestPriceForecast(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TimePeriod nextTime = new TimePeriod(now().laterBy(forecastRequestOffset), operationPeriod);
		ArrayList<TimeStamp> missingForecastTimes = strategist.getTimesMissingForecasts(nextTime);
		for (TimeStamp missingForecastTime : missingForecastTimes) {
			PointInTime pointInTime = new PointInTime(missingForecastTime);
			fulfilNext(contract, pointInTime);
		}
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
			BidData demandBid = prepareHourlyDemandBids(targetTime);
			BidData supplyBid = prepareHourlySupplyBids(targetTime);
			store(OutputFields.OfferedPowerInMW, supplyBid.offeredEnergyInMWH - demandBid.offeredEnergyInMWH);
			fulfilNext(contractToFulfil, demandBid);
			fulfilNext(contractToFulfil, supplyBid);
		}
	}

	/** Clears past sensitivities and creates new schedule based on current energy storage level
	 * 
	 * @param targetTime TimeStamp of bid to prepare */
	private void excuteBeforeBidPreparation(TimeStamp targetTime) {
		if (schedule == null || !schedule.isApplicable(targetTime, storage.getCurrentEnergyInStorageInMWH())) {
			strategist.clearSensitivitiesBefore(now());
			TimePeriod targetTimeSegment = new TimePeriod(targetTime, operationPeriod);
			schedule = strategist.createSchedule(targetTimeSegment, storage.getCurrentEnergyInStorageInMWH());
		}
	}

	/** Prepares hourly demand bids
	 * 
	 * @param requestedTime TimeStamp at which the demand bid should be defined
	 * @return demand bid for requestedTime */
	private BidData prepareHourlyDemandBids(TimeStamp requestedTime) {
		double demandPower = schedule.getScheduledChargingPowerInMW(requestedTime);
		double price = schedule.getScheduledBidInHourInEURperMWH(requestedTime);
		BidData demandBid = new BidData(demandPower, price, Double.NaN, getId(), Type.Demand, requestedTime);
		store(OutputFields.OfferedChargePrice, price);
		return demandBid;
	}

	/** Prepares hourly supply bid
	 * 
	 * @param requestedTime TimeStamp at which the supply bid should be defined
	 * @return supply bid for requestedTime */
	private BidData prepareHourlySupplyBids(TimeStamp requestedTime) {
		double supplyPower = schedule.getScheduledDischargingPowerInMW(requestedTime);
		double price = schedule.getScheduledBidInHourInEURperMWH(requestedTime);
		BidData supplyBid = new BidData(supplyPower, price, Double.NaN, getId(), Type.Supply, requestedTime);
		store(OutputFields.OfferedDischargePrice, price);
		return supplyBid;
	}

	/** Digests award information from {@link EnergyExchange} and writes out award data
	 * 
	 * @param input award information received from {@link EnergyExchange}
	 * @param contracts not used */
	private void digestAwards(ArrayList<Message> input, List<Contract> contracts) {
		Message awards = CommUtils.getExactlyOneEntry(input);
		double awardedChargePower = awards.getDataItemOfType(AwardData.class).demandEnergyInMWH;
		double awardedDischargePower = awards.getDataItemOfType(AwardData.class).supplyEnergyInMWH;
		double externalPowerDelta = awardedChargePower - awardedDischargePower;
		double powerPrice = awards.getDataItemOfType(AwardData.class).powerPriceInEURperMWH;
		double revenues = powerPrice * awardedDischargePower;
		double costs = powerPrice * awardedChargePower;
		storage.chargeInMW(externalPowerDelta);
		store(OutputFields.AwardedChargePower, awardedChargePower);
		store(OutputFields.AwardedDischargePower, -awardedDischargePower);
		store(OutputFields.AwardedPower, externalPowerDelta);
		store(OutputFields.StoredMWh, storage.getCurrentEnergyInStorageInMWH());
		store(OutputFields.Revenues, revenues);
		store(OutputFields.Costs, costs);
		store(OutputFields.Profit, revenues - costs);
	}
}