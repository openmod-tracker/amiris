// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.assessment;

import agents.flexibility.dynamicProgramming.Optimiser.Target;
import agents.forecast.sensitivity.SensitivityForecastProvider.ForecastType;

/** Maximise profits of transitions using a merit order forecast and estimating the impact of own transitions on profits
 * 
 * @author Christoph Schimeczek */
public class MaxProfit extends SensitivityBasedAssessment {
	@Override
	public double assessTransition(double externalEnergyDeltaInMWH) {
		double sign = -Math.signum(externalEnergyDeltaInMWH);
		return sign * currentSensitivity.getValue(externalEnergyDeltaInMWH);
	}

	@Override
	public Target getTargetType() {
		return Target.MAXIMISE;
	}

	@Override
	public ForecastType getSensitivityType() {
		return ForecastType.CostSensitivity;
	}
}
