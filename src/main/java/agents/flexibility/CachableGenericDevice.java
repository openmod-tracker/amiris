package agents.flexibility;

import static de.dlr.gitlab.fame.time.Constants.STEPS_PER_HOUR;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

public class CachableGenericDevice {

	private GenericDevice device;
	private double[] chargingEfficiency;
	private double[] dischargingEfficiency;
	private double[] energyContentUpperLimitInMWH;
	private double[] energyContentLowerLimitInMWH;

	private double[] effectiveSelfDischargeRate;
	private double[] maxNetChargingEnergyInMWH;
	private double[] maxNetDischargingEnergyInMWH;
	private double[] netInflowEnergyInMWH;

	private double intervalDurationInHours;

	public CachableGenericDevice(GenericDevice device) {
		this.device = device;
	}

	/** Caches all time series information of {@link GenericDevice} */
	public void cacheTimeSeries(TimePeriod startingPeriod, int numberOfTimeSteps) {
		initialiseArrays(numberOfTimeSteps);
		intervalDurationInHours = (double) startingPeriod.getDuration().getSteps() / STEPS_PER_HOUR;
		for (int timeIndex = 0; timeIndex < numberOfTimeSteps; timeIndex++) {
			TimeStamp time = startingPeriod.shiftByDuration(timeIndex).getStartTime();
			chargingEfficiency[timeIndex] = device.chargingEfficiency.getValueLinear(time);
			dischargingEfficiency[timeIndex] = device.dischargingEfficiency.getValueLinear(time);
			energyContentUpperLimitInMWH[timeIndex] = device.energyContentUpperLimitInMWH.getValueLinear(time);
			energyContentLowerLimitInMWH[timeIndex] = device.energyContentLowerLimitInMWH.getValueLinear(time);
			double netInflowPowerInMW = device.netInflowPowerInMW.getValueLinear(time);
			effectiveSelfDischargeRate[timeIndex] = 1.
					- Math.pow(1 - device.selfDischargeRatePerHour.getValueLinear(time), intervalDurationInHours);
			maxNetChargingEnergyInMWH[timeIndex] = (device.externalChargingPowerInMW.getValueLinear(time)
					* chargingEfficiency[timeIndex] + netInflowPowerInMW) * intervalDurationInHours;
			maxNetDischargingEnergyInMWH[timeIndex] = (netInflowPowerInMW
					- device.externalDischargingPowerInMW.getValueLinear(time) / dischargingEfficiency[timeIndex])
					* intervalDurationInHours;
			netInflowEnergyInMWH[timeIndex] = netInflowPowerInMW * intervalDurationInHours;
		}
	}

	/** Initialise arrays with correct length */
	private void initialiseArrays(int numberOfTimeSteps) {
		if (chargingEfficiency == null || chargingEfficiency.length != numberOfTimeSteps) {
			chargingEfficiency = new double[numberOfTimeSteps];
			dischargingEfficiency = new double[numberOfTimeSteps];
			energyContentUpperLimitInMWH = new double[numberOfTimeSteps];
			energyContentLowerLimitInMWH = new double[numberOfTimeSteps];
			effectiveSelfDischargeRate = new double[numberOfTimeSteps];
			maxNetChargingEnergyInMWH = new double[numberOfTimeSteps];
			maxNetDischargingEnergyInMWH = new double[numberOfTimeSteps];
			netInflowEnergyInMWH = new double[numberOfTimeSteps];
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
	 * @return maximum reachable energy content for given initial energy content in MWh */
	public double getMaxTargetEnergyContentInMWH(int timeIndex, double initialEnergyContentInMWH) {
		double targetEnergyInMWH = initialEnergyContentInMWH * (1 - effectiveSelfDischargeRate[timeIndex])
				+ maxNetChargingEnergyInMWH[timeIndex];
		return Math.min(targetEnergyInMWH, energyContentUpperLimitInMWH[timeIndex]);
	}

	/** Returns the minimum reachable internal energy content by applying maximum discharging power for the specified duration and
	 * considering the initial energy content, self discharge, and inward / outward flows. The transitions is assumed to start at
	 * the specified time - all related values are assumed to not change during the transition's duration. The energy content is
	 * capped at the lower energy content limit that applies to the {@link GenericDevice}.
	 * 
	 * @param timeIndex start of transition
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @return minimum allowed energy content for given initial energy content in MWh */
	public double getMinTargetEnergyContentInMWH(int timeIndex, double initialEnergyContentInMWH) {
		double targetEnergyInMWH = initialEnergyContentInMWH * (1 - effectiveSelfDischargeRate[timeIndex])
				+ maxNetDischargingEnergyInMWH[timeIndex];
		return Math.max(targetEnergyInMWH, energyContentLowerLimitInMWH[timeIndex]);
	}

	/** Simulates a transition at given time ignoring its current energy level, but starting from a given initial energy content.
	 * Returns required external energy delta (i.e. charging if positive) to reach given target energy content. Does <b>not</b>
	 * ensure power limits or energy limits.
	 * 
	 * @param timeIndex of transition
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @param targetEnergyContentInMWH at the end of transition
	 * @return external energy difference for transition from initial to final internal energy content level at given time */
	public double simulateTransition(int timeIndex, double initialEnergyContentInMWH, double targetEnergyContentInMWH) {
		double selfDischargeInMWH = initialEnergyContentInMWH * effectiveSelfDischargeRate[timeIndex];
		double internalEnergyDelta = targetEnergyContentInMWH - initialEnergyContentInMWH - netInflowEnergyInMWH[timeIndex]
				+ selfDischargeInMWH;
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
