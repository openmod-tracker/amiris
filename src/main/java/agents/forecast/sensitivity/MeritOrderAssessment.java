// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import agents.forecast.sensitivity.SensitivityForecastProvider.ForecastType;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.SupplyOrderBook;

/** Can assess a merit order defined by {@link SupplyOrderBook} and {@link DemandOrderBook} and return its sensitivity.
 * 
 * @author Christoph Schimeczek */
public interface MeritOrderAssessment {
	static final String ERR_NOT_IMPLEMENTED = "No MeritOrderAssessment implemented for forecast type: ";

	/** Assesses given supply and demand books for their sensitivity; results are stored internally.
	 * 
	 * @param supplyBook from the merit order to assess
	 * @param demandBook from the merit order to assess */
	void assess(SupplyOrderBook supplyBook, DemandOrderBook demandBook);

	/** Returns powers values for additional demand, between which the sensitivity value changes linearly */
	double[] getDemandSensitivityPowers();

	/** Returns the value of the sensitivity valid at the additional demand power entry with corresponding index */
	double[] getDemandSensitivityValues();

	/** Returns powers values for additional supply, between which the sensitivity value changes linearly */
	double[] getSupplySensitivityPowers();

	/** Returns the value of the sensitivity valid at the additional supply power entry with corresponding index */
	double[] getSupplySensitivityValues();

	/** Returns the {@link MeritOrderAssessment} suited to provide the requested type of sensitivity forecast
	 * 
	 * @param type of forecast tied to a type of sensitivity
	 * @return a new {@link MeritOrderAssessment} */
	static MeritOrderAssessment build(ForecastType type) {
		switch (type) {
			case CostSensitivity:
				return new CostSensitivity();
			case MarginalCostSensitivity:
				return new MarginalCostSensitivity();
			default:
				throw new RuntimeException(ERR_NOT_IMPLEMENTED + type);
		}
	}
}
