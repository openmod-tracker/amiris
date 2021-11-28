package agents.plantOperator;

import java.util.ArrayList;
import java.util.List;
import agents.forecast.MarketForecaster;
import agents.policy.SupportPolicy.EnergyCarrier;
import agents.policy.SupportPolicy.SupportInstrument;
import agents.trader.AggregatorTrader;
import communications.message.MarginalCost;
import communications.message.PointInTime;
import communications.message.TechnologySet;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** A {@link PowerPlantOperator} for renewable plants
 * 
 * @author Christoph Schimeczek, Johannes Kochems */
public abstract class RenewablePlantOperator extends PowerPlantOperator {
	public static enum SetType {
		PVRooftop, WindOn, WindOff, RunOfRiver, OtherPV, Biogas, Undefined
	}

	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newEnum("Set", SetType.class).optional(), Make.newEnum("EnergyCarrier", EnergyCarrier.class),
					Make.newEnum("SupportInstrument", SupportInstrument.class).optional(), Make.newSeries("InstalledPowerInMW"),
					Make.newSeries("OpexVarInEURperMWH"))
			.buildTree();

	@Product
	public static enum Products {
		SetRegistration
	};

	/** A group of plants of the same energy carrier and support type attributed to a dedicated PowerPlantOperator */
	private final TechnologySet technologySet;
	/** TimeSeries of total peak production capacity */
	private final TimeSeries tsInstalledPowerInMW;
	/** TimeSeries of variable cost of operation */
	private final TimeSeries tsOpexVarInEURperMWH;

	/** Creates a {@link RenewablePlantOperator}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public RenewablePlantOperator(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		tsInstalledPowerInMW = input.getTimeSeries("InstalledPowerInMW");
		tsOpexVarInEURperMWH = input.getTimeSeries("OpexVarInEURperMWH");

		SetType technologySetType = input.getEnumOrDefault("Set", SetType.class, null);
		EnergyCarrier energyCarrier = input.getEnum("EnergyCarrier", EnergyCarrier.class);
		SupportInstrument supportInstrument = input.getEnumOrDefault("SupportInstrument", SupportInstrument.class, null);
		technologySet = new TechnologySet(technologySetType, energyCarrier, supportInstrument,
				tsInstalledPowerInMW.getValueLowerEqual(now()));

		call(this::registerSet).on(Products.SetRegistration);
		call(this::sendSupplyMarginals).on(PowerPlantOperator.Products.MarginalCost);
		call(this::sendSupplyMarginalsForecast).on(PowerPlantOperator.Products.MarginalCostForecast)
				.use(MarketForecaster.Products.ForecastRequest);
	}

	/** Registers the {@link TechnologySet} to be marketed by a single associated {@link AggregatorTrader} */
	private void registerSet(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		fulfilNext(contract, technologySet);
	}

	/** Prepares supply {@link MarginalForecast}s and sends them to contracted trader
	 * 
	 * @param input one or multiple messages that define the time of the forecast to be created
	 * @param contracts single contract with trader */
	private void sendSupplyMarginalsForecast(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		for (Message message : input) {
			TimeStamp requestedTime = message.getDataItemOfType(PointInTime.class).timeStamp;
			fulfilSupplyContract(requestedTime, contract);
		}
	}

	/** Calculates supply marginals for the given TimeStamp to fulfil the given contract */
	private MarginalCost fulfilSupplyContract(TimeStamp targetTime, Contract contract) {
		MarginalCost marginal = calcSingleMarginal(targetTime);
		fulfilNext(contract, marginal);
		return marginal;
	}

	/** @param time to calculate costs for
	 * @return single MarginalCost item calculated for the given time */
	abstract protected MarginalCost calcSingleMarginal(TimeStamp time);

	/** Prepares supply {@link Marginal}s and sends them to single contracted trader
	 * 
	 * @param input not used
	 * @param contracts single contracted trader to receive calculated marginals */
	private void sendSupplyMarginals(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TimeStamp targetTime = now().laterBy(new TimeSpan(2L));// TODO:: HACK for Validation: remove + 2L
		MarginalCost marginal = fulfilSupplyContract(targetTime, contract);
		store(PowerPlantOperator.OutputFields.OfferedPowerInMW, marginal.powerPotentialInMW);
	}

	/** Returns the installed power at the specified time
	 * 
	 * @param time to get the installed power for
	 * @return installed peak power in MW at the specified time */
	protected double getInstalledPowerAtTime(TimeStamp time) {
		return tsInstalledPowerInMW.getValueLinear(time);
	}

	/** Returns variable operational expenses at the specified time
	 * 
	 * @param time to calculate the costs for
	 * @return variable operational expenses in EUR per MWh at the specified time */
	protected double getVariableOpexAtTime(TimeStamp time) {
		return tsOpexVarInEURperMWH.getValueLowerEqual(time);
	}

	@Override
	protected double dispatchPlants(double awardedPower, TimeStamp time) {
		return getVariableOpexAtTime(time) * awardedPower;
	}
}
