// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.conventionals;

import agents.markets.FuelsMarket.FuelType;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Stores common data of one type of conventional power plants
 * 
 * @author Christoph Schimeczek */
public abstract class PowerPlantPrototype implements Portable {
	static final Tree parameters = Make.newTree().add(
			Make.newEnum("FuelType", FuelType.class), Make.newDouble("SpecificCo2EmissionsInTperMWH"),
			Make.newSeries("PlannedAvailability"), Make.newDouble("UnplannedAvailabilityFactor"),
			Make.newSeries("OpexVarInEURperMWH"), Make.newDouble("CyclingCostInEURperMW")).buildTree();

	private FuelType fuelType;
	private double specificCo2EmissionsInTonsPerThermalMWH;
	private double unplannedAvailabilityFactor;
	private double cyclingCostInEURperMW;
	private TimeSeries tsAvailability;
	private TimeSeries tsVariableCosts;

	/** Technical specification template for a group conventional power plants */
	public static class PrototypeData {
		public FuelType fuelType;
		public double specificCo2EmissionsInTonsPerThermalMWH;
		public double unplannedAvailabilityFactor;
		public double cyclingCostInEURperMW;
		public TimeSeries tsAvailability;
		public TimeSeries tsVariableCosts;

		/** Creates a new {@link PrototypeData}
		 * 
		 * @param data input parameters of group {@link PowerPlantPrototype#parameters}
		 * @throws MissingDataException if any required parameter is not specified */
		public PrototypeData(ParameterData data) throws MissingDataException {
			fuelType = data.getEnum("FuelType", FuelType.class);
			specificCo2EmissionsInTonsPerThermalMWH = data.getDouble("SpecificCo2EmissionsInTperMWH");
			unplannedAvailabilityFactor = data.getDouble("UnplannedAvailabilityFactor");
			tsAvailability = data.getTimeSeries("PlannedAvailability");
			cyclingCostInEURperMW = data.getDouble("CyclingCostInEURperMW");
			tsVariableCosts = data.getTimeSeries("OpexVarInEURperMWH");
		}
	}

	/** required for {@link Portable}s */
	public PowerPlantPrototype() {}

	/** Creates {@link PowerPlantPrototype} based on
	 * 
	 * @param prototypeData template to initialise most technical plant parameters */
	public PowerPlantPrototype(PrototypeData prototypeData) {
		fuelType = prototypeData.fuelType;
		specificCo2EmissionsInTonsPerThermalMWH = prototypeData.specificCo2EmissionsInTonsPerThermalMWH;
		unplannedAvailabilityFactor = prototypeData.unplannedAvailabilityFactor;
		cyclingCostInEURperMW = prototypeData.cyclingCostInEURperMW;
		tsAvailability = prototypeData.tsAvailability;
		tsVariableCosts = prototypeData.tsVariableCosts;
	}

	/** required for {@link Portable}s */
	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeInts(fuelType.ordinal());
		collector.storeDoubles(specificCo2EmissionsInTonsPerThermalMWH, unplannedAvailabilityFactor, cyclingCostInEURperMW);
		collector.storeTimeSeries(tsAvailability, tsVariableCosts);
	}

	/** required for {@link Portable}s */
	@Override
	public void populate(ComponentProvider provider) {
		fuelType = FuelType.values()[provider.nextInt()];
		specificCo2EmissionsInTonsPerThermalMWH = provider.nextDouble();
		unplannedAvailabilityFactor = provider.nextDouble();
		cyclingCostInEURperMW = provider.nextDouble();
		tsAvailability = provider.nextTimeSeries();
		tsVariableCosts = provider.nextTimeSeries();
	}

	/** Returns availability of power plants at given time
	 * 
	 * @param time at which to return availability
	 * @return availability ratio between effective and nominal available net electricity generation considering planned and
	 *         unplanned availabilities */
	protected double getAvailability(TimeStamp time) {
		return tsAvailability.getValueLinear(time) * unplannedAvailabilityFactor;
	}

	/** Returns the variable costs at a specified time.
	 * 
	 * @param time to return costs for
	 * @return variable costs in EUR per (electric) MWh */
	protected double getVariableCostInEURperMWH(TimeStamp time) {
		return tsVariableCosts.getValueLinear(time);
	}

	/** Calculates CO2 emissions for given used thermal energy
	 * 
	 * @param thermalEnergyInMWH for which to calculate emissions for
	 * @return emitted co2 for the specified thermal energy used */
	public double calcCo2EmissionInTons(double thermalEnergyInMWH) {
		return thermalEnergyInMWH * specificCo2EmissionsInTonsPerThermalMWH;
	}

	/** Returns the cycling costs
	 * 
	 * @return cycling costs in Euro in MW */
	public double getCyclingCostInEURperMW() {
		return cyclingCostInEURperMW;
	}

	/** Returns the fuel type
	 * 
	 * @return fuel type */
	protected FuelType getFuelType() {
		return fuelType;
	}

	/** Returns specific CO2 emissions
	 * 
	 * @return specific CO2 emissions in tons per thermal MWH */
	public double getSpecificCo2EmissionsInTonsPerThermalMWH() {
		return specificCo2EmissionsInTonsPerThermalMWH;
	}

	/** Override {@link #unplannedAvailabilityFactor} with given value
	 * 
	 * @param unplannedAvailabilityFactor to replace template value */
	public void setUnplannedAvailabilityFactor(double unplannedAvailabilityFactor) {
		this.unplannedAvailabilityFactor = unplannedAvailabilityFactor;
	};

	/** Override {@link #cyclingCostInEURperMW} with given value
	 * 
	 * @param cyclingCostInEURperMW to replace template value */
	public void setCyclingCostInEURperMW(double cyclingCostInEURperMW) {
		this.cyclingCostInEURperMW = cyclingCostInEURperMW;
	}

	/** Override {@link #tsAvailability} with given value
	 * 
	 * @param tsAvailability to replace template value */
	public void setPlannedAvailability(TimeSeries tsAvailability) {
		this.tsAvailability = tsAvailability;
	}

	/** Override {@link #tsVariableCosts} with given value
	 * 
	 * @param tsVariableCosts to replace template value */
	public void setTsVariableCosts(TimeSeries tsVariableCosts) {
		this.tsVariableCosts = tsVariableCosts;
	}
}