// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader;

import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.communication.Product;

/** Can order power plants how to dispatch
 * 
 * @author Johannes Kochems */
public interface PowerPlantScheduler extends AgentAbility {

	/** Products of {@link PowerPlantScheduler} */
	@Product
	public static enum Products {
		/** Money paid to clients */
		Payout,
		/** Electricity production assignment to clients */
		DispatchAssignment
	};
}