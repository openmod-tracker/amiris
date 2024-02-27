<<<<<<< Upstream, based on origin/dev
<<<<<<< Upstream, based on origin/dev
// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader;

import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.communication.Product;



public interface PowerPlantScheduler extends AgentAbility {

	/** Products of {@link PowerPlantScheduler} */
	@Product
	public static enum Products {
		/** Money paid to clients */
		Payout,
		/** Electricity production assignment to clients */
		DispatchAssignment
	};

=======
package agents.trader;

import de.dlr.gitlab.fame.communication.Product;

public interface PowerPlantScheduler {
	/** Products of {@link PowerPlantScheduler} */
	@Product
	public static enum Products {
		/** Electricity production assignment to clients */
		DispatchAssignment
	};
>>>>>>> d43a0e2 Create new exchange between ElectrolysisTrader and VarREOperator with new interface PowerPlantScheduler
=======
package agents.trader;

import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.communication.Product;



public interface PowerPlantScheduler extends AgentAbility {

	/** Products of {@link PowerPlantScheduler} */
	@Product
	public static enum Products {
		/** Money paid to clients */
		Payout,
		/** Electricity production assignment to clients */
		DispatchAssignment
	};

>>>>>>> 74ac58c Add new interface PowerPlantScheduler and finalize first draft of green hydrogen with PPA
}
