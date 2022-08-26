// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
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
import de.dlr.gitlab.fame.data.TimeSeries;
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
					Make.newTimeStamp("DeactivationTime").optional(),
					Make.newString("Id").optional(),
					Make.newGroup("Override").add(
							Make.newSeries("PlannedAvailability").optional(),
							Make.newDouble("UnplannedAvailabilityFactor").optional(),
							Make.newSeries("OpexVarInEURperMWH").optional(),
							Make.newDouble("CyclingCostInEURperMW").optional()))
					.list())
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
		int plantCount = 0;
		for (ParameterData data : plantsData) {
			plantCount++;
			String identifier = data.getStringOrDefault("Id", "Auto_" + plantCount);
			PowerPlant plant = new PowerPlant(prototypeData, data.getDouble("Efficiency"), data.getDouble("NetCapacityInMW"), identifier);
			TimeStamp activationTime = data.getTimeStampOrDefault("ActivationTime", null);
			TimeStamp deactivationTime = data.getTimeStampOrDefault("DeactivationTime", null);
			if (activationTime != null) {
				plant.setConstructionTimeStep(activationTime.getStep());
			}
			if (deactivationTime != null) {
				plant.setTearDownTimeStep(deactivationTime.getStep());
			}
			try {
				ParameterData override = data.getGroup("Override");
				TimeSeries plannedAvailability = override.getTimeSeriesOrDefault("PlannedAvailability", null);
				Double unplannedAvailabilityFactor = override.getDoubleOrDefault("UnplannedAvailabilityFactor", null);
				TimeSeries opexVarInEURperMWH = override.getTimeSeriesOrDefault("OpexVarInEURperMWH", null);
				Double cyclingCostInEURperMW = override.getDoubleOrDefault("CyclingCostInEURperMW", null);
				if (plannedAvailability != null) {
					plant.setPlannedAvailability(plannedAvailability);
				}
				if (unplannedAvailabilityFactor != null) {
					plant.setUnplannedAvailabilityFactor(unplannedAvailabilityFactor);
				}
				if (opexVarInEURperMWH != null) {
					plant.setTsVariableCosts(opexVarInEURperMWH);
				}
				if (cyclingCostInEURperMW != null) {
					plant.setCyclingCostInEURperMW(cyclingCostInEURperMW);
				}
			} catch (MissingDataException e) {}
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
