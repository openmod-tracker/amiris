// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.markets.meritOrder.books;

import java.util.Comparator;

import agents.markets.meritOrder.Bid;
import agents.markets.meritOrder.Bid.Type;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;

/** An item of an {@link OrderBook}, either bid or ask
 * 
 * @author Christoph Schimeczek, Martin Klein , Evelyn Sperber, Farzad Sarfarazi */
public class OrderBookItem implements Portable {
	static final String ERR_NEGATIVE_POWER = "OrderBookItems with negative power received from Trader: ";
	
	public static final Comparator<OrderBookItem> BY_PRICE = Comparator.comparing(item -> item.getOfferPrice());
	private Bid bid;
	private double cumulatedPowerUpperValue = Double.NaN;
	private double awardedPower = Double.NaN;

	/** required for {@link Portable}s */
	public OrderBookItem() {}

	/** Creates an {@link OrderBookItem} based in given Bid
	 * 
	 * @param bid associated with this order book item */
	public OrderBookItem(Bid bid) {
		this.bid = bid;
		if (bid.getEnergyAmountInMWH() < 0.) {
			throw new RuntimeException(ERR_NEGATIVE_POWER + bid.getTraderUuid());
		}
	}

	/** Sets the cumulated power upper value in the context of the containing {@link OrderBook}
	 * 
	 * @param cumulatedPower sum of all previous bids in the merit-order plus the amount of power in this item's {@link Bid} */
	void setCumulatedPowerUpperValue(double cumulatedPower) {
		this.cumulatedPowerUpperValue = cumulatedPower;
	}

	/** Sets the actual awarded power in the context of the containing {@link OrderBook}
	 * 
	 * @param awardedPower of this bid; any value between 0 and {@link #getBlockPower()} */
	void setAwardedPower(double awardedPower) {
		this.awardedPower = awardedPower;
	}

	/** @return actual awarded power; call only after market clearing */
	public double getAwardedPower() {
		return awardedPower;
	}

	/** @return left over power which hasn't been awarded; call only after market clearing */
	public double getNotAwardedPower() {
		return getBlockPower() - awardedPower;
	}

	/** @return sum of all previous bids in the merit-order <b>plus</b> the amount of power in this item's {@link Bid} */
	public double getCumulatedPowerUpperValue() {
		return cumulatedPowerUpperValue;
	}

	/** @return sum of all previous bids in the merit-order <b>without</b> the amount of power in this item's {@link Bid} */
	public double getCumulatedPowerLowerValue() {
		return cumulatedPowerUpperValue - getBlockPower();
	}

	/** @return the amount of power of the associated {@link Bid} */
	public double getBlockPower() {
		return bid.getEnergyAmountInMWH();
	}

	/** @return maximum / minimum offered price associated with this {@link Bid}, depending on its {@link Type} */
	public double getOfferPrice() {
		return bid.getOfferPriceInEURperMWH();
	}

	/** @return actual marginal cost - if provided with the associated {@link Bid} */
	public double getMarginalCost() {
		return bid.getMarginalCost();
	}

	/** @return ID of the traded that issued the associated {@link Bid} */
	public long getTraderUuid() {
		return bid.getTraderUuid();
	}

	@Override
	public String toString() {
		return "Awarded: " + awardedPower + " " + bid;
	}

	/** required for {@link Portable}s */
	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeComponents(bid);
		collector.storeDoubles(cumulatedPowerUpperValue, awardedPower);
	}

	/** required for {@link Portable}s */
	@Override
	public void populate(ComponentProvider provider) {
		bid = provider.nextComponent(Bid.class);
		cumulatedPowerUpperValue = provider.nextDouble();
		awardedPower = provider.nextDouble();
	}
}