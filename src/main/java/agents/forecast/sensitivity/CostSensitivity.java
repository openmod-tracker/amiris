// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import java.util.Comparator;
import agents.markets.meritOrder.sensitivities.SensitivityItem;

public class CostSensitivity extends FullAssessor {

	@Override
	protected Comparator<SensitivityItem> getComparator() {
		return SensitivityItem.BY_PRICE;
	}

	@Override
	protected double calcMonetaryValue(SensitivityItem item) {
		return item.getPower() * item.getPrice();
	}
}
