// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.states;

import java.util.ArrayList;
import java.util.stream.IntStream;
import agents.flexibility.GenericDevice;
import agents.flexibility.GenericDeviceCache;
import agents.flexibility.dynamicProgramming.Optimiser;
import agents.flexibility.dynamicProgramming.assessment.AssessmentFunction;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** States of a device are represented along one dimension, representing its energy content or state of charge
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public class EnergyStateManager implements StateManager {
	private final GenericDevice device;
	private final GenericDeviceCache deviceCache;
	private final AssessmentFunction assessmentFunction;
	private final double planningHorizonInHours;
	private final double energyResolutionInMWH;

	private int numberOfTimeSteps;
	private int[][] bestNextState;
	private double[][] bestValue;
	private double lowestLevelEnergyInMWH;
	private int numberOfEnergyStates;
	private TimePeriod startingPeriod;
	private int currentOptimisationTimeIndex;

	private boolean hasSelfDischarge;
	private double[] transitionValuesCharging;
	private double[] transitionValuesDischarging;

	public EnergyStateManager(GenericDevice device, AssessmentFunction assessmentFunction, double planningHorizonInHours,
			double energyResolutionInMWH) {
		this.device = device;
		this.deviceCache = new GenericDeviceCache(device);
		this.assessmentFunction = assessmentFunction;
		this.planningHorizonInHours = planningHorizonInHours;
		this.energyResolutionInMWH = energyResolutionInMWH;
	}

	@Override
	public void initialise(TimePeriod startingPeriod) {
		this.numberOfTimeSteps = Optimiser.calcHorizonInPeriodSteps(startingPeriod, planningHorizonInHours);
		this.startingPeriod = startingPeriod;
		deviceCache.setPeriod(startingPeriod);
		analyseAvailableEnergyLevels();
		analyseSelfDischarge();
		bestNextState = new int[numberOfTimeSteps][numberOfEnergyStates];
		bestValue = new double[numberOfTimeSteps][numberOfEnergyStates];
	}

	/** Sets {@link #lowestLevelEnergyInMWH} and {@link #numberOfEnergyStates} for the current planning horizon */
	private void analyseAvailableEnergyLevels() {
		double minLowerLevel = Double.MAX_VALUE;
		double maxUpperLevel = -Double.MAX_VALUE;
		for (int timeIndex = 0; timeIndex < numberOfTimeSteps; timeIndex++) {
			TimeStamp time = getTimeByIndex(timeIndex);
			double lowerLevel = device.getEnergyContentLowerLimitInMWH(time);
			double upperLevel = device.getEnergyContentUpperLimitInMWH(time);
			minLowerLevel = lowerLevel < minLowerLevel ? lowerLevel : minLowerLevel;
			maxUpperLevel = upperLevel > maxUpperLevel ? upperLevel : maxUpperLevel;
		}
		int lowestStep = (int) (minLowerLevel / energyResolutionInMWH);
		lowestLevelEnergyInMWH = lowestStep * energyResolutionInMWH;
		int highestStep = (int) (maxUpperLevel / energyResolutionInMWH);
		numberOfEnergyStates = highestStep - lowestStep + 1;
	}

	/** @return time corresponding to the given timeIndex based on the current setting of {@link #startingPeriod} */
	private TimeStamp getTimeByIndex(int timeIndex) {
		return startingPeriod.shiftByDuration(timeIndex).getStartTime();
	}

	/** Sets {@link #hasSelfDischarge} to true if self discharge occurs in periods of the planning horizon, false otherwise */
	private void analyseSelfDischarge() {
		hasSelfDischarge = false;
		for (int timeIndex = 0; timeIndex < numberOfTimeSteps; timeIndex++) {
			hasSelfDischarge = device.getSelfDischargeRate(getTimeByIndex(timeIndex)) > 0;
			if (hasSelfDischarge) {
				break;
			}
		}
	}

	@Override
	public void prepareFor(TimeStamp time) {
		assessmentFunction.prepareFor(time);
		deviceCache.prepareFor(time);
		currentOptimisationTimeIndex = (int) ((time.getStep() - startingPeriod.getStartTime().getStep())
				/ startingPeriod.getDuration().getSteps());
		if (!hasSelfDischarge) {
			cacheTransitionValues();
		}
	}

	/** Cache values of transitions - only applicable without self discharge */
	private void cacheTransitionValues() {
		int maxChargingSteps = (int) Math.floor(deviceCache.getMaxNetChargingEnergyInMWH() / energyResolutionInMWH);
		transitionValuesCharging = new double[maxChargingSteps + 1];
		for (int chargingSteps = 0; chargingSteps <= maxChargingSteps; chargingSteps++) {
			double externalEnergyDeltaInMWH = deviceCache.simulateTransition(0, chargingSteps * energyResolutionInMWH);
			transitionValuesCharging[chargingSteps] = assessmentFunction.assessTransition(externalEnergyDeltaInMWH);
		}
		int maxDischargingSteps = -(int) Math.ceil(deviceCache.getMaxNetDischargingEnergyInMWH() / energyResolutionInMWH);
		transitionValuesDischarging = new double[maxDischargingSteps + 1];
		for (int dischargingSteps = 0; dischargingSteps <= maxDischargingSteps; dischargingSteps++) {
			double externalEnergyDeltaInMWH = deviceCache.simulateTransition(0, -dischargingSteps * energyResolutionInMWH);
			transitionValuesDischarging[dischargingSteps] = assessmentFunction.assessTransition(externalEnergyDeltaInMWH);
		}
	}

	@Override
	public int[] getInitialStates() {
		int lowestIndex = energyToCeilIndex(deviceCache.getEnergyContentLowerLimitInMWH());
		int highestIndex = energyToFloorIndex(deviceCache.getEnergyContentUpperLimitInMWH());
		return IntStream.range(lowestIndex, highestIndex + 1).toArray();
	}

	/** @return next lower index corresponding to given energy level */
	private int energyToFloorIndex(double energyAmountInMWH) {
		double energyLevel = Math.floor(energyAmountInMWH / energyResolutionInMWH) * energyResolutionInMWH;
		return (int) Math.round((energyLevel - lowestLevelEnergyInMWH) / energyResolutionInMWH);
	}

	/** @return next higher index corresponding to given energy level */
	private int energyToCeilIndex(double energyAmountInMWH) {
		double energyLevel = Math.ceil(energyAmountInMWH / energyResolutionInMWH) * energyResolutionInMWH;
		return (int) Math.round((energyLevel - lowestLevelEnergyInMWH) / energyResolutionInMWH);
	}

	@Override
	public int[] getFinalStates(int initialStateIndex) {
		double initialEnergyContentInMWH = initialStateIndex * energyResolutionInMWH;
		double lowestEnergyContentInMWH = deviceCache.getMinTargetEnergyContentInMWH(initialEnergyContentInMWH);
		double highestEnergyContentInMWH = deviceCache.getMaxTargetEnergyContentInMWH(initialEnergyContentInMWH);
		return IntStream
				.range(energyToCeilIndex(lowestEnergyContentInMWH), energyToFloorIndex(highestEnergyContentInMWH) + 1)
				.toArray();
	}

	@Override
	public double getTransitionValueFor(int initialStateIndex, int finalStateIndex) {
		return hasSelfDischarge ? calcValueFor(initialStateIndex, finalStateIndex)
				: getCachedValueFor(initialStateIndex, finalStateIndex);
	}

	private double getCachedValueFor(int initialStateIndex, int finalStateIndex) {
		int stateDelta = finalStateIndex - initialStateIndex;
		return stateDelta >= 0 ? transitionValuesCharging[stateDelta] : transitionValuesDischarging[-stateDelta];
	}

	private double calcValueFor(int initialStateIndex, int finalStateIndex) {
		double externalEnergyDeltaInMWH = deviceCache.simulateTransition(indexToEnergy(initialStateIndex),
				indexToEnergy(finalStateIndex));
		return assessmentFunction.assessTransition(externalEnergyDeltaInMWH);
	}

	/** @return energy content corresponding to the given index */
	private double indexToEnergy(int index) {
		return index * energyResolutionInMWH - lowestLevelEnergyInMWH;
	}

	@Override
	public double getBestValueNextPeriod(int finalStateIndex) {
		return currentOptimisationTimeIndex + 1 < numberOfTimeSteps
				? bestValue[currentOptimisationTimeIndex + 1][finalStateIndex]
				: getWaterValue();
	}

	private double getWaterValue() {
		return 0; // TODO: implement
	}

	@Override
	public void updateBestFinalState(int initialStateIndex, int bestFinalStateIndex, double bestAssessmentValue) {
		bestValue[currentOptimisationTimeIndex][initialStateIndex] = bestAssessmentValue;
		bestNextState[currentOptimisationTimeIndex][initialStateIndex] = bestFinalStateIndex;
	}

	@Override
	public int getNumberOfForecastTimeSteps() {
		return numberOfTimeSteps;
	}

	@Override
	public DispatchSchedule getBestDispatchSchedule(int schedulingSteps) {
		double currentInternalEnergyInMWH = device.getCurrentInternalEnergyInMWH();
		double[] externalEnergyDeltaInMWH = new double[schedulingSteps];
		double[] internalEnergyInMWH = new double[schedulingSteps];
		for (int timeIndex = 0; timeIndex < schedulingSteps; timeIndex++) {
			TimeStamp time = getTimeByIndex(timeIndex);
			deviceCache.prepareFor(time);

			internalEnergyInMWH[timeIndex] = currentInternalEnergyInMWH;
			int currentEnergyLevelIndex = energyToNearestIndex(currentInternalEnergyInMWH);
			int nextEnergyLevelIndex = bestNextState[timeIndex][currentEnergyLevelIndex];
			double nextInternalEnergyInMWH = currentInternalEnergyInMWH
					+ (nextEnergyLevelIndex - currentEnergyLevelIndex) * energyResolutionInMWH;
			double lowerLevelInMWH = deviceCache.getEnergyContentLowerLimitInMWH();
			double upperLevelInMWH = deviceCache.getEnergyContentUpperLimitInMWH();
			nextInternalEnergyInMWH = Math.max(lowerLevelInMWH, Math.min(upperLevelInMWH, nextInternalEnergyInMWH));

			externalEnergyDeltaInMWH[timeIndex] = deviceCache.simulateTransition(currentInternalEnergyInMWH,
					nextInternalEnergyInMWH);
			currentInternalEnergyInMWH = nextInternalEnergyInMWH;
		}
		return new DispatchSchedule(externalEnergyDeltaInMWH, internalEnergyInMWH);
	}

	/** @return closest index corresponding to given energy level */
	private int energyToNearestIndex(double energyAmountInMWH) {
		double energyLevel = Math.round(energyAmountInMWH / energyResolutionInMWH) * energyResolutionInMWH;
		return (int) Math.round((energyLevel - lowestLevelEnergyInMWH) / energyResolutionInMWH);
	}

	@Override
	public ArrayList<TimeStamp> getPlanningTimes(TimePeriod startingPeriod) {
		int numberOfTimeSteps = Optimiser.calcHorizonInPeriodSteps(startingPeriod, planningHorizonInHours);
		ArrayList<TimeStamp> planningTimes = new ArrayList<>(numberOfTimeSteps);
		for (int step = 0; step < numberOfTimeSteps; step++) {
			planningTimes.add(startingPeriod.shiftByDuration(step).getStartTime());
		}
		return planningTimes;
	}
}
