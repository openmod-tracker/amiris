package agents.conventionals;

import java.util.ArrayList;
import java.util.List;
import agents.plantOperator.ConventionalPlantOperator;
import de.dlr.gitlab.fame.agent.Agent;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Installs and tears down conventional power plants
 * 
 * @author Christoph Schimeczek */
public abstract class PlantBuildingManager extends Agent {
	@Input private static final Tree parameters = Make.newTree().add(Make.newLong("PortfolioBuildingOffsetInSeconds"))
			.addAs("Prototype", PowerPlantPrototype.parameters)
			.add(Make.newGroup("Efficiency").add(Make.newSeries("Minimal"), Make.newSeries("Maximal")),
					Make.newDouble("BlockSizeInMW"))
			.buildTree();

	@Product
	public static enum Products {
		/** Collection of power plants created and managed by this {@link PlantBuildingManager} */
		PowerPlantPortfolio
	};

	private final TimeSpan portfolioBuildingOffset;
	private final PowerPlantPrototype prototype;
	private final TimeSeries tsMinimumEfficiency;
	private final TimeSeries tsMaximumEfficiency;
	protected final Portfolio portfolio;
	protected final double blockSizeInMW;
	protected final int foresightOfPowerPlantOperator = 0;

	/** Creates a {@link PlantBuildingManager}
	 * 
	 * @param dataProvider contains all input data from config
	 * @throws MissingDataException thrown if requested input is not provided */
	public PlantBuildingManager(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);

		portfolioBuildingOffset = new TimeSpan(input.getLong("PortfolioBuildingOffsetInSeconds"));
		prototype = new PowerPlantPrototype(input.getGroup("Prototype"));
		portfolio = new Portfolio(prototype);
		blockSizeInMW = input.getDouble("BlockSizeInMW");
		tsMinimumEfficiency = input.getTimeSeries("Efficiency.Minimal");
		tsMaximumEfficiency = input.getTimeSeries("Efficiency.Maximal");

		call(this::updateAndSendPortfolio).on(Products.PowerPlantPortfolio);
	}

	/** Generates a new {@link Portfolio} and sends it to a connected agent
	 * 
	 * @param input not used
	 * @param contracts one contract to receiver of generated Portfolio - typically a {@link ConventionalPlantOperator} */
	public void updateAndSendPortfolio(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TimeStamp currentDeliveryTime = contract.getNextTimeOfDeliveryAfter(now());
		TimeStamp targetTime = currentDeliveryTime.laterBy(portfolioBuildingOffset);
		TimeStamp nextDeliveryTime = contract.getNextTimeOfDeliveryAfter(targetTime);
		TimeSpan deliveryInterval = new TimeSpan(nextDeliveryTime.getStep() - currentDeliveryTime.getStep());

		updatePortfolio(targetTime, deliveryInterval);
		fulfilNext(contract, portfolio, (DataItem) null);
	}

	/** Creates new plants and tears down old ones in order to match current and future plant specifications
	 * 
	 * @param time the target time when the generated portfolio shall become active
	 * @param deliveryInterval duration for which the portfolio shall be active */
	protected abstract void updatePortfolio(TimeStamp time, TimeSpan deliveryInterval);

	/** Calculates the minimum efficiency of portfolio at given time
	 * 
	 * @param time for which to calculate the efficiency
	 * @return the minimum efficiency of the power plant portfolio at the given time */
	protected double getMinEfficiencyAt(TimeStamp time) {
		return tsMinimumEfficiency.getValueLinear(time);
	}

	/** Calculates the maximum efficiency of portfolio at given time
	 * 
	 * @param time for which to calculate the efficiency
	 * @return the maximum efficiency of the power plant portfolio at the given time */
	protected double getMaxEfficiencyAt(TimeStamp time) {
		return tsMaximumEfficiency.getValueLinear(time);
	}
}