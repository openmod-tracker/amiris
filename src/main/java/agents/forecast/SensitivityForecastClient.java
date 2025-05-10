// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast;

import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.communication.Product;

public interface SensitivityForecastClient extends AgentAbility {
	@Product
	enum Products {
		Registration, Award, SensitivityRequest
	}

}
