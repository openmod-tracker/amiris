package agents.markets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import agents.plantOperator.ConventionalPlantOperator;
import communications.message.AmountAtTime;
import communications.message.ClearingTimes;
import communications.message.FuelCost;
import communications.message.FuelData;
import de.dlr.gitlab.fame.agent.Agent;
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
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.logging.Logging;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Determines market prices for all conventional power plant fuels
 * 
 * @author Christoph Schimeczek, Marc Deissenroth */
public class FuelsMarket extends Agent {
	static final String TIME_SERIES_MISSING = "No TimeSeries found for fuel ";
	static final String CONVERSION_FACTOR_MISSING = "No conversion factor found for fuel ";

	@Product
	public static enum Products {
		/** forecasted value of a fuel price */
		FuelPriceForecast,
		/** actual fuel price */
		FuelPrice,
		/** sum of costs for purchased fuel */
		FuelsBill
	};

	/** Available types of fuel traded at {@link FuelsMarket} */
	public static enum FuelType {
		NATURAL_GAS, LIGNITE, HARD_COAL, OIL, WASTE, NUCLEAR, HYDROGEN
	};

	@Input private static final Tree parameters = Make.newTree().add(Make.newGroup("FuelPrices").list()
			.add(Make.newEnum("FuelType", FuelType.class), Make.newSeries("Price"), Make.newDouble("ConversionFactor")))
			.buildTree();

	private final HashMap<FuelType, TimeSeries> fuelPrices = new HashMap<>();
	private final HashMap<FuelType, Double> conversionFactors = new HashMap<>();

	/** Creates a {@link FuelsMarket}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public FuelsMarket(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		loadFuelPrices(input.getGroupList("FuelPrices"));

		call(this::sendPrices).on(Products.FuelPriceForecast)
				.use(ConventionalPlantOperator.Products.FuelPriceForecastRequest);
		call(this::sendPrices).on(Products.FuelPrice).use(ConventionalPlantOperator.Products.FuelPriceRequest);
		call(this::sendBill).on(Products.FuelsBill).use(ConventionalPlantOperator.Products.ConsumedFuel);
	}

	/** Loads fuel prices specified as list elements, each with (FuelType, Price) items
	 * 
	 * @param groupList list of parameter data groups
	 * @throws MissingDataException if any group element misses either FuelType or Price */
	private void loadFuelPrices(List<ParameterData> groupList) throws MissingDataException {
		for (ParameterData group : groupList) {
			FuelType fuelType = group.getEnum("FuelType", FuelType.class);
			fuelPrices.put(fuelType, group.getTimeSeries("Price"));
			conversionFactors.put(fuelType, group.getDouble("ConversionFactor"));
		}
	}

	/** Sends prices matching requested {@link FuelType} and {@link ClearingTimes} to all {@link Contract}ed receivers
	 * 
	 * @param input FuelPriceRequests that specify {@link FuelType} and {@link ClearingTimes}
	 * @param contracts to return prices to - each receiver must issue a request first */
	private void sendPrices(ArrayList<Message> input, List<Contract> contracts) {
		for (Contract contract : contracts) {
			ArrayList<Message> connectedMessages = CommUtils.extractMessagesFrom(input, contract.getReceiverId());
			for (Message message : connectedMessages) {
				FuelType fuelType = message.getDataItemOfType(FuelData.class).fuelType;
				List<TimeStamp> targetTimes = message.getDataItemOfType(ClearingTimes.class).getTimes();
				for (TimeStamp targetTime : targetTimes) {
					sendFuelPriceFor(targetTime, fuelType, contract);
				}
			}
		}
	}

	/** Sends price in EUR per thermal MWh for requested FuelType at requested time to given contract r
	 * 
	 * @param time of the forecast
	 * @param fuelType for which to return forecast
	 * @param contract with receiver of the FuelCost forecast */
	private void sendFuelPriceFor(TimeStamp time, FuelType fuelType, Contract contract) {
		double fuelPriceInEURperThermalMWH = getFuelPrice(fuelType, time) * getConversionFactor(fuelType);
		FuelCost fuelPriceItem = new FuelCost(time, fuelPriceInEURperThermalMWH);
		fulfilNext(contract, fuelPriceItem);
	}

	/** Returns fuel price at specified time for specified fuel
	 * 
	 * @param fuel type of fuel to match
	 * @param time to match
	 * @return price for given FuelType at specified TimeStamp
	 * @throws RuntimeException if no TimeSeries is registered for given fuelType */
	private double getFuelPrice(FuelType fuel, TimeStamp time) {
		TimeSeries fuelPriceOverTime = fuelPrices.get(fuel);
		if (fuelPriceOverTime == null) {
			throw Logging.logFatalException(logger, TIME_SERIES_MISSING + fuel);
		}
		return fuelPriceOverTime.getValueLinear(time);
	}

	/** Returns the conversion factor for given fuel type
	 * 
	 * @param fuel type of fuel to match
	 * @return conversion factor for given FuelType
	 * @throws RuntimeException if no conversion factor is registered for given fuelType */
	private double getConversionFactor(FuelType fuel) {
		if (conversionFactors.containsKey(fuel)) {
			return conversionFactors.get(fuel);
		} else {
			throw Logging.logFatalException(logger, CONVERSION_FACTOR_MISSING + fuel);
		}
	}

	/** Sends bills to contracted partners for their ordered fuel(s)
	 * 
	 * @param input messages that specify amount of consumed fuel(s)
	 * @param contracts to return invoice to - each receiver should issue request(s) first */
	private void sendBill(ArrayList<Message> input, List<Contract> contracts) {
		for (Contract contract : contracts) {
			ArrayList<Message> connectedMessages = CommUtils.extractMessagesFrom(input, contract.getReceiverId());
			double fuelCostTotal = 0;
			for (Message message : connectedMessages) {
				AmountAtTime fuelConsumption = message.getDataItemOfType(AmountAtTime.class);
				FuelType fuelType = message.getDataItemOfType(FuelData.class).fuelType;
				double fuelPrice = getFuelPrice(fuelType, fuelConsumption.validAt);
				fuelCostTotal += fuelPrice * fuelConsumption.amount;
			}
			AmountAtTime bill = new AmountAtTime(now(), fuelCostTotal);
			fulfilNext(contract, bill);
		}
	}
}