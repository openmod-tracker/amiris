package agents.flexibility;

import agents.storage.Device;
import de.dlr.gitlab.fame.agent.input.GroupBuilder;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimeStamp;

/** A generic device representing any kind of electrical flexibility, e.g., pumped-hydro storages with inflow, reservoir storages,
 * heat pumps, electric vehicle fleets, load-shifting portfolios
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public class GenericDevice {
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
					Make.newSeries("NetInflowPowerInMW"),
					Make.newDouble("InitialEnergyContentInMWH"));

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

	public double getMaxEnergyIncrease(TimeStamp time, double assumedEnergyContentInMWH) {
		return chargingPowerInMW.getValueLinear(time) + netInflowPowerInMW.getValueLinear(time)
				- assumedEnergyContentInMWH * selfDischargeRatePerHour.getValueLinear(time);
	}

	public double getMaxEnergyDecrease(TimeStamp time, double assumedEnergyContentInMWH) {
		return dischargingPowerInMW.getValueLinear(time) - netInflowPowerInMW.getValueLinear(time)
				+ assumedEnergyContentInMWH * selfDischargeRatePerHour.getValueLinear(time);
	}

	public double getEnergyContentUpperLimitInMWH(TimeStamp time) {
		return energyContentUpperLimitInMWH.getValueLinear(time);
	}

	public double getEnergyContentLowerLimitInMWH(TimeStamp time) {
		return energyContentLowerLimitInMWH.getValueLinear(time);
	}

	public double getCurrentInternalEnergyInMWH() {
		return currentEnergyContentInMWH;
	}

	/** @param time of transition
	 * @param initialEnergyContentInMWH at the beginning of transition
	 * @param finalEnergyContentInMWH at the end of transition
	 * @return external energy difference for transition from initial to final internal energy content level at given time */
	public double simulateTransition(TimeStamp time, double initialEnergyContentInMWH,
			double finalEnergyContentInMWH) {
		return 0.;
	}

	/** @param time at which charging occurs
	 * @param externalEnergyDeltaInMWH (dis-)charging energy applied to this device (positive: charging, negative: discharging)
	 * @return actual external energy delta for transition considering power and capacity limits */
	public double charge(TimeStamp time, double externalEnergyDeltaInMWH) {
		return 0.;
	}

}
