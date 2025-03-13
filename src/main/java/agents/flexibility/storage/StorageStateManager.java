package agents.flexibility.storage;

import agents.flexibility.GenericDevice;
import agents.flexibility.dynamicProgramming.AssessmentFunction;
import agents.flexibility.dynamicProgramming.StateManager;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

public class StorageStateManager implements StateManager {
	private final GenericDevice device;
	private final AssessmentFunction assessmentFunction;
	private final int numberOfTimeSteps;
	private final double energyResolutionInMWH;

	private int[][] bestNextState;
	private double[][] bestValue;
	private double lowestLevelEnergyInMWH;
	private int numberOfEnergyStates;
	private TimePeriod startingPeriod;
	private int currentOptimisationTimeIndex;
	private TimeStamp currentOptimisationTime;

	public StorageStateManager(GenericDevice device, AssessmentFunction assessmentFunction, int numberOfTimeSteps,
			double energyResolutionInMWH) {
		this.device = device;
		this.assessmentFunction = assessmentFunction;
		this.numberOfTimeSteps = numberOfTimeSteps;
		this.energyResolutionInMWH = energyResolutionInMWH;
	}

	@Override
	public void initialise(TimePeriod startingPeriod) {
		this.startingPeriod = startingPeriod;
		analyseStorageEnergyLevels();
		bestNextState = new int[numberOfTimeSteps][numberOfEnergyStates];
		bestValue = new double[numberOfTimeSteps][numberOfEnergyStates];
	}

	private void analyseStorageEnergyLevels() {
		double minLowerLevel = Double.MAX_VALUE;
		double maxUpperLevel = -Double.MAX_VALUE;
		for (int i = 0; i < numberOfTimeSteps; i++) {
			TimePeriod timePeriod = startingPeriod.shiftByDuration(i);
			double lowerLevel = device.getEnergyContentLowerLimitInMWH(timePeriod.getStartTime());
			double upperLevel = device.getEnergyContentUpperLimitInMWH(timePeriod.getStartTime());
			minLowerLevel = lowerLevel < minLowerLevel ? lowerLevel : minLowerLevel;
			maxUpperLevel = upperLevel > maxUpperLevel ? upperLevel : maxUpperLevel;
		}
		int lowestStep = (int) (minLowerLevel / energyResolutionInMWH);
		lowestLevelEnergyInMWH = lowestStep * energyResolutionInMWH;
		int highestStep = (int) (maxUpperLevel / energyResolutionInMWH);
		numberOfEnergyStates = highestStep - lowestStep + 1;
	}

	@Override
	public void prepareFor(TimePeriod timePeriod) {
		assessmentFunction.prepareFor(timePeriod);
		currentOptimisationTimeIndex = (int) ((timePeriod.getStartTime().getStep()
				- startingPeriod.getStartTime().getStep()) / startingPeriod.getDuration().getSteps());
		currentOptimisationTime = timePeriod.getStartTime();
	}

	@Override
	public int[] getInitialStates() {
		int lowestIndex = energyToIndex(device.getEnergyContentLowerLimitInMWH(currentOptimisationTime));
		int highestIndex = energyToIndex(device.getEnergyContentUpperLimitInMWH(currentOptimisationTime));
		return new int[] {lowestIndex, highestIndex};
	}

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
		return new int[] {energyToIndex(lowestEnergyContentInMWH), energyToIndex(highestEnergyContentInMWH)};
	}

	@Override
	public double getTransitionValueFor(int initialStateIndex, int finalStateIndex) {
		double externalEnergyDeltaInMWH = device.simulateTransition(currentOptimisationTime,
				indexToEnergy(initialStateIndex), indexToEnergy(finalStateIndex), startingPeriod.getDuration());
		return 0;
	}

	private double indexToEnergy(int index) {
		return index * energyResolutionInMWH - lowestLevelEnergyInMWH;
	}

	@Override
	public double getBestValueNextPeriod(int finalStateIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateBestFinalState(int bestFinalStateIndex, double bestAssessmentValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumberOfTimeSteps() {
		return numberOfTimeSteps;
	}

}
