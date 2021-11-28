package agents.forecast;

import java.util.ArrayList;
import java.util.List;
import agents.markets.meritOrder.MarketClearingResult;
import agents.trader.Trader;
import communications.message.PointInTime;
import communications.portable.MeritOrderMessage;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Calculates and provides perfect foresight merit order forecasts
 * 
 * @author Christoph Schimeczek, Evelyn Sperber, Farzad Sarfarazi, Kristina Nienhaus */
public class MeritOrderForecaster extends MarketForecaster {
	@Product
	public static enum Products {
		/** Perfect foresight merit-order forecasts - issue a Request to obtain it */
		MeritOrderForecast
	};

	/** Creates a {@link MeritOrderForecaster}
	 * 
	 * @param dataProvider provides input from config file
	 * @throws MissingDataException if any required data is not provided */
	public MeritOrderForecaster(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		call(this::sendMeritOrderForecast).on(Products.MeritOrderForecast).use(Trader.Products.MeritOrderForecastRequest);
	}

	/** Sends {@link MeritOrderMessage}s to the requesting trader(s) based on incoming Forecast requests; requesting agent(s) must
	 * also have a MeritOrderForecast contract to get served
	 * 
	 * @param messages incoming forecast request message(s)
	 * @param contracts of partners that desire a MeritOrderForecast */
	private void sendMeritOrderForecast(ArrayList<Message> messages, List<Contract> contracts) {
		for (Contract contract : contracts) {
			ArrayList<Message> requests = CommUtils.extractMessagesFrom(messages, contract.getReceiverId());
			for (Message message : requests) {
				TimeStamp requestedTime = message.getDataItemOfType(PointInTime.class).timeStamp;
				MarketClearingResult result = getResultForRequestedTime(requestedTime);
				MeritOrderMessage meritOrderMessage = new MeritOrderMessage(result.getSupplyBook(), result.getDemandBook(),
						requestedTime);
				fulfilNext(contract, meritOrderMessage);
			}
		}
	}
}
