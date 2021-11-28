package agents.markets.meritOrder.sensitivities;

import java.util.Comparator;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.SupplyOrderBook;

/** Reflects the sensitivity of the merit order price to bid changes
 * 
 * @author Christoph Schimeczek */
public class PriceSensitivity extends MeritOrderSensitivity {
	@Override
	protected void selectOrderBookItems(SupplyOrderBook supplyBookForecast, DemandOrderBook demandBookForecast) {
		extractOrders(supplyBookForecast);
		extractOrders(demandBookForecast);
	}

	@Override
	protected Comparator<SensitivityItem> getComparator() {
		return SensitivityItem.BY_PRICE;
	}

	@Override
	protected double calcMonetaryValue(SensitivityItem item) {
		return Double.NaN;
	}

	@Override
	protected double calcValueOfItemAtPower(SensitivityItem item, double power) {
		return item.getPrice();
	}

	/** @return the price at a given power demand level induced by the additional power demand */
	public double calcPriceAtPowerDemand(double powerDemandInMW) {
		int chargingIndex = 0;
		while (chargingItems.get(chargingIndex).getCumulatedUpperPower() < powerDemandInMW) {
			chargingIndex++;
			if (chargingIndex >= chargingItems.size()) {
				throw new RuntimeException("Sensitivity: No price could be found for power demand.");
			}
		}
		return chargingItems.get(chargingIndex).getPrice();
	}

	/** @return price without any (dis-)charging activity in €/MWh */
	public double getPriceWithoutCharging() {
		return chargingItems.get(0).getPrice();
	}

	@Override
	public void updatePriceForecast(double electricityPriceForecast) {
		throw new RuntimeException("PriceSensitivity is incompatible to direct electricity price forecasts");
	}
}