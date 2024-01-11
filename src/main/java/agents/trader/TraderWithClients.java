/**
 * 
 */
package agents.trader;

import java.util.ArrayList;
import java.util.List;
import agents.forecast.MarketForecaster;
import agents.markets.DayAheadMarket;
import communications.message.ClearingTimes;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;

/** Abstract base class for all traders at {@link DayAheadMarket} that require clients in order to create their bids.
 *
 * @author Christoph Schimeczek, A. Achraf El Ghazi */
public abstract class TraderWithClients extends Trader {
	@Product
	public static enum Products {
		GateClosureForward,
		ForecastRequestForward,
	};

	/** @param dataProvider */
	public TraderWithClients(DataProvider dataProvider) {
		super(dataProvider);
		call(this::forwardClearingTimes).on(Products.GateClosureForward).use(DayAheadMarket.Products.GateClosureInfo);
		call(this::forwardClearingTimes).on(Products.ForecastRequestForward).use(MarketForecaster.Products.ForecastRequest);
	}

	/** Forwards one ClearingTimes to connected clients (if any)
	 * 
	 * @param input a single ClearingTimes message
	 * @param contracts connected clients */
	private void forwardClearingTimes(ArrayList<Message> input, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(input);
		ClearingTimes clearingTimes = message.getDataItemOfType(ClearingTimes.class);
		for (Contract contract : contracts) {
			fulfilNext(contract, clearingTimes);
		}
	}

}
