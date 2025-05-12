// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.communication.Product;

public interface SensitivityForecastClient extends AgentAbility {
	@Product
	enum Products {
		ForecastRegistration, NetAward, SensitivityRequest
	}

}
