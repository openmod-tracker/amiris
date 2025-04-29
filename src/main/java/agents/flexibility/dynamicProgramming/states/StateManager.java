// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.states;

import java.util.ArrayList;
import agents.flexibility.GenericDevice;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Manages the states allowed within a dynamic programming optimisation
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public interface StateManager {
	/** Contains the course of the internal energy levels and external energy deltas over a dispatch */
	public static class DispatchSchedule {
		public final double[] externalEnergyDeltasInMWH;
		public final double[] initialInternalEnergyInMWH;

		/** Instantiate new {@link DispatchSchedule}
		 * 
		 * @param externalEnergyDeltasInMWH course of external energy deltas during dispatch
		 * @param initialInternalEnergyInMWH course of expected internal energy during dispatch */
		public DispatchSchedule(double[] externalEnergyDeltasInMWH, double[] initialInternalEnergyInMWH) {
			this.externalEnergyDeltasInMWH = externalEnergyDeltasInMWH;
			this.initialInternalEnergyInMWH = initialInternalEnergyInMWH;
		}
	}

	/** Initialise {@link StateManager} to allow for planning in current planning period
	 * 
	 * @param startingPeriod first time period of an upcoming planning */
	void initialise(TimePeriod startingPeriod);

	/** Make {@link StateManager} aware of time currently under assessment
	 * 
	 * @param time to be assessed */
	void prepareFor(TimeStamp time);

	/** Tells whether {@link #getInitialStates()} and {@link #getFinalStates(int)} return a complete list of available states or
	 * just first and last index (inclusive)
	 * 
	 * @return true, if full state lists are returned; false, if exactly the first and last index are returned */
	boolean useStateList();

	/** Retrieve indices of initial states at prepared time; return value depends on result of {@link #useStateList()}:
	 * <ul>
	 * <li>if true, a complete list of available initial state indices is returned</li>
	 * <li>if false, only the first and last (inclusive) initial state index is returned</li>
	 * </ul>
	 * 
	 * @return state indices available at prepared time */
	int[] getInitialStates();

	/** Retrieve indices of possible final state for given initial state index at prepared time; return value depends on result of
	 * {@link #useStateList()}:
	 * <ul>
	 * <li>if true, a complete list of available final state indices is returned</li>
	 * <li>if false, only the first and last (inclusive) final state index is returned</li>
	 * </ul>
	 * 
	 * @param initialStateIndex index of state at the begin of a transition
	 * @return final state indices reachable from given initial state at prepared time */
	int[] getFinalStates(int initialStateIndex);

	/** Get a transition value from the transition from an initial to a final state
	 * 
	 * @param initialStateIndex index of state at the begin of a transition
	 * @param finalStateIndex index of state at the end of a transition
	 * @return value of the transition between two states */
	double getTransitionValueFor(int initialStateIndex, int finalStateIndex);

	/** Get best assessment function value available for final state index
	 * 
	 * @param finalStateIndex to get the best assessment value for
	 * @return best assessment known for given final state */
	double getBestValueNextPeriod(int finalStateIndex);

	/** Update the best final state for transition and log the associated best assessment value
	 * 
	 * @param initialStateIndex index of state at the begin of a transition
	 * @param bestFinalStateIndex index of state at the end of a transition
	 * @param bestAssessmentValue to be associated with this transition */
	void updateBestFinalState(int initialStateIndex, int bestFinalStateIndex, double bestAssessmentValue);

	/** Get number of time intervals within the foresight horizon
	 * 
	 * @return number of time intervals */
	int getNumberOfForecastTimeSteps();

	/** Return the {@link DispatchSchedule} from the starting period and the current state of the {@link GenericDevice}
	 * 
	 * @param schedulingSteps number of scheduling steps
	 * @return dispatch schedule extending over the given number of scheduling steps */
	DispatchSchedule getBestDispatchSchedule(int schedulingSteps);

	/** Return starting time of each planning interval in the planning horizon
	 * 
	 * @param startingPeriod first interval of the planning horizon
	 * @return list of starting times */
	public ArrayList<TimeStamp> getPlanningTimes(TimePeriod startingPeriod);
}
