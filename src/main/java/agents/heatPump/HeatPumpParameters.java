// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0

package agents.heatPump;

import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.agent.input.Tree;

/** Encapsulates heat pump parameters
 * 
 * @author Evelyn Sperber */
public class HeatPumpParameters {
	/** Input structure of {@link HeatPumpParameters} */
	public static final Tree parameters = Make.newTree()
			.add(Make.newDouble("MinElectricHeatPumpPowerInKW"), Make.newDouble("MaxElectricHeatPumpPowerInKW"),
					Make.newDouble("HeatPumpPenetrationFactor"), Make.newDouble("MaxCOP"), Make.newDouble("MinCOP"),
					Make.newSeries("InstalledUnits"))
			.buildTree();

	private double minElectricHeatPumpPowerInKW;
	private double maxElectricHeatPumpPowerInKW;
	private double heatPumpPenetrationFactor;
	private double maxCOP;
	private double minCOP;
	private TimeSeries installedUnits;

	/** Creates {@link HeatPumpParameters}
	 * 
	 * @param data input data from config
	 * @throws MissingDataException if any required data is not provided */
	public HeatPumpParameters(ParameterData data) throws MissingDataException {
		minElectricHeatPumpPowerInKW = data.getDouble("MinElectricHeatPumpPowerInKW");
		maxElectricHeatPumpPowerInKW = data.getDouble("MaxElectricHeatPumpPowerInKW");
		heatPumpPenetrationFactor = data.getDouble("HeatPumpPenetrationFactor");

		maxCOP = data.getDouble("MaxCOP");
		minCOP = data.getDouble("MinCOP");
		installedUnits = data.getTimeSeries("InstalledUnits");
	}

	/** @return minimum electric power of heat pump */
	public double getMinElectricHeatPumpPowerInKW() {
		return minElectricHeatPumpPowerInKW;
	}

	/** @return maximum electric power of heat pump */
	public double getMaxElectricHeatPumpPowerInKW() {
		return maxElectricHeatPumpPowerInKW;
	}

	/** @return maximum coefficient of performance of heat pump at upper ambient temperature for heat pump specification */
	public double getMaxCOP() {
		return maxCOP;
	}

	/** @return minimum coefficient of performance of heat pump at lower ambient temperature for heat pump specification */
	public double getMinCOP() {
		return minCOP;
	}

	/** @return installed heat pump units in scenario */
	public TimeSeries getInstalledUnits() {
		return installedUnits;
	}

	/** @return market penetration factor of heat pumps */
	public double getHeatPumpPenetrationFactor() {
		return heatPumpPenetrationFactor;
	}
}
