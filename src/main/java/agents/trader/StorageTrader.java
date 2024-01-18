// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader;

import java.util.ArrayList;
import java.util.List;
import agents.flexibility.DispatchSchedule;
import agents.flexibility.Strategist;
import agents.forecast.Forecaster;
import agents.forecast.MarketForecaster;
import agents.markets.DayAheadMarket;
import agents.markets.DayAheadMarketTrader;
import agents.markets.meritOrder.Bid.Type;
import agents.markets.meritOrder.Constants;
import agents.storage.Device;
import agents.storage.arbitrageStrategists.ArbitrageStrategist;
import agents.storage.arbitrageStrategists.FileDispatcher;
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
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Sells and buys energy utilising a Storage {@link Device} at the {@link DayAheadMarket}
 * 
 * @author Christoph Schimeczek, Johannes Kochems, Farzad Sarfarazi, Felix Nitsch */
public class StorageTrader extends FlexibilityTrader {
	@Input private static final Tree parameters = Make.newTree().addAs("Device", Device.parameters)
			.addAs("Strategy", ArbitrageStrategist.parameters).buildTree();

	@Output
	private static enum OutputFields {
		OfferedEnergyInMWH, OfferedChargePriceInEURperMWH, OfferedDischargePriceInEURperMWH, AwardedChargeEnergyInMWH,
		AwardedDischargeEnergyInMWH, AwardedEnergyInMWH, StoredEnergyInMWH
	};

	private final Device storage;
	private final ArbitrageStrategist strategist;
	private DispatchSchedule schedule;

	/** Creates a {@link StorageTrader}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public StorageTrader(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		this.storage = new Device(input.getGroup("Device"));
		this.strategist = ArbitrageStrategist.createStrategist(input.getGroup("Strategy"), storage);

		call(this::prepareForecasts).on(Trader.Products.BidsForecast).use(MarketForecaster.Products.ForecastRequest);
		call(this::requestElectricityForecast).on(Trader.Products.MeritOrderForecastRequest);
		call(this::updateMeritOrderForecast).on(Forecaster.Products.MeritOrderForecast)
				.use(Forecaster.Products.MeritOrderForecast);
		call(this::requestElectricityForecast).on(Trader.Products.PriceForecastRequest);
		call(this::updateElectricityPriceForecast).on(Forecaster.Products.PriceForecast)
				.use(Forecaster.Products.PriceForecast);
		call(this::prepareBids).on(DayAheadMarketTrader.Products.Bids).use(DayAheadMarket.Products.GateClosureInfo);
		call(this::digestAwards).on(DayAheadMarket.Products.Awards).use(DayAheadMarket.Products.Awards);
	}

	/** Prepares forecasts and sends them to the {@link MarketForecaster}; Calling this function will throw an Exception for
	 * Strategists other than {@link FileDispatcher}
	 * 
	 * @param input one ClearingTimes message
	 * @param contracts one partner */
	private void prepareForecasts(ArrayList<Message> input, List<Contract> contracts) {
		Contract contractToFulfil = CommUtils.getExactlyOneEntry(contracts);
		ClearingTimes clearingTimes = CommUtils.getExactlyOneEntry(input).getDataItemOfType(ClearingTimes.class);
		List<TimeStamp> targetTimes = clearingTimes.getTimes();
		for (TimeStamp targetTime : targetTimes) {
			double chargingPowerInMW = strategist.getChargingPowerForecastInMW(targetTime);
			BidData bid;
			if (chargingPowerInMW > 0) {
				bid = new BidData(chargingPowerInMW, Constants.SCARCITY_PRICE_IN_EUR_PER_MWH, Double.NaN, getId(), Type.Demand,
						targetTime);
			} else {
				bid = new BidData(-chargingPowerInMW, Constants.MINIMAL_PRICE_IN_EUR_PER_MWH, Double.NaN, getId(), Type.Supply,
						targetTime);
			}
			fulfilNext(contractToFulfil, bid);
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
			store(OutputFields.OfferedEnergyInMWH, supplyBid.offeredEnergyInMWH - demandBid.offeredEnergyInMWH);
			sendDayAheadMarketBids(contractToFulfil, demandBid, supplyBid);
		}
	}

	/** Clears past sensitivities and creates new schedule based on current energy storage level
	 * 
	 * @param targetTime TimeStamp of bid to prepare */
	private void excuteBeforeBidPreparation(TimeStamp targetTime) {
		if (schedule == null || !schedule.isApplicable(targetTime, storage.getCurrentEnergyInStorageInMWH())) {
			strategist.clearSensitivitiesBefore(now());
			TimePeriod targetTimeSegment = new TimePeriod(targetTime, Strategist.OPERATION_PERIOD);
			schedule = strategist.createSchedule(targetTimeSegment);
			storage.clearDischargingDeviationBefore(targetTime.earlierBy(Strategist.OPERATION_PERIOD));
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
		store(OutputFields.OfferedChargePriceInEURperMWH, price);
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
		store(OutputFields.OfferedDischargePriceInEURperMWH, price);
		return supplyBid;
	}

	/** Digests award information from {@link DayAheadMarket} and writes out award data
	 * 
	 * @param input award information received from {@link DayAheadMarket}
	 * @param contracts not used */
	private void digestAwards(ArrayList<Message> input, List<Contract> contracts) {
		Message awards = CommUtils.getExactlyOneEntry(input);
		double awardedChargePower = awards.getDataItemOfType(AwardData.class).demandEnergyInMWH;
		double awardedDischargePower = awards.getDataItemOfType(AwardData.class).supplyEnergyInMWH;
		double externalPowerDelta = awardedChargePower - awardedDischargePower;
		double powerPrice = awards.getDataItemOfType(AwardData.class).powerPriceInEURperMWH;
		double revenues = powerPrice * awardedDischargePower;
		double costs = powerPrice * awardedChargePower;
		TimeStamp time = awards.getDataItemOfType(AwardData.class).beginOfDeliveryInterval;
		storage.chargeInMW(externalPowerDelta, time);

		store(OutputFields.AwardedDischargeEnergyInMWH, awardedDischargePower);
		store(OutputFields.AwardedChargeEnergyInMWH, awardedChargePower);
		store(OutputFields.AwardedEnergyInMWH, externalPowerDelta);
		store(OutputFields.StoredEnergyInMWH, storage.getCurrentEnergyInStorageInMWH());
		store(FlexibilityTrader.Outputs.ReceivedMoneyInEUR, revenues);
		store(FlexibilityTrader.Outputs.VariableCostsInEUR, costs);
	}

	@Override
	protected double getInstalledCapacityInMW() {
		return storage.getInternalPowerInMW();
	}

	@Override
	protected Strategist getStrategist() {
		return strategist;
	}
}