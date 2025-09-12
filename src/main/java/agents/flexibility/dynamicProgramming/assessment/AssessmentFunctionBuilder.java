// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.assessment;

import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;

/** Builds {@link AssessmentFunction}s from provided input parameters
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public final class AssessmentFunctionBuilder {
	public static final Tree parameters = Make.newTree().add(Make.newEnum("Type", Type.class)).buildTree();

	/** Available {@link AssessmentFunction}s */
	enum Type {
		/** Maximises profit neglecting any price impact of bids */
		MAX_PROFIT_PRICE_TAKER,
		/** Minimises total system costs taking into account the impact of bids from all GenericFlexibilityTraders */
		MIN_SYSTEM_COST,
		/** Maximises own profits taking into account the impact of bids from all GenericFlexibilityTraders */
		MAX_PROFIT,
	}

	public static final String ERR_NOT_IMPLEMENTED = "Assessment function is not implemented: ";

	public static AssessmentFunction build(ParameterData input) throws MissingDataException {
		Type type = input.getEnum("Type", Type.class);
		switch (type) {
			case MAX_PROFIT_PRICE_TAKER:
				return new MaxProfitPriceTaker();
			case MIN_SYSTEM_COST:
				return new MinSystemCost();
			case MAX_PROFIT:
				return new MaxProfit();
			default:
				throw new RuntimeException(ERR_NOT_IMPLEMENTED + type);
		}
	}
}