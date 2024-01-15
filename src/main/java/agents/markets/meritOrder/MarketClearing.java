// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.markets.meritOrder;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import agents.markets.meritOrder.MeritOrderKernel.MeritOrderClearingException;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.OrderBook;
import agents.markets.meritOrder.books.OrderBook.DistributionMethod;
import agents.markets.meritOrder.books.SupplyOrderBook;
import communications.message.BidData;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.message.Message;

/** Performs market clearing of day-ahead market based on provided Bid-messages
 *
 * @author Farzad Sarfarazi, Christoph Schimeczek */
public class MarketClearing {
	static final String ERR_SHORTAGE_NOT_IMPLEMENTED = "ShortagePrice type not implemented: ";

	public enum ShortagePrice {
		ScarcityPrice, LastSupplyPrice
	};

	public static final Tree parameters = Make.newTree().add(Make.newEnum("DistributionMethod", DistributionMethod.class),
			Make.newEnum("ShortagePrice", ShortagePrice.class).optional()
					.help("Defines which price to use in case of shortage events (default: ScarcityPrice)"))
			.buildTree();

	private final DistributionMethod distributionMethod;
	/** Defines which price to use in case of shortage */
	private final ShortagePrice shortagePrice;
	protected static Logger logger = LoggerFactory.getLogger(MarketClearing.class);

	/** Creates a {@link MarketClearing}
	 * 
	 * @param input group holding all parameters
	 * @throws MissingDataException if any required parameters are missing */
	public MarketClearing(ParameterData input) throws MissingDataException {
		this.distributionMethod = input.getEnum("DistributionMethod", DistributionMethod.class);
		this.shortagePrice = input.getEnumOrDefault("ShortagePrice", ShortagePrice.class, ShortagePrice.ScarcityPrice);
	}

	/** Clears the market based on all the bids provided in form of messages
	 * 
	 * @param input unsorted messages containing demand and supply bids
	 * @return {@link MarketClearingResult result} of market clearing */
	public MarketClearingResult calculateMarketClearing(ArrayList<Message> input, String clearingEventId) {
		DemandOrderBook demandBook = new DemandOrderBook();
		SupplyOrderBook supplyBook = new SupplyOrderBook();
		fillOrderBooksWithTraderBids(input, supplyBook, demandBook);
		MarketClearingResult result;
		try {
			result = MeritOrderKernel.clearMarketSimple(supplyBook, demandBook);
		} catch (MeritOrderClearingException e) {
			result = new MarketClearingResult(0.0, Double.NaN);
			logger.error(clearingEventId + ": Market clearing failed due to: " + e.getMessage());
		}
		result.setBooks(supplyBook, demandBook, distributionMethod);
		if (hasScarcity(supplyBook, demandBook)) {
			updateResultForScarcity(result, supplyBook);
		}
		return result;
	}

	/** Fills received Bids into provided demand or supply OrderBook
	 * 
	 * @param input unsorted messages containing demand and supply bids
	 * @param supplyBook to be filled with supply bids
	 * @param demandBook to be filled with demand bids */
	private void fillOrderBooksWithTraderBids(ArrayList<Message> input, SupplyOrderBook supplyBook,
			DemandOrderBook demandBook) {
		demandBook.clear();
		supplyBook.clear();
		for (Message message : input) {
			BidData bidData = message.getDataItemOfType(BidData.class);
			if (bidData == null) {
				throw new RuntimeException("No BidData in message from " + message.getSenderId());
			}
			Bid bid = bidData.getBid();
			switch (bid.getType()) {
				case Supply:
					supplyBook.addBid(bid);
					break;
				case Demand:
					demandBook.addBid(bid);
					break;
				default:
					throw new RuntimeException("Bid type unknown.");
			}
		}
	}

	/** @return true if scarcity occurred according to the cleared {@link OrderBook}s */
	private boolean hasScarcity(SupplyOrderBook supplyBook, DemandOrderBook demandBook) {
		return demandBook.getAmountOfPowerShortage(supplyBook.getHighestItem()) > 0;
	}

	/** Update given {@link MarketClearingResult} scarcity price - depending on the parameterised {@link ShortagePrice} method */
	private void updateResultForScarcity(MarketClearingResult result, SupplyOrderBook supplyBook) {
		switch (shortagePrice) {
			case LastSupplyPrice:
				result.setMarketPriceInEURperMWH(supplyBook.getHighestItem().getOfferPrice());
				break;
			case ScarcityPrice:
				break;
			default:
				throw new RuntimeException(ERR_SHORTAGE_NOT_IMPLEMENTED + shortagePrice);
		}
	}
}
