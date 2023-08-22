// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader.renewable;

import java.util.ArrayList;
import agents.markets.EnergyExchange;
import agents.markets.meritOrder.Bid.Type;
import communications.message.BidData;
import communications.message.MarginalCost;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Offers energy at {@link EnergyExchange} according to given {@link TimeSeries} of renewable power plants and thereby obtaining
 * no support payments
 *
 * @author Johannes Kochems */
public class NoSupportTrader extends AggregatorTrader {
	public static final Tree parameters = Make.newTree().add(Make.newDouble("ShareOfRevenues")).buildTree();

	/** Share of market revenues the NoSupportTrader keeps to himself */
	private final double shareOfRevenues;

	/** Creates a {@link NoSupportTrader}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public NoSupportTrader(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		shareOfRevenues = input.getDouble("ShareOfRevenues");
	}

	/** Send {@link BidData bids} at marginal costs since no support payment is expected */
	@Override
	protected ArrayList<BidData> submitHourlyBids(TimeStamp targetTime, Contract contract,
			ArrayList<MarginalCost> marginals) {
		ArrayList<BidData> bids = new ArrayList<>();
		for (MarginalCost marginal : marginals) {
			BidData bidData = new BidData(marginal.powerPotentialWithErrorsInMW, marginal.marginalCostInEURperMWH,
					marginal.marginalCostInEURperMWH, marginal.powerPotentialInMW, getId(), marginal.producerUuid, Type.Supply,
					targetTime);
			fulfilNext(contract, bidData);
			bids.add(bidData);
		}
		return bids;
	}

	/** Pass through only the market revenues since there is no support payment */
	@Override
	protected double applyPayoutStrategy(long plantOperatorId, TimePeriod accountingPeriod, double marketRevenue) {
		return marketRevenue * (1 - shareOfRevenues);
	}
}
