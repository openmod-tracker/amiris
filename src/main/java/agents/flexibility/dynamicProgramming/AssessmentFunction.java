// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming;

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
	 * @param externalEnergyDeltaInMWH of the transition to be assessed
	 * @return the value or costs of the transition at the time the {@link AssessmentFunction} was
	 *         {@link #prepareFor(TimePeriod)} */
	double assessTransition(double externalEnergyDeltaInMWH);
}
