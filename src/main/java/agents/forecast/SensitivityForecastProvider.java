package agents.forecast;

import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.communication.Product;

/** A sensitivity forecast provider */
public interface SensitivityForecastProvider extends AgentAbility {
	@Product
	enum Products {
		SensitivityForecast
	}
	
	enum Type {
		PriceSensitivity
	}
}
