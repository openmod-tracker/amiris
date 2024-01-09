// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.storage.arbitrageStrategists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import agents.flexibility.Strategist;
import agents.markets.meritOrder.sensitivities.MeritOrderSensitivity;
import agents.storage.Device;
import agents.trader.StorageTrader;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterBuilder;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Creates arbitrage strategies for storage devices based on forecasts for merit-order or electricity prices
 * 
 * @author Christoph Schimeczek */
public abstract class ArbitrageStrategist extends Strategist {
	public static enum StrategistType {
		/** Creates a the schedule according to a given TimeSeries. */
		DISPATCH_FILE,
		/** Uses the storage {@link Device} in order to minimise the total system costs. */
		SINGLE_AGENT_MIN_SYSTEM_COST,
		/** Optimises the {@link Device} dispatch in order to maximise the profits of the {@link StorageTrader}. A perfect forecast of
		 * upcoming prices (and their changes due to charging) is used for the optimisation. */
		SINGLE_AGENT_MAX_PROFIT,
		/** Calculates the {@link Device} dispatch in order to maximise the profits of the {@link StorageTrader}. A median of the
		 * forecasted prices is used to estimate a good dispatch strategy in an environment with more than one flexible agent. */
		MULTI_AGENT_MEDIAN
	}

	static final String WARN_ROUND_UP = "`EnergyToPowerRatio * ModelledChargingSteps` no integer: storage capacity increased by ";
	static final String WARN_ROUND_DOWN = "`EnergyToPowerRatio * ModelledChargingSteps` no integer: storage capacity decreased by ";

	public static final Tree parameters = Make.newTree()
			.add(Strategist.forecastPeriodParam, Strategist.scheduleDurationParam, Strategist.bidToleranceParam,
					Make.newEnum("StrategistType", StrategistType.class))
			.addAs("SingleAgent", SystemCostMinimiser.parameters).addAs("FixedDispatch", FileDispatcher.parameters)
			.addAs("MultiAgent", MultiAgentMedian.parameters)
			.buildTree();

	public static final ParameterBuilder StrategistTypeParam = Make.newEnum("StrategistType", StrategistType.class);
	static final double ENERGY_STATE_ROUNDING_TOLERANCE = 1.E-2;
	protected static Logger logger = LoggerFactory.getLogger(ArbitrageStrategist.class);

	protected double[] scheduledInitialInternalEnergyInMWH;
	protected Device storage;

	/** Create {@link ArbitrageStrategist}
	 * 
	 * @param input parameters associated with strategists
	 * @param storage Device for which schedules are to be created
	 * @throws MissingDataException if any required input is missing */
	public ArbitrageStrategist(ParameterData input, Device storage) throws MissingDataException {
		super(input);
		this.storage = storage;
		scheduledInitialInternalEnergyInMWH = new double[scheduleDurationPeriods];
	}

	public static ArbitrageStrategist createStrategist(ParameterData input, Device storage) throws MissingDataException {
		StrategistType strategistType = input.getEnum("StrategistType", StrategistType.class);
		switch (strategistType) {
			case SINGLE_AGENT_MIN_SYSTEM_COST:
				return new SystemCostMinimiser(input, input.getGroup("SingleAgent"), storage);
			case DISPATCH_FILE:
				return new FileDispatcher(input, input.getGroup("FixedDispatch"), storage);
			case SINGLE_AGENT_MAX_PROFIT:
				return new ProfitMaximiser(input, input.getGroup("SingleAgent"), storage);
			case MULTI_AGENT_MEDIAN:
				return new MultiAgentMedian(input, input.getGroup("MultiAgent"), storage);
			default:
				throw new RuntimeException("Storage Strategist not implemented: " + strategistType);
		}
	}

	@Override
	protected void callOnSensitivity(MeritOrderSensitivity sensitivity) {
		sensitivity.updatePowers(storage.getExternalChargingPowerInMW(), storage.getExternalDischargingPowerInMW());
	}

	@Override
	protected double[] getInternalEnergySchedule() {
		return scheduledInitialInternalEnergyInMWH;
	}

	/** Returns forecasted external charging power, if possible - otherwise throws a RuntimeEception
	 * 
	 * @param targetTime for which to provide the forecast
	 * @return forecasted external charging power in MW
	 * @throws RuntimeException if this strategist cannot provide forecasts */
	public abstract double getChargingPowerForecastInMW(TimeStamp targetTime);

	/** Update scheduled initial energies and charging schedules to correct errors due to rounding of energies caused by
	 * discretisation of internal energy states
	 * 
	 * @param initialEnergyInStorage initial internal energy level in MWh of the storage at the beginning of the first hour of
	 *          planning interval */
	protected void correctForRoundingErrors(double initialEnergyInStorage) {
		double maxCapacity = storage.getEnergyStorageCapacityInMWH();
		for (int period = 0; period < scheduleDurationPeriods; period++) {
			scheduledInitialInternalEnergyInMWH[period] = initialEnergyInStorage;
			double internalChargingPower = storage.externalToInternalEnergy(demandScheduleInMWH[period]);
			double nextEnergy = Math.max(0, Math.min(maxCapacity, initialEnergyInStorage + internalChargingPower));
			demandScheduleInMWH[period] = storage.internalToExternalEnergy(nextEnergy - initialEnergyInStorage);
			initialEnergyInStorage = nextEnergy;
		}
	}

	/** Calculates number of energy states, logs warning if rounding is needed
	 * 
	 * @param numberOfTransitionStates
	 * @return numberOfTransitionStates (rounded to closest integer) */
	protected int calcNumberOfEnergyStates(int numberOfTransitionStates) {
		double numberOfEnergyStates = numberOfTransitionStates * storage.getEnergyToPowerRatio() + 1;
		int roundedNumberOfEnergyStates = (int) Math.round(numberOfEnergyStates);
		double stateDelta = roundedNumberOfEnergyStates - numberOfEnergyStates;
		double capacityDeltaInMWH = stateDelta * storage.getInternalPowerInMW() / numberOfTransitionStates;
		if (stateDelta > ENERGY_STATE_ROUNDING_TOLERANCE) {
			logger.warn(WARN_ROUND_UP + capacityDeltaInMWH + " MWh");
		} else if (stateDelta < ENERGY_STATE_ROUNDING_TOLERANCE) {
			logger.warn(WARN_ROUND_DOWN + capacityDeltaInMWH + " MWh");
		}
		return roundedNumberOfEnergyStates;
	}
}