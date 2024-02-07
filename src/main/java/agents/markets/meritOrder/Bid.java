// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.markets.meritOrder;

import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;

/** A bid offering or requesting a given amount of energy for a specified price
 * 
 * @author Martin Klein, Christoph Schimeczek */
public class Bid implements Portable {
	/** Type of market bid */
	public static enum Type {
		/** Offering energy */
		Supply,
		/** Requesting energy */
		Demand
	};

	private double offerPriceInEURperMWH;
	private double energyAmountInMWH;
	private double marginalCostInEURperMWH;
	private long traderUuid;
	private Type type;

	/** required for {@link Portable}s */
	public Bid() {}

	/** Creates a {@link Bid}
	 * 
	 * @param energyAmountInMWH amount of energy requested or offered (depending on Type of this Bid)
	 * @param offerPriceInEURperMWH maximum / minimum offer price (depending on Type of this Bid)
	 * @param marginalCostInEURperMWH actual marginal cost associated with this Bid
	 * @param traderUuid id of the Trader that issued this Bid
	 * @param type of this Bid */
	public Bid(double energyAmountInMWH, double offerPriceInEURperMWH, double marginalCostInEURperMWH, long traderUuid,
			Type type) {
		this.offerPriceInEURperMWH = offerPriceInEURperMWH;
		this.energyAmountInMWH = energyAmountInMWH;
		this.traderUuid = traderUuid;
		this.type = type;
		this.marginalCostInEURperMWH = marginalCostInEURperMWH;
	}

	@Override
	public String toString() {
		return energyAmountInMWH + " MWH @ " + offerPriceInEURperMWH + " €/MWh from " + traderUuid;
	}

	/** @return maximum / minimum offer price (depending on Type of this Bid) */
	public double getOfferPriceInEURperMWH() {
		return offerPriceInEURperMWH;
	}

	/** @return amount of energy requested or offered (depending on Type of this Bid) */
	public double getEnergyAmountInMWH() {
		return energyAmountInMWH;
	}

	/** @return id of the Trader that issued this Bid */
	public long getTraderUuid() {
		return traderUuid;
	}

	/** @return {@link Type} of this Bid */
	public Type getType() {
		return type;
	}

	/** @return actual marginal cost associated with this Bid */
	public double getMarginalCost() {
		return marginalCostInEURperMWH;
	}

	/** required for {@link Portable}s */
	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeDoubles(offerPriceInEURperMWH, energyAmountInMWH, marginalCostInEURperMWH);
		collector.storeLongs(traderUuid);
		collector.storeInts(type.ordinal());
	}

	/** required for {@link Portable}s */
	@Override
	public void populate(ComponentProvider provider) {
		offerPriceInEURperMWH = provider.nextDouble();
		energyAmountInMWH = provider.nextDouble();
		marginalCostInEURperMWH = provider.nextDouble();
		traderUuid = provider.nextLong();
		type = Type.values()[provider.nextInt()];
	}
}