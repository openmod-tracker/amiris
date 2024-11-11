// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0

package agents.heatPump;

/** Includes a basic technical description of heat pump performance
 * 
 * @author Evelyn Sperber */
public class HeatPump {
	private static final double DESIGN_AMBIENT_TEMPERATURE_IN_C = -14;
	private static final double UPPER_AMBIENT_TEMPERATURE_FOR_HEAT_PUMP_SPECIFICATION_IN_C = 10;
	private double coefficientOfPerformance;
	private final HeatPumpParameters heatPumpParams;

	/** Creates a new {@link HeatPump} instance
	 * 
	 * @param heatPumpParams required inputs */
	public HeatPump(HeatPumpParameters heatPumpParams) {
		this.heatPumpParams = heatPumpParams;
	}

	/** @return coefficient of performance according to ambient temperature
	 * @param ambientTemperatureInC ambient temperature in time step */
	public double calcCoefficientOfPerformance(double ambientTemperatureInC) {
		double copGradient = (heatPumpParams.getMaxCOP() - heatPumpParams.getMinCOP())
				/ (UPPER_AMBIENT_TEMPERATURE_FOR_HEAT_PUMP_SPECIFICATION_IN_C - DESIGN_AMBIENT_TEMPERATURE_IN_C);
		double copOffset = heatPumpParams.getMaxCOP()
				- copGradient * UPPER_AMBIENT_TEMPERATURE_FOR_HEAT_PUMP_SPECIFICATION_IN_C;
		coefficientOfPerformance = copGradient * ambientTemperatureInC + copOffset;
		return coefficientOfPerformance;
	}

	/** @return current electric power of heat pump according to ambient temperature
	 * @param ambientTemperatureInC ambient temperature in time step */
	public double calcCurrentElectricHeatPumpPowerInKW(double ambientTemperatureInC) {
		double heatPumpPowerGradient = (heatPumpParams.getMaxElectricHeatPumpPowerInKW()
				- heatPumpParams.getMinElectricHeatPumpPowerInKW())
				/ (DESIGN_AMBIENT_TEMPERATURE_IN_C - UPPER_AMBIENT_TEMPERATURE_FOR_HEAT_PUMP_SPECIFICATION_IN_C);
		double heatPumpPowerOffset = heatPumpParams.getMaxElectricHeatPumpPowerInKW()
				- heatPumpPowerGradient * DESIGN_AMBIENT_TEMPERATURE_IN_C;
		return heatPumpPowerGradient * ambientTemperatureInC + heatPumpPowerOffset;
	}

	/** @return maximum electric power of heat pump */
	public double getMaxElectricalHeatPumpPowerInKW() {
		return heatPumpParams.getMaxElectricHeatPumpPowerInKW();
	}

}