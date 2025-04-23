// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import agents.evAggregator.Fleet;
import agents.flexibility.Strategist;
import agents.forecast.Forecaster;
import agents.markets.DayAheadMarket;
import agents.markets.DayAheadMarketTrader;
import agents.markets.meritOrder.Bid;
import agents.markets.meritOrder.Constants;
import agents.storage.arbitrageStrategists.EvBiddingStrategist;
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
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Sells and buys energy utilizing a fleet of electric vehicles {@link Fleet} at the EnergyExchange. 
 * It has no own business logic, expect predicting the optimized load via connected ML-model.
 * 
 * @author A. Achraf El Ghazi, Ulrich Frey */
public class EvTraderExternal extends FlexibilityTrader {
	@Input private static final Tree parameters = Make.newTree()
			.add(
					Make.newString("ServiceUrl"),
					Make.newString("ModelId").optional(),
					Make.newInt("ForecastPeriodInHours"),
					Make.newSeries("AggregatedAvailableChargingPowerInMW"),
					Make.newSeries("AggregatedElectricConsumptionInMWH"))
			.addAs("PredictionWindows", EvBiddingStrategist.parameters)
			.buildTree();

	@Output
	private static enum OutputFields {
		OfferedChargePriceInEURperMWH,
		OfferedDischargePriceInEURperMWH,
		AwardedChargeEnergyInMWH,
		AwardedDischargeEnergyInMWH,
		StoredEnergyInMWH
	}

	private final EvBiddingStrategist biddingStrategist;

	/** Creates a {@link StorageTrader}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public EvTraderExternal(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		String urlService = input.getString("ServiceUrl");
		String modelId = input.getString("ModelId");
		int forecastPeriodInHours = input.getInteger("ForecastPeriodInHours");
		TimeSeries availableChargingPowerInMW = input.getTimeSeries("AggregatedAvailableChargingPowerInMW");
		TimeSeries elecConsumptionInMWH = input.getTimeSeries("AggregatedElectricConsumptionInMWH");
		biddingStrategist = new EvBiddingStrategist(urlService, modelId, forecastPeriodInHours,
				availableChargingPowerInMW, elecConsumptionInMWH, input.getGroup("PredictionWindows"));

		call(this::requestPriceForecast).on(Products.MeritOrderForecastRequest)
				.use(DayAheadMarket.Products.GateClosureInfo);
		call(this::updatePriceForecast).on(Forecaster.Products.MeritOrderForecast)
				.use(Forecaster.Products.MeritOrderForecast);
		call(this::prepareBids).on(DayAheadMarketTrader.Products.Bids).use(DayAheadMarket.Products.GateClosureInfo);
		call(this::digestAwards).on(DayAheadMarket.Products.Awards).use(DayAheadMarket.Products.Awards);
	}

	/** Requests PriceForecast from contracted partner (Forecaster)
	 * 
	 * @param input not used
	 * @param contracts single contracted Forecaster to request forecast from */
	private void requestPriceForecast(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		ClearingTimes clearingTimes = CommUtils.getExactlyOneEntry(input).getDataItemOfType(ClearingTimes.class);
		TimePeriod nextTime = new TimePeriod(clearingTimes.getTimes().get(0), Strategist.OPERATION_PERIOD);
		ArrayList<TimeStamp> missingForecastTimes = biddingStrategist.getTimesMissingForecasts(nextTime);
		for (TimeStamp missingForecastTime : missingForecastTimes) {
			PointInTime pointInTime = new PointInTime(missingForecastTime);
			fulfilNext(contract, pointInTime);
		}
	}

	/** Digests incoming price forecasts
	 * 
	 * @param input one or multiple price forecast message(s)
	 * @param contracts not used */
	private void updatePriceForecast(ArrayList<Message> input, List<Contract> contracts) {
		for (Message inputMessage : input) {
			MeritOrderMessage meritOrderMessage = inputMessage.getFirstPortableItemOfType(MeritOrderMessage.class);
			double priceForecast = meritOrderMessage.getSupplyOrderBook().getLastAwardedItem().getOfferPrice();
			TimeStamp targetTime = meritOrderMessage.getTimeStamp();
			biddingStrategist.storeElectricityPriceForecast(targetTime, priceForecast);
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
			double actualLoad = biddingStrategist.getNetLoadPredictionInMWH(targetTime);
			List<Bid> supplyBids = getSupplyBids(actualLoad);
			List<Bid> demandBids = getDemandBids(actualLoad);
			fulfilNext(contractToFulfil, new BidsAtTime(targetTime, getId(), supplyBids, demandBids));
		}
	}

	/** @return if given load is negative: single supply bid wrapped in a list; empty list otherwise */
	private List<Bid> getSupplyBids(double actualLoad) {
		if (actualLoad > 0) {
			return Collections.emptyList();
		} else {
			store(OutputColumns.OfferedEnergyInMWH, Math.abs(actualLoad));
			return Arrays.asList(new Bid(Math.abs(actualLoad), Constants.MINIMAL_PRICE_IN_EUR_PER_MWH, Double.NaN));
		}
	}

	/** @return if given load is positive: single demand bid wrapped in a list; empty list otherwise */
	private List<Bid> getDemandBids(double actualLoad) {
		if (actualLoad > 0) {
			store(OutputColumns.RequestedEnergyInMWH, actualLoad);
			return Arrays.asList(new Bid(actualLoad, Constants.SCARCITY_PRICE_IN_EUR_PER_MWH, Double.NaN));
		} else {
			return Collections.emptyList();
		}
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

		store(OutputFields.AwardedDischargeEnergyInMWH, awardedDischargePower);
		store(OutputFields.AwardedChargeEnergyInMWH, awardedChargePower);
		store(OutputColumns.AwardedEnergyInMWH, externalPowerDelta);
		store(Outputs.ReceivedMoneyInEUR, revenues);
		store(Outputs.VariableCostsInEUR, costs);
	}

	@Override
	protected double getInstalledCapacityInMW() {
		return 0;
	}

	@Override
	protected Strategist getStrategist() {
		return null;
	}
}