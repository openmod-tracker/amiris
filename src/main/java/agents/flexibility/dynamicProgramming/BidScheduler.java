// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming;

public interface BidScheduler {
	BidSchedule createBidSchedule(double[] dispatchSchedule);

	int getSchedulingSteps();
}
