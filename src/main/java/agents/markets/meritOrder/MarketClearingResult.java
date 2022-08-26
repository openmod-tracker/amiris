// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.markets.meritOrder;

import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.OrderBookItem;
import agents.markets.meritOrder.books.SupplyOrderBook;
import agents.markets.meritOrder.books.OrderBook.DistributionMethod;

/** Holds clearing price, sold energy, and updated {@link DemandOrderBook} and {@link SupplyOrderBook}
 *
 * @author Farzad Sarfarazi, Christoph Schimeczek */
public class MarketClearingResult {
	private final double tradedEnergyInMWH;
	private final double marketPriceInEURperMWH;
	private DemandOrderBook demandBook;
	private SupplyOrderBook supplyBook;

	/** Instantiate with price and awarded energy; books are set separately
	 * 
	 * @param tradedEnergyInMWH total awarded energy
	 * @param marketPriceInEURperMWH uniform market clearing price */
	public MarketClearingResult(double tradedEnergyInMWH, double marketPriceInEURperMWH) {
		this.tradedEnergyInMWH = tradedEnergyInMWH;
		this.marketPriceInEURperMWH = marketPriceInEURperMWH;
	}

	/** Set and update books, i.e. award contained bids according to their individual results
	 * 
	 * @param supplyBook Supply book used to clear the market
	 * @param demandBook Demand book used to clear the market
	 * @param distributionMethod defines method of how to award energy when multiple price-setting bids occur */
	void setBooks(SupplyOrderBook supplyBook, DemandOrderBook demandBook, DistributionMethod distributionMethod) {
		this.demandBook = demandBook;
		this.supplyBook = supplyBook;
		updateBooks(distributionMethod);
	}

	/** update books, i.e. award contained bids according to their individual results using the provided distribution method */
	private void updateBooks(DistributionMethod distributionMethod) {
		supplyBook.updateAwardedPowerInBids(tradedEnergyInMWH, marketPriceInEURperMWH, distributionMethod);
		demandBook.updateAwardedPowerInBids(tradedEnergyInMWH, marketPriceInEURperMWH, distributionMethod);
	}

	/** @return updated demand order book used to clear the market */
	public DemandOrderBook getDemandBook() {
		return demandBook;
	}

	/** @return updated supply order book used to clear the market */
	public SupplyOrderBook getSupplyBook() {
		return supplyBook;
	}

	/** @return total awarded energy */
	public double getTradedEnergyInMWH() {
		return tradedEnergyInMWH;
	}

	/** @return uniform market clearing price */
	public double getMarketPriceInEURperMWH() {
		return marketPriceInEURperMWH;
	}

	/** @return total system cost from generation based on awarded bids and their associated marginal cost */
	public double getSystemCostTotalInEUR() {
		double totalSystemCost = 0;
		for (OrderBookItem item : supplyBook.getOrderBookItems()) {
			double awardedPower = item.getAwardedPower();
			double marginalCost = item.getMarginalCost();
			if (Double.isNaN(awardedPower) || Double.isNaN(marginalCost)) {
				continue;
			}
			totalSystemCost += awardedPower * marginalCost;
		}
		return totalSystemCost;
	}
}