// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader;

import java.util.ArrayList;
import java.util.List;
import accounting.AnnualCostCalculator;
import agents.flexibility.Strategist;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.SupplyOrderBook;
import communications.message.AmountAtTime;
import communications.message.PointInTime;
import communications.portable.MeritOrderMessage;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

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

	/** Requests {@link MeritOrderMessage} or PriceForecast from contracted partner (Forecaster)
	 * 
	 * @param input not used
	 * @param contracts single contracted Forecaster to request forecast from */
	protected void requestForecast(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TimePeriod nextTime = new TimePeriod(now().laterBy(electricityForecastRequestOffset),
				Strategist.OPERATION_PERIOD);
		ArrayList<TimeStamp> missingForecastTimes = this.getStrategist().getTimesMissingElectricityPriceForecasts(nextTime);
		for (TimeStamp missingForecastTime : missingForecastTimes) {
			PointInTime pointInTime = new PointInTime(missingForecastTime);
			fulfilNext(contract, pointInTime);
		}
	}

	/** @return strategist instance of respective FlexibilityTrader */
	protected abstract Strategist getStrategist();

	/** Digests incoming {@link MeritOrderMessage} forecasts
	 * 
	 * @param input one or multiple merit order forecast message(s)
	 * @param contracts not used */
	protected void updateMeritOrderForecast(ArrayList<Message> input, List<Contract> contracts) {
		for (Message inputMessage : input) {
			MeritOrderMessage meritOrderMessage = inputMessage.getAllPortableItemsOfType(MeritOrderMessage.class).get(0);
			SupplyOrderBook supplyOrderBook = meritOrderMessage.getSupplyOrderBook();
			DemandOrderBook demandOrderBook = meritOrderMessage.getDemandOrderBook();
			TimePeriod timeSegment = new TimePeriod(meritOrderMessage.getTimeStamp(), Strategist.OPERATION_PERIOD);
			this.getStrategist().storeMeritOrderForesight(timeSegment, supplyOrderBook, demandOrderBook);
		}
	}

	/** Digests incoming price forecasts
	 * 
	 * @param input one or multiple price forecast message(s)
	 * @param contracts not used */
	protected void updatePriceForecast(ArrayList<Message> input, List<Contract> contracts) {
		for (Message inputMessage : input) {
			AmountAtTime priceForecastMessage = inputMessage.getDataItemOfType(AmountAtTime.class);
			double priceForecast = priceForecastMessage.amount;
			TimePeriod timeSegment = new TimePeriod(priceForecastMessage.validAt, Strategist.OPERATION_PERIOD);
			this.getStrategist().storeElectricityPriceForecast(timeSegment, priceForecast);
		}
	}
}
