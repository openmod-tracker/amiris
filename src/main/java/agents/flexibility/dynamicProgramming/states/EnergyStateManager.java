// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.states;

import java.util.ArrayList;
import java.util.stream.IntStream;
import agents.flexibility.GenericDevice;
import agents.flexibility.GenericDeviceCache;
import agents.flexibility.dynamicProgramming.Strategist;
import agents.flexibility.dynamicProgramming.assessment.AssessmentFunction;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** States of a device are represented along one dimension, representing its energy content or state of charge
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public class EnergyStateManager implements StateManager {
	private final GenericDeviceCache device;
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
	private double[] energyDeltasCharging;
	private double[] energyDeltasDischarging;

	public EnergyStateManager(GenericDevice device, AssessmentFunction assessmentFunction, double planningHorizonInHours,
			double energyResolutionInMWH) {
		this.device = new GenericDeviceCache(device);
		this.assessmentFunction = assessmentFunction;
		this.planningHorizonInHours = planningHorizonInHours;
		this.energyResolutionInMWH = energyResolutionInMWH;
	}

	@Override
	public void initialise(TimePeriod startingPeriod) {
		this.numberOfTimeSteps = Strategist.calcHorizonInPeriodSteps(startingPeriod, planningHorizonInHours);
		this.startingPeriod = startingPeriod;
		device.setPeriod(startingPeriod);
		analyseAvailableEnergyLevels();
		bestNextState = new int[numberOfTimeSteps][numberOfEnergyStates];
		bestValue = new double[numberOfTimeSteps][numberOfEnergyStates];
	}

	private void analyseAvailableEnergyLevels() {
		double minLowerLevel = Double.MAX_VALUE;
		double maxUpperLevel = -Double.MAX_VALUE;
		hasSelfDischarge = false;
		for (int timeIndex = 0; timeIndex < numberOfTimeSteps; timeIndex++) {
			TimeStamp time = startingPeriod.shiftByDuration(timeIndex).getStartTime();
			double lowerLevel = device.getGenericDevice().getEnergyContentLowerLimitInMWH(time);
			double upperLevel = device.getGenericDevice().getEnergyContentUpperLimitInMWH(time);
			if (!hasSelfDischarge) {
				hasSelfDischarge = device.getGenericDevice().getSelfDischargeRate(time) > 0;
			}
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
		device.prepareFor(time);
		currentOptimisationTimeIndex = (int) ((time.getStep() - startingPeriod.getStartTime().getStep())
				/ startingPeriod.getDuration().getSteps());
		if (!hasSelfDischarge) {
			int maxChargingSteps = (int) Math.floor(device.getMaxNetChargingEnergyInMWH() / energyResolutionInMWH);
			energyDeltasCharging = new double[maxChargingSteps + 1];
			for (int chargingSteps = 0; chargingSteps <= maxChargingSteps; chargingSteps++) {
				energyDeltasCharging[chargingSteps] = device.simulateTransition(0, chargingSteps * energyResolutionInMWH);
			}

			int maxDischargingSteps = -(int) Math.ceil(device.getMaxNetDischargingEnergyInMWH() / energyResolutionInMWH);
			energyDeltasDischarging = new double[maxDischargingSteps + 1];
			for (int dischargingSteps = 0; dischargingSteps <= maxDischargingSteps; dischargingSteps++) {
				energyDeltasDischarging[dischargingSteps] = device.simulateTransition(0,
						-dischargingSteps * energyResolutionInMWH);
			}
		}
	}

	@Override
	public int[] getInitialStates() {
		int lowestIndex = energyToCeilIndex(device.getEnergyContentLowerLimitInMWH());
		int highestIndex = energyToFloorIndex(device.getEnergyContentUpperLimitInMWH());
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
		double lowestEnergyContentInMWH = device.getMinTargetEnergyContentInMWH(initialEnergyContentInMWH);
		double highestEnergyContentInMWH = device.getMaxTargetEnergyContentInMWH(initialEnergyContentInMWH);
		return IntStream
				.range(energyToCeilIndex(lowestEnergyContentInMWH), energyToFloorIndex(highestEnergyContentInMWH) + 1)
				.toArray();
	}

	@Override
	public double getTransitionValueFor(int initialStateIndex, int finalStateIndex) {
		if (!hasSelfDischarge) {
			int stateDelta = finalStateIndex - initialStateIndex;
			return stateDelta >= 0 ? energyDeltasCharging[stateDelta] : energyDeltasDischarging[-stateDelta];
		}
		double externalEnergyDeltaInMWH = device.simulateTransition(indexToEnergy(initialStateIndex),
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
			TimeStamp time = startingPeriod.shiftByDuration(timeIndex).getStartTime();
			double lowerLevelInMWH = device.getGenericDevice().getEnergyContentLowerLimitInMWH(time);
			double upperLevelInMWH = device.getGenericDevice().getEnergyContentUpperLimitInMWH(time);
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
