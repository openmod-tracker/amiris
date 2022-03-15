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
	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newSeries("InstalledPowerInMW"), Make.newInt("EfficiencyRoundingPrecision").optional()).buildTree();

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
	}

	@Override
	protected void updatePortfolio(TimeStamp targetTime, TimeSpan deliveryInterval) {
		portfolio.tearDownPlants(now().getStep());
		TimeStamp followUpTime = targetTime.laterBy(deliveryInterval);
		TimeStamp followFollowUpTime = followUpTime.laterBy(deliveryInterval);

		if (portfolio.getPowerPlantList().isEmpty()) {
			portfolio.setupPlants(blockSizeInMW, getPlannedPowerAt(targetTime), getMinEfficiencyAt(targetTime),
					getMaxEfficiencyAt(targetTime), targetTime.getStep(), followUpTime.getStep(), roundingPrecision);
		}
		portfolio.setupPlants(blockSizeInMW, getPlannedPowerAt(followUpTime), getMinEfficiencyAt(followUpTime),
				getMaxEfficiencyAt(followUpTime), followUpTime.getStep(), followFollowUpTime.getStep(), roundingPrecision);
	}

	/** Returns the planned power at the specified time
	 * 
	 * @param time at which to calculate the installed power
	 * @return installed capacity in MW at the given time */
	private double getPlannedPowerAt(TimeStamp time) {
		return tsInstalledCapacityInMW.getValueLinear(time);
	}
}