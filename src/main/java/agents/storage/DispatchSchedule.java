package agents.storage;

import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Represents a charge / discharge schedule for flexibility options, e.g. storage systems
 * 
 * @author Christoph Schimeczek */
public class DispatchSchedule {
	public static final String ERROR_OVERRIDE = "Arrays in schedule may not be overridden.";
	public static final String ERROR_LENGTH = "Array length does not match schedule duration count in periods.";
	private static final double MAX_ABSOLUTE_ENERGY_DEVIATION_IN_MWH = 1.E-3;

	private final TimeStamp timeOfFirstElement;
	private final int durationInPeriods;
	private final TimeSpan period;

	private double[] chargingPerPeriodInMW;
	private double[] bidPerPeriodInEURperMWH;
	private double[] expectedInitialInternalEnergyPerPeriodInMWH;

	/** Creates a {@link DispatchSchedule}
	 * 
	 * @param timeSegment defines first time at which this schedule is valid and length of each period
	 * @param durationInPeriods number of time periods covered by this schedule, i.e. multiples of timeSegment durations */
	public DispatchSchedule(TimePeriod timeSegment, int durationInPeriods) {
		this.timeOfFirstElement = timeSegment.getStartTime();
		this.durationInPeriods = durationInPeriods;
		this.period = timeSegment.getDuration();
	}

	/** sets hourly charging schedule
	 * 
	 * @param chargingPerPeriodInMW charging schedule */
	public void setChargingPerPeriod(double[] chargingPerPeriodInMW) {
		ensureIsNull(this.chargingPerPeriodInMW);
		ensureCorrectLength(chargingPerPeriodInMW);
		this.chargingPerPeriodInMW = chargingPerPeriodInMW.clone();
	}

	/** @throws RuntimeException if given object is not null */
	private void ensureIsNull(double[] object) {
		if (object != null) {
			throw new RuntimeException(ERROR_OVERRIDE);
		}
	}

	/** @throws RuntimeException if given array's length is not equal to {@link #durationInPeriods} */
	private void ensureCorrectLength(double[] object) {
		if (object.length != durationInPeriods) {
			throw new RuntimeException(ERROR_LENGTH);
		}
	}

	/** sets hourly bidding schedule
	 * 
	 * @param bidPerPeriodInEURperMWH bidding schedule */
	public void setBidsScheduleInEURperMWH(double[] bidPerPeriodInEURperMWH) {
		ensureIsNull(this.bidPerPeriodInEURperMWH);
		ensureCorrectLength(bidPerPeriodInEURperMWH);
		this.bidPerPeriodInEURperMWH = bidPerPeriodInEURperMWH.clone();
	}

	/** sets schedule for expected initial internal energies
	 * 
	 * @param expectedInitialInternalEnergyPerPeriodInMWH initial energy schedule */
	public void setExpectedInitialInternalEnergyScheduleInMWH(double[] expectedInitialInternalEnergyPerPeriodInMWH) {
		ensureIsNull(this.expectedInitialInternalEnergyPerPeriodInMWH);
		ensureCorrectLength(expectedInitialInternalEnergyPerPeriodInMWH);
		this.expectedInitialInternalEnergyPerPeriodInMWH = expectedInitialInternalEnergyPerPeriodInMWH.clone();
	}

	/** Returns true if this schedule is defined for the given time, and if the given energy level matches the scheduled energy one
	 * 
	 * @param time TimeStamp which the schedule is checked for
	 * @param storageEnergyLevelInMWH current energy level in storage to be compared to the planned energy level at given time
	 * @return true if the schedule is applicable at the specified time and matches the given energy in storage */
	public boolean isApplicable(TimeStamp time, double storageEnergyLevelInMWH) {
		boolean isWithinTimeFrame = isWithinTimeFrame(time);
		boolean matchesTimeElement = matchesBeginningOfTimeElement(time);
		return isWithinTimeFrame && matchesTimeElement && energyLevelWithinTolerance(time, storageEnergyLevelInMWH);
	}

	/** @return true if given {@link TimeStamp} is within the time frame of this schedule */
	private boolean isWithinTimeFrame(TimeStamp time) {
		int periodShiftCount = (durationInPeriods - 1);
		TimeSpan shiftDuration = new TimeSpan(periodShiftCount * period.getSteps());
		TimeStamp latestTime = timeOfFirstElement.laterBy(shiftDuration);
		return time.isGreaterEqualTo(timeOfFirstElement) && time.isLessEqualTo(latestTime);
	}

	/** @return true if given {@link TimeStamp} matches any scheduling element beginning of this schedule exactly */
	private boolean matchesBeginningOfTimeElement(TimeStamp time) {
		long timeDelta = time.getStep() - timeOfFirstElement.getStep();
		return timeDelta % period.getSteps() == 0;
	}

	/** @return true if the schedule is applicable to the specified hour with the specified energy in storage */
	private boolean energyLevelWithinTolerance(TimeStamp time, double storageEnergyLevelInMWH) {
		double plannedEnergyInStorage = expectedInitialInternalEnergyPerPeriodInMWH[calcElementInSchedule(time)];
		double absoluteDeviation = Math.abs(plannedEnergyInStorage - storageEnergyLevelInMWH);
		return absoluteDeviation < MAX_ABSOLUTE_ENERGY_DEVIATION_IN_MWH;
	}

	/** @return element number in this {@link DispatchSchedule} corresponding to the given {@link TimeStamp} */
	private int calcElementInSchedule(TimeStamp time) {
		return (int) ((time.getStep() - timeOfFirstElement.getStep()) / period.getSteps());
	}

	/** Returns <b>charging power</b> for the given time as positive value - in case of discharging, 0 is returned
	 * 
	 * @param time must match schedule time element, use {@link #isApplicable(TimeStamp, double)} to check for validity
	 * @return positive charging power in MW for the specified time */
	public double getScheduledChargingPowerInMW(TimeStamp time) {
		double chargingPower = chargingPerPeriodInMW[calcElementInSchedule(time)];
		return chargingPower > 0 ? chargingPower : 0;
	}

	/** Returns <b>discharging power</b> for the given time as positive value - in case of charging, 0 is returned
	 * 
	 * @param time must match schedule time element, use {@link #isApplicable(TimeStamp, double)} to check for validity
	 * @return positive discharging power in MW for the specified time */
	public double getScheduledDischargingPowerInMW(TimeStamp time) {
		double chargingPower = chargingPerPeriodInMW[calcElementInSchedule(time)];
		return chargingPower < 0 ? -chargingPower : 0;
	}

	/** Returns bidding price for charging or discharging
	 * 
	 * @param time must match schedule time element, use {@link #isApplicable(TimeStamp, double)} to check for validity
	 * @return bidding price in EUR / MWh for the specified time */
	public double getScheduledBidInHourInEURperMWH(TimeStamp time) {
		return bidPerPeriodInEURperMWH[calcElementInSchedule(time)];
	}
}