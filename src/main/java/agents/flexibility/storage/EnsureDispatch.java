// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.storage;

import agents.flexibility.BidSchedule;
import agents.flexibility.dynamicProgramming.BidScheduler;
import agents.flexibility.dynamicProgramming.StateManager.DispatchSchedule;
import agents.markets.meritOrder.Constants;
import de.dlr.gitlab.fame.time.TimePeriod;

/** Offers energy with minimum price and requests energy with maximum price
 * 
 * @author Christoph Schimeczek */
public class EnsureDispatch implements BidScheduler {
	private final int numberOfScheduleSteps;

	public EnsureDispatch(int numberOfScheduleSteps) {
		this.numberOfScheduleSteps = numberOfScheduleSteps;
	}

	@Override
	public BidSchedule createBidSchedule(TimePeriod startingTime, DispatchSchedule dispatchSchedule) {
		BidSchedule bidSchedule = new BidSchedule(startingTime, numberOfScheduleSteps);

		double[] biddingPricePerPeriodInEURperMWH = new double[numberOfScheduleSteps];
		for (int i = 0; i < numberOfScheduleSteps; i++) {
			biddingPricePerPeriodInEURperMWH[i] = getBid(dispatchSchedule.externalEnergyDeltasInMWH[i]);
		}
		bidSchedule.setRequestedEnergyPerPeriod(dispatchSchedule.externalEnergyDeltasInMWH);
		bidSchedule.setBidsScheduleInEURperMWH(biddingPricePerPeriodInEURperMWH);
		bidSchedule.setExpectedInitialInternalEnergyScheduleInMWH(dispatchSchedule.initialInternalEnergyInMWH);
		return bidSchedule;
	}

	/** @return scarcity price for purchasing energy and minimum price for selling energy */
	private double getBid(double purchasedEnergy) {
		return purchasedEnergy > 0 ? Constants.SCARCITY_PRICE_IN_EUR_PER_MWH : Constants.MINIMAL_PRICE_IN_EUR_PER_MWH;
	}

	@Override
	public int getSchedulingSteps() {
		return numberOfScheduleSteps;
	}
}
