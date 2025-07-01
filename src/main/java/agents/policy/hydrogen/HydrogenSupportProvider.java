// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.policy.hydrogen;

import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterBuilder;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.communication.Product;

public interface HydrogenSupportProvider {
	static ParameterBuilder setParameter = Make.newStringSet("HydrogenPolicySet");

	@Product
	public enum Products {
		/** Info on the support scheme to be applied to a set of clients */
		SupportInfo,
		/** Actual pay-out of the support */
		SupportPayout
	}

	public static String readSet(ParameterData input) throws MissingDataException {
		return input.getString("HydrogenPolicySet");
	}
}
