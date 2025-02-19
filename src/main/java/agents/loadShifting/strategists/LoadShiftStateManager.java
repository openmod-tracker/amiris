// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.loadShifting.strategists;

import java.util.ArrayList;
import java.util.HashMap;
import agents.loadShifting.LoadShiftingPortfolio;
import de.dlr.gitlab.fame.time.TimePeriod;

/** Manages {@link LoadShiftState}s which consist of a shiftTime and an energyState by determining feasible resp. infeasible
 * transition paths
 * 
 * @author Johannes Kochems, Christoph Schimeczek */
public class LoadShiftStateManager {
	/** A LoadShiftState consists of a shiftTime, i.e. the time that load has already been shifted for and the energyState, i.e. the
	 * amount of load that has cumulatively been shifted since the last compensation of prior load shifts. */
	public static class LoadShiftState {
		/** load shifting duration so far */
		public final int shiftTime;
		/** energy state index */
		public final int energyState;

		/** Construct a LoadShiftState
		 * 
		 * @param shiftTime duration for which the portfolio is shifted
		 * @param energyState internal energy state index representing the amount of shifted energy */
		public LoadShiftState(int shiftTime, int energyState) {
			this.shiftTime = shiftTime;
			this.energyState = energyState;
		}

		/** Return the energy state delta between this {@link LoadShiftState} and the given one
		 * 
		 * @param other State to compare with
		 * @return positive value, if this {@link LoadShiftState} has a higher energy state than the given other one */
		public int calculateStateDelta(LoadShiftState other) {
			return this.energyState - other.energyState;
		}

		/** @return true, if this {@link LoadShiftState} cannot be reached, since either its shiftTime or energyState is zero while
		 *         the other is not
		 * @param zeroEnergyStateIndex index of the state that refers to no load shifting */
		public boolean isNotSensible(int zeroEnergyStateIndex) {
			if ((shiftTime == 0 && energyState != zeroEnergyStateIndex)
					|| (shiftTime != 0 && energyState == zeroEnergyStateIndex)) {
				return true;
			}
			return false;
		}
	}

	/** Monetary penalty applied to infeasible Paths (i.e. that cannot balance their shifted load within the maximum shift time */
	public static final double PENALTY = Math.pow(10, 200);
	/** Denotes an infeasible state of the load shifting portfolio */
	public static final LoadShiftState INFEASIBLE_STATE = new LoadShiftState(Integer.MIN_VALUE, Integer.MIN_VALUE);

	private LoadShiftingPortfolio loadShiftingPortfolio;
	private final int numberOfPowerStates;
	private final int numberOfEnergyStates;
	private final int zeroEnergyStateIndex;
	/** Stores all possible LoadShift states. */
	private final LoadShiftState[][] stateMap;

	/** Create a {@link LoadShiftStateManager} for the given {@link LoadShiftingPortfolio}
	 * 
	 * @param loadShiftingPortfolio to be managed by this {@link LoadShiftStateManager} */
	public LoadShiftStateManager(LoadShiftingPortfolio loadShiftingPortfolio) {
		this.loadShiftingPortfolio = loadShiftingPortfolio;
		numberOfPowerStates = ((int) (loadShiftingPortfolio.getPowerInMW() / getEnergyResolutionInMWH())) * 2 + 1;
		int numberOfEnergyStatesUp = (int) (loadShiftingPortfolio.getEnergyLimitUpInMWH() / getEnergyResolutionInMWH());
		int numberOfEnergyStatesDown = (int) (loadShiftingPortfolio.getEnergyLimitDownInMWH() / getEnergyResolutionInMWH());
		numberOfEnergyStates = numberOfEnergyStatesUp + numberOfEnergyStatesDown + 1;
		zeroEnergyStateIndex = numberOfEnergyStatesDown;
		stateMap = prepareStateMap();
	}

	/** @return portfolio's energy resolution */
	private double getEnergyResolutionInMWH() {
		return loadShiftingPortfolio.getEnergyResolutionInMWH();
	}

	/** Initialise the map of LoadShiftStates with all possible LoadShiftStates */
	private LoadShiftState[][] prepareStateMap() {
		LoadShiftState[][] stateMap = new LoadShiftState[getMaxShiftTime()][numberOfEnergyStates];
		for (int shiftTime = 0; shiftTime < getMaxShiftTime(); shiftTime++) {
			for (int energyState = 0; energyState < numberOfEnergyStates; energyState++) {
				stateMap[shiftTime][energyState] = new LoadShiftState(shiftTime, energyState);
			}
		}
		return stateMap;
	}

	/** @return portfolio's maximum shift time */
	private int getMaxShiftTime() {
		return loadShiftingPortfolio.getMaximumShiftTimeInHours();
	}

	/** Return feasible LoadShiftStates, ignoring states not sensible
	 * 
	 * @param initialStates list of States that is to be cleared and refilled with new initial states */
	public void returnInitialStates(ArrayList<LoadShiftState> initialStates) {
		initialStates.clear();
		for (int shiftTime = 0; shiftTime < getMaxShiftTime(); shiftTime++) {
			for (int energyState = 0; energyState < numberOfEnergyStates; energyState++) {
				LoadShiftState loadShiftState = stateMap[shiftTime][energyState];
				if (!loadShiftState.isNotSensible(zeroEnergyStateIndex)) {
					initialStates.add(loadShiftState);
				}
			}
		}
	}

	/** Extract the next feasible LoadShiftStates taking into account the shiftTime and the energy limit restrictions. Feasible
	 * states are written to given variable. Allow for a reset of shiftTime to 1 coming at the cost of compensation of a prior
	 * shift.
	 * 
	 * @param initialState to calculate feasible follow-up states for
	 * @param nextFeasibleStatesAndCostsForProlonging maps next feasible states to their costs for prolonging (if any)
	 * @param timePeriod of this state transition
	 * @param isLastPeriod must be true if this is the last transition of the optimisation period */
	public void insertNextFeasibleStates(LoadShiftState initialState,
			HashMap<LoadShiftState, Double> nextFeasibleStatesAndCostsForProlonging, TimePeriod timePeriod,
			boolean isLastPeriod) {
		nextFeasibleStatesAndCostsForProlonging.clear();

		int[] powerStepLimits = extractPowerStepLimits(timePeriod);
		int lowerBound = Math.max(0, initialState.energyState - powerStepLimits[0]);
		int upperBound = Math.min(numberOfEnergyStates - 1, initialState.energyState + powerStepLimits[1]);

		if (!isLastPeriod) {
			for (int energyStateIndex = lowerBound; energyStateIndex <= upperBound; energyStateIndex++) {
				int nextShiftTime = calcNextShiftTime(initialState, energyStateIndex);
				if (nextShiftTime < getMaxShiftTime()) {
					nextFeasibleStatesAndCostsForProlonging.put(stateMap[nextShiftTime][energyStateIndex], 0.0);
				}
			}
			if (initialState.shiftTime == getMaxShiftTime() - 1) {
				addShiftProlongingOptionAtProlongingCosts(initialState, powerStepLimits,
						nextFeasibleStatesAndCostsForProlonging, timePeriod);
			}
		}
		if (!nextFeasibleStatesAndCostsForProlonging.containsKey(stateMap[0][zeroEnergyStateIndex])) {
			nextFeasibleStatesAndCostsForProlonging.put(stateMap[0][zeroEnergyStateIndex], 0.0);
		}
	}

	/** @return The last feasible power steps as an array of integers */
	private int[] extractPowerStepLimits(TimePeriod timeSegment) {
		int maxStepDown = (int) (loadShiftingPortfolio.getMaxPowerDownInMW(timeSegment) / getEnergyResolutionInMWH());
		int maxStepUp = (int) (loadShiftingPortfolio.getMaxPowerUpInMW(timeSegment) / getEnergyResolutionInMWH());
		return new int[] {maxStepDown, maxStepUp};
	}

	/** Calculate the next shift time
	 * 
	 * @return 0 for zeroEnergyState; 1 if immediate shift in the other direction occurs; previous shift time + 1 else */
	private int calcNextShiftTime(LoadShiftState initialState, int finalEnergyIndex) {
		if (finalEnergyIndex == zeroEnergyStateIndex) {
			return 0;
		} else if ((finalEnergyIndex > zeroEnergyStateIndex && initialState.energyState < zeroEnergyStateIndex)
				|| (finalEnergyIndex < zeroEnergyStateIndex && initialState.energyState > zeroEnergyStateIndex)) {
			return 1;
		}
		return initialState.shiftTime + 1;
	}

	/** Add an reset option coming at variable costs which allows to balance parts of the {@link LoadShiftingPortfolio} while other
	 * parts are continued to be shifted in the same direction */
	private void addShiftProlongingOptionAtProlongingCosts(LoadShiftState initialState, int[] powerStepLimits,
			HashMap<LoadShiftState, Double> nextFeasibleStatesAndCostsForReset, TimePeriod timePeriod) {
		final int resettedShiftTime = 1;
		final int differenceToZeroState = Math.abs(initialState.energyState - zeroEnergyStateIndex);
		double specificShiftingCost = loadShiftingPortfolio.getVariableShiftCostsInEURPerMWH(timePeriod.getStartTime());
		double shiftedEnergy = 2 * differenceToZeroState * getEnergyResolutionInMWH();
		double variableCostsForProlongingInEUR = specificShiftingCost * shiftedEnergy;
		int remainder = Math.min(powerStepLimits[0], powerStepLimits[1]) - 2 * differenceToZeroState;
		if (remainder >= 0 && differenceToZeroState != 0) {
			int newEnergyState;
			if (initialState.energyState < zeroEnergyStateIndex) {
				// TODO: Add option for partial unloading of shift when prolonging
				for (int additionalShift = 0; additionalShift <= remainder; additionalShift++) {
					newEnergyState = Math.max(0, initialState.energyState - additionalShift);
					nextFeasibleStatesAndCostsForReset.put(stateMap[resettedShiftTime][newEnergyState],
							variableCostsForProlongingInEUR);
				}
			} else {
				for (int additionalShift = 0; additionalShift <= remainder; additionalShift++) {
					newEnergyState = Math.min(numberOfEnergyStates - 1, initialState.energyState + additionalShift);
					nextFeasibleStatesAndCostsForReset.put(stateMap[resettedShiftTime][newEnergyState],
							variableCostsForProlongingInEUR);
				}
			}
		}
	}

	/** Returns true if an energy state transition violates the portfolio's power limits
	 * 
	 * @param initialState starting state of the transition
	 * @param finalState target state of the transition
	 * @param timeSegment at which the transition is to occur
	 * @return true if transition would be infeasible, false otherwise */
	public boolean isInfeasibleTransition(LoadShiftState initialState, LoadShiftState finalState,
			TimePeriod timeSegment) {
		int energyStateDelta = finalState.calculateStateDelta(initialState);
		int[] powerStepLimits = extractPowerStepLimits(timeSegment);
		return energyStateDelta < -powerStepLimits[0] || energyStateDelta > powerStepLimits[1];
	}

	/** @return Index that represents a balanced energy state of the portfolio */
	public int getZeroEnergyStateIndex() {
		return zeroEnergyStateIndex;
	}

	/** @return Index that represents a transition without changing the energy state */
	public int getZeroPowerStateIndex() {
		return (numberOfPowerStates - 1) / 2;
	}

	/** @return total count of available power states */
	public int getNumberOfPowerStates() {
		return numberOfPowerStates;
	}

	/** @return total count of available energy states */
	public int getNumberOfEnergyStates() {
		return numberOfEnergyStates;
	}
}
