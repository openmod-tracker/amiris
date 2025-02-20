// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.forecastApi;

import java.util.Map;

/** Encapsulates a forecast request to forecastapi with the main objective to handle time series predictions
 * 
 * @author Felix Nitsch */
public class ForecastApiRequest {

	private long forecastStartTime;
	private int forecastWindow;
	private Map<Long, Double> pastTargets;
	private Map<Long, Double> residualLoad;

	/** Constructs ForecastApiRequest given a forecastStartTime, forecastWindow, and a pastTargets
	 * 
	 * @param forecastStartTime start time of forecast
	 * @param forecastWindow window of forecast
	 * @param pastTargets realized electricity prices with time steps
	 * @param residualLoad residual load with time steps */
	public ForecastApiRequest(long forecastStartTime, int forecastWindow, Map<Long, Double> pastTargets,
			Map<Long, Double> residualLoad) {
		this.forecastStartTime = forecastStartTime;
		this.forecastWindow = forecastWindow;
		this.pastTargets = pastTargets;
		this.residualLoad = residualLoad;
	}

	/** @return the forecastStartTime */
	public long getForecastStartTime() {
		return forecastStartTime;
	}

	/** @param forecastStartTime the forecastStartTime to set */
	public void setForecastStartTime(long forecastStartTime) {
		this.forecastStartTime = forecastStartTime;
	}

	/** @return the forecastWindow */
	public int getForecastWindow() {
		return forecastWindow;
	}

	/** @param forecastWindow the forecastWindow to set */
	public void setForecastWindow(int forecastWindow) {
		this.forecastWindow = forecastWindow;
	}

	/** @return the pastTargets */
	public Map<Long, Double> getPastTargets() {
		return pastTargets;
	}

	/** @param pastTargets the pastTargets to set */
	public void setPastTargets(Map<Long, Double> pastTargets) {
		this.pastTargets = pastTargets;
	}

	/** @return the residualLoad */
	public Map<Long, Double> getResidualLoad() {
		return residualLoad;
	}

	/** @param residualLoad the residualLoad to set */
	public void setResidualLoad(Map<Long, Double> residualLoad) {
		this.residualLoad = residualLoad;
	}

}
