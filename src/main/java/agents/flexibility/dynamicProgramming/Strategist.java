// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming;

import java.util.function.BiFunction;
import agents.flexibility.BidSchedule;
import agents.flexibility.dynamicProgramming.StateManager.DispatchSchedule;
import de.dlr.gitlab.fame.time.TimePeriod;

public class Strategist {
	static final String ERR_NO_FEASIBLE_SOLUTION = "No feasible transition found for time period: ";

	/** Optimisation target */
	public enum Target {
		/** Maximise the value of a target function */
		MAXIMISE,
		/** Minimise the value of a target function */
		MINIMISE
	}

	private final StateManager stateManager;
	private final BidScheduler bidScheduler;
	private final Target target;	

	/** Instantiates new {@link Optimiser}
	 * 
	 * @param stateManager to control feasible states
	 * @param assessmentFunction to evaluate transitions and states */
	public Strategist(StateManager stateManager, BidScheduler bidScheduler, Target target) {
		this.stateManager = stateManager;
		this.bidScheduler = bidScheduler;
		this.target = target;		
	}

	public BidSchedule createSchedule(TimePeriod startingPeriod) {
		optimise(startingPeriod);
		DispatchSchedule dispatchSchedule = stateManager.getBestDispatchSchedule(bidScheduler.getSchedulingSteps());
		return bidScheduler.createBidSchedule(startingPeriod, dispatchSchedule);
	}

	/** Optimise for a defined target */
	private void optimise(TimePeriod startingPeriod) {
		stateManager.initialise(startingPeriod);
		double initialAssessmentValue = target == Target.MAXIMISE ? -Double.MAX_VALUE : Double.MAX_VALUE;
		BiFunction<Double, Double, Boolean> compare = target == Target.MAXIMISE ? (v, b) -> v > b : (v, b) -> v < b;
		for (int k = 0; k < stateManager.getNumberOfForecastTimeSteps(); k++) {
			int step = stateManager.getNumberOfForecastTimeSteps() - k - 1; // step backwards in time
			TimePeriod timePeriod = startingPeriod.shiftByDuration(step);
			stateManager.prepareFor(timePeriod.getStartTime());
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
