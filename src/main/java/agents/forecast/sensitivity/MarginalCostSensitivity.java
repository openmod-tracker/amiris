// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import java.util.Comparator;
import agents.markets.meritOrder.sensitivities.SensitivityItem;

public class MarginalCostSensitivity extends MeritOrderAssessor {

	@Override
	protected Comparator<SensitivityItem> getComparator() {
		return SensitivityItem.BY_PRICE_THEN_POWER;
	}

	@Override
	protected double calcMonetaryValue(SensitivityItem item) {
		return item.getPower() * item.getMarginal();
	}
}
