// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming;

import java.util.function.BiFunction;
import de.dlr.gitlab.fame.time.TimePeriod;

/** An optimiser that seeks to optimise a target using dynamic programming
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public class Optimiser {
	static final String ERR_NO_FEASIBLE_SOLUTION = "No feasible transition found for time period: ";

	/** Optimisation target */
	public enum Target {
		/** Maximise the value of a target function */
		MAXIMISE,
		/** Minimise the value of a target function */
		MINIMISE
	}

	private final StateManager stateManager;
	private final Target target;

	/** Instantiates new {@link Optimiser}
	 * 
	 * @param stateManager to control feasible states
	 * @param assessmentFunction to evaluate transitions and states */
	public Optimiser(StateManager stateManager, Target target) {
		this.stateManager = stateManager;
		this.target = target;
	}

	/** Optimise for a defined target */
	public void optimise(TimePeriod startingPeriod) {
		stateManager.initialise(startingPeriod);
		double initialAssessmentValue = target == Target.MAXIMISE ? -Double.MAX_VALUE : Double.MAX_VALUE;
		BiFunction<Double, Double, Boolean> compare = target == Target.MAXIMISE ? (v, b) -> v > b : (v, b) -> v < b;
		for (int k = 0; k < stateManager.getNumberOfTimeSteps(); k++) {
			int step = stateManager.getNumberOfTimeSteps() - k - 1; // step backwards in time
			TimePeriod timePeriod = startingPeriod.shiftByDuration(step);
			stateManager.prepareFor(timePeriod);
			for (int initialStateIndex : stateManager.getInitialStates()) {
				double bestAssessmentValue = initialAssessmentValue;
				int bestFinalStateIndex = Integer.MIN_VALUE;
				for (int finalStateIndex : stateManager.getFinalStates(initialStateIndex)) {
					double value = stateManager.getTransitionValueFor(initialStateIndex, finalStateIndex)
							+ stateManager.getBestValueNextPeriod(finalStateIndex);
					if (compare.apply(value, bestAssessmentValue)) {
						bestAssessmentValue = value;
						bestFinalStateIndex = finalStateIndex;
					}
				}
				if (bestFinalStateIndex == Integer.MIN_VALUE) {
					throw new RuntimeException(ERR_NO_FEASIBLE_SOLUTION + timePeriod);
				}
				stateManager.updateBestFinalState(initialStateIndex, bestFinalStateIndex, bestAssessmentValue);
			}
		}
	}

}
