// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.storage.arbitrageStrategists;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import agents.markets.meritOrder.Constants;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.SupplyOrderBook;
import agents.markets.meritOrder.sensitivities.MeritOrderSensitivity;
import agents.storage.Device;
import agents.storage.DispatchSchedule;
import agents.trader.StorageTrader;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Creates arbitrage strategies for storage devices based on forecasts for merit-order or electricity prices
 * 
 * @author Christoph Schimeczek */
public abstract class ArbitrageStrategist {
	protected static final String ERR_CANNOT_PROVIDE_FORECAST = "Cannot provide forecasts with storage strategist type ";

	public static enum StrategistType {
		/** Creates a the schedule according to a given TimeSeries. */
		DISPATCH_FILE,
		/** Uses the storage {@link Device} in order to minimize the total system costs. */
		SINGLE_AGENT_MIN_SYSTEM_COST,
		/** Optimizes the {@link Device} dispatch in order to maximize the profits of the {@link StorageTrader}. A perfect forecast of
		 * upcoming prices (and their changes due to charging) is used for the optimization. */
		SINGLE_AGENT_MAX_PROFIT,
		/** Calculates the {@link Device} dispatch in order to maximize the profits of the {@link StorageTrader}. A median of the
		 * forecasted prices is used to estimate a good dispatch strategy in an environment with more than one flexible agent. */
		MULTI_AGENT_SIMPLE
	}

	private final static double BID_TOLERANCE = 1E-3;

	protected final int forecastSteps;
	protected final int scheduleDurationPeriods;
	protected double[] periodChargingScheduleInMW;
	protected double[] periodPriceScheduleInEURperMWH;
	protected double[] periodScheduledInitialInternalEnergyInMWH;
	protected double[] periodScheduledBidsInEURperMWH;
	protected Device storage;
	private TreeMap<TimePeriod, MeritOrderSensitivity> sensitivities = new TreeMap<>();

	/** Create {@link ArbitrageStrategist}
	 * 
	 * @param forecastSteps number of forecast intervals
	 * @param storage Device for which schedules are to be created
	 * @param scheduleDurationPeriods number of time periods to be scheduled (&#x2264; forecastSteps) */
	public ArbitrageStrategist(int forecastSteps, Device storage, int scheduleDurationPeriods) {
		this.forecastSteps = forecastSteps;
		this.storage = storage;
		this.scheduleDurationPeriods = scheduleDurationPeriods;
		allocateScheduleResources();
	}

	/** initialises permanent arrays used for schedule preparation */
	private void allocateScheduleResources() {
		periodChargingScheduleInMW = new double[scheduleDurationPeriods];
		periodPriceScheduleInEURperMWH = new double[scheduleDurationPeriods];
		periodScheduledInitialInternalEnergyInMWH = new double[scheduleDurationPeriods];
		periodScheduledBidsInEURperMWH = new double[scheduleDurationPeriods];
	}

	/** Returns list of times at which forecasts are missing needed for schedule planning
	 * 
	 * @param firstTime first time period to be covered by a created schedule
	 * @return List of {@link TimeStamp}s at which {@link MeritOrderSensitivity} is not yet defined */
	public ArrayList<TimeStamp> getTimesMissingForecasts(TimePeriod firstTime) {
		ArrayList<TimeStamp> missingTimes = new ArrayList<>();
		for (int period = 0; period < forecastSteps; period++) {
			TimePeriod timeSegment = firstTime.shiftByDuration(period);
			if (!sensitivities.containsKey(timeSegment)) {
				missingTimes.add(timeSegment.getStartTime());
			}
		}
		return missingTimes;
	}

	/** Creates a {@link DispatchSchedule} for the connected storage {@link Device}
	 * 
	 * @param timePeriod first TimePeriod element of the schedule to be created
	 * @param initialEnergyInStorageInMWh level of energy in storage device at beginning of schedule
	 * @return created {@link DispatchSchedule} for the specified {@link TimePeriod} */
	public DispatchSchedule createSchedule(TimePeriod timePeriod, double initialEnergyInStorageInMWh) {
		updateSchedule(timePeriod, initialEnergyInStorageInMWh);
		updateBidSchedule();
		DispatchSchedule schedule = new DispatchSchedule(timePeriod, scheduleDurationPeriods);
		schedule.setBidsScheduleInEURperMWH(periodScheduledBidsInEURperMWH);
		schedule.setChargingPerPeriod(periodChargingScheduleInMW);
		schedule.setExpectedInitialInternalEnergyScheduleInMWH(periodScheduledInitialInternalEnergyInMWH);
		return schedule;
	}

	/** Updates schedule arrays starting at the given TimePeriod with the given initial energy level
	 * 
	 * @param timePeriod first period of the schedule to be created
	 * @param initialEnergyInStorageInMWh energy level at the beginning of the first period */
	protected abstract void updateSchedule(TimePeriod timePeriod, double initialEnergyInStorageInMWh);

	/** Updates the bid schedules considering safety margins for the bid prices and market price limits */
	private void updateBidSchedule() {
		for (int period = 0; period < scheduleDurationPeriods; period++) {
			if (periodChargingScheduleInMW[period] > 0) {
				periodScheduledBidsInEURperMWH[period] = Math.min(Constants.SCARCITY_PRICE_IN_EUR_PER_MWH,
						periodPriceScheduleInEURperMWH[period] + BID_TOLERANCE);
			} else if (periodChargingScheduleInMW[period] < 0) {
				periodScheduledBidsInEURperMWH[period] = Math.max(Constants.MINIMAL_PRICE_IN_EUR_PER_MWH,
						periodPriceScheduleInEURperMWH[period] - BID_TOLERANCE);
			} else {
				periodScheduledBidsInEURperMWH[period] = 0;
			}
		}
	}

	/** Removes any stored MeritOrderSensitivity whose associated TimePeriod ends before given time
	 *
	 * @param time limiting TimeStamp - earlier events to be deleted */
	public void clearSensitivitiesBefore(TimeStamp time) {
		Iterator<TimePeriod> mapIterator = sensitivities.keySet().iterator();
		while (mapIterator.hasNext()) {
			TimePeriod timePeriod = mapIterator.next();
			if (timePeriod.getLastTime().isLessThan(time)) {
				mapIterator.remove();
			} else {
				break;
			}
		}
	}

	/** Stores given supply and demand bid forecasts for the associated TimePeriod: merit-order forecasting method
	 * 
	 * @param timePeriod associated with the forecast data
	 * @param supplyForecast forecasted supply OrderBook
	 * @param demandForecast forecasted demand OrderBook */
	public void storeMeritOrderForesight(TimePeriod timePeriod, SupplyOrderBook supplyForecast,
			DemandOrderBook demandForecast) {
		MeritOrderSensitivity sensitivity = createBlankSensitivity();
		sensitivity.updatePowers(storage.getExternalChargingPowerInMW(), storage.getExternalDischargingPowerInMW());
		sensitivity.updateSensitivities(supplyForecast, demandForecast);
		sensitivities.put(timePeriod, sensitivity);
	}

	/** @return an empty {@link MeritOrderSensitivity} item of the type used by this {@link ArbitrageStrategist}-type */
	protected abstract MeritOrderSensitivity createBlankSensitivity();

	/** Stores given electricity price forecast for the associated TimePeriod: price-forecasting method
	 * 
	 * @param timePeriod associated with the forecast data
	 * @param electricityPriceForecastInEURperMWH forecast for the electricity price in EUR per MWh */
	public void storeElectricityPriceForecast(TimePeriod timePeriod, double electricityPriceForecastInEURperMWH) {
		MeritOrderSensitivity sensitivity = createBlankSensitivity();
		sensitivity.updatePriceForecast(electricityPriceForecastInEURperMWH);
		sensitivities.put(timePeriod, sensitivity);
	}

	/** Returns MeritOrderSensitivity associated with the given TimePeriod
	 * 
	 * @param timePeriod to search for associated MeritOrderSensitivity
	 * @return MeritOrderSensitivity associated with the given TimePeriod */
	protected MeritOrderSensitivity getSensitivityForSegment(TimePeriod timePeriod) {
		return sensitivities.get(timePeriod);
	}

	/** Returns forecasted external charging power, if possible - otherwise throws a RuntimeEception
	 * 
	 * @param targetTime for which to provide the forecast
	 * @return forecasted external charging power in MW
	 * @throws RuntimeException if this strategist cannot provide forecasts */
	public abstract double getChargingPowerForecastInMW(TimeStamp targetTime);

	/** Update scheduled initial energies and charging schedules to correct errors due to rounding of energies caused by
	 * discretisation of internal energy states
	 * 
	 * @param initialEnergyInStorage at the beginning of the schedule */
	protected void correctForRoundingErrors(double initialEnergyInStorage) {
		double maxCapacity = storage.getEnergyStorageCapacityInMWH();
		for (int period = 0; period < scheduleDurationPeriods; period++) {
			periodScheduledInitialInternalEnergyInMWH[period] = initialEnergyInStorage;
			double internalChargingPower = storage.externalToInternalEnergy(periodChargingScheduleInMW[period]);
			double nextEnergy = Math.max(0, Math.min(maxCapacity, initialEnergyInStorage + internalChargingPower));
			periodChargingScheduleInMW[period] = storage.internalToExternalEnergy(nextEnergy - initialEnergyInStorage);
			initialEnergyInStorage = nextEnergy;
		}
	}
}