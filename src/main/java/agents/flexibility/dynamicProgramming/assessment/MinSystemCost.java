// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.assessment;

import java.util.ArrayList;
import java.util.TreeMap;
import agents.flexibility.dynamicProgramming.Optimiser.Target;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.SupplyOrderBook;
import agents.markets.meritOrder.sensitivities.MarginalCostSensitivity;
import communications.portable.MeritOrderMessage;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Minimise system cost of transitions using a system cost forecast and estimating the impact of own transitions on merit order
 * 
 * @author Christoph Schimeczek */
public class MinSystemCost implements AssessmentFunction {
	private TreeMap<TimeStamp, MarginalCostSensitivity> marginalCostSensitivityForecasts = new TreeMap<>();
	private MarginalCostSensitivity currentSensitivity;

	@Override
	public void prepareFor(TimeStamp time) {
		currentSensitivity = marginalCostSensitivityForecasts.get(time);
	}

	@Override
	public double assessTransition(double externalEnergyDeltaInMWH) {
		return currentSensitivity.getValue(externalEnergyDeltaInMWH);
	}

	@Override
	public void clearBefore(TimeStamp time) {
		Util.clearMapBefore(marginalCostSensitivityForecasts, time);
	}

	@Override
	public ArrayList<TimeStamp> getMissingForecastTimes(ArrayList<TimeStamp> planningTimes) {
		return Util.findMissingKeys(marginalCostSensitivityForecasts, planningTimes);
	}

	@Override
	public void storeForecast(ArrayList<Message> messages) {
		for (Message inputMessage : messages) {
			MeritOrderMessage meritOrderMessage = inputMessage.getAllPortableItemsOfType(MeritOrderMessage.class).get(0);
			SupplyOrderBook supplyOrderBook = meritOrderMessage.getSupplyOrderBook();
			DemandOrderBook demandOrderBook = meritOrderMessage.getDemandOrderBook();
			MarginalCostSensitivity sensitivity = new MarginalCostSensitivity();
			sensitivity.updateSensitivities(supplyOrderBook, demandOrderBook);
			marginalCostSensitivityForecasts.put(meritOrderMessage.getTimeStamp(), sensitivity);
		}
	}

	@Override
	public Target getTargetType() {
		return Target.MINIMISE;
	}
}
