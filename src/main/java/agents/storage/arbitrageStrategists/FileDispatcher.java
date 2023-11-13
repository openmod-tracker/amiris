// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.storage.arbitrageStrategists;

import agents.flexibility.DispatchSchedule;
import agents.markets.meritOrder.Constants;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.SupplyOrderBook;
import agents.markets.meritOrder.sensitivities.MeritOrderSensitivity;
import agents.storage.Device;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Creates {@link DispatchSchedule}s from file for a connected storage {@link Device}
 *
 * @author Christoph Schimeczek */
public class FileDispatcher extends ArbitrageStrategist {
	public static final Tree parameters = Make.newTree()
			.add(Make.newSeries("Schedule").optional().help(
					"Change of internal storage energy relative to available charging power. Values should be -1 <= x <= 1."))
			.buildTree();

	static final String WARN_SUSPICIOUS_DISPATCH = "Warning:: Storage below empty or above full:: Dispatch file may be not suitable";
	static final String ERR_CANNOT_USE_FORECAST = "Error:: Storage strategist 'FileDispatcher' cannot digest forecasts. Remove contracts.";

	private static final double ABSOLUTE_TOLERANCE_IN_MWH = 0.1;
	/** TimeSeries of storage charging power (< 0:discharging; >0: charging) relative to internal charging power */
	private TimeSeries tsDispatch;

	/** Creates a {@link FileDispatcher}
	 * 
	 * @param generalInput general parameters associated with strategists
	 * @param specificInput specific parameters for this strategist
	 * @param storage device to be optimised
	 * @throws MissingDataException if any required input is missing */
	public FileDispatcher(ParameterData generalInput, ParameterData specificInput, Device storage)
			throws MissingDataException {
		super(generalInput, storage);
		this.tsDispatch = specificInput.getTimeSeries("Schedule");
	}

	/** No {@link MeritOrderSensitivity} needed for {@link FileDispatcher}, as dispatch is read from file */
	@Override
	protected MeritOrderSensitivity createBlankSensitivity() {
		return null;
	}

	/** Not needed for {@link FileDispatcher} */
	@Override
	protected void updateSchedule(TimePeriod t) {}

	/** @return {@link DispatchSchedule} for the connected {@link Device} for the specified simulation hour **/
	@Override
	public DispatchSchedule createSchedule(TimePeriod timeSegment) {
		double currentEnergyInStorageInMWH = storage.getCurrentEnergyInStorageInMWH();
		for (int element = 0; element < scheduleDurationPeriods; element++) {
			final TimeStamp planningTime = timeSegment.shiftByDuration(element).getStartTime();
			final double internalChargePowerInMW = calcInternalChargingPowerAt(planningTime);
			final double externalChargePowerInMW = storage.internalToExternalEnergy(internalChargePowerInMW);

			demandScheduleInMWH[element] = externalChargePowerInMW;
			scheduledInitialInternalEnergyInMWH[element] = currentEnergyInStorageInMWH;
			currentEnergyInStorageInMWH += internalChargePowerInMW;
			issueWarningIfOutsideTolerance(currentEnergyInStorageInMWH);
			currentEnergyInStorageInMWH = ensureWithinBounds(currentEnergyInStorageInMWH);
			setBidPrice(element, externalChargePowerInMW);
		}
		return buildSchedule(timeSegment);
	}

	/** @return internal charging power in the dispatch file at the given {@link TimeStamp} */
	private double calcInternalChargingPowerAt(TimeStamp planningTime) {
		final double relativeChargePower = tsDispatch.getValueLinear(planningTime);
		return storage.getInternalPowerInMW() * relativeChargePower;
	}

	/** prints a warning message if storage is significantly outside its constraints */
	private void issueWarningIfOutsideTolerance(double currentEnergyInStorageInMWH) {
		if (isOutsideTolerance(currentEnergyInStorageInMWH)) {
			System.out.println(WARN_SUSPICIOUS_DISPATCH);
		}
	}

	/** @return true if the {@link Device storage} is operated outside its constraints by more than
	 *         {@link #ABSOLUTE_TOLERANCE_IN_MWH} */
	private boolean isOutsideTolerance(double currentEnergyInStorageInMWH) {
		final double storageCapacityInMWH = storage.getEnergyStorageCapacityInMWH();
		final boolean tooNegative = currentEnergyInStorageInMWH < -ABSOLUTE_TOLERANCE_IN_MWH;
		final boolean tooFarAboveCapacity = currentEnergyInStorageInMWH > storageCapacityInMWH + ABSOLUTE_TOLERANCE_IN_MWH;
		return tooNegative || tooFarAboveCapacity;
	}

	/** @return energy in storage, ensured to be within the bounds of the connected {@link Device} */
	private double ensureWithinBounds(double currentEnergyInStorageInMWH) {
		final double storageCapacityInMWH = storage.getEnergyStorageCapacityInMWH();
		return Math.max(0, Math.min(storageCapacityInMWH, currentEnergyInStorageInMWH));
	}

	/** sets bid price for a given time element and charging power; to enforce dispatch, max & min allowed prices are used */
	private void setBidPrice(int element, double externalChargePowerInMW) {
		if (externalChargePowerInMW > 0) {
			scheduledBidPricesInEURperMWH[element] = Constants.SCARCITY_PRICE_IN_EUR_PER_MWH;
		} else if (externalChargePowerInMW < 0) {
			scheduledBidPricesInEURperMWH[element] = Constants.MINIMAL_PRICE_IN_EUR_PER_MWH;
		} else {
			scheduledBidPricesInEURperMWH[element] = 0;
		}
	}

	/** @return {@link DispatchSchedule} for the given TimeSegment created from prepared Bid arrays */
	private DispatchSchedule buildSchedule(TimePeriod timeSegment) {
		final DispatchSchedule schedule = new DispatchSchedule(timeSegment, scheduleDurationPeriods);
		schedule.setBidsScheduleInEURperMWH(scheduledBidPricesInEURperMWH);
		schedule.setChargingPerPeriod(demandScheduleInMWH);
		schedule.setExpectedInitialInternalEnergyScheduleInMWH(scheduledInitialInternalEnergyInMWH);
		return schedule;
	}

	@Override
	public double getChargingPowerForecastInMW(TimeStamp targetTime) {
		double internalEnergyInMW = calcInternalChargingPowerAt(targetTime);
		return storage.internalToExternalEnergy(internalEnergyInMW);
	}

	/** Unused method - will throw an Exception */
	@Override
	public void storeMeritOrderForesight(TimePeriod timePeriod, SupplyOrderBook supplyForecast,
			DemandOrderBook demandForecast) {
		throw new RuntimeException(ERR_CANNOT_USE_FORECAST);
	}
}