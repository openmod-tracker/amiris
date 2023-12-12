package agents.markets;

import java.util.ArrayList;
import java.util.List;
import communications.message.BidData;
import communications.message.ClearingTimes;
import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Interface for traders at the {@link DayAheadMarket}
 * 
 * @author Christoph Schimeczek */
public interface DayAheadMarketTrader extends AgentAbility {
	static String ERR_CLEARING_TIMES_MISSING = "None of the given messages contained a ClearingTimes payload.";
	static String ERR_CLEARING_TIMES_AMBIGUOUS = "More than one of the given messages contained a ClearingTimes payload.";

	@Product
	/** Products of traders interacting with {@link DayAheadMarket} */
	public static enum Products {
		/** Sell/Buy order to be placed at the {@link DayAheadMarket} */
		Bids
	}

	/** Send Bids to contracted {@link DayAheadMarket}
	 * 
	 * @param contract with the {@link DayAheadMarket}
	 * @param bidData 1..N bids to be placed */
	public default void sendDayAheadMarketBids(Contract contract, BidData... bidData) {
		for (BidData oneBid : bidData) {
			fulfilNext(contract, oneBid);
		}
	}

	/** Searches for a single message with {@link ClearingTimes} in given messages and returns its {@link TimeStamp}s
	 * 
	 * @param messages list of messages to search for a single one with {@link ClearingTimes} payload
	 * @return {@link TimeStamp}s contained in the found {@link ClearingTimes} payload
	 * @throws IllegalArgumentException if not exactly one message contained a ClearingTimes payload */
	public default List<TimeStamp> extractClearingTimesFromMessages(ArrayList<Message> messages) {
		ClearingTimes clearingTimes = null;
		for (Message message : messages) {
			if (message.containsType(ClearingTimes.class)) {
				if (clearingTimes != null) {
					throw new IllegalArgumentException(ERR_CLEARING_TIMES_AMBIGUOUS);
				} else {
					clearingTimes = message.getDataItemOfType(ClearingTimes.class);
				}
			}
		}
		if (clearingTimes == null) {
			throw new IllegalArgumentException(ERR_CLEARING_TIMES_MISSING);
		}
		return clearingTimes.getTimes();
	}
}
