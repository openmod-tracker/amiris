// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.assessment;

import java.util.ArrayList;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** A function to assess transitions
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public interface AssessmentFunction {
	/** Prepare {@link AssessmentFunction} for follow-up transition assessments at given time
	 * 
	 * @param time for which transitions will be assessed */
	void prepareFor(TimeStamp time);

	/** Return estimated value or costs of the transition
	 * 
	 * @param externalEnergyDeltaInMWH of the transition to be assessed; positive values correspond to "charging"
	 * @return the value or costs of the transition at the time the {@link AssessmentFunction} was
	 *         {@link #prepareFor(TimePeriod)} */
	double assessTransition(double externalEnergyDeltaInMWH);

	/** Clear entries of electricity price forecasts before given time
	 * 
	 * @param time before which elements are cleared */
	void clearBefore(TimeStamp time);

	/** Return list of time stamps at which additional information is required, based on given list of time stamps required for
	 * dispatch planning
	 * 
	 * @param planningTimes times in the upcoming planning horizon
	 * @return list of time stamp which require additional information */
	public ArrayList<TimeStamp> getMissingForecastTimes(ArrayList<TimeStamp> planningTimes);

	/** Store forecasts for dispatch assessment
	 * 
	 * @param messages to be scraped for forecast data */
	public void storeForecast(ArrayList<Message> messages);
}
