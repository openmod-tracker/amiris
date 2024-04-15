// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader;

import java.util.ArrayList;
import java.util.List;
import agents.electrolysis.Electrolyzer;
import agents.electrolysis.ElectrolyzerStrategist;
import agents.flexibility.DispatchSchedule;
import agents.flexibility.Strategist;
import agents.forecast.Forecaster;
import agents.forecast.MarketForecaster;
import agents.markets.DayAheadMarket;
import agents.markets.DayAheadMarketTrader;
import agents.markets.FuelsMarket;
import agents.markets.FuelsMarket.FuelType;
import agents.markets.FuelsTrader;
import agents.markets.meritOrder.Bid;
import agents.markets.meritOrder.Bid.Type;
import agents.markets.meritOrder.Constants;
import agents.plantOperator.renewable.VariableRenewableOperator;
import agents.storage.arbitrageStrategists.FileDispatcher;
import communications.message.AmountAtTime;
import communications.message.AwardData;
import communications.message.BidData;
import communications.message.ClearingTimes;
import communications.message.FuelBid;
import communications.message.FuelBid.BidType;
import communications.message.FuelCost;
import communications.message.FuelData;
import communications.message.PointInTime;
import communications.message.PpaInformation;
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
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** A flexible Trader demanding electricity and producing hydrogen from it via electrolysis.
 * 
 * @author Christoph Schimeczek */
public class ElectrolysisTrader extends FlexibilityTrader implements FuelsTrader, PowerPlantScheduler {
	@Input private static final Tree parameters = Make.newTree().addAs("Device", Electrolyzer.parameters)
			.addAs("Strategy", ElectrolyzerStrategist.parameters)
			.add(Make.newInt("HydrogenForecastRequestOffsetInSeconds")).buildTree();

	@Output
	private static enum Outputs {
		RequestedEnergyInMWH, OfferedEnergyPriceInEURperMWH, AwardedEnergyInMWH, ProducedHydrogenInMWH
	};

	@Product
	public static enum Products {
		PpaInformationRequest
	};

	private static final FuelData FUEL_HYDROGEN = new FuelData(FuelType.HYDROGEN);
	private final Electrolyzer electrolyzer;
	private final ElectrolyzerStrategist strategist;
	private final TimeSpan hydrogenForecastRequestOffset;
	private double ppaPriceInEURperMWH;

	/** Creates a new {@link ElectrolysisTrader} based on given input parameters
	 * 
	 * @param data configured input
	 * @throws MissingDataException if any required input is missing */
	public ElectrolysisTrader(DataProvider data) throws MissingDataException {
		super(data);
		ParameterData input = parameters.join(data);
		electrolyzer = new Electrolyzer(input.getGroup("Device"));
		strategist = ElectrolyzerStrategist.newStrategist(input.getGroup("Strategy"), electrolyzer);
		hydrogenForecastRequestOffset = new TimeSpan(input.getInteger("HydrogenForecastRequestOffsetInSeconds"));

		call(this::prepareForecasts).on(Trader.Products.BidsForecast).use(MarketForecaster.Products.ForecastRequest);
		call(this::requestElectricityForecast).on(FlexibilityTrader.Products.PriceForecastRequest);
		call(this::requestHydrogenPriceForecast).on(FuelsTrader.Products.FuelPriceForecastRequest);
		call(this::updateElectricityPriceForecast).on(Forecaster.Products.PriceForecast)
				.use(Forecaster.Products.PriceForecast);
		call(this::updateHydrogenPriceForecast).on(FuelsMarket.Products.FuelPriceForecast)
				.use(FuelsMarket.Products.FuelPriceForecast);
		call(this::forwardClearingTimes).on(Products.PpaInformationRequest).use(DayAheadMarket.Products.GateClosureInfo);
		call(this::prepareBids).on(DayAheadMarketTrader.Products.Bids).use(DayAheadMarket.Products.GateClosureInfo);
		call(this::assignDispatch).on(PowerPlantScheduler.Products.DispatchAssignment)
				.use(VariableRenewableOperator.Products.PpaInformation);
		call(this::sellProducedHydrogen).on(FuelsTrader.Products.FuelBid).use(DayAheadMarket.Products.Awards);
		call(this::sellProducedGreenHydrogen).on(FuelsTrader.Products.FuelBid);
		call(this::digestSaleReturns).on(FuelsMarket.Products.FuelBill).use(FuelsMarket.Products.FuelBill);
		call(this::payoutClient).on(PowerPlantScheduler.Products.Payout)
				.use(VariableRenewableOperator.Products.PpaInformation);
	}

	/** Prepares forecasts and sends them to the {@link MarketForecaster}; Calling this function will throw an Exception for
	 * Strategists other than {@link FileDispatcher}
	 * 
	 * @param input one ClearingTimes message specifying for which TimeStamps to calculate the forecasts
	 * @param contracts one partner to send the forecasts to */
	private void prepareForecasts(ArrayList<Message> input, List<Contract> contracts) {
		Contract contractToFulfil = CommUtils.getExactlyOneEntry(contracts);
		ClearingTimes clearingTimes = CommUtils.getExactlyOneEntry(input).getDataItemOfType(ClearingTimes.class);
		List<TimeStamp> targetTimes = clearingTimes.getTimes();
		for (TimeStamp targetTime : targetTimes) {
			double electricDemandInMW = strategist.getElectricDemandForecastInMW(targetTime);
			Bid bid = new Bid(electricDemandInMW, Constants.SCARCITY_PRICE_IN_EUR_PER_MWH, Double.NaN, getId(),
					Type.Demand);
			fulfilNext(contractToFulfil, bid, new PointInTime(targetTime));
		}
	}

	/** Requests forecast of hydrogen prices from one contracted {@link FuelsMarket}
	 * 
	 * @param input not used
	 * @param contracts single contracted fuels market to request hydrogen price(s) from */
	private void requestHydrogenPriceForecast(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TimePeriod nextTime = new TimePeriod(now().laterBy(hydrogenForecastRequestOffset), Strategist.OPERATION_PERIOD);
		ArrayList<TimeStamp> missingForecastTimes = strategist.getMissingHydrogenPriceForecastsTimes(nextTime);
		ClearingTimes clearingTimes = new ClearingTimes(
				missingForecastTimes.toArray(new TimeStamp[missingForecastTimes.size()]));
		sendFuelPriceRequest(contract, FUEL_HYDROGEN, clearingTimes);
	}

	/** Digests one or multiple incoming hydrogen price forecasts
	 * 
	 * @param input one or multiple hydrogen price forecast message(s)
	 * @param contracts not used */
	private void updateHydrogenPriceForecast(ArrayList<Message> input, List<Contract> contracts) {
		for (Message inputMessage : input) {
			FuelCost priceForecastMessage = readFuelPriceMessage(inputMessage);
			double priceForecastInEURperThermalMWH = priceForecastMessage.amount;
			TimePeriod timeSegment = new TimePeriod(priceForecastMessage.validAt, Strategist.OPERATION_PERIOD);
			strategist.storeHydrogenPriceForecast(timeSegment, priceForecastInEURperThermalMWH);
		}
	}

	/** Forwards one ClearingTimes to connected clients (if any)
	 * 
	 * @param input a single ClearingTimes message
	 * @param contracts connected client */
	private void forwardClearingTimes(ArrayList<Message> input, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(input);
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		ClearingTimes clearingTimes = message.getDataItemOfType(ClearingTimes.class);
		fulfilNext(contract, clearingTimes);
	}

	/** Prepares and sends Bids to one contracted exchange
	 * 
	 * @param input one GateClosureInfo message containing ClearingTimes
	 * @param contracts single contract with a {@link DayAheadMarket} */
	private void prepareBids(ArrayList<Message> input, List<Contract> contracts) {
		Contract contractToFulfil = CommUtils.getExactlyOneEntry(contracts);
		for (TimeStamp targetTime : extractTimesFromGateClosureInfoMessages(input)) {
			DispatchSchedule schedule = strategist.getValidSchedule(targetTime);
			BidData bidData = prepareHourlyDemandBid(targetTime, schedule);
			store(Outputs.RequestedEnergyInMWH, bidData.offeredEnergyInMWH);
			sendDayAheadMarketBids(contractToFulfil, bidData);
		}
	}

	/** Prepares hourly demand bids
	 * 
	 * @param requestedTime TimeStamp at which the demand bid should be defined
	 * @return demand bid for requestedTime */
	private BidData prepareHourlyDemandBid(TimeStamp targetTime, DispatchSchedule schedule) {
		double demandPower = schedule.getScheduledChargingPowerInMW(targetTime);
		double price = schedule.getScheduledBidInHourInEURperMWH(targetTime);
		BidData demandBid = new BidData(demandPower, price, Double.NaN, getId(), Type.Demand, targetTime);
		store(Outputs.OfferedEnergyPriceInEURperMWH, price);
		return demandBid;
	}

	/** Determine capacity to be dispatched based on maximum consumption
	 * 
	 * @param messages not used
	 * @param contracts with the {@link VariableRenewablePlantOperator} to send it the dispatch assignment */
	private void assignDispatch(ArrayList<Message> messages, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(messages);
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		PpaInformation ppaInformation = message.getDataItemOfType(PpaInformation.class);
		strategist.updateMaximumConsumption(ppaInformation.validAt, ppaInformation.yieldPotential);
		AmountAtTime dispatch = new AmountAtTime(ppaInformation.validAt, strategist.getMaximumConsumption());
		fulfilNext(contract, dispatch);
	}

	/** Digests award information from {@link DayAheadMarket}, writes dispatch and sells hydrogen at fuels market using a "negative
	 * purchase" message
	 * 
	 * @param messages award information received from {@link DayAheadMarket}
	 * @param contracts a contract with one {@link FuelsMarket} */
	private void sellProducedHydrogen(ArrayList<Message> messages, List<Contract> contracts) {
		Message awardMessage = CommUtils.getExactlyOneEntry(messages);
		AwardData award = awardMessage.getDataItemOfType(AwardData.class);

		double awardedEnergyInMWH = award.demandEnergyInMWH;
		double costs = award.powerPriceInEURperMWH * awardedEnergyInMWH;
		TimeStamp deliveryTime = award.beginOfDeliveryInterval;

		double producedHydrogenInThermalMWH = electrolyzer.calcProducedHydrogenOneHour(awardedEnergyInMWH,
				deliveryTime);
		strategist.updateProducedHydrogenTotal(producedHydrogenInThermalMWH);
		sendHydrogenSellMessage(contracts, producedHydrogenInThermalMWH, deliveryTime);

		store(Outputs.AwardedEnergyInMWH, awardedEnergyInMWH);
		store(Outputs.ProducedHydrogenInMWH, producedHydrogenInThermalMWH);
		store(FlexibilityTrader.Outputs.VariableCostsInEUR, costs);
	}

	/** Sell Hydrogen according to production schedule following the contracted renewable power plant
	 * 
	 * @param messages not used
	 * @param contracts a contract with one {@link FuelsMarket} */
	private void sellProducedGreenHydrogen(ArrayList<Message> messages, List<Contract> contracts) {
		DispatchSchedule schedule = strategist.getValidSchedule(now());
		TimeStamp deliveryTime = schedule.getTimeOfFirstElement();
		double chargingPowerInMWH = schedule.getScheduledChargingPowerInMW(deliveryTime);
		double costs = ppaPriceInEURperMWH * chargingPowerInMWH;
		double producedHydrogenInThermalMWH = electrolyzer.calcProducedHydrogenOneHour(chargingPowerInMWH,
				deliveryTime);
		strategist.updateProducedHydrogenTotal(producedHydrogenInThermalMWH);
		sendHydrogenSellMessage(contracts, producedHydrogenInThermalMWH, deliveryTime);

		store(Outputs.AwardedEnergyInMWH, chargingPowerInMWH);
		store(Outputs.ProducedHydrogenInMWH, producedHydrogenInThermalMWH);
		store(FlexibilityTrader.Outputs.VariableCostsInEUR, costs);
	}

	/** Sends a single {@link FuelData} message to one contracted {@link FuelsMarket} to sell the given amount of hydrogen */
	private void sendHydrogenSellMessage(List<Contract> contracts, double producedHydrogenInThermalMWH,
			TimeStamp deliveryTime) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		FuelBid fuelBid = new FuelBid(deliveryTime, producedHydrogenInThermalMWH, BidType.Supply, FuelType.HYDROGEN);
		sendFuelBid(contract, fuelBid);
	}

	/** Evaluate revenues (i.e. negative purchase cost) from selling hydrogen at the fuels market
	 * 
	 * @param messages one AmountAtTime message from fuels market
	 * @param contracts ignored */
	private void digestSaleReturns(ArrayList<Message> messages, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(messages);
		double cost = readFuelBillMessage(message);
		store(FlexibilityTrader.Outputs.ReceivedMoneyInEUR, -cost);
	}

	/** Pay client according to PPA specification
	 * 
	 * @param messages contain PPA information
	 * @param contracts payment to client from PPA */
	private void payoutClient(ArrayList<Message> messages, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(messages);
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		PpaInformation ppaInformation = message.getDataItemOfType(PpaInformation.class);
		double payment = strategist.getMaximumConsumption() * ppaInformation.price;
		AmountAtTime paymentToClient = new AmountAtTime(ppaInformation.validAt, payment);
		fulfilNext(contract, paymentToClient);
		store(FlexibilityTrader.Outputs.ReceivedMoneyInEUR, -payment);
	}

	@Override
	protected double getInstalledCapacityInMW() {
		return electrolyzer.getPeakPower(now());
	}

	@Override
	protected Strategist getStrategist() {
		return strategist;
	}
}
