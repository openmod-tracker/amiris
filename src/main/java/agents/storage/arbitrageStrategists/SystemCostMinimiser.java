package agents.storage.arbitrageStrategists;

import agents.markets.meritOrder.sensitivities.MarginalCostSensitivity;
import agents.markets.meritOrder.sensitivities.MeritOrderSensitivity;
import agents.storage.Device;
import de.dlr.gitlab.fame.time.TimePeriod;

/** Strategy to minimise system cost (i.e. supply-side marginals) via dynamic programming, running backwards in time. The state
 * considered is the discretised (internal) storage energy level. System costs are minimised by finding the best sequence of
 * states, also considering (dis-)charging efficiencies. The full merit-order forecast information is required to implement a
 * perfect-foresight optimisation.
 * 
 * @author Christoph Schimeczek */
public class SystemCostMinimiser extends ArbitrageStrategist {
	private final int numberOfEnergyStates;
	private final int numberOfTransitionStates;

	/** costSum[t][i]: summed marginal cost of the best sequence of states starting in period t and internal state i */
	private final double[][] followUpCostSum;
	/** bestNextState[t][i]: best next internal state identified when current state is i in period t */
	private final int[][] bestNextState;

	/** Creates a {@link SystemCostMinimiser}
	 * 
	 * @param forecastPeriod number of time segments of the forecast
	 * @param storage device to be optimised
	 * @param scheduleDuration number of time segments that shall be scheduled
	 * @param transitionSteps resolution of energy level discretisation: total energy levels = transitionSteps * Device.E2P + 1 */
	public SystemCostMinimiser(int forecastPeriod, Device storage, int scheduleDuration, int transitionSteps) {
		super(forecastPeriod, storage, scheduleDuration);
		this.numberOfTransitionStates = transitionSteps;
		this.numberOfEnergyStates = (int) Math.ceil(numberOfTransitionStates * storage.getEnergyToPowerRatio()) + 1;

		followUpCostSum = new double[forecastPeriod][numberOfEnergyStates];
		bestNextState = new int[forecastPeriod][numberOfEnergyStates];
	}

	@Override
	public void updateSchedule(TimePeriod startTimePeriod, double initialEnergyInStorageInMWh) {
		clearPlanningArrays();
		optimiseDispatch(startTimePeriod);
		updateScheduleArrays(initialEnergyInStorageInMWh);
	}

	/** replaces all entries in the planning arrays with 0 or Integer.MIN_VALUE */
	private void clearPlanningArrays() {
		for (int t = 0; t < forecastSteps; t++) {
			for (int initialState = 0; initialState < numberOfEnergyStates; initialState++) {
				followUpCostSum[t][initialState] = 0.0;
				bestNextState[t][initialState] = Integer.MIN_VALUE;
			}
		}
	}

	/** calculates least costly & reachable final state for every initial state in every period */
	private void optimiseDispatch(TimePeriod startPeriod) {
		for (int k = 0; k < forecastSteps; k++) {
			int step = forecastSteps - k - 1; // step backwards in time
			int nextStep = step + 1;
			TimePeriod timePeriod = startPeriod.shiftByDuration(step);
			double[] marginalCostDeltaPerStep = calcCostSteps(timePeriod);

			for (int initialState = 0; initialState < numberOfEnergyStates; initialState++) {
				double currentLowestCost = Double.MAX_VALUE;
				int bestFinalState = Integer.MIN_VALUE;
				int firstFinalState = calcFinalStateLowerBound(initialState);
				int lastFinalState = calcFinalStateUpperBound(initialState);
				for (int finalState = firstFinalState; finalState <= lastFinalState; finalState++) {
					int stateDelta = finalState - initialState;
					int costStepIndex = numberOfTransitionStates + stateDelta;
					double cost = marginalCostDeltaPerStep[costStepIndex] + getFollowUpCost(nextStep, finalState);
					if (cost < currentLowestCost) {
						currentLowestCost = cost;
						bestFinalState = finalState;
					}
				}
				if (bestFinalState == Integer.MIN_VALUE) {
					throw new RuntimeException("No valid storage strategy found!");
				}
				followUpCostSum[step][initialState] = currentLowestCost;
				bestNextState[step][initialState] = bestFinalState;
			}
		}
	}

	/** @return marginal cost delta for each charging or discharging option, i.e. 2 * transitionStates + 1 options */
	private double[] calcCostSteps(TimePeriod timePeriod) {
		MarginalCostSensitivity sensitivity = (MarginalCostSensitivity) getSensitivityForSegment(timePeriod);
		if (sensitivity != null) {
			return sensitivity.getValuesInSteps(numberOfTransitionStates);
		} else {
			return new double[2 * numberOfTransitionStates + 1];
		}
	}

	/** @return lower bound (inclusive) of discrete states reachable from specified initialState */
	private int calcFinalStateLowerBound(int initialState) {
		return Math.max(0, initialState - numberOfTransitionStates);
	}

	/** @return upper bound (inclusive) of discrete states reachable from specified initialState */
	private int calcFinalStateUpperBound(int initialState) {
		return Math.min(numberOfEnergyStates - 1, initialState + numberOfTransitionStates);
	}

	/** @return lowest revenues of best strategy starting in given hour at given state */
	private double getFollowUpCost(int hour, int state) {
		return hour < forecastSteps ? followUpCostSum[hour][state] : 0;
	}

	/** For scheduling period: updates arrays for expected initial energy levels, (dis-)charging power & bidding prices */
	private void updateScheduleArrays(double initialEnergyInStorageInMWh) {
		double internalEnergyPerState = storage.getInternalPowerInMW() / numberOfTransitionStates;
		int initialState = (int) Math.round(initialEnergyInStorageInMWh / internalEnergyPerState);
		for (int period = 0; period < scheduleDurationPeriods; period++) {
			periodScheduledInitialInternalEnergyInMWH[period] = internalEnergyPerState * initialState;

			int nextState = bestNextState[period][initialState];
			int stateDelta = nextState - initialState;
			double externalEnergyDelta = storage.internalToExternalEnergy(stateDelta * internalEnergyPerState);
			periodChargingScheduleInMW[period] = externalEnergyDelta;
			if (stateDelta == 0) {
				periodPriceScheduleInEURperMWH[period] = Double.NaN;
			} else {
				periodPriceScheduleInEURperMWH[period] = externalEnergyDelta > 0 ? Double.MAX_VALUE : -Double.MAX_VALUE;
			}
			initialState = nextState;
		}
	}

	/** @return a blank MarginalCostSensitivity item */
	@Override
	protected MeritOrderSensitivity createBlankSensitivity() {
		return new MarginalCostSensitivity();
	}
}