// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility;

import static de.dlr.gitlab.fame.time.Constants.STEPS_PER_HOUR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import agents.storage.Device;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** A generic device representing any kind of electrical flexibility, e.g., pumped-hydro storages with inflow, reservoir storages,
 * heat pumps, electric vehicle fleets, load-shifting portfolios. See also the
 * <a href="https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Classes/Modules/GenericDevice">Wiki description</a>
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
	private TimeSeries externalChargingPowerInMW;
	private TimeSeries externalDischargingPowerInMW;
	private TimeSeries chargingEfficiency;
	private TimeSeries dischargingEfficiency;
	private TimeSeries energyContentUpperLimitInMWH;
	private TimeSeries energyContentLowerLimitInMWH;
	private TimeSeries selfDischargeRatePerHour;
	private TimeSeries netInflowPowerInMW;
	private double currentEnergyContentInMWH;

	/** Input parameters of a storage {@link Device} */
	public static final Tree parameters = Make.newTree()
			.add(Make.newSeries("GrossChargingPowerInMW"), Make.newSeries("NetDischargingPowerInMW"),
					Make.newSeries("ChargingEfficiency"), Make.newSeries("DischargingEfficiency"),
					Make.newSeries("EnergyContentUpperLimitInMWH"),
					Make.newSeries("EnergyContentLowerLimitInMWH"), Make.newSeries("SelfDischargeRatePerHour"),
					Make.newSeries("NetInflowPowerInMW"), Make.newDouble("InitialEnergyContentInMWH"))
			.buildTree();

	/** Instantiate new {@link GenericDevice}
	 * 
	 * @param input parameters from file
	 * @throws MissingDataException if any required input parameter is missing */
	public GenericDevice(ParameterData input) throws MissingDataException {
		externalChargingPowerInMW = input.getTimeSeries("GrossChargingPowerInMW");
		externalDischargingPowerInMW = input.getTimeSeries("NetDischargingPowerInMW");
		chargingEfficiency = input.getTimeSeries("ChargingEfficiency");
		dischargingEfficiency = input.getTimeSeries("DischargingEfficiency");
		energyContentUpperLimitInMWH = input.getTimeSeries("EnergyContentUpperLimitInMWH");
		energyContentLowerLimitInMWH = input.getTimeSeries("EnergyContentLowerLimitInMWH");
		selfDischargeRatePerHour = input.getTimeSeries("SelfDischargeRatePerHour");
		netInflowPowerInMW = input.getTimeSeries("NetInflowPowerInMW");
		currentEnergyContentInMWH = input.getDouble("InitialEnergyContentInMWH");
	}

	/** @return effective self discharge rate for given duration, considering exponential reduction over time */
	private double calcSelfDischarge(TimeStamp time, TimeSpan duration) {
		return 1. - Math.pow(1 - selfDischargeRatePerHour.getValueLinear(time), calcDurationInHours(duration));
	}

	private double calcDurationInHours(TimeSpan duration) {
		return (double) duration.getSteps() / STEPS_PER_HOUR;
	}

	/** @return current internal energy level of this {@link GenericDevice} in MWh */
	public double getCurrentInternalEnergyInMWH() {
		return currentEnergyContentInMWH;
	}

	/** Return external energy delta equivalent of given internal energy delta
	 * 
	 * @param internalEnergyDelta &gt; 0: charging; &lt; 0: depleting
	 * @param time of transition
	 * @return external energy delta equivalent */
	public double internalToExternalEnergy(double internalEnergyDelta, TimeStamp time) {
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
		double externalPowerInMW = ensurePowerWithinLimits(time, externalEnergyDeltaInMWH / calcDurationInHours(duration));
		double internalPowerInMW = externalToInternal(externalPowerInMW, time);
		double netChargingEnergyInMWH = (internalPowerInMW + netInflowPowerInMW.getValueLinear(time))
				* calcDurationInHours(duration);
		double selfDischargeRate = calcSelfDischarge(time, duration);
		selfDischargeRate = ensureNoNegativeSelfDischarge(time, selfDischargeRate);
		double selfDischargeLossInMWH = currentEnergyContentInMWH * selfDischargeRate;
		double finalEnergyContentInMWH = currentEnergyContentInMWH + netChargingEnergyInMWH - selfDischargeLossInMWH;
		finalEnergyContentInMWH = ensureEnergyWithinLimits(time, finalEnergyContentInMWH);
		double internalEnergyDeltaInMWH = finalEnergyContentInMWH - currentEnergyContentInMWH
				+ selfDischargeLossInMWH - netInflowPowerInMW.getValueLinear(time) * calcDurationInHours(duration);
		currentEnergyContentInMWH = finalEnergyContentInMWH;
		return internalToExternalEnergy(internalEnergyDeltaInMWH, time);
	}

	/** Return internal energy delta or power equivalent of given external energy delta or power
	 * 
	 * @param externalValue &gt; 0: charging; &lt; 0: depleting
	 * @param time of transition
	 * @return corresponding internal energy delta or power equivalent */
	private double externalToInternal(double externalValue, TimeStamp time) {
		if (externalValue > 0) {
			return externalValue * chargingEfficiency.getValueLinear(time);
		} else {
			return externalValue / dischargingEfficiency.getValueLinear(time);
		}
	}

	/** Logs an error if the given external (dis-)charging power exceeds its corresponding limit.
	 * 
	 * @param time at which the power is applied
	 * @param powerInMW positive values represent charging
	 * @return the applied (dis-)charging power - capped at its corresponding maximum at the given time */
	private double ensurePowerWithinLimits(TimeStamp time, double powerInMW) {
		if (powerInMW > externalChargingPowerInMW.getValueLinear(time) + TOLERANCE) {
			logger.error(time + ERR_EXCEED_CHARGING_POWER + (powerInMW - externalChargingPowerInMW.getValueLinear(time)));
			powerInMW = externalChargingPowerInMW.getValueLinear(time);
		} else if (powerInMW < -externalDischargingPowerInMW.getValueLinear(time) - TOLERANCE) {
			logger
					.error(time + ERR_EXCEED_DISCHARGING_POWER + (externalDischargingPowerInMW.getValueLinear(time) + powerInMW));
			powerInMW = -externalDischargingPowerInMW.getValueLinear(time);
		}
		return powerInMW;
	}

	/** Logs an error if the given internal target energy content exceeds its upper or lower limit.
	 * 
	 * @param time at which the energy content shall be applied
	 * @param targetEnergyContentInMWH to be checked for consistency with energy content limits
	 * @return valid energy content closest to provided energy content target */
	private double ensureEnergyWithinLimits(TimeStamp time, double targetEnergyContentInMWH) {
		if (targetEnergyContentInMWH > energyContentUpperLimitInMWH.getValueLinear(time) + TOLERANCE) {
			double exceedance = (targetEnergyContentInMWH - energyContentUpperLimitInMWH.getValueLinear(time));
			logger.error(time + ERR_EXCEED_UPPER_ENERGY_LIMIT + exceedance);
			targetEnergyContentInMWH = energyContentUpperLimitInMWH.getValueLinear(time);
		} else if (targetEnergyContentInMWH < energyContentLowerLimitInMWH.getValueLinear(time) - TOLERANCE) {
			double exceedance = (energyContentLowerLimitInMWH.getValueLinear(time) - targetEnergyContentInMWH);
			logger.error(time + ERR_EXCEED_LOWER_ENERGY_LIMIT + exceedance);
			targetEnergyContentInMWH = energyContentLowerLimitInMWH.getValueLinear(time);
		}
		return targetEnergyContentInMWH;
	}

	/** If self discharge is applied based on a negative energy content, return 0 and log an error */
	private double ensureNoNegativeSelfDischarge(TimeStamp time, double selfDischargeRate) {
		if (currentEnergyContentInMWH < -TOLERANCE && selfDischargeRate > 0) {
			logger.error(ERR_NEGATIVE_SELF_DISCHARGE + time);
			return 0;
		}
		return selfDischargeRate;
	}

	/** Return lower limit of energy content at given time
	 * 
	 * @param time at which the lower limit is to be returned
	 * @return lower energy content limit in MWh */
	public double getEnergyContentLowerLimitInMWH(TimeStamp time) {
		return energyContentLowerLimitInMWH.getValueLinear(time);
	}

	/** Return upper limit of energy content at given time
	 * 
	 * @param time at which the upper limit is to be returned
	 * @return upper energy content limit in MWh */
	public double getEnergyContentUpperLimitInMWH(TimeStamp time) {
		return energyContentUpperLimitInMWH.getValueLinear(time);
	}

	/** Return hourly self discharge rate at given time
	 * 
	 * @param time at which the self discharge rate is to be returned
	 * @return hourly self discharge rate */
	public double getSelfDischargeRate(TimeStamp time) {
		return selfDischargeRatePerHour.getValueLinear(time);
	}

	/** Return charging efficiency at given time
	 * 
	 * @param time at which to return charging efficiency
	 * @return charging efficiency at given time */
	protected double getChargingEfficiency(TimeStamp time) {
		return chargingEfficiency.getValueLinear(time);
	}

	/** Return discharging efficiency at given time
	 * 
	 * @param time at which to return discharging efficiency
	 * @return discharging efficiency at given time */
	protected double getDischargingEfficiency(TimeStamp time) {
		return dischargingEfficiency.getValueLinear(time);
	}

	/** Return net inflow power at given time
	 * 
	 * @param time at which to return the net inflow power
	 * @return net inflow power at given time in MW */
	protected double getNetInflowInMW(TimeStamp time) {
		return netInflowPowerInMW.getValueLinear(time);
	}

	/** Return external charging power at given time
	 * 
	 * @param time at which to return the external charging power
	 * @return external charging power at given time in MW */
	public double getExternalChargingPowerInMW(TimeStamp time) {
		return externalChargingPowerInMW.getValueLinear(time);
	}

	/** Return external discharging power at given time
	 * 
	 * @param time at which to return the external discharging power
	 * @return external discharging power at given time in MW */
	public double getExternalDischargingPowerInMW(TimeStamp time) {
		return externalDischargingPowerInMW.getValueLinear(time);
	}
}
