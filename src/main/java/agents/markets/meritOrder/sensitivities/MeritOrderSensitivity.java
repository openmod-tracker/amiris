package agents.markets.meritOrder.sensitivities;

import java.util.ArrayList;
import java.util.Comparator;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.OrderBook;
import agents.markets.meritOrder.books.OrderBookItem;
import agents.markets.meritOrder.books.SupplyOrderBook;

/** Represents changes of a merit-order derived value (e.g. electricity price or system cost) when the awarded power for supply or
 * demand are changed
 *
 * @author Christoph Schimeczek */
public abstract class MeritOrderSensitivity {
	protected double externalChargingPowerInMW;
	protected double externalDischargingPowerInMW;
	protected ArrayList<SensitivityItem> chargingItems = new ArrayList<>();
	protected ArrayList<SensitivityItem> dischargingItems = new ArrayList<>();

	/** Sets maximum charging and discharging powers in MW according to specified values */
	public final void updatePowers(double maxChargePowerInMW, double maxDischargePowerInMW) {
		this.externalChargingPowerInMW = maxChargePowerInMW;
		this.externalDischargingPowerInMW = maxDischargePowerInMW;
	}

	/** updates sensitivities from given order books */
	public final void updateSensitivities(SupplyOrderBook supplyBookForecast, DemandOrderBook demandBookForecast) {
		clear();
		selectOrderBookItems(supplyBookForecast, demandBookForecast);
		// addItemForPowerShortage(supplyBookForecast, demandBookForecast);
		chargingItems.sort(getComparator());
		dischargingItems.sort(getComparator().reversed());
		setCumulativeValues(chargingItems);
		setCumulativeValues(dischargingItems);
		chargingItems.removeIf(i -> i.getCumulatedLowerPower() > externalChargingPowerInMW);
		dischargingItems.removeIf(i -> i.getCumulatedLowerPower() > externalDischargingPowerInMW);
	}

	/** clears stored sensitivity data */
	private void clear() {
		chargingItems.clear();
		dischargingItems.clear();
	}

	/** pick from given supply and / or demand {@link OrderBook}s - and add picked one(s) to this sensitivity */
	protected abstract void selectOrderBookItems(SupplyOrderBook supplyBookForecast, DemandOrderBook demandBookForecast);

	/** Returns true if the given item as a positive block power
	 * 
	 * @param item to inspect
	 * @return true if block power of given {@link OrderBookItem} is larger than Zero */
	protected final boolean hasPositiveBlockPower(OrderBookItem item) {
		return item.getBlockPower() > 0;
	}

	/** Adds entries of given {@link OrderBook} (depending on its type) to either charging or discharging sensitivity */
	protected void extractOrders(OrderBook book) {
		boolean isSupplyBook = book instanceof SupplyOrderBook;
		for (OrderBookItem item : book.getOrderBookItems()) {
			if (!hasPositiveBlockPower(item)) {
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
			monetaryValueOffset += calcMonetaryValue(item);
		}
	}

	/** @return monetary value of this {@link SensitivityItem} according to this Sensitivity type */
	protected abstract double calcMonetaryValue(SensitivityItem item);

	/** @return values of Sensitivity in (2 * numberOfTransitionSteps + 1) steps, equally dividing max charging and discharging
	 *         powers; first entry corresponds to maximum discharging power, while the last entry resembles sensitivity value at
	 *         maximum charging power */
	public double[] getValuesInSteps(int numberOfTransitionSteps) {
		double[] values = new double[2 * numberOfTransitionSteps + 1];

		double chargingPowerPerStep = externalChargingPowerInMW / numberOfTransitionSteps;
		int maxChargingIndex = chargingItems.size();
		int chargingIndex = 0;
		values[numberOfTransitionSteps] = 0.0;
		for (int step = 1; step <= numberOfTransitionSteps; step++) {
			double power = chargingPowerPerStep * step;
			while (chargingIndex < maxChargingIndex && chargingItems.get(chargingIndex).getCumulatedUpperPower() < power) {
				chargingIndex++;
			}
			int indexInArray = numberOfTransitionSteps + step;
			if (chargingIndex < maxChargingIndex) {
				values[indexInArray] = calcValueOfItemAtPower(chargingItems.get(chargingIndex), power);
			} else {
				values[indexInArray] = Double.NaN;
			}
		}

		double dischargingPowerPerStep = externalDischargingPowerInMW / numberOfTransitionSteps;
		int dischargingIndex = 0;
		int maxDischargingIndex = dischargingItems.size();
		for (int step = 1; step <= numberOfTransitionSteps; step++) {
			double power = dischargingPowerPerStep * step;
			while (dischargingIndex < maxDischargingIndex
					&& dischargingItems.get(dischargingIndex).getCumulatedUpperPower() < power) {
				dischargingIndex++;
			}
			int indexInArray = numberOfTransitionSteps - step;
			if (dischargingIndex < maxDischargingIndex) {
				values[indexInArray] = calcValueOfItemAtPower(dischargingItems.get(dischargingIndex), -power);
			} else {
				values[indexInArray] = Double.NaN;
			}
		}
		return values;
	}

	/** @return {@link StepPower} corresponding to step values returned by {@link #getValuesInSteps(int)} */
	public final StepPower getStepPowers(int numberOfTransitionSteps) {
		return new StepPower(externalChargingPowerInMW, externalDischargingPowerInMW, numberOfTransitionSteps);
	}

	/** @return value of given {@link SensitivityItem} at specified power according to this Sensitivity type;<br />
	 *         when power > 0: <b>charging</b>, otherwise <b>discharging</b> */
	protected abstract double calcValueOfItemAtPower(SensitivityItem item, double power);

	public abstract void updatePriceForecast(double electricityPriceForecast);

	/** @return true if sensitivities have not been set yet */
	public boolean isEmpty() {
		return chargingItems.isEmpty() && dischargingItems.isEmpty();
	}
}