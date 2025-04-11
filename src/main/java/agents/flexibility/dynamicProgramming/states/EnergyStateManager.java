// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.states;

import java.util.ArrayList;
import java.util.stream.IntStream;
import agents.flexibility.GenericDevice;
import agents.flexibility.dynamicProgramming.Strategist;
import agents.flexibility.dynamicProgramming.assessment.AssessmentFunction;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** States of a device are represented along one dimension, representing its energy content or state of charge
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public class EnergyStateManager implements StateManager {
	private final GenericDevice device;
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
	private TimeStamp currentOptimisationTime;

	public EnergyStateManager(GenericDevice device, AssessmentFunction assessmentFunction, double planningHorizonInHours,
			double energyResolutionInMWH) {
		this.device = device;
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
		for (int i = 0; i < numberOfTimeSteps; i++) {
			TimeStamp time = startingPeriod.shiftByDuration(i).getStartTime();
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

	@Override
	public void prepareFor(TimeStamp time) {
		assessmentFunction.prepareFor(time);
		currentOptimisationTimeIndex = (int) ((time.getStep() - startingPeriod.getStartTime().getStep())
				/ startingPeriod.getDuration().getSteps());
		currentOptimisationTime = time;
	}

	@Override
	public int[] getInitialStates() {
		int lowestIndex = energyToIndex(device.getEnergyContentLowerLimitInMWH(currentOptimisationTime));
		int highestIndex = energyToIndex(device.getEnergyContentUpperLimitInMWH(currentOptimisationTime));
		return IntStream.range(lowestIndex, highestIndex + 1).toArray();
	}

	/** @return next lower index corresponding to given energy level */
	private int energyToIndex(double energyAmountInMWH) {
		double energyLevel = (int) (energyAmountInMWH / energyResolutionInMWH) * energyResolutionInMWH;
		return (int) Math.round((energyLevel - lowestLevelEnergyInMWH) / energyResolutionInMWH);
	}

	@Override
	public int[] getFinalStates(int initialStateIndex) {
		double initialEnergyContentInMWH = initialStateIndex * energyResolutionInMWH;
		double lowestEnergyContentInMWH = device.getMinTargetEnergyContentInMWH(currentOptimisationTime,
				initialEnergyContentInMWH, startingPeriod.getDuration());
		double highestEnergyContentInMWH = device.getMaxTargetEnergyContentInMWH(currentOptimisationTime,
				initialEnergyContentInMWH, startingPeriod.getDuration());
		return IntStream.range(energyToIndex(lowestEnergyContentInMWH), energyToIndex(highestEnergyContentInMWH) + 1)
				.toArray();
	}

	@Override
	public double getTransitionValueFor(int initialStateIndex, int finalStateIndex) {
		double externalEnergyDeltaInMWH = device.simulateTransition(currentOptimisationTime,
				indexToEnergy(initialStateIndex), indexToEnergy(finalStateIndex), startingPeriod.getDuration());
		return assessmentFunction.assessTransition(externalEnergyDeltaInMWH);
	}

	/** @return energy content corresponding to the given index */
	private double indexToEnergy(int index) {
		return index * energyResolutionInMWH - lowestLevelEnergyInMWH;
	}

	@Override
	public double getBestValueNextPeriod(int finalStateIndex) {
		return currentOptimisationTimeIndex < numberOfTimeSteps ? bestValue[currentOptimisationTimeIndex][finalStateIndex]
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
		for (int i = 0; i < schedulingSteps; i++) {
			internalEnergyInMWH[i] = currentInternalEnergyInMWH;
			TimePeriod timePeriod = startingPeriod.shiftByDuration(i);
			int currentEnergyLevelIndex = energyToIndex(currentInternalEnergyInMWH);
			int nextEnergyLevelIndex = bestNextState[i][currentEnergyLevelIndex];
			double nextInternalEnergyInMWH = currentInternalEnergyInMWH
					+ (nextEnergyLevelIndex - currentEnergyLevelIndex) * energyResolutionInMWH;
			double lowerLevelInMWH = device.getEnergyContentLowerLimitInMWH(timePeriod.getStartTime());
			double upperLevelInMWH = device.getEnergyContentUpperLimitInMWH(timePeriod.getStartTime());
			nextInternalEnergyInMWH = Math.max(lowerLevelInMWH, Math.min(upperLevelInMWH, nextInternalEnergyInMWH));
			double internalEnergyDeltaInMWH = nextInternalEnergyInMWH - currentInternalEnergyInMWH;
			externalEnergyDeltaInMWH[i] = device.internalToExternalEnergy(internalEnergyDeltaInMWH,
					timePeriod.getStartTime());
			currentInternalEnergyInMWH = nextInternalEnergyInMWH;
		}
		return new DispatchSchedule(externalEnergyDeltaInMWH, internalEnergyInMWH);
	}

	@Override
	public double getCurrentDeviceEnergyContentInMWH() {
		return device.getCurrentInternalEnergyInMWH();
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
