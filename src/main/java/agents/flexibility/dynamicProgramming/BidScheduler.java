// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming;

import agents.flexibility.BidSchedule;
import de.dlr.gitlab.fame.time.TimePeriod;

/** Creates {@link BidSchedule}s
 * 
 * @author Johannes Kochems, Felix Nitsch, Christoph Schimeczek */
public interface BidScheduler {

	/** Creates bidding schedule based on given dispatch schedule
	 * 
	 * @param startingTime first time period in the schedule
	 * @param dispatchSchedule that serves as basis for bidding schedule
	 * @param initialEnergyLevelInMWH energy level of the associated device at the beginning of the dispatch schedule
	 * @return created bidding schedule */
	BidSchedule createBidSchedule(TimePeriod startingTime, double[] dispatchSchedule, double initialEnergyLevelInMWH);

	/** Returns number of time steps in bidding schedule
	 * 
	 * @return number of time steps in bidding schedule */
	int getSchedulingSteps();
}
