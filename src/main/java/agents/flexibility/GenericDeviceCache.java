// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility;

import static de.dlr.gitlab.fame.time.Constants.STEPS_PER_HOUR;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Caches properties of a connected {@link GenericDevice} a certain time
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

	/** Instantiates a new {@link GenericDeviceCache} for given device
	 * 
	 * @param device the {@link GenericDevice} to cache properties for */
	public GenericDeviceCache(GenericDevice device) {
		this.device = device;
	}

	/** Set the time period to cache properties of the connected {@link GenericDevice} for */
	public void setPeriod(TimePeriod timePeriod) {
		intervalDurationInHours = (double) timePeriod.getDuration().getSteps() / STEPS_PER_HOUR;
	}

	/** Caches all time series information of {@link GenericDevice} at given time */
	public void prepareFor(TimeStamp time) {
		chargingEfficiency = device.getChargingEfficiency(time);
		dischargingEfficiency = device.getDischargingEfficiency(time);
		energyContentUpperLimitInMWH = device.getEnergyContentUpperLimitInMWH(time);
		energyContentLowerLimitInMWH = device.getEnergyContentLowerLimitInMWH(time);
		double netInflowPowerInMW = device.getNetInflowInMW(time);
		effectiveSelfDischargeRate = 1. - Math.pow(1 - device.getSelfDischargeRate(time), intervalDurationInHours);
		double maxNetChargingPowerInMW = netInflowPowerInMW
				+ device.getExternalChargingPowerInMW(time) * chargingEfficiency;
		maxNetChargingEnergyInMWH = maxNetChargingPowerInMW * intervalDurationInHours;
		maxNetDischargingEnergyInMWH = (netInflowPowerInMW
				- device.getExternalDischargingPowerInMW(time) / dischargingEfficiency) * intervalDurationInHours;
		netInflowEnergyInMWH = netInflowPowerInMW * intervalDurationInHours;
	}

	/** Return upper limit of energy content for currently cached time, may be negative;
	 *
	 * @return maximum internal energy content of {@link GenericDevice} in MWh */
	public double getEnergyContentUpperLimitInMWH() {
		return energyContentUpperLimitInMWH;
	}

	/** Return lower limit of energy content for currently cached time, may be negative;
	 * 
	 * @return minimum internal energy content of {@link GenericDevice} in MWh */
	public double getEnergyContentLowerLimitInMWH() {
		return energyContentLowerLimitInMWH;
	}

	/** Returns the maximum reachable internal energy content by applying maximum charging power for the specified duration and
	 * considering the initial energy content, self discharge, and inward / outward flows. The transitions is assumed to start at
	 * the currently cached time - all related values are assumed to not change during the transition's duration. The energy content
	 * is capped at the upper energy content limit that applies to the {@link GenericDevice}.
	 * 
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @return maximum reachable energy content for given initial energy content in MWh */
	public double getMaxTargetEnergyContentInMWH(double initialEnergyContentInMWH) {
		double targetEnergyInMWH = initialEnergyContentInMWH * (1 - effectiveSelfDischargeRate) + maxNetChargingEnergyInMWH;
		return Math.min(targetEnergyInMWH, energyContentUpperLimitInMWH);
	}

	/** Returns the minimum reachable internal energy content by applying maximum discharging power for the specified duration and
	 * considering the initial energy content, self discharge, and inward / outward flows. The transitions is assumed to start at
	 * the currently cached time - all related values are assumed to not change during the transition's duration. The energy content
	 * is capped at the lower energy content limit that applies to the {@link GenericDevice}.
	 * 
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @return minimum allowed energy content for given initial energy content in MWh */
	public double getMinTargetEnergyContentInMWH(double initialEnergyContentInMWH) {
		double targetEnergyInMWH = initialEnergyContentInMWH * (1 - effectiveSelfDischargeRate)
				+ maxNetDischargingEnergyInMWH;
		return Math.max(targetEnergyInMWH, energyContentLowerLimitInMWH);
	}

	/** Simulates a transition at the currently cached time ignoring its current energy level, but starting from a given initial
	 * energy content. Returns required external energy delta (i.e. charging if positive) to reach given target energy content. Does
	 * <b>not</b> ensure power limits or energy limits.
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

	/** Return external energy delta equivalent of given internal energy delta based on (dis-)charging efficiency at currently
	 * cached time
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

	/** Return the {@link GenericDevice} connected to this cache
	 * 
	 * @return the connected device */
	public GenericDevice getGenericDevice() {
		return device;
	}

	/** Return the maximum internal energy delta that can occur from charging at currently cached time
	 * 
	 * @return maximum positive net energy delta */
	public double getMaxNetChargingEnergyInMWH() {
		return maxNetChargingEnergyInMWH;
	}

	/** Return the maximum (negative) internal energy delta that can occur from discharging at currently cached time
	 * 
	 * @return maximum negative net energy delta */
	public double getMaxNetDischargingEnergyInMWH() {
		return maxNetDischargingEnergyInMWH;
	}
}
