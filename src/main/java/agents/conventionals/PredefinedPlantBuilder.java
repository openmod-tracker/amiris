package agents.conventionals;

import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Installs and dismantles power plants according to a given predefined power TimeSeries
 *
 * @author Christoph Schimeczek */
public class PredefinedPlantBuilder extends PlantBuildingManager {
	@Input private static final Tree parameters = Make.newTree().add(
			Make.newSeries("InstalledPowerInMW"), Make.newInt("EfficiencyRoundingPrecision").optional(),
			Make.newGroup("Efficiency").add(Make.newSeries("Minimal"), Make.newSeries("Maximal")),
			Make.newDouble("BlockSizeInMW"))
			.buildTree();

	private final TimeSeries tsMinimumEfficiency;
	private final TimeSeries tsMaximumEfficiency;
	private final double blockSizeInMW;
	private final TimeSeries tsInstalledCapacityInMW;
	private final int roundingPrecision;

	/** Creates a {@link PredefinedPlantBuilder}
	 * 
	 * @param dataProvider provides input from config file
	 * @throws MissingDataException if any required data is not provided */
	public PredefinedPlantBuilder(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		tsInstalledCapacityInMW = input.getTimeSeries("InstalledPowerInMW");
		roundingPrecision = input.getIntegerOrDefault("EfficiencyRoundingPrecision", 20);
		blockSizeInMW = input.getDouble("BlockSizeInMW");
		tsMinimumEfficiency = input.getTimeSeries("Efficiency.Minimal");
		tsMaximumEfficiency = input.getTimeSeries("Efficiency.Maximal");
	}

	@Override
	protected void updatePortfolio(TimeStamp targetTime, TimeSpan deliveryInterval) {
		removeOldPlantsFromPortfolio();

		boolean isFirstBuild = portfolio.getPowerPlantList().isEmpty();
		TimeStamp contructionTime = isFirstBuild ? targetTime : targetTime.laterBy(deliveryInterval);
		TimeStamp tearDownTime = contructionTime.laterBy(deliveryInterval);
		portfolio.setupPlants(blockSizeInMW, getPlannedPowerAt(contructionTime), getMinEfficiencyAt(contructionTime),
				getMaxEfficiencyAt(contructionTime), contructionTime.getStep(), tearDownTime.getStep(), roundingPrecision);
	}

	/** Returns the planned power at the specified time
	 * 
	 * @param time at which to calculate the installed power
	 * @return installed capacity in MW at the given time */
	private double getPlannedPowerAt(TimeStamp time) {
		return tsInstalledCapacityInMW.getValueLinear(time);
	}

	/** Calculates the minimum efficiency of portfolio at given time
	 * 
	 * @param time for which to calculate the efficiency
	 * @return the minimum efficiency of the power plant portfolio at the given time */
	private double getMinEfficiencyAt(TimeStamp time) {
		return tsMinimumEfficiency.getValueLinear(time);
	}

	/** Calculates the maximum efficiency of portfolio at given time
	 * 
	 * @param time for which to calculate the efficiency
	 * @return the maximum efficiency of the power plant portfolio at the given time */
	private double getMaxEfficiencyAt(TimeStamp time) {
		return tsMaximumEfficiency.getValueLinear(time);
	}
}