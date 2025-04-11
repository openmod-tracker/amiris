// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.states;

import java.util.ArrayList;
import java.util.stream.IntStream;
import agents.flexibility.CachableGenericDevice;
import agents.flexibility.GenericDevice;
import agents.flexibility.dynamicProgramming.Strategist;
import agents.flexibility.dynamicProgramming.assessment.AssessmentFunction;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** States of a device are represented along one dimension, representing its energy content or state of charge
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public class EnergyStateManager implements StateManager {
	private final CachableGenericDevice device;
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

	public EnergyStateManager(GenericDevice device, AssessmentFunction assessmentFunction, double planningHorizonInHours,
			double energyResolutionInMWH) {
		this.device = new CachableGenericDevice(device);
		this.assessmentFunction = assessmentFunction;
		this.planningHorizonInHours = planningHorizonInHours;
		this.energyResolutionInMWH = energyResolutionInMWH;
	}

	@Override
	public void initialise(TimePeriod startingPeriod) {
		this.numberOfTimeSteps = Strategist.calcHorizonInPeriodSteps(startingPeriod, planningHorizonInHours);
		this.startingPeriod = startingPeriod;
		analyseAvailableEnergyLevels();
		bestNextState = new int[numberOfTimeSteps][numberOfEnergyStates];
		bestValue = new double[numberOfTimeSteps][numberOfEnergyStates];
	}

	private void analyseAvailableEnergyLevels() {
		double minLowerLevel = Double.MAX_VALUE;
		double maxUpperLevel = -Double.MAX_VALUE;
		for (int timeIndex = 0; timeIndex < numberOfTimeSteps; timeIndex++) {
			double lowerLevel = device.getEnergyContentLowerLimitInMWH(timeIndex);
			double upperLevel = device.getEnergyContentUpperLimitInMWH(timeIndex);
			minLowerLevel = lowerLevel < minLowerLevel ? lowerLevel : minLowerLevel;
			maxUpperLevel = upperLevel > maxUpperLevel ? upperLevel : maxUpperLevel;
		}
		int lowestStep = (int) (minLowerLevel / energyResolutionInMWH);
		lowestLevelEnergyInMWH = lowestStep * energyResolutionInMWH;
		int highestStep = (int) (maxUpperLevel / energyResolutionInMWH);
		numberOfEnergyStates = highestStep - lowestStep + 1;
	}

	@Override
	public void prepareFor(TimeStamp time) {
		assessmentFunction.prepareFor(time);
		currentOptimisationTimeIndex = (int) ((time.getStep() - startingPeriod.getStartTime().getStep())
				/ startingPeriod.getDuration().getSteps());
	}

	@Override
	public int[] getInitialStates() {
		int lowestIndex = energyToCeilIndex(device.getEnergyContentLowerLimitInMWH(currentOptimisationTimeIndex));
		int highestIndex = energyToFloorIndex(device.getEnergyContentUpperLimitInMWH(currentOptimisationTimeIndex));
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
		double lowestEnergyContentInMWH = device.getMinTargetEnergyContentInMWH(currentOptimisationTimeIndex,
				initialEnergyContentInMWH, startingPeriod.getDuration());
		double highestEnergyContentInMWH = device.getMaxTargetEnergyContentInMWH(currentOptimisationTimeIndex,
				initialEnergyContentInMWH, startingPeriod.getDuration());
		return IntStream
				.range(energyToCeilIndex(lowestEnergyContentInMWH), energyToFloorIndex(highestEnergyContentInMWH) + 1)
				.toArray();
	}

	@Override
	public double getTransitionValueFor(int initialStateIndex, int finalStateIndex) {
		double externalEnergyDeltaInMWH = device.simulateTransition(currentOptimisationTimeIndex,
				indexToEnergy(initialStateIndex), indexToEnergy(finalStateIndex), startingPeriod.getDuration());
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
		GenericDevice actualDevice = device.getGenericDevice();
		double currentInternalEnergyInMWH = actualDevice.getCurrentInternalEnergyInMWH();
		double[] externalEnergyDeltaInMWH = new double[schedulingSteps];
		double[] internalEnergyInMWH = new double[schedulingSteps];
		for (int timeIndex = 0; timeIndex < schedulingSteps; timeIndex++) {
			internalEnergyInMWH[timeIndex] = currentInternalEnergyInMWH;
			TimePeriod timePeriod = startingPeriod.shiftByDuration(timeIndex);
			int currentEnergyLevelIndex = energyToNearestIndex(currentInternalEnergyInMWH);
			int nextEnergyLevelIndex = bestNextState[timeIndex][currentEnergyLevelIndex];
			double nextInternalEnergyInMWH = currentInternalEnergyInMWH
					+ (nextEnergyLevelIndex - currentEnergyLevelIndex) * energyResolutionInMWH;
			double lowerLevelInMWH = device.getEnergyContentLowerLimitInMWH(timeIndex);
			double upperLevelInMWH = device.getEnergyContentUpperLimitInMWH(timeIndex);
			nextInternalEnergyInMWH = Math.max(lowerLevelInMWH, Math.min(upperLevelInMWH, nextInternalEnergyInMWH));
			double internalEnergyDeltaInMWH = nextInternalEnergyInMWH - currentInternalEnergyInMWH;
			externalEnergyDeltaInMWH[timeIndex] = actualDevice.internalToExternalEnergy(internalEnergyDeltaInMWH,
					timePeriod.getStartTime());
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
		int numberOfTimeSteps = Strategist.calcHorizonInPeriodSteps(startingPeriod, planningHorizonInHours);
		ArrayList<TimeStamp> planningTimes = new ArrayList<>(numberOfTimeSteps);
		for (int step = 0; step < numberOfTimeSteps; step++) {
			planningTimes.add(startingPeriod.shiftByDuration(step).getStartTime());
		}
		return planningTimes;
	}
}
