// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility;

import static de.dlr.gitlab.fame.time.Constants.STEPS_PER_HOUR;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Holds cached variables at a certain time
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public class GenericDeviceCache {
	private GenericDevice device;

	private double intervalDurationInHours;

	private double chargingEfficiency;
	private double dischargingEfficiency;
	private double energyContentUpperLimitInMWH;
	private double energyContentLowerLimitInMWH;
	private double effectiveSelfDischargeRate;
	private double maxNetChargingEnergyInMWH;
	private double maxNetDischargingEnergyInMWH;
	private double netInflowEnergyInMWH;

	public GenericDeviceCache(GenericDevice device) {
		this.device = device;
	}

	public void setPeriod(TimePeriod timePeriod) {
		intervalDurationInHours = (double) timePeriod.getDuration().getSteps() / STEPS_PER_HOUR;
	}

	/** Caches all time series information of {@link GenericDevice} at given time */
	public void prepareFor(TimeStamp time) {
		chargingEfficiency = device.chargingEfficiency.getValueLinear(time);
		dischargingEfficiency = device.dischargingEfficiency.getValueLinear(time);
		energyContentUpperLimitInMWH = device.energyContentUpperLimitInMWH.getValueLinear(time);
		energyContentLowerLimitInMWH = device.energyContentLowerLimitInMWH.getValueLinear(time);
		double netInflowPowerInMW = device.netInflowPowerInMW.getValueLinear(time);
		effectiveSelfDischargeRate = 1.
				- Math.pow(1 - device.selfDischargeRatePerHour.getValueLinear(time), intervalDurationInHours);
		maxNetChargingEnergyInMWH = (device.externalChargingPowerInMW.getValueLinear(time) * chargingEfficiency
				+ netInflowPowerInMW) * intervalDurationInHours;
		maxNetDischargingEnergyInMWH = (netInflowPowerInMW
				- device.externalDischargingPowerInMW.getValueLinear(time) / dischargingEfficiency) * intervalDurationInHours;
		netInflowEnergyInMWH = netInflowPowerInMW * intervalDurationInHours;
	}

	/** Return upper limit of energy content for given time, may be negative;
	 *
	 * @return maximum internal energy content of {@link GenericDevice} in MWh */
	public double getEnergyContentUpperLimitInMWH() {
		return energyContentUpperLimitInMWH;
	}

	/** Return lower limit of energy content for given time, may be negative;
	 * 
	 * @return minimum internal energy content of {@link GenericDevice} in MWh */
	public double getEnergyContentLowerLimitInMWH() {
		return energyContentLowerLimitInMWH;
	}

	/** Returns the maximum reachable internal energy content by applying maximum charging power for the specified duration and
	 * considering the initial energy content, self discharge, and inward / outward flows. The transitions is assumed to start at
	 * the specified time - all related values are assumed to not change during the transition's duration. The energy content is
	 * capped at the upper energy content limit that applies to the {@link GenericDevice}.
	 * 
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @return maximum reachable energy content for given initial energy content in MWh */
	public double getMaxTargetEnergyContentInMWH(double initialEnergyContentInMWH) {
		double targetEnergyInMWH = initialEnergyContentInMWH * (1 - effectiveSelfDischargeRate) + maxNetChargingEnergyInMWH;
		return Math.min(targetEnergyInMWH, energyContentUpperLimitInMWH);
	}

	/** Returns the minimum reachable internal energy content by applying maximum discharging power for the specified duration and
	 * considering the initial energy content, self discharge, and inward / outward flows. The transitions is assumed to start at
	 * the specified time - all related values are assumed to not change during the transition's duration. The energy content is
	 * capped at the lower energy content limit that applies to the {@link GenericDevice}.
	 * 
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @return minimum allowed energy content for given initial energy content in MWh */
	public double getMinTargetEnergyContentInMWH(double initialEnergyContentInMWH) {
		double targetEnergyInMWH = initialEnergyContentInMWH * (1 - effectiveSelfDischargeRate)
				+ maxNetDischargingEnergyInMWH;
		return Math.max(targetEnergyInMWH, energyContentLowerLimitInMWH);
	}

	/** Simulates a transition at given time ignoring its current energy level, but starting from a given initial energy content.
	 * Returns required external energy delta (i.e. charging if positive) to reach given target energy content. Does <b>not</b>
	 * ensure power limits or energy limits.
	 * 
	 * @param timeIndex of transition
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @param targetEnergyContentInMWH at the end of transition
	 * @return external energy difference for transition from initial to final internal energy content level at given time */
	public double simulateTransition(double initialEnergyContentInMWH, double targetEnergyContentInMWH) {
		double selfDischargeInMWH = initialEnergyContentInMWH * effectiveSelfDischargeRate;
		double internalEnergyDelta = targetEnergyContentInMWH - initialEnergyContentInMWH - netInflowEnergyInMWH
				+ selfDischargeInMWH;
		return internalToExternalEnergy(internalEnergyDelta);
	}

	/** Return external energy delta equivalent of given internal energy delta
	 * 
	 * @param internalEnergyDelta &gt; 0: charging; &lt; 0: depleting
	 * @return external energy delta equivalent */
	public double internalToExternalEnergy(double internalEnergyDelta) {
		if (internalEnergyDelta > 0) {
			return internalEnergyDelta / chargingEfficiency;
		} else {
			return internalEnergyDelta * dischargingEfficiency;
		}
	}

	public GenericDevice getGenericDevice() {
		return device;
	}
}
