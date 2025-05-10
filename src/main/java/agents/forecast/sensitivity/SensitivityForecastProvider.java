// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.communication.Product;

/** A sensitivity forecast provider */
public interface SensitivityForecastProvider extends AgentAbility {
	@Product
	enum Products {
		SensitivityForecast
	}

	enum ForecastType {
		CostInsensitive, CostSensitivity, MarginalCostSensitivity
	}
}
