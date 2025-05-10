// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.SupplyOrderBook;

/** Assessment of cost / revenues associated with added demand / supply; assumes the price does not change
 * 
 * @author Christoph Schimeczek */
public class CostInsensitive implements MeritOrderAssessment {
	private static final double MAX_ENERGY_IN_MWH = 1E10;
	private double electricityPrice = 0;

	@Override
	public void assess(SupplyOrderBook supplyBook, DemandOrderBook demandBook) {
		electricityPrice = supplyBook.getLastAwardedItem().getOfferPrice();
	}

	@Override
	public double[] getDemandSensitivityPowers() {
		return new double[] {0., MAX_ENERGY_IN_MWH};
	}

	@Override
	public double[] getDemandSensitivityValues() {
		return new double[] {0., electricityPrice * MAX_ENERGY_IN_MWH};
	}

	@Override
	public double[] getSupplySensitivityPowers() {
		return new double[] {0., MAX_ENERGY_IN_MWH};
	}

	@Override
	public double[] getSupplySensitivityValues() {
		return new double[] {0., electricityPrice * MAX_ENERGY_IN_MWH};
	}
}
