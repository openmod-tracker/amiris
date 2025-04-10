// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.bidding;

import agents.flexibility.BidSchedule;
import agents.flexibility.dynamicProgramming.Strategist;
import agents.flexibility.dynamicProgramming.states.StateManager.DispatchSchedule;
import agents.markets.meritOrder.Constants;
import de.dlr.gitlab.fame.time.TimePeriod;

/** Offers energy with minimum price and requests energy with maximum price
 * 
 * @author Christoph Schimeczek */
public class EnsureDispatch implements BidScheduler {
	private final double schedulingHorizonInHours;

	public EnsureDispatch(double schedulingHorizonInHours) {
		this.schedulingHorizonInHours = schedulingHorizonInHours;
	}

	@Override
	public BidSchedule createBidSchedule(TimePeriod startingTime, DispatchSchedule dispatchSchedule) {
		int numberOfScheduleSteps = Strategist.calcHorizonInPeriodSteps(startingTime, schedulingHorizonInHours);
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
	public double getScheduleHorizonInHours() {
		return schedulingHorizonInHours;
	}
}
