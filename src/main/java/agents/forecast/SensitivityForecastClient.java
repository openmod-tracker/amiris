package agents.forecast;

import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.communication.Product;

public interface SensitivityForecastClient extends AgentAbility {
	@Product
	enum Products {
		Registration, Bid, SensitivityRequest
	}

}
