// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming;

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

	/** Initialize {@link StateManager} to allow for planning in current planning period */
	void initialise(TimePeriod startingPeriod);

	/** Make {@link StateManager} aware of time currently under assessment */
	void prepareFor(TimeStamp time);

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

	/** Return the {@link DispatchSchedule} from the starting period and the current state of the {@link GenericDevice}
	 * 
	 * @param schedulingSteps number of scheduling steps
	 * @return dispatch schedule extending over the given number of scheduling steps */
	DispatchSchedule getBestDispatchSchedule(int schedulingSteps);

	/** Return energy level of the device in its current state
	 * 
	 * @return current device energy content in MWh */
	double getCurrentDeviceEnergyContentInMWH();
}
