// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.assessment;

import java.util.ArrayList;
import java.util.TreeMap;
import agents.flexibility.Strategist;
import communications.message.AmountAtTime;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Assess profit of transitions using an electricity price forecast neglecting any price impact of bids
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public class ProfitPriceTaker implements AssessmentFunction {
	private double currentElectricityPriceInEURperMWH;
	private TreeMap<TimeStamp, Double> electricityPriceForecastsInEURperMWH = new TreeMap<>();

	@Override
	public void prepareFor(TimeStamp time) {
		currentElectricityPriceInEURperMWH = electricityPriceForecastsInEURperMWH.getOrDefault(time, 0.);
	}

	@Override
	public double assessTransition(double externalEnergyDeltaInMWH) {
		return -externalEnergyDeltaInMWH * currentElectricityPriceInEURperMWH;
	}

	@Override
	public void clearBefore(TimeStamp time) {
		electricityPriceForecastsInEURperMWH.headMap(time).clear();
	}

	/** Add electricity price forecast for a given time
	 * 
	 * @param time of electricity price forecast
	 * @param valueInEURperMWH electricity price forecast */
	public void addElectricityPriceForecastFor(TimeStamp time, Double valueInEURperMWH) {
		electricityPriceForecastsInEURperMWH.put(time, valueInEURperMWH);
	}

	@Override
	public ArrayList<TimeStamp> getMissingForecastTimes(ArrayList<TimeStamp> requiredTimes) {
		ArrayList<TimeStamp> missingTimes = new ArrayList<>();
		for (TimeStamp time : requiredTimes) {
			if (!electricityPriceForecastsInEURperMWH.containsKey(time)) {
				missingTimes.add(time);
			}
		}
		return missingTimes;
	}

	@Override
	public void storeForecast(ArrayList<Message> messages) {
		for (Message inputMessage : messages) {
			AmountAtTime priceForecastMessage = inputMessage.getDataItemOfType(AmountAtTime.class);
			double priceForecast = priceForecastMessage.amount;
			TimePeriod timePeriod = new TimePeriod(priceForecastMessage.validAt, Strategist.OPERATION_PERIOD);
			electricityPriceForecastsInEURperMWH.put(timePeriod.getStartTime(), priceForecast);
		}
	}
}
