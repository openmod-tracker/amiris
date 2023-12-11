// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.markets;

import java.util.ArrayList;
import java.util.List;
import agents.markets.meritOrder.MarketClearing;
import agents.markets.meritOrder.MarketClearingResult;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.OrderBook;
import agents.markets.meritOrder.books.OrderBook.DistributionMethod;
import agents.markets.meritOrder.books.OrderBookItem;
import agents.markets.meritOrder.books.SupplyOrderBook;
import agents.trader.Trader;
import communications.message.AwardData;
import communications.message.ClearingTimes;
import de.dlr.gitlab.fame.agent.Agent;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Performs market clearing for a single day-ahead energy market zone
 * 
 * @author Christoph Schimeczek, Johannes Kochems */
public class DayAheadMarketSingleZone extends Agent {
	static final String LONE_LIST = "At most one element is expected in this list: ";
	
	@Product
	public static enum Products {
		/** Awarded energy and price per bidding trader */
		Awards,
		/** Information on when the market clearing is performed */
		GateClosureInfo
	};

	@Output
	private static enum OutputFields {
		TotalAwardedPowerInMW, ElectricityPriceInEURperMWH, DispatchSystemCostInEUR
	};

	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newEnum("DistributionMethod", DistributionMethod.class),
					 Make.newInt("GateClosureInfoOffsetInSeconds")).buildTree();

	private DemandOrderBook demandBook = new DemandOrderBook();
	private SupplyOrderBook supplyBook = new SupplyOrderBook();
	private final MarketClearing marketClearing;
	private final TimeSpan gateClosureInfoOffset;
	private ClearingTimes clearingTimes;

	/** Creates an {@link DayAheadMarketSingleZone}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public DayAheadMarketSingleZone(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		marketClearing = new MarketClearing(input.getEnum("DistributionMethod", DistributionMethod.class));
		gateClosureInfoOffset = new TimeSpan(input.getInteger("GateClosureInfoOffsetInSeconds"));

		/**Sends out ClearingTimes*/
		call(this::sendGateClosureInfo).on(Products.GateClosureInfo);
		/** Clears market by using incoming bids and sending Awards */
		call(this::clearMarket).on(Products.Awards).use(Trader.Products.Bids);
	}
	
	/** Sends info upon next gate closure to connected traders
	 * 
	 * @param input n/a
	 * @param contracts connected traders to inform */
	private void sendGateClosureInfo(ArrayList<Message> input, List<Contract> contracts) {
		clearingTimes = new ClearingTimes(now().laterBy(gateClosureInfoOffset));
		for (Contract contract : contracts) {
			fulfilNext(contract, clearingTimes);
		}
	}

	/** Clears the market based on all the bids provided; writes out some market-clearing data
	 * 
	 * @param input supply and demand bids
	 * @param contracts with anyone who wants to receive information about the market clearing outcome */
	protected void clearMarket(ArrayList<Message> input, List<Contract> contracts) {
		MarketClearingResult result = marketClearing.calculateMarketClearing(input);
		demandBook = result.getDemandBook();
		supplyBook = result.getSupplyBook();
		double powerPrice = result.getMarketPriceInEURperMWH();
		sendAwardsToTraders(contracts, powerPrice);

		store(OutputFields.ElectricityPriceInEURperMWH, powerPrice);
		store(OutputFields.TotalAwardedPowerInMW, result.getTradedEnergyInMWH());
		store(OutputFields.DispatchSystemCostInEUR, result.getSystemCostTotalInEUR());
	}

	/** For each given Contract, account for awarded power from associated incoming bids (if any) and respond with an Award message
	 * 
	 * @param contracts list of partners; anyone (bidder or not) that wants to receive an Award message
	 * @param powerPrice the final uniform electricity price */
	private void sendAwardsToTraders(List<Contract> contracts, double powerPrice) {
		for (Contract contract : contracts) {
			long receiverId = contract.getReceiverId();
			double awardedSupplyPower = calcAwardedEnergyForAgent(supplyBook, receiverId);
			double awardedDemandPower = calcAwardedEnergyForAgent(demandBook, receiverId);
			List<TimeStamp> clearingTimeList = clearingTimes.getTimes();
			if (clearingTimeList.size() > 1) {
				throw new RuntimeException(LONE_LIST + clearingTimeList);
			}
			for (TimeStamp clearingTime : clearingTimeList) {
				AwardData awardData = new AwardData(awardedSupplyPower, awardedDemandPower, powerPrice, clearingTime);
				fulfilNext(contract, awardData);
			}
		}
	}

	/** @return sum of awarded energy from all bids in given order book from the agent with given UUID */
	private double calcAwardedEnergyForAgent(OrderBook orderBook, long agentUuid) {
		double awardedEnergySum = 0;
		for (OrderBookItem item : orderBook.getOrderBookItems()) {
			if (item.getTraderUuid() == agentUuid) {
				awardedEnergySum += item.getAwardedPower();
			}
		}
		return awardedEnergySum;
	}
}
