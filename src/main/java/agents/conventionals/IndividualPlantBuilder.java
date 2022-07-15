package agents.conventionals;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Installs and dismantles power plants according to a list of individual plants
 * 
 * @author Christoph Schimeczek */
public class IndividualPlantBuilder extends PlantBuildingManager {
	@Input private static final Tree parameters = Make.newTree().add(
			Make.newGroup("Plants").add(
					Make.newDouble("Efficiency"),
					Make.newDouble("NetCapacityInMW"),
					Make.newTimeStamp("ActivationTime").optional(),
					Make.newTimeStamp("DeactivationTime").optional()).list())
			.buildTree();

	private final List<PowerPlant> powerPlants;

	/** Creates an {@link IndividualPlantBuilder}
	 * 
	 * @param dataProvider provides input from config file
	 * @throws MissingDataException if any required data is not provided */
	public IndividualPlantBuilder(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		powerPlants = readPowerPlants(input.getGroupList("Plants"));
	}

	/** @return list of all power plants created from the corresponding input group list */
	private List<PowerPlant> readPowerPlants(List<ParameterData> plantsData) throws MissingDataException {
		LinkedList<PowerPlant> plants = new LinkedList<>();
		for (ParameterData data : plantsData) {
			PowerPlant plant = new PowerPlant(prototype, data.getDouble("Efficiency"), data.getDouble("NetCapacityInMW"));
			TimeStamp activationTime = data.getTimeStampOrDefault("ActivationTime", null);
			TimeStamp deactivationTime = data.getTimeStampOrDefault("DeactivationTime", null);
			if (activationTime != null) {
				plant.setConstructionTimeStep(activationTime.getStep());
			}
			if (deactivationTime != null) {
				plant.setTearDownTimeStep(deactivationTime.getStep());
			}
			plants.add(plant);
		}
		return plants;
	}

	@Override
	protected void updatePortfolio(TimeStamp targetTime, TimeSpan deliveryInterval) {
		removeOldPlantsFromPortfolio();
		TimeStamp followUpTime = targetTime.laterBy(deliveryInterval);
		TimeStamp followFollowUpTime = followUpTime.laterBy(deliveryInterval);

		ListIterator<PowerPlant> iterator = powerPlants.listIterator();
		while (iterator.hasNext()) {
			PowerPlant plant = iterator.next();
			if (plant.isActiveAnyTimeBetween(targetTime.getStep(), followFollowUpTime.getStep())) {
				iterator.remove();
				portfolio.addPlant(plant);
			}
		}
	}
}
