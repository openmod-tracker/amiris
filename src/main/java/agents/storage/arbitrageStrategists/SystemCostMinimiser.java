// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.storage.arbitrageStrategists;

import agents.markets.meritOrder.sensitivities.MarginalCostSensitivity;
import agents.markets.meritOrder.sensitivities.MeritOrderSensitivity;
import agents.storage.Device;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Strategy to minimise system cost (i.e. supply-side marginals) via dynamic programming, running backwards in time. The state
 * considered is the discretised (internal) storage energy level. System costs are minimised by finding the best sequence of
 * states, also considering (dis-)charging efficiencies. The full merit-order forecast information is required to implement a
 * perfect-foresight optimisation.
 * 
 * @author Christoph Schimeczek, Felix Nitsch, A. Achraf El Ghazi */
public class SystemCostMinimiser extends ArbitrageStrategist {
	public static final Tree parameters = Make.newTree()
			.add(Make.newInt("ModelledChargingSteps").optional()
					.help("Resolution of discretisation, total levels = ModelledChargingSteps * Device.EnergyToPowerRatio + 1"))
			.buildTree();

	private final int numberOfEnergyStates;
	private final int numberOfTransitionStates;
	private final double internalEnergyPerState;

	/** costSum[t][i]: summed marginal cost of the best sequence of states starting in period t and internal state i */
	private final double[][] followUpCostSum;
	/** bestNextState[t][i]: best next internal state identified when current state is i in period t */
	private final int[][] bestNextState;

	/** Creates a {@link SystemCostMinimiser}
	 * 
	 * @param generalInput general parameters associated with strategists
	 * @param specificInput specific parameters for this strategist
	 * @param storage device to be optimised
	 * @throws MissingDataException if any required input is missing */
	public SystemCostMinimiser(ParameterData generalInput, ParameterData specificInput, Device storage)
			throws MissingDataException {
		super(generalInput, storage);
		this.numberOfTransitionStates = specificInput.getInteger("ModelledChargingSteps");
		this.numberOfEnergyStates = calcNumberOfEnergyStates(numberOfTransitionStates);
		this.internalEnergyPerState = storage.getInternalPowerInMW() / numberOfTransitionStates;

		followUpCostSum = new double[forecastSteps][numberOfEnergyStates];
		bestNextState = new int[forecastSteps][numberOfEnergyStates];
	}

	@Override
	public void updateSchedule(TimePeriod startTimePeriod) {
		clearPlanningArrays();
		optimiseDispatch(startTimePeriod);
		double initialEnergyInStorageInMWh = storage.getCurrentEnergyInStorageInMWH();
		updateScheduleArrays(initialEnergyInStorageInMWh);
		correctForRoundingErrors(initialEnergyInStorageInMWh);
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
		MarginalCostSensitivity sensitivity = (MarginalCostSensitivity) getSensitivityForPeriod(timePeriod);
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
		double totalEnergySurplus = 0;
		int initialState = findNearestState(initialEnergyInStorageInMWh);
		totalEnergySurplus += determineEnergyDeviation(initialEnergyInStorageInMWh, initialState);
		for (int period = 0; period < scheduleDurationPeriods; period++) {
			scheduledInitialInternalEnergyInMWH[period] = internalEnergyPerState * initialState + totalEnergySurplus;
			int nextState = bestNextState[period][initialState];

			double selfDischarge = storage.getSelfDischargeRatePerHour() * scheduledInitialInternalEnergyInMWH[period];
			double nextExactEnergy = nextState * internalEnergyPerState + totalEnergySurplus - selfDischarge;
			nextExactEnergy = Math.max(0, Math.min(storage.getEnergyStorageCapacityInMWH(), nextExactEnergy));
			double internalEnergyDelta = nextExactEnergy - scheduledInitialInternalEnergyInMWH[period];
			
			nextState = findNearestState(correctedInternalEnergyInMWh);
			totalEnergySurplus += determineEnergyDeviation(correctedInternalEnergyInMWh);

			int stateDelta = nextState - initialState;
			double externalEnergyDelta = storage.internalToExternalEnergy(stateDelta * internalEnergyPerState);
			demandScheduleInMWH[period] = externalEnergyDelta;
			if (stateDelta == 0) {
				priceScheduleInEURperMWH[period] = Double.NaN;
			} else {
				priceScheduleInEURperMWH[period] = externalEnergyDelta > 0 ? Double.MAX_VALUE : -Double.MAX_VALUE;
			}
			initialState = nextState;
		}
	}

	/** @return the nearest energy state */
	private int findNearestState(double currentEnergyInStorageInMWh) {
		return Math.max(0,
				Math.min(numberOfEnergyStates, (int) Math.round(currentEnergyInStorageInMWh / internalEnergyPerState)));
	}

	/** @return the energy deviation caused between the exact energy and its associated state */
	private double determineEnergyDeviation(double exactEnergyInMWH, int associatedState) {
		return exactEnergyInMWH - associatedState * internalEnergyPerState;
	}

	/** @return a blank MarginalCostSensitivity item */
	@Override
	protected MeritOrderSensitivity createBlankSensitivity() {
		return new MarginalCostSensitivity();
	}

	@Override
	public double getChargingPowerForecastInMW(TimeStamp targetTime) {
		throw new RuntimeException(ERR_PROVIDE_FORECAST + StrategistType.SINGLE_AGENT_MIN_SYSTEM_COST);
	}

	@Override
	public void storeElectricityPriceForecast(TimePeriod timePeriod, double electricityPriceForecastInEURperMWH) {
		throw new RuntimeException(ERR_USE_PRICE_FORECAST + StrategistType.SINGLE_AGENT_MIN_SYSTEM_COST);
	}
}