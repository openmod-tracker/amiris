package agents.flexibility;

import static de.dlr.gitlab.fame.time.Constants.STEPS_PER_HOUR;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

public class CachableGenericDevice {

	private GenericDevice device;
	private double[] externalChargingPowerInMW;
	private double[] externalDischargingPowerInMW;
	private double[] chargingEfficiency;
	private double[] dischargingEfficiency;
	private double[] energyContentUpperLimitInMWH;
	private double[] energyContentLowerLimitInMWH;
	private double[] selfDischargeRatePerHour;
	private double[] netInflowPowerInMW;

	public CachableGenericDevice(GenericDevice device) {
		this.device = device;
	}

	/** Caches all time series information of {@link GenericDevice} */
	public void cacheTimeSeries(TimePeriod startingPeriod, int numberOfTimeSteps) {
		initialiseArrays(numberOfTimeSteps);
		for (int step = 0; step < numberOfTimeSteps; step++) {
			TimeStamp time = startingPeriod.shiftByDuration(step).getStartTime();
			externalChargingPowerInMW[step] = device.externalChargingPowerInMW.getValueLinear(time);
			externalDischargingPowerInMW[step] = device.externalDischargingPowerInMW.getValueLinear(time);
			chargingEfficiency[step] = device.chargingEfficiency.getValueLinear(time);
			dischargingEfficiency[step] = device.dischargingEfficiency.getValueLinear(time);
			energyContentUpperLimitInMWH[step] = device.energyContentUpperLimitInMWH.getValueLinear(time);
			energyContentLowerLimitInMWH[step] = device.energyContentLowerLimitInMWH.getValueLinear(time);
			selfDischargeRatePerHour[step] = device.selfDischargeRatePerHour.getValueLinear(time);
			netInflowPowerInMW[step] = device.netInflowPowerInMW.getValueLinear(time);
		}
	}

	/** Initialise arrays with correct length */
	private void initialiseArrays(int numberOfTimeSteps) {
		if (externalChargingPowerInMW == null || externalChargingPowerInMW.length != numberOfTimeSteps) {
			externalChargingPowerInMW = new double[numberOfTimeSteps];
			externalDischargingPowerInMW = new double[numberOfTimeSteps];
			chargingEfficiency = new double[numberOfTimeSteps];
			dischargingEfficiency = new double[numberOfTimeSteps];
			energyContentUpperLimitInMWH = new double[numberOfTimeSteps];
			energyContentLowerLimitInMWH = new double[numberOfTimeSteps];
			selfDischargeRatePerHour = new double[numberOfTimeSteps];
			netInflowPowerInMW = new double[numberOfTimeSteps];
		}
	}

	/** Return upper limit of energy content for given time, may be negative;
	 * 
	 * @param timeIndex for which to get the upper energy content limit
	 * @return maximum internal energy content of {@link GenericDevice} in MWh */
	public double getEnergyContentUpperLimitInMWH(int timeIndex) {
		return energyContentUpperLimitInMWH[timeIndex];
	}

	/** Return lower limit of energy content for given time, may be negative;
	 * 
	 * @param timeIndex for which to get the lower energy content limit
	 * @return minimum internal energy content of {@link GenericDevice} in MWh */
	public double getEnergyContentLowerLimitInMWH(int timeIndex) {
		return energyContentLowerLimitInMWH[timeIndex];
	}

	/** Returns the maximum reachable internal energy content by applying maximum charging power for the specified duration and
	 * considering the initial energy content, self discharge, and inward / outward flows. The transitions is assumed to start at
	 * the specified time - all related values are assumed to not change during the transition's duration. The energy content is
	 * capped at the upper energy content limit that applies to the {@link GenericDevice}.
	 * 
	 * @param timeIndex start of transition
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @param duration of the transition
	 * @return maximum reachable energy content for given initial energy content in MWh */
	public double getMaxTargetEnergyContentInMWH(int timeIndex, double initialEnergyContentInMWH, TimeSpan duration) {
		double netChargingEnergyInMWH = (externalChargingPowerInMW[timeIndex] * chargingEfficiency[timeIndex]
				+ netInflowPowerInMW[timeIndex]) * calcDurationInHours(duration);
		double targetEnergyInMWH = initialEnergyContentInMWH * (1 - calcSelfDischarge(timeIndex, duration))
				+ netChargingEnergyInMWH;
		return Math.min(targetEnergyInMWH, getEnergyContentUpperLimitInMWH(timeIndex));
	}

	private double calcDurationInHours(TimeSpan duration) {
		return (double) duration.getSteps() / STEPS_PER_HOUR;
	}

	/** @return effective self discharge rate for given duration, considering exponential reduction over time */
	private double calcSelfDischarge(int timeIndex, TimeSpan duration) {
		return 1. - Math.pow(1 - selfDischargeRatePerHour[timeIndex], calcDurationInHours(duration));
	}

	/** Returns the minimum reachable internal energy content by applying maximum discharging power for the specified duration and
	 * considering the initial energy content, self discharge, and inward / outward flows. The transitions is assumed to start at
	 * the specified time - all related values are assumed to not change during the transition's duration. The energy content is
	 * capped at the lower energy content limit that applies to the {@link GenericDevice}.
	 * 
	 * @param timeIndex start of transition
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @param duration of the transition
	 * @return minimum allowed energy content for given initial energy content in MWh */
	public double getMinTargetEnergyContentInMWH(int timeIndex, double initialEnergyContentInMWH, TimeSpan duration) {
		double netDischargingEnergyInMWH = (netInflowPowerInMW[timeIndex]
				- externalDischargingPowerInMW[timeIndex] / dischargingEfficiency[timeIndex]);
		double targetEnergyInMWH = initialEnergyContentInMWH * (1 - calcSelfDischarge(timeIndex, duration))
				+ netDischargingEnergyInMWH;
		return Math.max(targetEnergyInMWH, getEnergyContentLowerLimitInMWH(timeIndex));
	}

	/** Simulates a transition at given time ignoring its current energy level, but starting from a given initial energy content.
	 * Returns required external energy delta (i.e. charging if positive) to reach given target energy content. Does <b>not</b>
	 * ensure power limits or energy limits.
	 * 
	 * @param timeIndex of transition
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @param targetEnergyContentInMWH at the end of transition
	 * @param duration of the transition
	 * @return external energy difference for transition from initial to final internal energy content level at given time */
	public double simulateTransition(int timeIndex, double initialEnergyContentInMWH, double targetEnergyContentInMWH,
			TimeSpan duration) {
		double selfDischargeInMWH = initialEnergyContentInMWH * calcSelfDischarge(timeIndex, duration);
		double internalEnergyDelta = targetEnergyContentInMWH - initialEnergyContentInMWH
				- netInflowPowerInMW[timeIndex] * calcDurationInHours(duration) + selfDischargeInMWH;
		return internalToExternalEnergy(internalEnergyDelta, timeIndex);
	}

	/** Return external energy delta equivalent of given internal energy delta
	 * 
	 * @param internalEnergyDelta &gt; 0: charging; &lt; 0: depleting
	 * @param timeIndex of transition
	 * @return external energy delta equivalent */
	public double internalToExternalEnergy(double internalEnergyDelta, int timeIndex) {
		if (internalEnergyDelta > 0) {
			return internalEnergyDelta / chargingEfficiency[timeIndex];
		} else {
			return internalEnergyDelta * dischargingEfficiency[timeIndex];
		}
	}

	public GenericDevice getGenericDevice() {
		return device;
	}

}
