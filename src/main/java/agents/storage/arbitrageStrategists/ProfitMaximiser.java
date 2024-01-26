// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.storage.arbitrageStrategists;

import agents.markets.meritOrder.sensitivities.MeritOrderSensitivity;
import agents.markets.meritOrder.sensitivities.PriceSensitivity;
import agents.markets.meritOrder.sensitivities.StepPower;
import agents.storage.Device;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Strategy to maximise profits via dynamic programming, running backwards in time. The state considered is the discretised
 * (internal) storage energy level. Profits are maximised by finding the best sequence of states, also considering (dis-)charging
 * efficiencies. The full merit-order forecast information is used to implement a perfect-foresight optimisation. As a
 * consequence, the result fully utilises market power to improve profits.
 * 
 * @author Christoph Schimeczek */
public class ProfitMaximiser extends ArbitrageStrategist {
	private final int numberOfEnergyStates;
	private final int numberOfTransitionStates;

	/** incomeSum[t][i]: income that can be collected in time step t being in internal state i */
	private final double[][] incomeSum;
	/** bestNextState[t][i]: best next internal state identified when current state is i in time step t */
	private final int[][] bestNextState;

	/** Creates a {@link ProfitMaximiser}
	 * 
	 * @param generalInput general parameters associated with strategists
	 * @param specificInput specific parameters for this strategist
	 * @param storage device to be optimised
	 * @throws MissingDataException if any required input is missing */
	public ProfitMaximiser(ParameterData generalInput, ParameterData specificInput, Device storage)
			throws MissingDataException {
		super(generalInput, storage);
		this.numberOfTransitionStates = specificInput.getInteger("ModelledChargingSteps");
		this.numberOfEnergyStates = calcNumberOfEnergyStates(numberOfTransitionStates);
		incomeSum = new double[forecastSteps][numberOfEnergyStates];
		bestNextState = new int[forecastSteps][numberOfEnergyStates];
	}

	@Override
	public void updateSchedule(TimePeriod firstPeriod) {
		clearPlanningArrays();
		optimiseDispatch(firstPeriod);
		double initialEnergyInStorageInMWh = storage.getCurrentEnergyInStorageInMWH();
		updateScheduleArrays(firstPeriod, initialEnergyInStorageInMWh);
	}

	/** replaces all entries in the planning arrays with 0 or Integer.MIN_VALUE */
	private void clearPlanningArrays() {
		for (int t = 0; t < forecastSteps; t++) {
			for (int initialState = 0; initialState < numberOfEnergyStates; initialState++) {
				incomeSum[t][initialState] = 0.0;
				bestNextState[t][initialState] = Integer.MIN_VALUE;
			}
		}
	}

	/** update most profitable final state for each possible initial state in every period */
	private void optimiseDispatch(TimePeriod firstPeriod) {
		for (int k = 0; k < forecastSteps; k++) {
			int period = forecastSteps - k - 1; // step backwards in time
			int nextPeriod = period + 1;
			TimePeriod timePeriod = firstPeriod.shiftByDuration(period);
			double[] chargePrices = calcChargePrices(timePeriod);
			double[] powerDeltasInMW = calcPowerDeltas(timePeriod);

			for (int initialState = 0; initialState < numberOfEnergyStates; initialState++) {
				double currentBestIncome = (double) Integer.MIN_VALUE;
				int bestFinalState = Integer.MIN_VALUE;
				int firstFinalState = calcFinalStateLowerBound(initialState);
				int lastFinalState = calcFinalStateUpperBound(initialState);
				for (int finalState = firstFinalState; finalState <= lastFinalState; finalState++) {
					int stateDelta = finalState - initialState;
					double incomeTransition = calcIncomeTransition(stateDelta, chargePrices, powerDeltasInMW);
					double income = incomeTransition + getBestIncome(nextPeriod, finalState);
					if (income > currentBestIncome) {
						currentBestIncome = income;
						bestFinalState = finalState;
					}
				}
				if (bestFinalState == Integer.MIN_VALUE) {
					throw new RuntimeException("No valid storage strategy found!");
				}
				incomeSum[period][initialState] = currentBestIncome;
				bestNextState[period][initialState] = bestFinalState;
			}
		}
	}

	/** @return price steps for charging & discharging in the specified {@link TimeSegment} */
	private double[] calcChargePrices(TimePeriod timePeriod) {
		final PriceSensitivity sensitivity = (PriceSensitivity) getSensitivityForPeriod(timePeriod);
		if (sensitivity != null) {
			return sensitivity.getValuesInSteps(numberOfTransitionStates);
		} else {
			return new double[2 * numberOfTransitionStates + 1];
		}
	}

	/** @return powers from max discharging to max charging in the specified {@link TimeSegment} */
	private double[] calcPowerDeltas(TimePeriod timePeriod) {
		double[] powerDeltasInMW = new double[numberOfTransitionStates * 2 + 1];
		StepPower stepPower = calcStepPower(timePeriod);
		for (int i = -numberOfTransitionStates; i <= numberOfTransitionStates; i++) {
			int index = i + numberOfTransitionStates;
			powerDeltasInMW[index] = stepPower.getPower(i);
		}
		return powerDeltasInMW;
	}

	/** @return power steps for charging & discharging in the specified {@link TimeSegment} */
	private StepPower calcStepPower(TimePeriod timePeriod) {
		final PriceSensitivity sensitivity = (PriceSensitivity) getSensitivityForPeriod(timePeriod);
		if (sensitivity != null) {
			return sensitivity.getStepPowers(numberOfTransitionStates);
		} else {
			return new StepPower(0, 0, numberOfTransitionStates);
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

	/** @return income of best strategy starting in given period at given state */
	private double getBestIncome(int period, int state) {
		return period < forecastSteps ? incomeSum[period][state] : 0;
	}

	/** @return income for a state transition under specified chargePrices */
	private double calcIncomeTransition(int stateDelta, double[] chargePrices, double[] powerDeltasInMW) {
		int arrayIndex = numberOfTransitionStates + stateDelta;
		double externalEnergyDelta = powerDeltasInMW[arrayIndex];
		double price = chargePrices[arrayIndex];
		return -externalEnergyDelta * price;
	}

	/** For scheduling period: updates arrays for expected initial energy levels, (dis-)charging power & bidding prices */
	private void updateScheduleArrays(TimePeriod firstPeriod, double initialEnergyInStorageInMWh) {
		double internalEnergyPerState = storage.getInternalPowerInMW() / numberOfTransitionStates;
		int initialState = Math.min(numberOfEnergyStates,
				(int) Math.round(initialEnergyInStorageInMWh / internalEnergyPerState));
		for (int period = 0; period < scheduleDurationPeriods; period++) {
			scheduledInitialInternalEnergyInMWH[period] = internalEnergyPerState * initialState;

			int nextState = bestNextState[period][initialState];
			int stateDelta = nextState - initialState;
			demandScheduleInMWH[period] = storage.internalToExternalEnergy(stateDelta * internalEnergyPerState);
			if (stateDelta == 0) {
				priceScheduleInEURperMWH[period] = Double.NaN;
			} else {
				TimePeriod timePeriod = firstPeriod.shiftByDuration(period);
				priceScheduleInEURperMWH[period] = calcPriceInPeriod(timePeriod, stateDelta);
			}
			initialState = nextState;
		}
	}

	/** @return electricity price in the specified {@link TimePeriod} for the specified state transition */
	private double calcPriceInPeriod(TimePeriod timeperiod, int stateDelta) {
		double[] chargePrices = calcChargePrices(timeperiod);
		int priceArrayIndex = numberOfTransitionStates + stateDelta;
		return chargePrices[priceArrayIndex];
	}

	@Override
	protected MeritOrderSensitivity createBlankSensitivity() {
		return new PriceSensitivity();
	}

	@Override
	public double getChargingPowerForecastInMW(TimeStamp targetTime) {
		throw new RuntimeException(ERR_PROVIDE_FORECAST + StrategistType.SINGLE_AGENT_MAX_PROFIT);
	}

	@Override
	public void storeElectricityPriceForecast(TimePeriod timePeriod, double electricityPriceForecastInEURperMWH) {
		throw new RuntimeException(ERR_USE_PRICE_FORECAST + StrategistType.SINGLE_AGENT_MAX_PROFIT);
	}
}