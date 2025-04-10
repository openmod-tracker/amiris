// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.assessment;

import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;

/** Builds {@link AssessmentFunction}s */
public final class AssessmentFunctionBuilder {
	public static final Tree parameters = Make.newTree().add(Make.newEnum("Type", Type.class)).buildTree();

	/** Available {@link AssessmentFunction}s */
	enum Type {
		/** Assess profit using an electricity price forecast neglecting any price impact of bids */
		ProfitPriceTaker,
	}

	public static final String ERR_NOT_IMPLEMENTED = "Assessment function is not implemented: ";

	public static AssessmentFunction build(ParameterData input) throws MissingDataException {
		Type type = input.getEnum("Type", Type.class);
		switch (type) {
			case ProfitPriceTaker:
				return new ProfitPriceTaker();
			default:
				throw new RuntimeException(ERR_NOT_IMPLEMENTED + type);
		}
	}
}