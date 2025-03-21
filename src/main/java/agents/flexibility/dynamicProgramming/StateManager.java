// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming;

import agents.flexibility.GenericDevice;
import de.dlr.gitlab.fame.time.TimePeriod;

/** Manages the states allowed within a dynamic programming optimisation
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public interface StateManager {
	/** Initialize {@link StateManager} to allow for planning in current planning period */
	void initialise(TimePeriod startingPeriod);

	/** Make {@link StateManager} aware of time period currently under assessment */
	void prepareFor(TimePeriod timePeriod);

	/** Retrieve list of initial states indices at prepared time */
	int[] getInitialStates();

	/** Retrieve list of possible final state indices for given initial state index at prepared time */
	int[] getFinalStates(int initialStateIndex);

	/** Get a transition value from the transition from an initial to a final state */
	double getTransitionValueFor(int initialStateIndex, int finalStateIndex);

	/** Get best assessment function value available for final state index */
	double getBestValueNextPeriod(int finalStateIndex);

	/** Update the best final state for transition and log the associated best assessment value */
	void updateBestFinalState(int initialStateIndex, int bestFinalStateIndex, double bestAssessmentValue);

	int getNumberOfForecastTimeSteps();

	/** Return the external energy delta starting from the starting period and the current state of the {@link GenericDevice}
	 * 
	 * @param schedulingSteps for actual dispatch scheduling
	 * @return array of external energy deltas (charging > 0; discharging < 0) */
	double[] getBestDispatchSchedule(int schedulingSteps);

}
