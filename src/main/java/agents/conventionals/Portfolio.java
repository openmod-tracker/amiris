package agents.conventionals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.math3.util.Precision;
import agents.markets.FuelsMarket.FuelType;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;
import util.SortedLinkedList;
import util.Util;

/** Summarises a set of power plants
 * 
 * @author Christoph Schimeczek */
public class Portfolio implements Portable {
	private PowerPlantPrototype prototype;
	private final SortedLinkedList<PowerPlant> powerPlants = new SortedLinkedList<>();

	/** required for {@link Portable}s */
	public Portfolio() {}

	/** Creates {@link Portfolio} based on given {@link PowerPlantPrototype}
	 * 
	 * @param prototype common to all power plants of this portfolio */
	public Portfolio(PowerPlantPrototype prototype) {
		this.prototype = prototype;
	}

	/** required for {@link Portable}s */
	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeComponents(prototype);
		for (PowerPlant powerPlant : powerPlants) {
			collector.storeComponents(powerPlant);
		}
	}

	/** required for {@link Portable}s */
	@Override
	public void populate(ComponentProvider provider) {
		prototype = provider.nextComponent(PowerPlantPrototype.class);
		powerPlants.addAll(provider.nextComponentList(PowerPlant.class));
	}

	/** Creates an new {@link PowerPlant}s which are added to the portfolio, sorted from lowest to highest efficiency
	 * 
	 * @param blockSizeInMW nominal capacity of each (but the final) created power plant block
	 * @param installedCapacityInMW total nominal capacity of all power plants to be generated
	 * @param minEfficiency the lowest efficiency in the power plant list
	 * @param maxEfficiency the highest efficiency in the power plant list
	 * @param constructionTimeStep the time at which all power plants become active
	 * @param tearDownTimeStep time step at which all power plant are deactivated
	 * @param roundingPrecision number of decimal places to round interpolated precision to */
	public void setupPlants(double blockSizeInMW, double installedCapacityInMW, double minEfficiency,
			double maxEfficiency, long constructionTimeStep, long tearDownTimeStep, int roundingPrecision) {
		int numberOfBlocks = calcBlocks(installedCapacityInMW, blockSizeInMW);
		ArrayList<Double> efficiencySet = Util.linearInterpolation(minEfficiency, maxEfficiency, numberOfBlocks);
		efficiencySet = roundEfficiencySet(efficiencySet, roundingPrecision);
		double remainingPowerInMW = installedCapacityInMW;
		for (int plantIndex = 0; plantIndex < efficiencySet.size(); plantIndex++) {
			double powerOfPlant = Math.min(remainingPowerInMW, blockSizeInMW);
			PowerPlant powerPlant = new PowerPlant(prototype, efficiencySet.get(plantIndex), powerOfPlant);
			powerPlant.setConstructionTimeStep(constructionTimeStep);
			powerPlant.setTearDownTimeStep(tearDownTimeStep);
			powerPlants.add(powerPlant);
			remainingPowerInMW -= powerOfPlant;
		}
	}

	/** Calculates the number of blocks required to match the given total capacity
	 * 
	 * @param totalCapacityInMW the total nominal capacity to be installed in MW
	 * @param blockSizeInMW the nominal block size of the power plants to generate in MW
	 * @return the number of power plant blocks; the last block may not have full power */
	private int calcBlocks(double totalCapacityInMW, double blockSizeInMW) {
		return (int) Math.ceil(totalCapacityInMW / blockSizeInMW);
	}

	/** Applies rounding to given efficiencies by given precision, if appropriate
	 * 
	 * @param efficiencies the list of efficiencies to be rounded
	 * @param roundingPrecision number of decimal places to round to [1..15] - for other values no rounding is applied
	 * @return new (or old, if not rounded) list of efficiencies */
	private ArrayList<Double> roundEfficiencySet(ArrayList<Double> efficiencies, int roundingPrecision) {
		if (roundingPrecision < 16 && roundingPrecision > 0) {
			ArrayList<Double> newValues = new ArrayList<>(efficiencies.size());
			for (double originalValue : efficiencies) {
				newValues.add(Precision.round(originalValue, roundingPrecision));
			}
			return newValues;
		} else {
			return efficiencies;
		}
	}

	/** @return Type of Fuel that all contained {@link PowerPlant}s have in common */
	public FuelType getFuelType() {
		return prototype.getFuelType();
	}

	/** @return {@link Collections#unmodifiableList(List) UnmodifiableList} of {@link PowerPlant}s, ordered from lowest to highest efficiency */
	public List<PowerPlant> getPowerPlantList() {
		return Collections.unmodifiableList(powerPlants);
	}

	/** Removes any plants from the portfolio that have a tear-down time before the specified time step
	 * 
	 * @param currentTimeStep specified TimeStep */
	public void tearDownPlants(long currentTimeStep) {
		ListIterator<PowerPlant> iterator = powerPlants.listIterator();
		while (iterator.hasNext()) {
			PowerPlant powerPlant = iterator.next();
			if (powerPlant.readyToTearDownIn(currentTimeStep)) {
				iterator.remove();
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (PowerPlant plant : powerPlants) {
			stringBuilder.append(plant.toString());
		}
		return stringBuilder.toString();
	}

	/** @return power plant prototype common to all power plants in this {@link Portfolio} */
	public PowerPlantPrototype getPrototype() {
		return prototype;
	}
}