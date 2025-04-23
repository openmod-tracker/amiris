// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.evAggregator;

import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Represents an aggregator managing a fleet of electric vehicles
 *
 * @author Felix Nitsch, Johannes Kochems, Ulrich Frey */
public class Fleet extends AbstractFleet {
	static final String ERR_INITIAL_ENERGY_TOO_SMALL = "`InitialEnergyLevelInMWH` is smaller than the required `MinimumEnergyLevelInMWH`.";
	static final String ERR_INITIAL_ENERGY_TOO_LARGE = "`InitialEnergyLevelInMWH` is larger than the `MaximumEnergyLevelInMWH`.";

	private double currentEnergyInStorageInMWH;
	private TimeSeries minimumEnergyInStorageInMWH;
	private TimeSeries maximumEnergyInStorageInMWH;
	private TimeSeries connectedVehicleShare;

	public static final Tree parameters = Make.newTree()
			.add(Make.newDouble("EnergyToPowerRatio"), Make.newDouble("ChargingEfficiency"),
					Make.newDouble("DischargingEfficiency"),
					Make.newDouble("InitialEnergyLevelInMWH"), Make.newSeries("MinimumEnergyLevelInMWH"),
					Make.newSeries("MaximumEnergyLevelInMWH"),
					Make.newDouble("InstalledPowerInMW"),
					Make.newSeries("ConnectionShare").help("Share of vehicles connected to the power grid."))
			.buildTree();

	/** Creates a physical {@link Fleet}
	 * 
	 * @param input ParameterData group to match the {@link #parameters} Tree
	 * @throws MissingDataException if any required data is not provided */
	public Fleet(ParameterData input) throws MissingDataException {
		super(input.getDouble("EnergyToPowerRatio"), input.getDouble("ChargingEfficiency"),
				input.getDouble("DischargingEfficiency"));
		setInternalPowerInMW(input.getDouble("InstalledPowerInMW"));
		setEnergyStorageCapacityInMWH();
		setInternalEnergyInMWH(input.getDouble("InitialEnergyLevelInMWH"));
		minimumEnergyInStorageInMWH = input.getTimeSeries("MinimumEnergyLevelInMWH");
		maximumEnergyInStorageInMWH = input.getTimeSeries("MaximumEnergyLevelInMWH");
		connectedVehicleShare = input.getTimeSeries("ConnectionShare");
	}

	/** Checks if the InitialEnergyLevelInMWH is within the MinimumEnergyLevelInMWH
	 * 
	 * @param time TimeStamp for which to check minimum energy level */
	public void checkInitialEnergyLevel(TimeStamp time) {
		if (currentEnergyInStorageInMWH < minimumEnergyInStorageInMWH.getValueLaterEqual(time)) {
			throw new IllegalStateException(ERR_INITIAL_ENERGY_TOO_SMALL);
		}
		if (currentEnergyInStorageInMWH > maximumEnergyInStorageInMWH.getValueLaterEqual(time)) {
			throw new IllegalStateException(ERR_INITIAL_ENERGY_TOO_LARGE);
		}
	}

	/** Set the initial energy content and thereby ensure to keep it within bounds */
	private void setInternalEnergyInMWH(double initialEnergyLevelInMWH) {
		currentEnergyInStorageInMWH = Math.max(0, Math.min(initialEnergyLevelInMWH, energyStorageCapacityInMWH));
	}

	/** (Dis-)charges this fleet according to given external energy delta
	 * 
	 * @param externalChargingPower
	 *          <ul>
	 *          <li>externalChargingPower &gt; 0: charging</li>
	 *          <li>externalChargingPower &lt; 0: depleting</li>
	 *          </ul>
	 * @return actual external charging power, considering power and energy capacity restrictions of this fleet */
	public double chargeInMW(double externalChargingPower) {
		double internalChargingPowerInMW = externalToInternalEnergy(externalChargingPower);
		internalChargingPowerInMW = considerPowerLimits(internalChargingPowerInMW);
		double nextEnergyInStorageInMWH = currentEnergyInStorageInMWH + internalChargingPowerInMW;
		double internalEnergyDelta = nextEnergyInStorageInMWH - currentEnergyInStorageInMWH;
		currentEnergyInStorageInMWH = nextEnergyInStorageInMWH;
		return internalToExternalEnergy(internalEnergyDelta);
	}

	/** reduces given (dis-)charging power to the maximum defined by {@link #getInternalPowerInMW()} */
	private double considerPowerLimits(double internalChargingPowerInMW) {
		if (internalChargingPowerInMW > 0) {
			return Math.min(internalChargingPowerInMW, internalPowerInMW);
		} else {
			return Math.max(internalChargingPowerInMW, -internalPowerInMW);
		}
	}

	/** @return current (internal) energy content in storage */
	public double getCurrentEnergyInStorageInMWH() {
		return currentEnergyInStorageInMWH;
	}

	/** @return minimum energy in storage in MWh at given time
	 * @param time TimeStamp to evaluate */
	public double getCurrentMinEnergyInStorageInMWH(TimeStamp time) {
		return minimumEnergyInStorageInMWH.getValueLinear(time);
	}

	/** @return maximum energy in storage in MWh at given time
	 * @param time TimeStamp to evaluate */
	public double getCurrentMaxEnergyInStorageInMWH(TimeStamp time) {
		return maximumEnergyInStorageInMWH.getValueLinear(time);
	}

	/** @return connection share at given time
	 * @param time TimeStamp to evaluate */
	public double getCurrentConnectionShare(TimeStamp time) {
		return connectedVehicleShare.getValueLinear(time);
	}

}