// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader.renewable;

import java.util.ArrayList;
import agents.markets.DayAheadMarket;
import agents.markets.meritOrder.Bid.Type;
import agents.markets.meritOrder.Constants;
import communications.message.BidData;
import communications.message.MarginalCost;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Offers energy at {@link DayAheadMarket} of renewable power plants using a feed-in tariff support scheme
 *
 * @author Christoph Schimeczek, Ulrich Frey, Marc Deissenroth */
public class SystemOperatorTrader extends AggregatorTrader {

	public SystemOperatorTrader(DataProvider dataProvider) {
		super(dataProvider);
	}

	/** Send price-independent {@link BidData bids} to {@link DayAheadMarket} for marketing RES in FIT scheme */
	@Override
	protected ArrayList<BidData> submitHourlyBids(TimeStamp targetTime, Contract contract,
			ArrayList<MarginalCost> sortedMarginals) {
		ArrayList<BidData> bids = new ArrayList<>();
		for (MarginalCost marginal : sortedMarginals) {
			BidData bidData = new BidData(marginal.powerPotentialWithErrorsInMW, Constants.MINIMAL_PRICE_IN_EUR_PER_MWH,
					marginal.marginalCostInEURperMWH, marginal.powerPotentialInMW, getId(), marginal.producerUuid, Type.Supply,
					targetTime);
			fulfilNext(contract, bidData);
			bids.add(bidData);
		}
		return bids;
	}

	/** Pass through only the support pay-out in a FIT scheme */
	@Override
	protected double applyPayoutStrategy(long plantOperatorId, TimePeriod accountingPeriod, double marketRevenue) {
		return clientMap.get(plantOperatorId).getSupportRevenueInEUR().getOrDefault(accountingPeriod, 0.0);
	}
}
