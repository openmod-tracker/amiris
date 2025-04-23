// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.evAggregator;

/** Represents a fleet of electric vehicles in its abstract form, i.e. without a specific state of charge
 * 
 * @author Felix Nitsch, Johannes Kochems, Ulrich Frey */
public abstract class AbstractFleet {
	protected double internalPowerInMW;
	protected double energyStorageCapacityInMWH;
	private double energyToPowerRatio;
	private double dischargingEfficiency;
	private double chargingEfficiency;

	/** Creates an {@link AbstractFleet}
	 * 
	 * @param energyToPowerRatio ratio of energy capacity to maximum internal charging and dis-charging power (assumed symmetric)
	 * @param chargingEfficiency efficiency during charging [0..1]; affects maximum external charging power
	 * @param dischargingEfficiency efficiency during discharging [0..1]; affects maximum external discharging power */
	protected AbstractFleet(double energyToPowerRatio, double chargingEfficiency,
			double dischargingEfficiency) {
		this.energyToPowerRatio = energyToPowerRatio;
		this.chargingEfficiency = chargingEfficiency;
		this.dischargingEfficiency = dischargingEfficiency;
	}

	/** @return internal charging / discharging power maximum in MW */
	public double getInternalPowerInMW() {
		return internalPowerInMW;
	}

	/** Set internal charging / discharging power maximum in MW
	 * 
	 * @param internalPowerInMW new internal power */
	public void setInternalPowerInMW(double internalPowerInMW) {
		this.internalPowerInMW = internalPowerInMW;
	}

	/** Set internal energy storage capacity */
	public void setEnergyStorageCapacityInMWH() {
		this.energyStorageCapacityInMWH = energyToPowerRatio * internalPowerInMW;
	}

	/** @return internal energy storage capacity in MWh */
	public double getEnergyStorageCapacityInMWH() {
		return energyStorageCapacityInMWH;
	}

	/** @return the energy-to-power ratio of this storage; equals the time needed to fully charge / discharge the storage */
	public double getEnergyToPowerRatio() {
		return energyToPowerRatio;
	}

	/** Return internal energy delta equivalent of given external energy delta
	 * 
	 * @param externalEnergyDelta &gt; 0: charging; &lt; 0: depleting
	 * @return corresponding internal energy delta equivalent */
	public double externalToInternalEnergy(double externalEnergyDelta) {
		if (externalEnergyDelta > 0) {
			return externalEnergyDelta * chargingEfficiency;
		} else {
			return externalEnergyDelta / dischargingEfficiency;
		}
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

	/** @return maximum external charging power */
	public double getExternalChargingPowerInMW() {
		return internalPowerInMW / chargingEfficiency;
	}

	/** @return maximum external discharging power */
	public double getExternalDischargingPowerInMW() {
		return internalPowerInMW * dischargingEfficiency;
	}
}