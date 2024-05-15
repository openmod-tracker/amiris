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
import communications.portable.BidsAtTime;
import agents.markets.meritOrder.books.SupplyOrderBook;
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
	static final String ERR_CLEARING_FAILED = ": Market clearing failed. Energy set to Zero, Price set to NaN. Cause: ";
	static final String WARN_BIDS_MISSING = "MarketClearing:: No Bids contained in message from ";

	/** Defines what market clearing price results in case of shortage */
	enum ShortagePriceMethod {
		/** The value of lost load is used as the market clearing price */
		ValueOfLostLoad,
		/** The last available supply bid determines the market clearing price */
		LastSupplyPrice
	};

	/** Input parameters of {@link MarketClearing} */
	public static final Tree parameters = Make.newTree().add(Make.newEnum("DistributionMethod", DistributionMethod.class),
			Make.newEnum("ShortagePriceMethod", ShortagePriceMethod.class).optional()
					.help("Defines which price to use in case of shortage events (default: ScarcityPrice)"))
			.buildTree();

	/** Defines how to distribute energy amounts between multiple price-setting bids */
	public final DistributionMethod distributionMethod;
	/** Defines which price to use in case of shortage */
	private final ShortagePriceMethod shortagePriceMethod;
	/** Logs errors of {@link MarketClearing} */
	protected static Logger logger = LoggerFactory.getLogger(MarketClearing.class);

	/** Creates a {@link MarketClearing}
	 * 
	 * @param input group holding all parameters
	 * @throws MissingDataException if any required parameters are missing */
	public MarketClearing(ParameterData input) throws MissingDataException {
		this.distributionMethod = input.getEnum("DistributionMethod", DistributionMethod.class);
		this.shortagePriceMethod = input.getEnumOrDefault("ShortagePriceMethod", ShortagePriceMethod.class,
				ShortagePriceMethod.ValueOfLostLoad);
	}

	/** Clears the market based on all the bids provided in form of messages
	 * 
	 * @param input unsorted messages containing demand and supply bids
	 * @param clearingEventId text to specify in what context the market clearing was attempted in case of an error
	 * @return {@link MarketClearingResult result} of market clearing */
	public MarketClearingResult calculateMarketClearing(ArrayList<Message> input, String clearingEventId) {
		DemandOrderBook demandBook = new DemandOrderBook();
		SupplyOrderBook supplyBook = new SupplyOrderBook();
		fillOrderBooksWithTraderBids(input, supplyBook, demandBook);
		ClearingResult clearingResult = calculateClearing(supplyBook, demandBook, clearingEventId);
		MarketClearingResult marketClearingResult = new MarketClearingResult(clearingResult, demandBook, supplyBook);
		marketClearingResult.setBooks(supplyBook, demandBook, distributionMethod);
		if (hasScarcity(supplyBook, demandBook)) {
			updateResultForScarcity(marketClearingResult, supplyBook);
		}
		return marketClearingResult;
	}

	/** Clears the market based on a SupplyOrderBook and a DemandOrderBook
	 * 
	 * @param supplyBook book of all supply bids
	 * @param demandBook book of all demand bids
	 * @param clearingEventId string identifying the calling agent
	 * @return {@link MarketClearingResult result} of market clearing */
	public MarketClearingResult calculateMarketClearing(SupplyOrderBook supplyBook, DemandOrderBook demandBook,
			String clearingEventId) {
		ClearingResult clearingResult = calculateClearing(supplyBook, demandBook, clearingEventId);
		MarketClearingResult marketClearingResult = new MarketClearingResult(clearingResult, demandBook, supplyBook);
		marketClearingResult.setBooks(supplyBook, demandBook, distributionMethod);
		if (hasScarcity(supplyBook, demandBook)) {
			updateResultForScarcity(marketClearingResult, supplyBook);
		}
		return marketClearingResult;
	}

	/** Computes the ClearingResult of the specified SupplyOrderBook and DemandOrderBook.
	 * 
	 * @param supplyBook book of all supply bids
	 * @param demandBook book of all demand bids
	 * @param clearingEventId string identifying the calling agent
	 * @return the ClearingResult of the specified SupplyOrderBook and DemandOrderBook */
	public static ClearingResult calculateClearing(SupplyOrderBook supplyBook, DemandOrderBook demandBook,
			String clearingEventId) {
		ClearingResult clearingResult;
		try {
			clearingResult = MeritOrderKernel.clearMarketSimple(supplyBook, demandBook);
		} catch (MeritOrderClearingException e) {
			clearingResult = new ClearingResult(0.0, Double.NaN);
			logger.error(clearingEventId + ERR_CLEARING_FAILED + e.getMessage());
		}
		return clearingResult;
	}

	/** Fills received Bids into provided demand or supply OrderBook
	 * 
	 * @param input unsorted messages containing demand and supply bids
	 * @param supplyBook to be filled with supply bids
	 * @param demandBook to be filled with demand bids */
	public void fillOrderBooksWithTraderBids(ArrayList<Message> input, SupplyOrderBook supplyBook,
			DemandOrderBook demandBook) {
		demandBook.clear();
		supplyBook.clear();
		for (Message message : input) {
			BidsAtTime bids = message.getFirstPortableItemOfType(BidsAtTime.class);
			if (bids == null) {
				logger.warn(WARN_BIDS_MISSING + message.getSenderId());
			} else {
				supplyBook.addBids(bids.getSupplyBids(), bids.getTraderUuid());
				demandBook.addBids(bids.getDemandBids(), bids.getTraderUuid());
			}
		}
	}

	/** @return true if scarcity occurred according to the cleared {@link OrderBook}s */
	private boolean hasScarcity(SupplyOrderBook supplyBook, DemandOrderBook demandBook) {
		return demandBook.getAmountOfPowerShortage(supplyBook.getHighestItem()) > 0;
	}

	/** Update given {@link MarketClearingResult} scarcity price - depending on the parameterised {@link ShortagePriceMethod}
	 * method */
	private void updateResultForScarcity(MarketClearingResult result, SupplyOrderBook supplyBook) {
		switch (shortagePriceMethod) {
			case LastSupplyPrice:
				result.setMarketPriceInEURperMWH(supplyBook.getHighestItem().getOfferPrice());
				break;
			case ValueOfLostLoad:
				break;
			default:
				throw new RuntimeException(ERR_SHORTAGE_NOT_IMPLEMENTED + shortagePriceMethod);
		}
	}
}
