// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.conventionals;

import agents.plantOperator.DispatchResult;
import agents.plantOperator.Marginal;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;
import de.dlr.gitlab.fame.time.TimeStamp;

/** A conventional power plant unit
 * 
 * @author Christoph Schimeczek */
public class PowerPlant extends PowerPlantPrototype implements Comparable<PowerPlant>, Portable {
	private double efficiency;
	private double installedGenerationPowerInMW;
	private double currentLoadLevel = 0;
	private double currentPowerOutputInMW = 0;
	private long constructionTimeStep = Long.MIN_VALUE;
	private long tearDownTimeStep = Long.MAX_VALUE;
	private String id;

	/** required for {@link Portable}s */
	public PowerPlant() {}

	/** Creates a {@link PowerPlant}
	 * 
	 * @param prototypeData technical specification template for a group of power plants
	 * @param efficiency conversion ratio of thermal to electric energy
	 * @param installedBlockPowerInMW nominal (maximum) net electricity generation capacity
	 * @param id name of this power plant unit used in outputs */
	public PowerPlant(PrototypeData prototypeData, double efficiency, double installedBlockPowerInMW, String id) {
		super(prototypeData);
		this.efficiency = efficiency;
		this.installedGenerationPowerInMW = installedBlockPowerInMW;
		this.id = id;
	}

	/** required for {@link Portable}s */
	@Override
	public void addComponentsTo(ComponentCollector collector) {
		super.addComponentsTo(collector);
		collector.storeDoubles(efficiency, installedGenerationPowerInMW);
		collector.storeLongs(constructionTimeStep, tearDownTimeStep);
		collector.storeStrings(id);
	}

	/** required for {@link Portable}s */
	@Override
	public void populate(ComponentProvider provider) {
		super.populate(provider);
		efficiency = provider.nextDouble();
		installedGenerationPowerInMW = provider.nextDouble();
		constructionTimeStep = provider.nextLong();
		tearDownTimeStep = provider.nextLong();
		id = provider.nextString();
	}

	/** Calculates marginal costs and available power for this {@link PowerPlant} at given time
	 * 
	 * @param time the targeted time of generation
	 * @param fuelCostInEURperThermalMWH cost fuel of the associated fuel
	 * @param co2CostInEURperTon cost for CO2 emission certificates in Euro per ton
	 * @return Marginal */
	public Marginal calcMarginal(TimeStamp time, double fuelCostInEURperThermalMWH,
			double co2CostInEURperTon) {
		double marginalCostInEURperMWH = calcMarginalCost(time, fuelCostInEURperThermalMWH, co2CostInEURperTon);
		double availablePowerInMW = getAvailablePowerInMW(time);
		return new Marginal(availablePowerInMW, marginalCostInEURperMWH);
	}

	/** Calculates specific marginal costs of this {@link PowerPlant} at given time
	 * 
	 * @param time the targeted time of generation
	 * @param fuelCostInEURperThermalMWH cost fuel of the associated fuel
	 * @param co2CostInEURperTon cost for CO2 emission certificates in Euro per ton
	 * @return specific marginalCost for one additional MWh of energy by this power plant at the given time */
	public double calcMarginalCost(TimeStamp time, double fuelCostInEURperThermalMWH, double co2CostInEURperTon) {
		double co2CostInEURperThermalMWH = co2CostInEURperTon * getSpecificCo2EmissionsInTonsPerThermalMWH();
		double thermalEnergyCostInEURperThermalMWH = fuelCostInEURperThermalMWH + co2CostInEURperThermalMWH;
		double variableCostInEURperMWH = getVariableCostInEURperMWH(time);
		return thermalEnergyCostInEURperThermalMWH / efficiency + variableCostInEURperMWH;
	}

	/** @param time at which to calculate
	 * @return nominal installed power at given time */
	public double getInstalledPowerInMW(TimeStamp time) {
		return isActive(time.getStep()) ? installedGenerationPowerInMW : 0;
	}

	/** Calculates effectively available net electricity generation capacity considering plant availabilities
	 *
	 * @param time at which to calculate
	 * @return effectively available net electricity generation capacity in MW at the given time considering plant availability.<br>
	 *         If a power plant is not active at the specified time, 0 is returned. */
	public double getAvailablePowerInMW(TimeStamp time) {
		return isActive(time.getStep()) ? installedGenerationPowerInMW * getAvailability(time) : 0;
	}

	/** Checks if the power plant is active at given time
	 * 
	 * @param timeStep at which to test activeness
	 * @return true if the plant is active at given time, false otherwise */
	public boolean isActive(long timeStep) {
		return timeStep >= constructionTimeStep && timeStep < tearDownTimeStep;
	}

	/** Checks if the power plant is active at any time between the given start time step and end time step
	 * 
	 * @param startStep (included) left border of the time interval to check
	 * @param endStep (excluded) right border of the time interval to check
	 * @return true, if the power plant has any phase of activity between the given time steps; false otherwise */
	public boolean isActiveAnyTimeBetween(long startStep, long endStep) {
		return !(tearDownTimeStep <= startStep || constructionTimeStep >= endStep);
	}

	/** Changes current generation of plant according to desired electricity output, returns associated {@link DispatchResult}
	 * 
	 * @param time at which to perform the load change
	 * @param requestedPowerInMW the desired net electricity generation in MW
	 * @param fuelPriceInEURperMWH at given time in EUR per thermal MWH
	 * @param co2PriceInEURperTon at given time in EUR per Ton of CO2
	 * @return {@link DispatchResult result} from the dispatch */
	public DispatchResult updateGeneration(TimeStamp time, double requestedPowerInMW, double fuelPriceInEURperMWH,
			double co2PriceInEURperTon) {
		Marginal marginal = calcMarginal(time, fuelPriceInEURperMWH, co2PriceInEURperTon);
		double newLoadLevel = calcLoadLevel(marginal.getPowerPotentialInMW(), requestedPowerInMW);
		double rampingCostInEUR = calcSpecificCostOfLoadChangeInEURperMW(currentLoadLevel, newLoadLevel)
				* marginal.getPowerPotentialInMW();
		currentLoadLevel = newLoadLevel;
		currentPowerOutputInMW = currentLoadLevel * getAvailablePowerInMW(time);
		double fuelConsumptionInThermalMWH = requestedPowerInMW / efficiency;
		double co2EmissionsInTons = calcCo2EmissionInTons(fuelConsumptionInThermalMWH);
		double variableCost = rampingCostInEUR + marginal.getMarginalCostInEURperMWH() * requestedPowerInMW;
		return new DispatchResult(requestedPowerInMW, variableCost, fuelConsumptionInThermalMWH, co2EmissionsInTons);
	}

	/** @returns new load level based on the available and requested power */
	private double calcLoadLevel(double availablePowerInMW, double requestedPowerInMW) {
		double newLoadLevel = requestedPowerInMW / availablePowerInMW;
		return ensureValidLoadLevel(newLoadLevel);
	}

	/** Returns electricity production from last update of load level
	 * 
	 * @return power production as set by last update */
	public double getCurrentPowerOutputInMW() {
		return currentPowerOutputInMW;
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
	 * @param newLoadLevel next load level
	 * @return specific cost for changing load level of this plant from old to new */
	public double calcSpecificCostOfLoadChangeInEURperMW(double oldLoadLevel, double newLoadLevel) {
		if (oldLoadLevel == 0 && newLoadLevel > 0) {
			return getCyclingCostInEURperMW();
		}
		return 0;
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

	/** @return identifier of this {@link PowerPlant} */
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "(" + installedGenerationPowerInMW + " MW " + getFuelType() + " @" + efficiency * 100. + "%)";
	}

}