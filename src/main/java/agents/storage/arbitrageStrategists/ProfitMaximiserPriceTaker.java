// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.storage.arbitrageStrategists;

import agents.markets.meritOrder.Constants;
import agents.markets.meritOrder.sensitivities.MeritOrderSensitivity;
import agents.markets.meritOrder.sensitivities.PriceNoSensitivity;
import agents.markets.meritOrder.sensitivities.StepPower;
import agents.storage.Device;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.time.TimePeriod;

/** Strategy to maximise profits via dynamic programming, running backwards in time. Profits are maximised by finding the best
 * sequence of states. In contrast to ProfitMaximiser, the optimization does not account for potential impact on prices, but can
 * be considered as a "price taker".
 * 
 * @author Felix Nitsch, Christoph Schimeczek */
public class ProfitMaximiserPriceTaker extends DynamicProgrammingStrategist {
	/** incomeSum[t][i]: income that can be collected in time step t being in internal state i */
	private final double[][] incomeSum;

	public ProfitMaximiserPriceTaker(ParameterData generalInput, ParameterData specificInput, Device storage)
			throws MissingDataException {
		super(generalInput, specificInput, storage);
		incomeSum = new double[forecastSteps][numberOfEnergyStates];
	}

	@Override
	protected void clearPlanningArrays() {
		for (int t = 0; t < forecastSteps; t++) {
			for (int initialState = 0; initialState < numberOfEnergyStates; initialState++) {
				incomeSum[t][initialState] = 0.0;
				bestNextState[t][initialState] = Integer.MIN_VALUE;
			}
		}
	}

	/** update most profitable final state for each possible initial state in every period */
	@Override
	protected void optimiseDispatch(TimePeriod firstPeriod) {
		for (int k = 0; k < forecastSteps; k++) {
			int period = forecastSteps - k - 1; // step backwards in time
			int nextPeriod = period + 1;
			TimePeriod timePeriod = firstPeriod.shiftByDuration(period);
			double chargePrice = calcChargePrice(timePeriod);
			double[] powerDeltasInMW = calcPowerDeltas(timePeriod);
			for (int initialState = 0; initialState < numberOfEnergyStates; initialState++) {
				double currentBestIncome = -Double.MAX_VALUE;
				int bestFinalState = Integer.MIN_VALUE;
				int firstFinalState = calcFinalStateLowerBound(initialState);
				int lastFinalState = calcFinalStateUpperBound(initialState);
				for (int finalState = firstFinalState; finalState <= lastFinalState; finalState++) {
					int stateDelta = finalState - initialState;
					double incomeTransition = calcIncomeTransition(stateDelta, chargePrice, powerDeltasInMW);
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

	/** @return price for charging & discharging in the specified {@link TimePeriod} */
	private double calcChargePrice(TimePeriod timePeriod) {
		final PriceNoSensitivity sensitivity = (PriceNoSensitivity) getSensitivityForPeriod(timePeriod);
		if (sensitivity != null) {
			return sensitivity.getPriceForecast();
		} else {
			return 0;
		}
	}

	/** @return powers from max discharging to max charging in the specified {@link TimePeriod} */
	private double[] calcPowerDeltas(TimePeriod timePeriod) {
		double[] powerDeltasInMW = new double[numberOfTransitionStates * 2 + 1];
		StepPower stepPower = calcStepPower(timePeriod);
		for (int i = -numberOfTransitionStates; i <= numberOfTransitionStates; i++) {
			int index = i + numberOfTransitionStates;
			powerDeltasInMW[index] = stepPower.getPower(i);
		}
		return powerDeltasInMW;
	}

	/** @return power steps for charging & discharging in the specified {@link TimePeriod} */
	private StepPower calcStepPower(TimePeriod timePeriod) {
		final MeritOrderSensitivity sensitivity = getSensitivityForPeriod(timePeriod);
		if (sensitivity != null) {
			return sensitivity.getStepPowers(numberOfTransitionStates);
		} else {
			return new StepPower(0, 0, numberOfTransitionStates);
		}
	}

	/** @return income of best strategy starting in given period at given state */
	private double getBestIncome(int period, int state) {
		return period < forecastSteps ? incomeSum[period][state] : 0;
	}

	/** @return income for a state transition under specified chargePrice */
	private double calcIncomeTransition(int stateDelta, double chargePrice, double[] powerDeltasInMW) {
		int arrayIndex = numberOfTransitionStates + stateDelta;
		double externalEnergyDelta = powerDeltasInMW[arrayIndex];
		return -externalEnergyDelta * chargePrice;
	}

	@Override
	protected double calcBidPrice(TimePeriod timePeriod, double externalEnergyDelta) {
		if (externalEnergyDelta == 0) {
			return Double.NaN;
		} else if (externalEnergyDelta < 0) {
			return Constants.MINIMAL_PRICE_IN_EUR_PER_MWH;
		} else {
			return Constants.SCARCITY_PRICE_IN_EUR_PER_MWH;
		}
	}

	@Override
	protected MeritOrderSensitivity createBlankSensitivity() {
		return new PriceNoSensitivity();
	}
}
