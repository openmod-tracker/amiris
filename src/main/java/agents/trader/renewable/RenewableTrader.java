// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader.renewable;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import agents.markets.meritOrder.Bid.Type;
import agents.policy.PolicyItem.SupportInstrument;
import agents.trader.ClientData;
import agents.trader.renewable.bidding.AtMarginalCost;
import agents.trader.renewable.bidding.BiddingStrategy;
import agents.trader.renewable.bidding.ContractForDifferences;
import agents.trader.renewable.bidding.FixedPremium;
import agents.trader.renewable.bidding.PremiumBased;
import agents.trader.renewable.bidding.VariablePremium;
import communications.message.BidData;
import communications.message.MarginalCost;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Markets renewable capacities considering various different support instruments
 * 
 * @author Johannes Kochems, Christoph Schimeczek, Felix Nitsch, Farzad Sarfarazi, Kristina Nienhaus */
public class RenewableTrader extends AggregatorTrader {
	static final String ERR_SUPPORT_INSTRUMENT = " is not configured for support instrument: ";

	public static final Tree parameters = Make.newTree().add(Make.newDouble("ShareOfRevenues"),
			PremiumBased.marketValueForecastParam, PremiumBased.fileForecastParams).buildTree();

	/** The share of market revenues the {@link RenewableTrader} keeps to himself */
	private final double shareOfRevenues;
	private final HashMap<SupportInstrument, BiddingStrategy> strategies = new HashMap<>();

	/** Creates a {@link RenewableTrader}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public RenewableTrader(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		shareOfRevenues = input.getDouble("ShareOfRevenues");
		configureStrategies(input);
	}

	/** configures {@link BiddingStrategy}s for different support instruments */
	private void configureStrategies(ParameterData input) throws MissingDataException {
		strategies.put(SupportInstrument.MPFIX, new FixedPremium(input));
		strategies.put(SupportInstrument.MPVAR, new VariablePremium(input));
		strategies.put(SupportInstrument.CFD, new ContractForDifferences(input));
		strategies.put(SupportInstrument.CP, new AtMarginalCost());
		strategies.put(SupportInstrument.FINANCIAL_CFD, new AtMarginalCost());
	}

	@Override
	protected ArrayList<BidData> submitHourlyBids(TimeStamp targetTime, Contract contract,
			ArrayList<MarginalCost> sortedMarginals) {
		ArrayList<BidData> bids = new ArrayList<>();
		for (MarginalCost marginal : sortedMarginals) {
			BidData bidData = calcBids(marginal, targetTime);
			fulfilNext(contract, bidData);
			bids.add(bidData);
		}
		return bids;
	}

	/** @return {@link BidData bid} for given time according the associated client's support instrument */
	private BidData calcBids(MarginalCost marginal, TimeStamp targetTime) {
		long clientId = marginal.producerUuid;
		ClientData clientData = clientMap.get(clientId);
		BiddingStrategy strategy = getStrategy(clientData);
		double bidPrice = strategy.calcBiddingPrice(marginal.marginalCostInEURperMWH, targetTime, clientData);
		return new BidData(marginal.powerPotentialWithErrorsInMW, bidPrice, marginal.marginalCostInEURperMWH,
				marginal.powerPotentialInMW, getId(), clientId, Type.Supply, targetTime);
	}

	/** @return {@link BiddingStrategy} */
	private BiddingStrategy getStrategy(ClientData clientData) {
		SupportInstrument supportInstrument = clientData.getTechnologySet().supportInstrument;
		BiddingStrategy strategy = strategies.get(supportInstrument);
		if (strategy == null) {
			throw new InvalidParameterException(this + ERR_SUPPORT_INSTRUMENT + supportInstrument);
		}
		return strategy;
	}

	/** Forward the sum of revenues from support and markets to plant operators; may keep a certain share of the overall revenues */
	@Override
	protected double applyPayoutStrategy(long plantOperatorId, TimePeriod accountingPeriod, double marketRevenue) {
		ClientData clientData = clientMap.get(plantOperatorId);
		double supportRevenue = clientData.getSupportRevenueInEUR().get(accountingPeriod);
		return supportRevenue + marketRevenue * (1 - shareOfRevenues);
	}
}
