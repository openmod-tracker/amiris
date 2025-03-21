// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.storage;

import java.util.TreeMap;
import agents.flexibility.dynamicProgramming.AssessmentFunction;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** A profit maximiser using an electricity price forecast insensitive to price repercussions
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public class MaxProfitPriceTaker implements AssessmentFunction {
	private double currentElectricityPriceInEURperMWH;
	private TreeMap<TimeStamp, Double> electricityPriceForecastsInEURperMWH = new TreeMap<>();

	@Override
	public void prepareFor(TimePeriod timePeriod) {
		currentElectricityPriceInEURperMWH = electricityPriceForecastsInEURperMWH.getOrDefault(timePeriod.getStartTime(),
				0.);
	}

	@Override
	public double getEnergyCosts(double externalEnergyDeltaInMWH) {
		return externalEnergyDeltaInMWH * currentElectricityPriceInEURperMWH;
	}

	/** Clear entries of electricity price forecasts before given time
	 * 
	 * @param time before which elements are cleared */
	public void clearBefore(TimeStamp time) {
		electricityPriceForecastsInEURperMWH.headMap(time).clear();
	}

	/** Add electricity price forecast for a given time
	 * 
	 * @param time of electricity price forecast
	 * @param valueInEURperMWH electricity price forecast */
	public void addElectricityPriceForecastFor(TimeStamp time, Double valueInEURperMWH) {
		electricityPriceForecastsInEURperMWH.put(time, valueInEURperMWH);
	}
}
