// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast;

import java.util.ArrayList;
import java.util.List;
import agents.markets.meritOrder.MarketClearingResult;
import agents.trader.Trader;
import communications.message.AmountAtTime;
import communications.message.PointInTime;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Calculates and provides perfect foresight electricity price forecasts
 * 
 * @author Christoph Schimeczek, Evelyn Sperber, Farzad Sarfarazi, Kristina Nienhaus */
public class PriceForecaster extends MarketForecaster {

	/** Create a {@link PriceForecaster}
	 * 
	 * @param dataProvider provides input from config file
	 * @throws MissingDataException if any required data is not provided */
	public PriceForecaster(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		call(this::sendPriceForecast).on(Forecaster.Products.PriceForecast).use(Trader.Products.PriceForecastRequest);
	}

	/** Sends {@link AmountAtTime} from {@link MarketClearingResult} to the requesting trader(s) based on incoming Forecast
	 * requests; requesting agent(s) must also have a PriceForecast contract to get served
	 * 
	 * @param messages incoming forecast request message(s)
	 * @param contracts of partners that desire a PriceForecast */
	private void sendPriceForecast(ArrayList<Message> messages, List<Contract> contracts) {
		for (Contract contract : contracts) {
			ArrayList<Message> requests = CommUtils.extractMessagesFrom(messages, contract.getReceiverId());
			for (Message message : requests) {
				TimeStamp requestedTime = message.getDataItemOfType(PointInTime.class).timeStamp;
				MarketClearingResult result = getResultForRequestedTime(requestedTime);
				double forecastedPriceInEURperMWH = result.getMarketPriceInEURperMWH();
				AmountAtTime priceForecastMessage = new AmountAtTime(requestedTime, forecastedPriceInEURperMWH);
				fulfilNext(contract, priceForecastMessage);
			}
		}
		saveNextForecast();
	}
}