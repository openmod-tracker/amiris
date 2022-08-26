// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.TreeMap;
import agents.markets.meritOrder.Bid.Type;
import agents.policy.MpfixInfo;
import agents.policy.SupportPolicy.SupportInstrument;
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
	static final String ERR_SUPPORT_INSTRUMENT = " does not feature: ";

	public static final Tree parameters = Make.newTree().add(Make.newDouble("ShareOfRevenues")).buildTree();

	/** The share of market revenues the {@link RenewableTrader} keeps to himself */
	private final double shareOfRevenues;

	/** Creates a {@link RenewableTrader}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public RenewableTrader(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		shareOfRevenues = input.getDouble("ShareOfRevenues");
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

	/** Returns calculated {@link BidData bid} at given time according the associated client's support instrument
	 * <ul>
	 * <li>MPFIX, MPVAR or CFD: price = marginal costs - market premium; ignore negative premia</li>
	 * <li>CAPACITY_PREMIUM: price equal to marginal costs</li>
	 * </ul>
	 */
	private BidData calcBids(MarginalCost marginal, TimeStamp targetTime) {
		long clientId = marginal.producerUuid;
		SupportInstrument supportInstrument = clientMap.get(clientId).getTechnologySet().supportInstrument;
		double bidPrice;
		switch (supportInstrument) {
			case MPVAR:
			case CFD:
				double marketPremiumPreviousMonth = calcExpectedMarketPremium(clientId, targetTime);
				bidPrice = marginal.marginalCostInEURperMWH - Math.max(0, marketPremiumPreviousMonth);
				break;
			case MPFIX:
				MpfixInfo mpFixInfo = clientMap.get(clientId).getSupportInfo().getPolicyInfoOfType(MpfixInfo.class);
				double marketPremiumThisMonth = mpFixInfo.getPremium().getValueLowerEqual(targetTime);
				bidPrice = marginal.marginalCostInEURperMWH - marketPremiumThisMonth;
				break;
			case CP:
				bidPrice = marginal.marginalCostInEURperMWH;
				break;
			default:
				throw new InvalidParameterException(this + ERR_SUPPORT_INSTRUMENT + supportInstrument);
		}
		double powerOffered = marginal.powerPotentialWithErrorsInMW;
		return new BidData(powerOffered, bidPrice, marginal.marginalCostInEURperMWH, marginal.powerPotentialInMW, getId(),
				clientId, Type.Supply, targetTime);
	}

	/** Return the expected market premium by using the value of the previous month
	 * 
	 * @param clientId used to search for client-specific market premium
	 * @param targetTime current time of marketing
	 * @return market premium from previous month; 0: if premium was not yet logged defined */
	private double calcExpectedMarketPremium(long clientId, TimeStamp targetTime) {
		TreeMap<TimePeriod, Double> marketPremium = clientMap.get(clientId).getMarketPremiaInEURperMWH();
		if (!marketPremium.isEmpty()) {
			TimePeriod timePeriod = new TimePeriod(targetTime, marketPremium.firstKey().getDuration());
			return marketPremium.get(marketPremium.floorKey(timePeriod));
		} else {
			return 0;
		}
	}

	/** Forward the sum of revenues from support and markets to plant operators; may keep a certain share of the overall revenues */
	@Override
	protected double applyPayoutStrategy(long plantOperatorId, TimePeriod accountingPeriod, double marketRevenue) {
		ClientData clientData = clientMap.get(plantOperatorId);
		double supportRevenue = clientData.getSupportRevenueInEUR().get(accountingPeriod);
		return supportRevenue + marketRevenue * (1 - shareOfRevenues);
	}
}
