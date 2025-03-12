// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility;

import static de.dlr.gitlab.fame.time.Constants.STEPS_PER_HOUR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import agents.storage.Device;
import de.dlr.gitlab.fame.agent.input.GroupBuilder;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** A generic device representing any kind of electrical flexibility, e.g., pumped-hydro storages with inflow, reservoir storages,
 * heat pumps, electric vehicle fleets, load-shifting portfolios
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public class GenericDevice {
	static final String ERR_EXCEED_CHARGING_POWER = " Maximum charging power exceeded by MW: ";
	static final String ERR_EXCEED_DISCHARGING_POWER = " Maximum discharging power exceeded by MW: ";
	static final String ERR_EXCEED_UPPER_ENERGY_LIMIT = " Upper energy limit exceeded by MWh: ";
	static final String ERR_EXCEED_LOWER_ENERGY_LIMIT = " Lower energy limit exceeded by MWh: ";
	static final String ERR_NEGATIVE_SELF_DISCHARGE = "Energy out of nothing: negative self discharge occured at time: ";
	static final double TOLERANCE = 1E-3;

	private static Logger logger = LoggerFactory.getLogger(GenericDevice.class);
	private TimeSeries chargingPowerInMW;
	private TimeSeries dischargingPowerInMW;
	private TimeSeries chargingEfficiency;
	private TimeSeries dischargingEfficiency;
	private TimeSeries energyContentUpperLimitInMWH;
	private TimeSeries energyContentLowerLimitInMWH;
	private TimeSeries selfDischargeRatePerHour;
	private TimeSeries netInflowPowerInMW;
	private double currentEnergyContentInMWH;

	/** Input parameters of a storage {@link Device} */
	public static final GroupBuilder parameters = Make.newTree()
			.add(Make.newSeries("ChargingPowerInMW"), Make.newSeries("DischargingPowerInMW"),
					Make.newSeries("ChargingEfficiency"), Make.newSeries("DischargingEfficiency"),
					Make.newSeries("EnergyContentUpperLimitInMWH"),
					Make.newSeries("EnergyContentLowerLimitInMWH"), Make.newSeries("SelfDischargeRatePerHour"),
					Make.newSeries("NetInflowPowerInMW"), Make.newDouble("InitialEnergyContentInMWH"));

	/** Instantiate new {@link GenericDevice}
	 * 
	 * @param input parameters from file
	 * @throws MissingDataException if any required input parameter is missing */
	public GenericDevice(ParameterData input) throws MissingDataException {
		chargingPowerInMW = input.getTimeSeries("ChargingPowerInMW");
		dischargingPowerInMW = input.getTimeSeries("DischargingPowerInMW");
		chargingEfficiency = input.getTimeSeries("ChargingEfficiency");
		dischargingEfficiency = input.getTimeSeries("DischargingEfficiency");
		energyContentUpperLimitInMWH = input.getTimeSeries("EnergyContentUpperLimitInMWH");
		energyContentLowerLimitInMWH = input.getTimeSeries("EnergyContentLowerLimitInMWH");
		selfDischargeRatePerHour = input.getTimeSeries("SelfDischargeRatePerHour");
		netInflowPowerInMW = input.getTimeSeries("NetInflowPowerInMW");
		currentEnergyContentInMWH = input.getDoubleOrDefault("InitialEnergyContentInMWH", 0.);
	}

	/** @param time of transition
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @param duration of the transition
	 * @return maximum allowed energy content for given initial energy content considering charging power and energy bounds */
	public double getMaxTargetEnergyContentInMWH(TimeStamp time, double initialEnergyContentInMWH, TimeSpan duration) {
		double netChargingEnergyInMWH = (chargingPowerInMW.getValueLinear(time) + netInflowPowerInMW.getValueLinear(time))
				* calcDurationInHours(duration);
		return initialEnergyContentInMWH * (1 - calcSelfDischarge(time, duration)) + netChargingEnergyInMWH;
	}

	private double calcDurationInHours(TimeSpan duration) {
		return (double) duration.getSteps() / STEPS_PER_HOUR;
	}

	/** @return effective self discharge rate for given duration, considering exponential reduction over time */
	private double calcSelfDischarge(TimeStamp time, TimeSpan duration) {
		return 1. - Math.pow(1 - selfDischargeRatePerHour.getValueLinear(time), calcDurationInHours(duration));
	}

	/** @param time of transition
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @param duration of the transition
	 * @return minimum allowed energy content for given initial energy content considering charging power and energy bounds */
	public double getMinTargetEnergyContentInMWH(TimeStamp time, double initialEnergyContentInMWH, TimeSpan duration) {
		double netDischargingEnergyInMWH = (netInflowPowerInMW.getValueLinear(time)
				- dischargingPowerInMW.getValueLinear(time)) * calcDurationInHours(duration);
		return initialEnergyContentInMWH * (1 - calcSelfDischarge(time, duration)) + netDischargingEnergyInMWH;
	}

	/** Return upper limit of energy content for given time, may be negative;
	 * 
	 * @param time for which to get the upper energy content limit
	 * @return maximum internal energy content of {@link GenericDevice} in MWh */
	public double getEnergyContentUpperLimitInMWH(TimeStamp time) {
		return energyContentUpperLimitInMWH.getValueLinear(time);
	}

	/** Return lower limit of energy content for given time, may be negative;
	 * 
	 * @param time for which to get the lower energy content limit
	 * @return minimum internal energy content of {@link GenericDevice} in MWh */
	public double getEnergyContentLowerLimitInMWH(TimeStamp time) {
		return energyContentLowerLimitInMWH.getValueLinear(time);
	}

	/** @return current internal energy level of this {@link GenericDevice} in MWh */
	public double getCurrentInternalEnergyInMWH() {
		return currentEnergyContentInMWH;
	}

	/** Simulates a transition at given time ignoring its current energy level, but starting from a given initial energy content.
	 * Returns required external energy delta (i.e. charging if positive) to reach given target energy content. Does <b>not</b>
	 * ensure power limits or energy limits.
	 * 
	 * @param time of transition
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @param targetEnergyContentInMWH at the end of transition
	 * @param duration of the transition
	 * @return external energy difference for transition from initial to final internal energy content level at given time */
	public double simulateTransition(TimeStamp time, double initialEnergyContentInMWH, double targetEnergyContentInMWH,
			TimeSpan duration) {
		double selfDischargeInMWH = initialEnergyContentInMWH * calcSelfDischarge(time, duration);
		double internalEnergyDelta = targetEnergyContentInMWH - initialEnergyContentInMWH
				- netInflowPowerInMW.getValueLinear(time) * calcDurationInHours(duration) + selfDischargeInMWH;
		return internalToExternalEnergy(internalEnergyDelta, time);
	}

	/** Return external energy delta equivalent of given internal energy delta
	 * 
	 * @param internalEnergyDelta &gt; 0: charging; &lt; 0: depleting
	 * @param time of transition
	 * @return external energy delta equivalent */
	private double internalToExternalEnergy(double internalEnergyDelta, TimeStamp time) {
		if (internalEnergyDelta > 0) {
			return internalEnergyDelta / chargingEfficiency.getValueLinear(time);
		} else {
			return internalEnergyDelta * dischargingEfficiency.getValueLinear(time);
		}
	}

	/** Performs an actual transition from current energy content at given time using a given external energy delta. Enforces energy
	 * and power limits. Returns actual external energy delta considering applied limits. Logs errors in case limits are violated.
	 * 
	 * @param time at which charging occurs
	 * @param externalEnergyDeltaInMWH (dis-)charging energy applied to this device (positive: charging, negative: discharging)
	 * @param duration of the transition
	 * @return actual external energy delta for transition considering power and capacity limits */
	public double transition(TimeStamp time, double externalEnergyDeltaInMWH, TimeSpan duration) {
		double internalEnergyDeltaInMWH = externalToInternalEnergy(externalEnergyDeltaInMWH, time);
		double internalPowerInMW = ensurePowerWithinLimits(time, internalEnergyDeltaInMWH / calcDurationInHours(duration));
		double netChargingEnergyInMWH = (internalPowerInMW + netInflowPowerInMW.getValueLinear(time))
				* calcDurationInHours(duration);
		double selfDischargeRate = calcSelfDischarge(time, duration);
		double selfDischargeLossInMWH = currentEnergyContentInMWH * selfDischargeRate;
		double finalEnergyContentInMWH = currentEnergyContentInMWH + netChargingEnergyInMWH - selfDischargeLossInMWH;
		finalEnergyContentInMWH = ensureEnergyWithinLimits(time, finalEnergyContentInMWH);
		ensureNoNegativeSelfDischarge(time, selfDischargeRate);
		internalEnergyDeltaInMWH = finalEnergyContentInMWH - currentEnergyContentInMWH
				+ selfDischargeLossInMWH - netInflowPowerInMW.getValueLinear(time) * calcDurationInHours(duration);
		currentEnergyContentInMWH = finalEnergyContentInMWH;
		return internalToExternalEnergy(internalEnergyDeltaInMWH, time);
	}

	/** Return internal energy delta equivalent of given external energy delta
	 * 
	 * @param externalEnergyDelta &gt; 0: charging; &lt; 0: depleting
	 * @param time of transition
	 * @return corresponding internal energy delta equivalent */
	private double externalToInternalEnergy(double externalEnergyDelta, TimeStamp time) {
		if (externalEnergyDelta > 0) {
			return externalEnergyDelta * chargingEfficiency.getValueLinear(time);
		} else {
			return externalEnergyDelta / dischargingEfficiency.getValueLinear(time);
		}
	}

	private double ensurePowerWithinLimits(TimeStamp time, double powerInMW) {
		if (powerInMW > chargingPowerInMW.getValueLinear(time) + TOLERANCE) {
			logger.error(time + ERR_EXCEED_CHARGING_POWER + (powerInMW - chargingPowerInMW.getValueLinear(time)));
			powerInMW = chargingPowerInMW.getValueLinear(time);
		} else if (powerInMW < -dischargingPowerInMW.getValueLinear(time) - TOLERANCE) {
			logger.error(time + ERR_EXCEED_DISCHARGING_POWER + (dischargingPowerInMW.getValueLinear(time) + powerInMW));
			powerInMW = -dischargingPowerInMW.getValueLinear(time);
		}
		return powerInMW;
	}

	private double ensureEnergyWithinLimits(TimeStamp time, double energyInMWH) {
		if (energyInMWH > energyContentUpperLimitInMWH.getValueLinear(time) + TOLERANCE) {
			logger.error(
					time + ERR_EXCEED_UPPER_ENERGY_LIMIT + (energyInMWH - energyContentUpperLimitInMWH.getValueLinear(time)));
			energyInMWH = energyContentUpperLimitInMWH.getValueLinear(time);
		} else if (energyInMWH < energyContentLowerLimitInMWH.getValueLinear(time) - TOLERANCE) {
			logger.error(
					time + ERR_EXCEED_LOWER_ENERGY_LIMIT + (energyContentLowerLimitInMWH.getValueLinear(time) - energyInMWH));
			energyInMWH = energyContentLowerLimitInMWH.getValueLinear(time);
		}
		return energyInMWH;
	}

	private void ensureNoNegativeSelfDischarge(TimeStamp time, double selfDischargeRate) {
		if (currentEnergyContentInMWH < 0 && selfDischargeRate > 0) {
			logger.error(ERR_NEGATIVE_SELF_DISCHARGE + time);
		}
	}
}
