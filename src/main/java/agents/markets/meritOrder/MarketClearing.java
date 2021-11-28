package agents.markets.meritOrder;

import java.util.ArrayList;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.OrderBook.DistributionMethod;
import agents.markets.meritOrder.books.SupplyOrderBook;
import communications.message.BidData;
import de.dlr.gitlab.fame.communication.message.Message;

/** Performs market clearing of day-ahead market based on provided Bid-messages
 *
 * @author Farzad Sarfarazi, Christoph Schimeczek */
public class MarketClearing {
	private final DistributionMethod distributionMethod;

	/** Creates a {@link MarketClearing}
	 * 
	 * @param distributionMethod defines method of how to award energy when multiple price-setting bids occur */
	public MarketClearing(DistributionMethod distributionMethod) {
		this.distributionMethod = distributionMethod;
	}

	/** Clears the market based on all the bids provided in form of messages
	 * 
	 * @param input unsorted messages containing demand and supply bids
	 * @return {@link MarketClearingResult result} of market clearing */
	public MarketClearingResult calculateMarketClearing(ArrayList<Message> input) {
		DemandOrderBook demandBook = new DemandOrderBook();
		SupplyOrderBook supplyBook = new SupplyOrderBook();
		fillOrderBooksWithTraderBids(input, supplyBook, demandBook);
		MarketClearingResult result = MeritOrderKernel.clearMarketSimple(supplyBook, demandBook);
		result.setBooks(supplyBook, demandBook, distributionMethod);
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
			Bid bid = message.getDataItemOfType(BidData.class).getBid();
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
}