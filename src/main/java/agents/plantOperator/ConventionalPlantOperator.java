package agents.plantOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import agents.conventionals.PlantBuildingManager;
import agents.conventionals.Portfolio;
import agents.conventionals.PowerPlant;
import agents.forecast.MarketForecaster;
import agents.markets.CarbonMarket;
import agents.markets.FuelsMarket;
import agents.markets.FuelsMarket.FuelType;
import communications.message.AmountAtTime;
import communications.message.Co2Cost;
import communications.message.FuelCost;
import communications.message.FuelData;
import communications.message.MarginalCost;
import communications.message.PointInTime;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;
import util.Util;

/** Operates a portfolio of conventional power plant units of same type, e.g. nuclear or hard-coal power plant unit. */
public class ConventionalPlantOperator extends PowerPlantOperator {
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
		/** Request for Fuel price forecast at a given time and for a given fuel */
		FuelPriceForecastRequest,
		/** Request for Fuel price at a given time and for a given fuel */
		FuelPriceRequest,
		/** Total actual fuel consumption */
		ConsumedFuel
	}

	@Output
	private static enum OutputFields {}

	/** The list of all power plants to be operated (now and possibly power plants to become active in the near future) */
	private Portfolio portfolio;
	private FuelData myFuelData;
	private ArrayList<AmountAtTime> fuelConsumption = new ArrayList<>();
	private ArrayList<AmountAtTime> co2Emissions = new ArrayList<>();

	private HashMap<TimeStamp, Double> fuelPrice = new HashMap<>();
	private HashMap<TimeStamp, Double> co2Price = new HashMap<>();

	/** Creates a {@link ConventionalPlantOperator}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public ConventionalPlantOperator(DataProvider dataProvider) {
		super(dataProvider);

		call(this::updatePortfolio).on(PlantBuildingManager.Products.PowerPlantPortfolio)
				.use(PlantBuildingManager.Products.PowerPlantPortfolio);
		call(this::requestFuelPriceForecast).on(Products.FuelPriceForecastRequest)
				.use(MarketForecaster.Products.ForecastRequest);
		call(this::requestCo2PriceForecast).on(Products.Co2PriceForecastRequest)
				.use(MarketForecaster.Products.ForecastRequest);
		call(this::sendSupplyMarginalsForecast).on(PowerPlantOperator.Products.MarginalCostForecast)
				.use(CarbonMarket.Products.Co2PriceForecast, FuelsMarket.Products.FuelPriceForecast);
		call(this::requestFuelPrice).on(Products.FuelPriceRequest);
		call(this::sendSupplyMarginals).on(PowerPlantOperator.Products.MarginalCost).use(CarbonMarket.Products.Co2Price,
				FuelsMarket.Products.FuelPrice);
		call(this::reportCo2Emissions).on(Products.Co2Emissions);
		call(this::reportFuelConsumption).on(Products.ConsumedFuel);
	}

	/** updates {@link #portfolio} to match that received from {@link PlantBuildingManager} */
	private void updatePortfolio(ArrayList<Message> input, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(input);
		portfolio = message.getAllPortableItemsOfType(Portfolio.class).get(0);
		myFuelData = new FuelData(portfolio.getFuelType());
	}

	/** sends {@link FuelData} message to specify {@link FuelType} for fuel price request */
	private void requestFuelPrice(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		fulfilNext(contract, myFuelData);
	}

	/** sends {@link FuelData} and {@link PointInTime} message to specify {@link FuelType} for fuel price forecast request */
	private void requestFuelPriceForecast(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		for (Message message : input) {
			PointInTime requestedTime = message.getDataItemOfType(PointInTime.class);
			fulfilNext(contract, myFuelData, requestedTime);
		}
	}

	/** sends {@link PointInTime} message to specify time of delivery for CO2 price forecast request */
	private void requestCo2PriceForecast(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		for (Message message : input) {
			PointInTime requestedTime = message.getDataItemOfType(PointInTime.class);
			fulfilNext(contract, requestedTime);
		}
	}

	/** sends {@link MarginalCost} forecast to connected Trader based using incoming fuelCost and CO2 cost forecasts for one or
	 * multiple {@link TimeStamp}s */
	private void sendSupplyMarginalsForecast(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		ArrayList<Message> fuelCosts = CommUtils.extractMessagesWith(input, FuelCost.class);
		ArrayList<Message> co2Costs = CommUtils.extractMessagesWith(input, Co2Cost.class);

		ArrayList<ArrayList<Message>> fuelCo2CostPairs = findMatchingCostItems(fuelCosts, co2Costs);
		for (ArrayList<Message> costPair : fuelCo2CostPairs) {
			TimeStamp targetTime = retrieveTargetTime(costPair);
			ArrayList<MarginalCost> marginals = calcSupplyMarginalList(targetTime, costPair);
			for (MarginalCost marginal : marginals) {
				if (marginal.powerPotentialInMW > 0) {
					fulfilNext(contract, marginal);
				}
			}
		}
	}

	/** @return List of pairs of fuelCost and CO2Cost that are valid at the same {@link TimeStamp}s */
	private ArrayList<ArrayList<Message>> findMatchingCostItems(ArrayList<Message> fuelCosts,
			ArrayList<Message> co2Costs) {
		ArrayList<ArrayList<Message>> costPairs = new ArrayList<>();

		Iterator<Message> fuelIterator = fuelCosts.iterator();
		while (fuelIterator.hasNext()) {
			ArrayList<Message> costPair = new ArrayList<>();
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

	/** sends {@link MarginalCost} to connected Trader based using incoming fuelCost and CO2 costs for a single {@link TimeStamp} */
	private void sendSupplyMarginals(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		// TODO:: HACK for Validation: remove + 1L
		TimeStamp targetTime = contract.getNextTimeOfDeliveryAfter(now()).laterBy(new TimeSpan(1L));
		ArrayList<MarginalCost> marginals = calcSupplyMarginalList(targetTime, input);

		double totalPowerPotentialInMW = 0;
		for (MarginalCost marginal : marginals) {
			if (marginal.powerPotentialInMW > 0) {
				fulfilNext(contract, marginal);
				totalPowerPotentialInMW += marginal.powerPotentialInMW;
			}
		}
		store(PowerPlantOperator.OutputFields.OfferedPowerInMW, totalPowerPotentialInMW);
	}

	@Override
	protected double dispatchPlants(double requiredEnergy, TimeStamp time) {
		double rampingCost = updatePowerPlantStatus(requiredEnergy, time);
		double fuelConsumptionSum = calcFuelConsumption(time);
		this.fuelConsumption.add(new AmountAtTime(time, fuelConsumptionSum));

		double co2Emissions = portfolio.getPrototype().calcCo2EmissionInTons(fuelConsumptionSum);
		this.co2Emissions.add(new AmountAtTime(time, co2Emissions));

		return rampingCost + fuelConsumptionSum * fuelPrice.remove(time) + co2Emissions * co2Price.remove(time);
	}

	/** Sets load level of plants in {@link #portfolio}, starting at the highest efficiency, to generated required energy at the
	 * given TimeStamp. Remaining power plants' load level is set to Zero.
	 *
	 * @param requiredEnergy to produce
	 * @param time to dispatch at
	 * @return the accumulated ramping cost */
	private double updatePowerPlantStatus(double requiredEnergy, TimeStamp time) {
		List<PowerPlant> plantList = portfolio.getPowerPlantList();
		ListIterator<PowerPlant> iterator = plantList.listIterator(plantList.size());
		double rampingCost = 0;
		while (iterator.hasPrevious()) {
			PowerPlant powerPlant = iterator.previous();
			double powerToDispatch = 0;
			if (requiredEnergy > 0) {
				powerToDispatch = Math.min(requiredEnergy, powerPlant.getAvailablePowerInMW(time));
				requiredEnergy -= powerToDispatch;
			}
			rampingCost += powerPlant.updateCurrentLoadLevel(time, powerToDispatch);
		}
		if (requiredEnergy > NUMERIC_TOLERANCE) {
			logger.error(ERR_MISSING_POWER + requiredEnergy);
		}
		return rampingCost;
	}

	/** @return fuel consumption from dispatched PowerPlants in the {@link #portfolio} at given time */
	private double calcFuelConsumption(TimeStamp time) {
		double fuelConsumptionSum = 0;
		for (PowerPlant powerPlant : portfolio.getPowerPlantList()) {
			fuelConsumptionSum += powerPlant.calcFuelConsumptionOfGenerationInThermalMWH(time);
		}
		return fuelConsumptionSum;
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
			fulfilNext(contract, myFuelData, fuelDataItem);
		}
		fuelConsumption.clear();
	}
}