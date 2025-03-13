package agents.flexibility.dynamicProgramming;

import de.dlr.gitlab.fame.time.TimePeriod;

/** Manages the states allowed within a dynamic programming optimisation
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public interface StateManager {
	/** Initialize {@link StateManager} to allow for planning in current planning period */
	void initialise(TimePeriod startingPeriod, int numberOfTimeSteps);

	/** Make {@link StateManager} aware of time period currently under assessment */
	void prepareFor(TimePeriod timePeriod);

	/** Retrieve possible initial state indices at prepared time */
	int[] getInitialStates();

	/** Retrieve possible final state indices for given initial state index at prepared time */
	int[] getFinalStates(int initialStateIndex);

	/** Get best assessment function value available for final state index */
	double getBestValueNextPeriod(int finalStateIndex);

	/** Update the best final state for transition and log the associated best assessment value */
	void updateBestFinalState(int bestFinalStateIndex, double bestAssessmentValue);

}
