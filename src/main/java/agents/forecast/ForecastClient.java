// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast;

import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.communication.Product;

public interface ForecastClient extends AgentAbility {
	@Product
	public static enum Products {
		/** Requests for merit-order forecasts */
		MeritOrderForecastRequest,
		/** Requests for price-forecasts */
		PriceForecastRequest,
	}
}
