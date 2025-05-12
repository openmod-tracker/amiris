// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.assessment;

import agents.flexibility.dynamicProgramming.Optimiser.Target;
import agents.forecast.sensitivity.SensitivityForecastProvider.ForecastType;

/** Minimise system cost of transitions using a merit order forecast and estimating the impact of own transitions on system costs
 * 
 * @author Christoph Schimeczek */
public class MinSystemCost extends SensitivityBasedAssessment {
	@Override
	public double assessTransition(double externalEnergyDeltaInMWH) {
		double sign = Math.signum(externalEnergyDeltaInMWH);
		return sign * currentSensitivity.getValue(externalEnergyDeltaInMWH);
	}

	@Override
	public Target getTargetType() {
		return Target.MINIMISE;
	}

	@Override
	public ForecastType getSensitivityType() {
		return ForecastType.MarginalCostSensitivity;
	}
}
