// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.plantOperator;

import java.util.ArrayList;
import java.util.List;
import agents.trader.Trader;
import communications.message.AmountAtTime;
import de.dlr.gitlab.fame.agent.Agent;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Handles communication of power plant operators with their contracting {@link Trader}
 * 
 * @author Christoph Schimeczek */
public abstract class PowerPlantOperator extends Agent {
	@Product
	public static enum Products {
		/** Cost(s) for the production of a given amount of energy at a specific time */
		MarginalCost,
		/** Forecasted cost(s) for the production of a given amount of energy at a specific time */
		MarginalCostForecast
	};

	@Output
	protected static enum OutputFields {
		AwardedPowerInMWH, OfferedPowerInMW, ReceivedMoneyInEUR, VariableCostsInEUR,
	}

	/** Creates a {@link PowerPlantOperator}
	 * 
	 * @param dataProvider provides input from config file - not required here, but in super class */
	public PowerPlantOperator(DataProvider dataProvider) {
		super(dataProvider);
		call(this::executeDispatch).on(Trader.Products.DispatchAssignment).use(Trader.Products.DispatchAssignment);
		call(this::digestPayment).on(Trader.Products.Payout).use(Trader.Products.Payout);
	}

	/** Runs power plant(s) according to received dispatch instructions
	 * 
	 * @param input single message declaring the power to dispatch
	 * @param contracts not used */
	public void executeDispatch(ArrayList<Message> input, List<Contract> contracts) {
		AmountAtTime award = CommUtils.getExactlyOneEntry(input).getDataItemOfType(AmountAtTime.class);
		store(OutputFields.AwardedPowerInMWH, award.amount);
		double costs = dispatchPlants(award.amount, award.validAt);
		store(OutputFields.VariableCostsInEUR, costs);
	}

	/** Dispatches associated power plants to generate the specified awarded power
	 * 
	 * @param awardedPower amount of power to generate
	 * @param time at which to dispatch
	 * @return total costs for dispatch */
	protected abstract double dispatchPlants(double awardedPower, TimeStamp time);

	/** Writes the income received from an associated trader to output
	 * 
	 * @param input single message payment from an associated trader
	 * @param contracts not used */
	protected void digestPayment(ArrayList<Message> input, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(input);
		AmountAtTime payout = message.getDataItemOfType(AmountAtTime.class);
		store(OutputFields.ReceivedMoneyInEUR, payout.amount);
		digestPaymentPerPlant(payout.validAt, payout.amount);
	}

	/** Optional function to digest payments for individual plants of plant operator
	 * 
	 * @param dispatchTime time at which the payment is received
	 * @param totalPaymentInEUR total money to be paid out to plant operator */
	protected void digestPaymentPerPlant(TimeStamp dispatchTime, double totalPaymentInEUR) {}
}
