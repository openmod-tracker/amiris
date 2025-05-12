// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import java.util.Comparator;
import agents.markets.meritOrder.sensitivities.SensitivityItem;

/** Provides full merit order assessment for changes in marginal cost totals if demand or supply is added
 * 
 * @author Christoph Schimeczek */
public class MarginalCostSensitivity extends FullAssessor {
	@Override
	protected Comparator<SensitivityItem> getComparator() {
		return SensitivityItem.BY_PRICE_THEN_POWER;
	}

	@Override
	protected double calcMonetaryValue(SensitivityItem item) {
		return item.getMonetaryOffset() + item.getPower() * item.getMarginal();
	}
}
