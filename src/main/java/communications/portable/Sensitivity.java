// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.portable;

import agents.forecast.MeritOrderAssessor;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;

public class Sensitivity implements Portable {
	private double multiplier;
	private double[] demandPowers;
	private double[] demandValues;
	private double[] supplyPowers;
	private double[] supplyValues;

	private int lastDemandIndex = 1;
	private int lastSupplyIndex = 1;
	private double lastDemandEnergy = 0.;
	private double lastSupplyEnergy = 0.;

	/** required for {@link Portable}s */
	public Sensitivity() {}

	public Sensitivity(MeritOrderAssessor assessor, double multiplier) {
		this.demandPowers = assessor.getDemandSensitivityPowers();
		this.demandValues = assessor.getDemandSensitivityValues();
		this.supplyPowers = assessor.getSupplySensitivityPowers();
		this.supplyValues = assessor.getSupplySensitivityValues();
		this.multiplier = multiplier;
	}

	public double getMultiplier() {
		return multiplier;
	}

	public void updateMultiplier(double multiplier) {
		this.multiplier = multiplier;
		lastDemandIndex = 1;
		lastSupplyIndex = 1;
		lastDemandEnergy = 0;
		lastSupplyEnergy = 0;
	}

	/** Returns sensitivity value for given requested energy delta
	 * 
	 * @param requestedEnergyInMWH demand &gt; 0; supply &lt; 0
	 * @return sensitivity value */
	public double getValue(double requestedEnergyInMWH) {
		double modifiedEnergy = multiplier * requestedEnergyInMWH;
		if (modifiedEnergy > 0) {
			return getValueAddedDemand(modifiedEnergy);
		} else if (modifiedEnergy < 0) {
			return getValueAddedSupply(-modifiedEnergy);
		}
		return 0;
	}

	/** @return value associated with the additional demand energy */
	private double getValueAddedDemand(double additionalDemandInMWH) {
		int firstIndex = additionalDemandInMWH >= lastDemandEnergy ? lastDemandIndex : 1;
		// TODO: use binary search instead
		for (int index = firstIndex; index < demandPowers.length; index++) {
			if (demandPowers[index] >= additionalDemandInMWH) {
				lastDemandEnergy = additionalDemandInMWH;
				lastDemandIndex = index;
				return interpolateValue(demandPowers[index - 1], demandValues[index - 1], demandPowers[index],
						demandValues[index], additionalDemandInMWH);
			}
		}
		return Double.NaN;
	}

	private double interpolateValue(double x1, double y1, double x2, double y2, double x) {
		return y1 + (y2 - y1) / (x2 - x1) * (x - x1);
	}

	/** @return value associated with the additional supply energy */
	private double getValueAddedSupply(double additionalSupplyInMWH) {
		int firstIndex = additionalSupplyInMWH >= lastSupplyEnergy ? lastSupplyIndex : 1;
		// TODO: use binary search instead
		for (int index = firstIndex; index < supplyPowers.length; index++) {
			if (supplyPowers[index] >= additionalSupplyInMWH) {
				lastSupplyEnergy = additionalSupplyInMWH;
				lastSupplyIndex = index;
				return interpolateValue(supplyPowers[index - 1], supplyValues[index - 1], supplyPowers[index],
						supplyValues[index], additionalSupplyInMWH);
			}
		}
		return Double.NaN;
	}

	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeDoubles(multiplier);
		storeDoubleArray(collector, demandPowers);
		storeDoubleArray(collector, demandValues);
		storeDoubleArray(collector, supplyPowers);
		storeDoubleArray(collector, supplyValues);
	}

	private final void storeDoubleArray(ComponentCollector collector, double[] data) {
		collector.storeInts(data.length);
		collector.storeDoubles(data);
	}

	@Override
	public void populate(ComponentProvider provider) {
		multiplier = provider.nextDouble();
		demandPowers = new double[provider.nextInt()];
		demandValues = new double[provider.nextInt()];
		supplyPowers = new double[provider.nextInt()];
		supplyValues = new double[provider.nextInt()];
		fillArray(provider, demandPowers);
		fillArray(provider, demandValues);
		fillArray(provider, supplyPowers);
		fillArray(provider, supplyValues);
	}

	private final void fillArray(ComponentProvider provider, double[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = provider.nextDouble();
		}
	}
}
