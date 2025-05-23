// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import agents.forecast.forecastApi.ForecastApiRequest;
import agents.forecast.forecastApi.ForecastApiResponse;
import agents.markets.DayAheadMarket;
import agents.markets.meritOrder.MarketClearingResult;
import communications.message.AmountAtTime;
import communications.message.AwardData;
import communications.message.ClearingTimes;
import communications.message.PointInTime;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.Constants.Interval;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;
import util.UrlModelService;

/** Provides electricity price forecasts from external model via forecastApi. In order to reduce amount of calls to the external
 * forecasting model, the forecast window can be extended beyond the configured forecasting period. Specifically, the external
 * model is asked to provide a forecast for the period of {@link MarketForecaster#forecastPeriodInHours} plus
 * {@link #forecastWindowExtensionInHours} hours. The received forecasts are saved and used to fulfil the client forecast
 * requests. The "additional" hours from the {@link #forecastWindowExtensionInHours} are used in a rolling horizon way. <br>
 * However, if forecast errors exceed {@link #forecastErrorToleranceInEURperMWH}, an immediate update via the external forecasting
 * model is triggered.
 * 
 * @author Felix Nitsch, Christoph Schimeczek */
public class PriceForecasterApi extends MarketForecaster {
	private final UrlModelService<ForecastApiRequest, ForecastApiResponse> urlService;
	private final int lookBackWindowInHours;
	private final int forecastWindowExtensionInHours;
	private final double forecastErrorToleranceInEURperMWH;
	/** Stores electricity market prices over time */
	private final TreeMap<Long, Double> marketClearingPrices = new TreeMap<>();
	private final TreeMap<Long, Double> residualLoadInMWh = new TreeMap<>();
	private final TimeSeries tsResidualLoadInMWh;
	private TreeMap<Long, Double> priceForecastMeans;
	private TreeMap<Long, Double> priceForecastVariances;
	private TimeStamp nextClearingTimeStep = now();

	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newString("ServiceURL").help("URL to amiris-priceforecast api"),
					Make.newInt("LookBackWindowInHours").help("Number of TimeSteps for look back"),
					Make.newInt("ForecastWindowExtensionInHours")
							.help("Number of TimeSteps additional to forecast horizon to be requested from API"),
					Make.newDouble("ForecastErrorToleranceInEURperMWH").help(
							"Max accepted deviation between forecasted and realized electricity prices; if violated a new price forecast request is sent"),
					Make.newSeries("ResidualLoadInMWh").optional())
			.buildTree();

	@Output
	private static enum OutputFields {
		ElectricityPriceForecastVarianceInEURperMWH
	};

	/** Creates new {@link PriceForecasterApi}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException in case mandatory input is missing */
	public PriceForecasterApi(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		String serviceUrl = input.getString("ServiceURL");
		urlService = new UrlModelService<ForecastApiRequest, ForecastApiResponse>(serviceUrl) {};
		lookBackWindowInHours = input.getInteger("LookBackWindowInHours");
		forecastWindowExtensionInHours = input.getInteger("ForecastWindowExtensionInHours");
		forecastErrorToleranceInEURperMWH = input.getDouble("ForecastErrorToleranceInEURperMWH");
		tsResidualLoadInMWh = input.getTimeSeriesOrDefault("ResidualLoadInMWh", null);

		call(this::logClearingPrices).onAndUse(DayAheadMarket.Products.Awards);
		call(this::registerClearingTime).onAndUse(DayAheadMarket.Products.GateClosureInfo);
		call(this::sendPriceForecast).on(Forecaster.Products.PriceForecast)
				.use(ForecastClient.Products.PriceForecastRequest);
	}

	/** Extracts and store power prices reported from {@link DayAheadMarket}
	 * 
	 * @param input single power price message to read
	 * @param contracts not used */
	private void logClearingPrices(ArrayList<Message> input, List<Contract> contracts) {
		AwardData award = CommUtils.getExactlyOneEntry(input).getDataItemOfType(AwardData.class);
		marketClearingPrices.put(award.beginOfDeliveryInterval.getStep(), award.powerPriceInEURperMWH);
	}

	/** Notes clearing time for later determination of price forecasting times */
	private void registerClearingTime(ArrayList<Message> input, List<Contract> contracts) {
		ClearingTimes clearingTimes = CommUtils.getExactlyOneEntry(input).getDataItemOfType(ClearingTimes.class);
		nextClearingTimeStep = clearingTimes.getTimes().get(0);
	}

	/** Sends {@link AmountAtTime} from {@link MarketClearingResult} to the requesting trader */
	private void sendPriceForecast(ArrayList<Message> messages, List<Contract> contracts) {
		if (tsResidualLoadInMWh != null) {
			chunkResidualLoad();
		}
		removeDataBefore(marketClearingPrices, now().earlierBy(new TimeSpan(lookBackWindowInHours, Interval.HOURS)));
		boolean forecastUpdateRequired = checkForecastUpdateRequired();
		if (forecastUpdateRequired) {
			updateForecasts();
		}
		for (Contract contract : contracts) {
			ArrayList<Message> requests = CommUtils.extractMessagesFrom(messages, contract.getReceiverId());
			for (Message message : requests) {
				TimeStamp requestedTime = message.getDataItemOfType(PointInTime.class).validAt;
				for (AmountAtTime response : calcForecastResponses(requestedTime, forecastUpdateRequired)) {
					fulfilNext(contract, response);
				}
			}
		}
		removeDataBefore(priceForecastMeans, now());
		removeDataBefore(priceForecastVariances, now());
		store(MarketForecaster.OutputFields.ElectricityPriceForecastInEURperMWH,
				priceForecastMeans.firstEntry().getValue());
		store(OutputFields.ElectricityPriceForecastVarianceInEURperMWH, priceForecastVariances.firstEntry().getValue());
	}

	/** Chunks tsResidualLoadInMWh to residualLoadInMWh from lookBackWindowInHours to forecastPeriodInHours +
	 * forecastWindowExtensionInHours */
	private void chunkResidualLoad() {
		residualLoadInMWh.clear();
		TimeStamp startTime = nextClearingTimeStep.earlierBy(new TimeSpan(lookBackWindowInHours, Interval.HOURS));
		TimeStamp stopTime = nextClearingTimeStep.laterBy(new TimeSpan(forecastPeriodInHours, Interval.HOURS))
				.laterBy(new TimeSpan(forecastWindowExtensionInHours, Interval.HOURS));
		TimeStamp step = startTime;
		while (step.getStep() < stopTime.getStep()) {
			residualLoadInMWh.put(step.getStep(), tsResidualLoadInMWh.getValueLaterEqual(step));
			step = step.laterBy(new TimeSpan(1L, Interval.HOURS));
		}
	}

	/** Removes all values before given TimeStamp from specified container */
	private void removeDataBefore(TreeMap<Long, Double> container, TimeStamp time) {
		Iterator<Entry<Long, Double>> iterator = container.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Long, Double> entry = iterator.next();
			if (entry.getKey() < time.getStep()) {
				iterator.remove();
			} else {
				break;
			}
		}
	}

	/** @return True if last result is missing, not within price tolerance, or required forecast price window is not fully available
	 *         in stored forecast map */
	private boolean checkForecastUpdateRequired() {
		if (priceForecastMeans == null) {
			return true;
		}
		var timeStep = marketClearingPrices.lastKey();
		boolean resultNotInTolerance = Math
				.abs(marketClearingPrices.get(timeStep) - priceForecastMeans.get(timeStep)) > forecastErrorToleranceInEURperMWH;
		var offset = new TimeSpan(forecastPeriodInHours, Interval.HOURS);
		long endOfForecastWindow = new TimeStamp(timeStep).laterBy(offset).getStep();
		boolean priceMissing = !priceForecastMeans.containsKey(endOfForecastWindow);
		return resultNotInTolerance || priceMissing;
	}

	/** Updates forecasts for required forecast price window considering extension */
	private void updateForecasts() {
		var request = new ForecastApiRequest(nextClearingTimeStep.getStep(),
				forecastPeriodInHours + forecastWindowExtensionInHours,
				marketClearingPrices, residualLoadInMWh);
		ForecastApiResponse response = urlService.call(request);
		priceForecastMeans = response.getForecastMeans().get(0);
		priceForecastVariances = response.getForecastVariances().get(0);
	}

	/** If forecastUpdateRequired, returns all updated forecasts until requestedTime; else, only forecast for requestedTime */
	private List<AmountAtTime> calcForecastResponses(TimeStamp requestedTime, boolean forecastUpdateRequired) {
		var messages = new ArrayList<AmountAtTime>();
		if (forecastUpdateRequired) {
			for (var entry : priceForecastMeans.headMap(requestedTime.laterByOne().getStep()).entrySet()) {
				messages.add(new AmountAtTime(new TimeStamp(entry.getKey()), entry.getValue()));
			}
		} else {
			var forecast = priceForecastMeans.get(requestedTime.getStep());
			messages.add(new AmountAtTime(requestedTime, forecast));
		}
		return messages;
	}
}
