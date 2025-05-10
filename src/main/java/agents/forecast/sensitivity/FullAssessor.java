// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import java.util.ArrayList;
import java.util.Comparator;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.OrderBook;
import agents.markets.meritOrder.books.OrderBookItem;
import agents.markets.meritOrder.books.SupplyOrderBook;
import agents.markets.meritOrder.sensitivities.SensitivityItem;

/** Base class for full merit order assessment; actual type of sensitivity assessed depends on the child class.
 * 
 * @author Johannes Kochems, Christoph Schimeczek */
public abstract class FullAssessor implements MeritOrderAssessment {
	/** list of changes (in terms of cumulated power and price) in the merit order for all possible charging events of the
	 * associated flexibility device */
	protected ArrayList<SensitivityItem> chargingItems = new ArrayList<>();
	/** list of changes (in terms of cumulated power and price) in the merit order for all possible discharging events of the
	 * associated flexibility device */
	protected ArrayList<SensitivityItem> dischargingItems = new ArrayList<>();

	@Override
	public final void assess(SupplyOrderBook supplyBook, DemandOrderBook demandBook) {
		extractOrders(supplyBook);
		extractOrders(demandBook);
		chargingItems.sort(getComparator());
		dischargingItems.sort(getComparator().reversed());
		setCumulativeValues(chargingItems);
		setCumulativeValues(dischargingItems);
	}

	/** Adds entries of given {@link OrderBook} (depending on its type) to either charging or discharging sensitivity
	 * 
	 * @param book to be read out */
	protected void extractOrders(OrderBook book) {
		boolean isSupplyBook = book instanceof SupplyOrderBook;
		for (OrderBookItem item : book.getOrderBookItems()) {
			if (item.getBlockPower() <= 0) {
				continue;
			}
			if (isSupplyBook) {
				addSupplyItem(item);
			} else {
				addDemandItem(item);
			}
		}
	}

	/** adds notAwardedPower to {@link #chargingItems} and awardedPower to {@link #dischargingItems} */
	private void addSupplyItem(OrderBookItem item) {
		double notAwardedPower = item.getNotAwardedPower();
		double awardedPower = item.getAwardedPower();
		if (notAwardedPower > 0) {
			chargingItems.add(new SensitivityItem(notAwardedPower, item.getOfferPrice(), item.getMarginalCost()));
		}
		if (awardedPower > 0) {
			dischargingItems.add(new SensitivityItem(awardedPower, item.getOfferPrice(), item.getMarginalCost()));
		}
	}

	/** adds awardedPower to {@link #chargingItems} and notAwardedPower to {@link #dischargingItems} */
	private void addDemandItem(OrderBookItem item) {
		double notAwardedPower = item.getNotAwardedPower();
		double awardedPower = item.getAwardedPower();
		if (notAwardedPower > 0) {
			dischargingItems.add(new SensitivityItem(notAwardedPower, item.getOfferPrice(), item.getMarginalCost()));
		}
		if (awardedPower > 0) {
			chargingItems.add(new SensitivityItem(awardedPower, item.getOfferPrice(), item.getMarginalCost()));
		}
	}

	/** @return {@link Comparator} for {@link SensitivityItem}s to be used by this Sensitivity type */
	protected abstract Comparator<SensitivityItem> getComparator();

	/** sets cumulative power and monetary value of given sorted {@link SensitivityItem}s */
	private void setCumulativeValues(ArrayList<SensitivityItem> items) {
		double cumulatedPower = 0;
		double monetaryValueOffset = 0;
		for (SensitivityItem item : items) {
			item.setCumulatedLowerPower(cumulatedPower);
			item.setMonetaryOffset(monetaryValueOffset);
			cumulatedPower += item.getPower();
			item.setUpperMonetaryValue(calcMonetaryValue(item));
			monetaryValueOffset += item.getUpperMonetaryValue();
		}
	}

	/** Calculates monetary value of given item
	 * 
	 * @param item to assess
	 * @return monetary value of this {@link SensitivityItem} according to this Sensitivity type */
	protected abstract double calcMonetaryValue(SensitivityItem item);

	@Override
	public double[] getDemandSensitivityPowers() {
		double[] powers = new double[chargingItems.size() + 1];
		powers[0] = 0;
		for (int i = 1; i <= powers.length; i++) {
			powers[i] = chargingItems.get(i - 1).getCumulatedUpperPower();
		}
		return powers;
	}

	@Override
	public double[] getDemandSensitivityValues() {
		double[] values = new double[chargingItems.size() + 1];
		values[0] = 0;
		for (int i = 1; i <= values.length; i++) {
			values[i] = chargingItems.get(i - 1).getUpperMonetaryValue();
		}
		return values;
	}

	@Override
	public double[] getSupplySensitivityPowers() {
		double[] powers = new double[dischargingItems.size() + 1];
		powers[0] = 0;
		for (int i = 1; i <= powers.length; i++) {
			powers[i] = dischargingItems.get(i - 1).getCumulatedUpperPower();
		}
		return powers;
	}

	@Override
	public double[] getSupplySensitivityValues() {
		double[] values = new double[dischargingItems.size() + 1];
		values[0] = 0;
		for (int i = 1; i <= values.length; i++) {
			values[i] = dischargingItems.get(i - 1).getUpperMonetaryValue();
		}
		return values;
	}
}
