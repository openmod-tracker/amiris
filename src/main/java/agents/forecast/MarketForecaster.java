// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.IntStream;
import agents.markets.EnergyExchange;
import agents.markets.meritOrder.MarketClearing;
import agents.markets.meritOrder.MarketClearingResult;
import agents.markets.meritOrder.books.OrderBook.DistributionMethod;
import agents.trader.Trader;
import communications.message.BidData;
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
import de.dlr.gitlab.fame.time.Constants.Interval;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Common base class related to {@link EnergyExchange} market forecasting; issues {@link Products#ForecastRequest}s to ask for
 * required forecasts; uses forecasted bids to clear market ahead of time and thus provide forecasts
 * 
 * @author Christoph Schimeczek */
public abstract class MarketForecaster extends Agent {
	@Input private static final Tree parameters = Make.newTree().add(Make.newInt("ForecastPeriodInHours"),
			Make.newInt("ForecastRequestOffsetInSeconds"), Make.newEnum("DistributionMethod", DistributionMethod.class))
			.buildTree();

	@Product
	public static enum Products {
		/** Send this out to every (start) agent of an {@link EnergyExchange} bidding chain (e.g. demand and power plant agents) */
		ForecastRequest
	};

	@Output
	private static enum OutputFields {
		AwardedPowerForecast, ElectricityPriceForecast
	};

	protected final int forecastPeriodInHours;
	protected final TimeSpan forecastRequestOffset;
	protected final MarketClearing marketClearing;
	protected final TreeMap<TimeStamp, MarketClearingResult> calculatedForecastContainer = new TreeMap<>();

	/** Creates a {@link MarketForecaster}
	 * 
	 * @param dataProvider provides input from config file
	 * @throws MissingDataException if any required data is not provided */
	public MarketForecaster(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		marketClearing = new MarketClearing(input.getEnum("DistributionMethod", DistributionMethod.class));
		forecastPeriodInHours = input.getInteger("ForecastPeriodInHours");
		forecastRequestOffset = new TimeSpan(input.getInteger("ForecastRequestOffsetInSeconds"));

		/** Send out forecast requests to make other agents prepare their bids ahead of time */
		call(this::sendForecastRequests).on(Products.ForecastRequest);
		/** On incoming bid forecasts: clear the market ahead and store the clearing result */
		call(this::calcMarketClearingForecasts).on(Trader.Products.BidsForecast).use(Trader.Products.BidsForecast);
	}

	/** Requests bid forecast for all future hours within forecast period
	 * 
	 * @param input not used
	 * @param contracts with all agents that start an {@link EnergyExchange} bidding chain */
	private void sendForecastRequests(ArrayList<Message> input, List<Contract> contracts) {
		if (calculatedForecastContainer.isEmpty()) {
			fulfilForecastRequestContracts(contracts, IntStream.range(0, forecastPeriodInHours + 1).toArray());
		} else {
			fulfilForecastRequestContracts(contracts, forecastPeriodInHours);
			removeFirstOutdatedForecast();
		}
	}

	/** send out forecast request to all receivers of given contracts */
	private void fulfilForecastRequestContracts(List<Contract> contracts, int... hourDeltas) {
		TimeStamp[] targetTimes = new TimeStamp[hourDeltas.length];
		for (int i = 0; i < hourDeltas.length; i++) {
			TimeSpan hourOffset = new TimeSpan(hourDeltas[i], Interval.HOURS);
			targetTimes[i] = now().laterBy(forecastRequestOffset).laterBy(hourOffset);
		}
		for (Contract contract : contracts) {
			fulfilNext(contract, new ClearingTimes(targetTimes));
		}
	}

	/** remove first out-dated market clearing result */
	private void removeFirstOutdatedForecast() {
		Entry<TimeStamp, MarketClearingResult> entry = calculatedForecastContainer.firstEntry();
		if (entry.getKey().isLessThan(now())) {
			calculatedForecastContainer.remove(entry.getKey());
		}
	}

	/** Use received BidForecasts to clear market and store the clearing result(s) for later usage
	 **
	 * @param messages bid forecast(s) received
	 * @param contracts not used */
	private void calcMarketClearingForecasts(ArrayList<Message> messages, List<Contract> contracts) {
		TreeMap<TimeStamp, ArrayList<Message>> messagesByTimeStamp = sortMessagesByBidTimeStamp(messages);
		for (Entry<TimeStamp, ArrayList<Message>> entry : messagesByTimeStamp.entrySet()) {
			TimeStamp requestedTime = entry.getKey();
			ArrayList<Message> bidsAtRequestedTime = entry.getValue();
			MarketClearingResult marketClearingResult = marketClearing.calculateMarketClearing(bidsAtRequestedTime);
			calculatedForecastContainer.put(requestedTime, marketClearingResult);
		}
	}

	/** Groups given messages by their targeted time of delivery into an ordered Map
	 * 
	 * @param messages to group by
	 * @return a Map of Messages sorted by the {@link TimeStamp} their contained {@link BidData} are valid at */
	protected TreeMap<TimeStamp, ArrayList<Message>> sortMessagesByBidTimeStamp(ArrayList<Message> messages) {
		TreeMap<TimeStamp, ArrayList<Message>> messageByTimeStamp = new TreeMap<>();
		for (Message message : messages) {
			TimeStamp deliveryTime = message.getDataItemOfType(BidData.class).deliveryTime;
			ArrayList<Message> messageList = saveGet(messageByTimeStamp, deliveryTime);
			messageList.add(message);
		}
		return messageByTimeStamp;
	}

	/** @return Stored messages from given Map at given {@link TimeStamp} - if TimeStamp is not yet registered, a new empty list is
	 *         created, stored to the Map and returned */
	private ArrayList<Message> saveGet(TreeMap<TimeStamp, ArrayList<Message>> messagesByTimeStamp, TimeStamp timeStamp) {
		if (!messagesByTimeStamp.containsKey(timeStamp)) {
			messagesByTimeStamp.put(timeStamp, new ArrayList<Message>());
		}
		return messagesByTimeStamp.get(timeStamp);
	}

	/** Returns stored clearing result for the given time - or throws an Exception if no result is stored
	 * 
	 * @param requestedTime to fetch market clearing result for
	 * @return the result stored for the requested time */
	protected MarketClearingResult getResultForRequestedTime(TimeStamp requestedTime) {
		MarketClearingResult result = calculatedForecastContainer.get(requestedTime);
		if (result == null) {
			throw new RuntimeException("Forecast not available for requested time: " + requestedTime);
		}
		return result;
	}
	
	/** writes out the nearest upcoming forecast */
	protected void saveNextForecast() {
		MarketClearingResult marketClearingResults = calculatedForecastContainer.ceilingEntry(now()).getValue();
		store(OutputFields.ElectricityPriceForecast, marketClearingResults.getMarketPriceInEURperMWH());
		store(OutputFields.AwardedPowerForecast, marketClearingResults.getTradedEnergyInMWH());
	}	
}
