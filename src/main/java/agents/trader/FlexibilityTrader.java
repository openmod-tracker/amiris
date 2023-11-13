// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader;

import java.util.ArrayList;
import java.util.List;
import accounting.AnnualCostCalculator;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimeSpan;

/** A type of Trader that also operates a flexibility asset, e.g. storage device or flexible heat pump
 *
 * @author Christoph Schimeczek */
public abstract class FlexibilityTrader extends Trader {
	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newInt("ElectricityForecastRequestOffsetInSeconds"))
			.addAs("Refinancing", AnnualCostCalculator.parameters)
			.buildTree();

	protected final TimeSpan electricityForecastRequestOffset;

	@Output
	protected static enum Outputs {
		FixedCostsInEUR, InvestmentAnnuityInEUR, VariableCostsInEUR, ReceivedMoneyInEUR
	}

	private AnnualCostCalculator annualCost;

	public FlexibilityTrader(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		annualCost = AnnualCostCalculator.build(input, "Refinancing");
		electricityForecastRequestOffset = new TimeSpan(input.getInteger("ElectricityForecastRequestOffsetInSeconds"));

		call(this::reportCosts).on(Products.AnnualCostReport);
	}

	/** Write annual costs to output; To trigger contract {@link FlexibilityTrader} with itself
	 * 
	 * @param input not used
	 * @param contracts not used */
	protected void reportCosts(ArrayList<Message> input, List<Contract> contracts) {
		store(Outputs.InvestmentAnnuityInEUR, annualCost.calcInvestmentAnnuityInEUR(getInstalledCapacityInMW()));
		store(Outputs.FixedCostsInEUR, annualCost.calcFixedCostInEUR(getInstalledCapacityInMW()));
	}

	/** Return installed capacity of the operated flexibility device
	 * 
	 * @return installed capacity in MW */
	protected abstract double getInstalledCapacityInMW();
}
