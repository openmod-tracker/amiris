package agents.conventionals;

import agents.markets.FuelsMarket.FuelType;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;
import de.dlr.gitlab.fame.time.TimeStamp;

/** A conventional power plant unit
 * 
 * @author Christoph Schimeczek */
public class PowerPlant implements Comparable<PowerPlant>, Portable {
	private PowerPlantPrototype prototype;
	private double efficiency;
	private double installedGenerationPowerInMW;
	private double currentLoadLevel = 0;
	private long constructionTimeStep = Long.MIN_VALUE;
	private long tearDownTimeStep = Long.MAX_VALUE;

	/** required for {@link Portable}s */
	public PowerPlant() {}

	/** Creates a {@link PowerPlant}
	 * 
	 * @param prototype general technical specifications
	 * @param efficiency conversion ratio of thermal to electric energy
	 * @param installedBlockPowerInMW nominal (maximum) net electricity generation capacity */
	public PowerPlant(PowerPlantPrototype prototype, double efficiency, double installedBlockPowerInMW) {
		this.prototype = prototype;
		this.efficiency = efficiency;
		this.installedGenerationPowerInMW = installedBlockPowerInMW;
	}

	/** required for {@link Portable}s */
	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeComponents(prototype);
		collector.storeDoubles(efficiency, installedGenerationPowerInMW);
		collector.storeLongs(constructionTimeStep, tearDownTimeStep);
	}

	/** required for {@link Portable}s */
	@Override
	public void populate(ComponentProvider provider) {
		prototype = provider.nextComponent(PowerPlantPrototype.class);
		efficiency = provider.nextDouble();
		installedGenerationPowerInMW = provider.nextDouble();
		constructionTimeStep = provider.nextLong();
		tearDownTimeStep = provider.nextLong();
	}

	/** Calculates marginal costs and available power this {@link PowerPlant}
	 * 
	 * @param time the targeted time of generation
	 * @param fuelCostInEURperThermalMWH cost fuel of the associated fuel
	 * @param co2CostInEURperTon cost for CO2 emission certificates in Euro per ton
	 * @return array of [availablePower, marginalCost] for this power plant at the given time */
	public double[] calcMarginalCost(TimeStamp time, double fuelCostInEURperThermalMWH, double co2CostInEURperTon) {
		double co2CostInEURperThermalMWH = co2CostInEURperTon * prototype.getSpecificCo2EmissionsInTonsPerThermalMWH();
		double thermalEnergyCostInEURperThermalMWH = fuelCostInEURperThermalMWH + co2CostInEURperThermalMWH;
		double variableCostInEURperMWH = prototype.getVariableCostInEURperMWH(time);
		double availablePower = getAvailablePowerInMW(time);
		double marginalCostValue = thermalEnergyCostInEURperThermalMWH / efficiency + variableCostInEURperMWH;
		return new double[] {availablePower, marginalCostValue};
	}

	/** Calculates effectively available net electricity generation capacity considering plant availabilities
	 *
	 * @param time at which to calculate
	 * @return effectively available net electricity generation capacity in MW at the given time considering plant availability.<br>
	 *         If a power plant is not active at the specified time, 0 is returned. */
	public double getAvailablePowerInMW(TimeStamp time) {
		if (isActive(time.getStep())) {
			return installedGenerationPowerInMW * prototype.getAvailability(time);
		}
		return 0;
	}

	/** Checks if a power plant is currently active.
	 * 
	 * @return true if the plant is active at given time, false otherwise */
	public boolean isActive(long timeStep) {
		return timeStep >= constructionTimeStep && timeStep < tearDownTimeStep;
	}

	/** Returns consumed fuel based on the (previously set) load of the plant
	 * 
	 * @param time of generation
	 * @return consumed fuel in thermal MWh based on the current load level of the plant */
	public double calcFuelConsumptionOfGenerationInThermalMWH(TimeStamp time) {
		double availablePowerInMW = getAvailablePowerInMW(time);
		double electricityGenerationInMWH = availablePowerInMW * currentLoadLevel;
		return electricityGenerationInMWH / efficiency;
	}

	/** Changes current load level of plant according to desired electricity output
	 * 
	 * @param time at which to perform the load change
	 * @param electricityOutputInMW the desired net electricity generation in MW
	 * @return ramping cost in EUR */
	public double updateCurrentLoadLevel(TimeStamp time, double electricityOutputInMW) {
		double availablePowerInMW = getAvailablePowerInMW(time);
		double newLoadLevel = electricityOutputInMW / availablePowerInMW;
		newLoadLevel = ensureValidLoadLevel(newLoadLevel);
		double oldOutputPowerInMW = currentLoadLevel * availablePowerInMW;
		double powerDeltaInMW = electricityOutputInMW - oldOutputPowerInMW;
		double costOfLoadChangeInEUR = calcSpecificCostOfLoadChangeInEURperMW(currentLoadLevel, newLoadLevel)
				* powerDeltaInMW;
		currentLoadLevel = newLoadLevel;
		return costOfLoadChangeInEUR;
	}

	/** Throws an Exception if given load level is not between [0..1], else returns given load level
	 * 
	 * @param loadLevel to check for validity
	 * @return given loadLevel as is, if between 0 and 1; 0 if load level is {@link Double#isNaN()};
	 * @throws RuntimeException on invalid load level */
	private double ensureValidLoadLevel(double loadLevel) {
		if (Double.isNaN(loadLevel)) {
			return 0;
		} else if (loadLevel >= 0 && loadLevel <= 1) {
			return loadLevel;
		}
		throw new RuntimeException("Load level not element of [0, 1].");
	}

	/** Returns ramping cost on load level change
	 * 
	 * @param oldLoadLevel previous load level
	 * @param newLoadLevel tload level
	 * @return specific cost for changing load level of this plant from old to new */
	public double calcSpecificCostOfLoadChangeInEURperMW(double oldLoadLevel, double newLoadLevel) {
		if (oldLoadLevel == 0 && newLoadLevel > 0) {
			return prototype.getCyclingCostInEURperMW();
		}
		return 0;
	}

	/** Returns type of fuel used by this {@link PowerPlant}
	 * 
	 * @return fuel type of power plant */
	public FuelType getFuelType() {
		return prototype.getFuelType();
	}

	/** Sets first time step in which the power plant will be able to deliver power
	 * 
	 * @param constructionTimeStep first time step where plant {@link #isActive(long)} == true */
	public void setConstructionTimeStep(long constructionTimeStep) {
		this.constructionTimeStep = constructionTimeStep;
	}

	/** Specifies the time step in and after which the power plant will no longer deliver power
	 * 
	 * @param tearDownTimeStep first time step after construction where plant {@link #isActive(long)} == false */
	public void setTearDownTimeStep(long tearDownTimeStep) {
		this.tearDownTimeStep = tearDownTimeStep;
	}

	/** Checks if a power plant is still active or has already been deactivated at the specified time
	 * 
	 * @param timeStep to check for power plant activity
	 * @return true if the plant has been deactivated at the specified timeStep */
	public boolean readyToTearDownIn(long timeStep) {
		return timeStep >= tearDownTimeStep;
	}

	/** Compares power plants in regard of efficiency (in order to sort them) */
	@Override
	public int compareTo(PowerPlant other) {
		return ((Double) efficiency).compareTo(other.efficiency);
	}

	/** @return conversion ratio of thermal to electric energy */
	public double getEfficiency() {
		return efficiency;
	}

	/** @return cycling cost in Euro per MW */
	public double getCyclingCostInEURperMW() {
		return prototype.getCyclingCostInEURperMW();
	}

	@Override
	public String toString() {
		return "(" + installedGenerationPowerInMW + " MW " + prototype.getFuelType() + " @" + efficiency * 100. + "%)";
	}
}