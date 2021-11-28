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

/** Energy exchange performs market clearing for day-ahead energy market
 * 
 * @author Christoph Schimeczek, Johannes Kochems */
public class EnergyExchange extends Agent {
	@Product
	public static enum Products {
		/** Awarded energy and price per bidding trader */
		Awards
	};

	@Output
	private static enum OutputFields {
		TotalAwardedPowerInMW, ElectricityPriceInEURperMWH, DispatchSystemCostInEUR
	};

	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newEnum("DistributionMethod", DistributionMethod.class)).buildTree();

	private DemandOrderBook demandBook = new DemandOrderBook();
	private SupplyOrderBook supplyBook = new SupplyOrderBook();
	private final MarketClearing marketClearing;

	/** Creates an {@link EnergyExchange}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public EnergyExchange(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		marketClearing = new MarketClearing(input.getEnum("DistributionMethod", DistributionMethod.class));

		/** Clear market by using incoming bids and sending Awards */
		call(this::clearMarket).on(Products.Awards).use(Trader.Products.Bids);
	}

	/** Clears the market based on all the bids provided; writes out some market-clearing data
	 * 
	 * @param input supply and demand bids
	 * @param contracts with anyone who wants to receive information about the market clearing outcome */
	private void clearMarket(ArrayList<Message> input, List<Contract> contracts) {
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
			AwardData awardData = new AwardData(awardedSupplyPower, awardedDemandPower, powerPrice, now());
			fulfilNext(contract, awardData);
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
