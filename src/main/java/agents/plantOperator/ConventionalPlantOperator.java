// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.plantOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import agents.conventionals.PlantBuildingManager;
import agents.conventionals.Portfolio;
import agents.conventionals.PowerPlant;
import agents.markets.CarbonMarket;
import agents.markets.FuelsMarket;
import agents.markets.FuelsMarket.FuelType;
import agents.markets.FuelsTrader;
import agents.trader.Trader;
import communications.message.AmountAtTime;
import communications.message.ClearingTimes;
import communications.message.Co2Cost;
import communications.message.FuelBid;
import communications.message.FuelBid.BidType;
import communications.message.FuelCost;
import communications.message.FuelData;
import communications.message.MarginalCost;
import communications.message.PointInTime;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.service.output.ComplexIndex;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimeStamp;
import util.Util;

/** Operates a portfolio of conventional power plant units of same type, e.g. nuclear or hard-coal power plant unit. */
public class ConventionalPlantOperator extends PowerPlantOperator implements FuelsTrader {
	static final String ERR_MISSING_CO2_COST = "Missing at least one CO2 cost item to match corresponding fuel cost item(s).";
	static final String ERR_MISSING_FUEL_COST = "Missing at least one fuel cost item to match corresponding CO2 cost item(s).";
	static final String ERR_MISSING_POWER = "Missing power to fulfil dispatch: ";
	private static final double NUMERIC_TOLERANCE = 1E-10;

	@Product
	public static enum Products {
		/** total actual emissions produced during power generation */
		Co2Emissions,
		/** Request for a Co2 Price forecast at given time */
		Co2PriceForecastRequest,
		/** Request for a Co2 Price at given time */
		Co2PriceRequest,
	}

	@Output
	private static enum OutputFields {
		DispatchedPowerInMWHperPlant, VariableCostsInEURperPlant, ReceivedMoneyInEURperPlant
	}

	/** The list of all power plants to be operated (now and possibly power plants to become active in the near future) */
	private Portfolio portfolio;
	private FuelData myFuelData;
	private ArrayList<AmountAtTime> fuelConsumption = new ArrayList<>();
	private ArrayList<AmountAtTime> co2Emissions = new ArrayList<>();

	private HashMap<TimeStamp, Double> fuelPrice = new HashMap<>();
	private HashMap<TimeStamp, Double> co2Price = new HashMap<>();
	private double lastDispatchedTotalInMW = 0;

	private enum PlantsKey {
		ID
	}

	private static final ComplexIndex<PlantsKey> dispatch = ComplexIndex.build(
			OutputFields.DispatchedPowerInMWHperPlant, PlantsKey.class);
	private static final ComplexIndex<PlantsKey> variableCosts = ComplexIndex.build(
			OutputFields.VariableCostsInEURperPlant, PlantsKey.class);
	private static final ComplexIndex<PlantsKey> money = ComplexIndex.build(OutputFields.ReceivedMoneyInEURperPlant,
			PlantsKey.class);

	/** Creates a {@link ConventionalPlantOperator}
	 * 
	 * @param dataProvider provides input from config */
	public ConventionalPlantOperator(DataProvider dataProvider) {
		super(dataProvider);

		call(this::updatePortfolio).on(PlantBuildingManager.Products.PowerPlantPortfolio)
				.use(PlantBuildingManager.Products.PowerPlantPortfolio);
		call(this::requestFuelPrice).on(FuelsTrader.Products.FuelPriceForecastRequest)
				.use(Trader.Products.ForecastRequestForward);
		call(this::requestCo2Price).on(Products.Co2PriceForecastRequest).use(Trader.Products.ForecastRequestForward);
		call(this::sendSupplyMarginals).on(PowerPlantOperator.Products.MarginalCostForecast)
				.use(CarbonMarket.Products.Co2PriceForecast, FuelsMarket.Products.FuelPriceForecast);
		call(this::requestFuelPrice).on(FuelsTrader.Products.FuelPriceRequest).use(Trader.Products.GateClosureForward);
		call(this::requestCo2Price).on(Products.Co2PriceRequest).use(Trader.Products.GateClosureForward);
		call(this::sendSupplyMarginals).on(PowerPlantOperator.Products.MarginalCost).use(CarbonMarket.Products.Co2Price,
				FuelsMarket.Products.FuelPrice);
		call(this::reportCo2Emissions).on(Products.Co2Emissions);
		call(this::reportFuelConsumption).on(FuelsTrader.Products.FuelBid);
	}

	/** updates {@link #portfolio} to match that received from {@link PlantBuildingManager} */
	private void updatePortfolio(ArrayList<Message> input, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(input);
		portfolio = message.getAllPortableItemsOfType(Portfolio.class).get(0);
		myFuelData = new FuelData(portfolio.getFuelType());
	}

	/** sends {@link FuelData} message to specify {@link FuelType} and clearing time(s) for fuel price request
	 * 
	 * @param input requested ClearingTimes from associated Trader
	 * @param contracts single contract with FuelsMarket */
	private void requestFuelPrice(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		Message message = CommUtils.getExactlyOneEntry(input);
		ClearingTimes requestedTimes = message.getDataItemOfType(ClearingTimes.class);
		sendFuelPriceRequest(contract, myFuelData, requestedTimes);
	}

	/** sends {@link PointInTime} message to specify time of delivery for CO2 price forecast request */
	private void requestCo2Price(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		Message message = CommUtils.getExactlyOneEntry(input);
		ClearingTimes requestedTimes = message.getDataItemOfType(ClearingTimes.class);
		fulfilNext(contract, requestedTimes);
	}

	/** sends {@link MarginalCost} to connected Trader using incoming {@link FuelCost} and {@link Co2Cost} for one or multiple
	 * {@link TimeStamp}s */
	private void sendSupplyMarginals(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		ArrayList<Message> fuelCosts = CommUtils.extractMessagesWith(input, FuelCost.class);
		ArrayList<Message> co2Costs = CommUtils.extractMessagesWith(input, Co2Cost.class);
		ArrayList<ArrayList<Message>> fuelCo2CostPairs = findMatchingCostItems(fuelCosts, co2Costs);

		double totalPowerPotentialInMW = 0;
		for (ArrayList<Message> costPair : fuelCo2CostPairs) {
			TimeStamp targetTime = retrieveTargetTime(costPair);
			double totalPowerPotentialPerTimeStamp = calculateAndSubmitNonZeroMarginals(contract, costPair, targetTime);
			totalPowerPotentialInMW += totalPowerPotentialPerTimeStamp;
			if (totalPowerPotentialPerTimeStamp == 0) {
				fulfilNext(contract, new MarginalCost(getId(), 0, 0, targetTime));
			}
		}
		if (contract.getProduct() == PowerPlantOperator.Products.MarginalCost) {
			store(PowerPlantOperator.OutputFields.OfferedPowerInMW, totalPowerPotentialInMW);
		}
	}

	/** Calculates all {@link MarginalCost} for a given pair of {@link FuelCost} and {@link Co2Cost} (at a specific time), submits
	 * them to the given {@link Contract}or and returns the sum of their powerPotentials */
	private double calculateAndSubmitNonZeroMarginals(Contract contract, ArrayList<Message> costPair,
			TimeStamp targetTime) {
		ArrayList<MarginalCost> marginals = calcSupplyMarginalList(targetTime, costPair);
		double totalPowerPotentialPerTimeStamp = 0;
		for (MarginalCost marginal : marginals) {
			if (marginal.powerPotentialInMW > 0) {
				fulfilNext(contract, marginal);
				totalPowerPotentialPerTimeStamp += marginal.powerPotentialInMW;
			}
		}
		return totalPowerPotentialPerTimeStamp;
	}

	/** @return List of pairs of fuelCost and CO2Cost that are valid at the same {@link TimeStamp}s */
	private ArrayList<ArrayList<Message>> findMatchingCostItems(ArrayList<Message> fuelCosts,
			ArrayList<Message> co2Costs) {
		ArrayList<ArrayList<Message>> costPairs = new ArrayList<>();

		Iterator<Message> fuelIterator = fuelCosts.iterator();
		while (fuelIterator.hasNext()) {
			ArrayList<Message> costPair = new ArrayList<>(2);
			Message fuelMessage = fuelIterator.next();
			TimeStamp targetTime = fuelMessage.getDataItemOfType(FuelCost.class).validAt;
			costPair.add(fuelMessage);

			Iterator<Message> co2Iterator = co2Costs.iterator();
			co2Search: while (co2Iterator.hasNext()) {
				Message co2Message = co2Iterator.next();
				if (co2Message.getDataItemOfType(Co2Cost.class).validAt.equals(targetTime)) {
					costPair.add(co2Message);
					co2Iterator.remove();
					break co2Search;
				}
			}
			if (costPair.size() != 2) {
				throw new RuntimeException(ERR_MISSING_CO2_COST);
			}
			costPairs.add(costPair);
		}
		if (co2Costs.size() != 0) {
			throw new RuntimeException(ERR_MISSING_FUEL_COST);
		}
		return costPairs;
	}

	/** @return {@link TimeStamp} for which given pair of FuelCost and CO2Cost are valid at */
	private TimeStamp retrieveTargetTime(ArrayList<Message> costPair) {
		return costPair.stream().filter(i -> i.containsType(FuelCost.class)).findFirst().get()
				.getDataItemOfType(FuelCost.class).validAt;
	}

	/** @return List of marginal cost items valid for the given time using the incoming FuelCost & Co2Cost messages */
	private ArrayList<MarginalCost> calcSupplyMarginalList(TimeStamp targetTime, ArrayList<Message> input) {
		double fuelCost = Util.removeFirstMessageWithDataItem(FuelCost.class, input).amount;
		double co2Cost = Util.removeFirstMessageWithDataItem(Co2Cost.class, input).amount;
		if (!input.isEmpty()) {
			throw new RuntimeException(this + " received unused messages: " + input);
		}
		fuelPrice.put(targetTime, fuelCost);
		co2Price.put(targetTime, co2Cost);

		ArrayList<MarginalCost> marginalCosts = new ArrayList<>();
		for (PowerPlant plant : portfolio.getPowerPlantList()) {
			double[] marginal = plant.calcMarginalCost(targetTime, fuelCost, co2Cost);
			marginalCosts.add(new MarginalCost(getId(), marginal[0], marginal[1], targetTime));
		}
		return marginalCosts;
	}

	@Override
	protected double dispatchPlants(double requiredEnergy, TimeStamp time) {
		DispatchResult dispatchResult = updatePowerPlantStatus(requiredEnergy, time);
		this.fuelConsumption.add(new AmountAtTime(time, dispatchResult.getFuelConsumptionInThermalMWH()));
		this.co2Emissions.add(new AmountAtTime(time, dispatchResult.getCo2EmissionsInTons()));
		return dispatchResult.getVariableCostsInEUR();
	}

	/** Sets load level of plants in {@link #portfolio}, starting at the highest efficiency, to generated required energy at the
	 * given TimeStamp. Remaining power plants' load level is set to Zero. Accounts for all emissions, fuel consumption and variable
	 * costs of dispatch.
	 *
	 * @param requiredEnergy to produce
	 * @param time to dispatch at
	 * @return {@link DispatchResult} showing emissions, fuel consumption and variable costs */
	private DispatchResult updatePowerPlantStatus(double requiredEnergy, TimeStamp time) {
		lastDispatchedTotalInMW = requiredEnergy;
		double currentFuelPrice = fuelPrice.remove(time);
		double currentCo2Price = co2Price.remove(time);
		DispatchResult totals = new DispatchResult();

		List<PowerPlant> plantList = portfolio.getPowerPlantList();
		ListIterator<PowerPlant> iterator = plantList.listIterator(plantList.size());
		while (iterator.hasPrevious()) {
			PowerPlant powerPlant = iterator.previous();
			double powerToDispatch = 0;
			double availablePower = powerPlant.getAvailablePowerInMW(time);
			if (requiredEnergy > 0 && availablePower > 0) {
				powerToDispatch = Math.min(requiredEnergy, availablePower);
				requiredEnergy -= powerToDispatch;
				store(dispatch.key(PlantsKey.ID, powerPlant.getId()), powerToDispatch);
			}
			DispatchResult plantDispatchResult = powerPlant.updateGeneration(time, powerToDispatch, currentFuelPrice,
					currentCo2Price);
			if (plantDispatchResult.getVariableCostsInEUR() > 0) {
				store(variableCosts.key(PlantsKey.ID, powerPlant.getId()), plantDispatchResult.getVariableCostsInEUR());
			}
			totals.add(plantDispatchResult);
		}
		if (requiredEnergy > NUMERIC_TOLERANCE) {
			logger.error(ERR_MISSING_POWER + requiredEnergy);
		}
		return totals;
	}

	/** send co2 emissions caused by power generation to single contract receiver */
	private void reportCo2Emissions(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		for (AmountAtTime co2DataItem : co2Emissions) {
			fulfilNext(contract, co2DataItem);
		}
		co2Emissions.clear();
	}

	/** send thermal fuel consumption from power generation to single contract receiver */
	private void reportFuelConsumption(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		for (AmountAtTime fuelDataItem : fuelConsumption) {
			FuelBid fuelBid = new FuelBid(fuelDataItem.validAt, fuelDataItem.amount, BidType.Demand, myFuelData.fuelType);
			sendFuelBid(contract, fuelBid);
		}
		fuelConsumption.clear();
	}

	@Override
	protected void digestPaymentPerPlant(TimeStamp dispatchTime, double totalPaymentInEUR) {
		for (PowerPlant plant : portfolio.getPowerPlantList()) {
			double shareOfLastDispatch = plant.getCurrentPowerOutputInMW() / lastDispatchedTotalInMW;
			double plantPaymentInEUR = totalPaymentInEUR * shareOfLastDispatch;
			if (Math.abs(plantPaymentInEUR) > 1E-10) {
				store(money.key(PlantsKey.ID, plant.getId()), plantPaymentInEUR);
			}
		}
	}
}