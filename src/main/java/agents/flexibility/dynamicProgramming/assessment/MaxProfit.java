// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.assessment;

import java.util.ArrayList;
import java.util.TreeMap;
import agents.flexibility.dynamicProgramming.Optimiser.Target;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.SupplyOrderBook;
import agents.markets.meritOrder.sensitivities.PriceSensitivity;
import communications.portable.MeritOrderMessage;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Maximise profits of transitions using a merit order forecast and estimating the impact of own transitions on profits
 * 
 * @author Christoph Schimeczek */
public class MaxProfit implements AssessmentFunction {
	private final TreeMap<TimeStamp, PriceSensitivity> priceSensitivityForecasts = new TreeMap<>();
	private PriceSensitivity currentSensitivity;

	@Override
	public void prepareFor(TimeStamp time) {
		currentSensitivity = priceSensitivityForecasts.get(time);
	}

	@Override
	public double assessTransition(double externalEnergyDeltaInMWH) {
		return currentSensitivity.getValue(externalEnergyDeltaInMWH);
	}

	@Override
	public void clearBefore(TimeStamp time) {
		Util.clearMapBefore(priceSensitivityForecasts, time);
	}

	@Override
	public ArrayList<TimeStamp> getMissingForecastTimes(ArrayList<TimeStamp> planningTimes) {
		return Util.findMissingKeys(priceSensitivityForecasts, planningTimes);
	}

	@Override
	public void storeForecast(ArrayList<Message> messages) {
		for (Message inputMessage : messages) {
			MeritOrderMessage meritOrderMessage = inputMessage.getAllPortableItemsOfType(MeritOrderMessage.class).get(0);
			SupplyOrderBook supplyOrderBook = meritOrderMessage.getSupplyOrderBook();
			DemandOrderBook demandOrderBook = meritOrderMessage.getDemandOrderBook();
			PriceSensitivity sensitivity = new PriceSensitivity();
			sensitivity.updateSensitivities(supplyOrderBook, demandOrderBook);
			priceSensitivityForecasts.put(meritOrderMessage.getTimeStamp(), sensitivity);
		}
	}

	@Override
	public Target getTargetType() {
		return Target.MAXIMISE;
	}
}
