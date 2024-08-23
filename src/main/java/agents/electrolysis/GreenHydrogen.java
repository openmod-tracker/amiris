package agents.electrolysis;

import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.service.output.Output;

public interface GreenHydrogen extends AgentAbility {

	/** Available output columns */
	@Output
	public static enum Outputs {
		/** Amount of electricity consumed in this period for operating the electrolysis unit */
		ConsumedElectricityInMWH,
		/** Variable operation and maintenance costs in EUR */
		VariableCostsInEUR,
		/** Total received money for selling hydrogen in EUR */
		ReceivedMoneyForHydrogenInEUR,
		/** Total received money for selling electricity in EUR */
		ReceivedMoneyForElectricityInEUR,
		/** Surplus electricity generation offered to the day-ahead market in MWh */
		OfferedSurplusEnergyInMWH
	};

	/** Available products */
	@Product
	public static enum Products {
		/** Request for Power Purchase Agreement (PPA) contract data with electricity production unit */
		PpaInformationRequest,
		/** Request for forecasted Power Purchase Agreement (PPA) contract data with electricity production unit */
		PpaInformationForecastRequest
	};
}
