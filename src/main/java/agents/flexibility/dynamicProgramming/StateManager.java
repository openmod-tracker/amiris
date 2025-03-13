// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming;

import de.dlr.gitlab.fame.time.TimePeriod;

/** Manages the states allowed within a dynamic programming optimisation
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public interface StateManager {
	/** Initialize {@link StateManager} to allow for planning in current planning period */
	void initialise(TimePeriod startingPeriod);

	/** Make {@link StateManager} aware of time period currently under assessment */
	void prepareFor(TimePeriod timePeriod);

	/** Retrieve lowest and highest initial state indices at prepared time */
	int[] getInitialStates();

	/** Retrieve lowest and highest possible final state indices for given initial state index at prepared time */
	int[] getFinalStates(int initialStateIndex);

	/** Get a transition value from the transition from an initial to a final state	*/
	double getTransitionValueFor(int initialStateIndex, int finalStateIndex);
	
	/** Get best assessment function value available for final state index */
	double getBestValueNextPeriod(int finalStateIndex);

	/** Update the best final state for transition and log the associated best assessment value */
	void updateBestFinalState(int bestFinalStateIndex, double bestAssessmentValue);

	int getNumberOfTimeSteps();

}
