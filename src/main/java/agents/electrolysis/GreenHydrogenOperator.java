// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.electrolysis;

import java.util.ArrayList;
import java.util.List;
import agents.flexibility.DispatchSchedule;
import agents.flexibility.Strategist;
import agents.markets.DayAheadMarket;
import agents.markets.FuelsMarket;
import agents.markets.FuelsMarket.FuelType;
import agents.markets.FuelsTrader;
import agents.plantOperator.renewable.VariableRenewableOperator;
import agents.trader.ElectrolysisTrader;
import agents.trader.FlexibilityTrader;
import agents.trader.PowerPlantScheduler;
import communications.message.AmountAtTime;
import communications.message.ClearingTimes;
import communications.message.FuelBid;
import communications.message.FuelBid.BidType;
import communications.message.FuelData;
import communications.message.PpaInformation;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimeStamp;


/** A green hydrogen electrolysis operator
 * 
 * @author Johannes Kochems, Leonard Willeke */
public class GreenHydrogenOperator extends FlexibilityTrader implements FuelsTrader, PowerPlantScheduler {
	@Input private static final Tree parameters = Make.newTree().addAs("Device", Electrolyzer.parameters)
			.addAs("Strategy", ElectrolyzerStrategist.parameters).buildTree();

	@Output
	private static enum Outputs {
		AwardedEnergyInMWH, ProducedHydrogenInMWH
	};

	@Product
	public static enum Products {
		PpaInformationRequest
	};

	private final Electrolyzer electrolyzer;
	private final ElectrolyzerStrategist strategist;
	private double ppaPriceInEURperMWH;

	/** Creates a new {@link ElectrolysisTrader} based on given input parameters
	 * 
	 * @param data configured input
	 * @throws MissingDataException if any required input is missing */
	public GreenHydrogenOperator(DataProvider data) throws MissingDataException {
		super(data);
		ParameterData input = parameters.join(data);
		electrolyzer = new Electrolyzer(input.getGroup("Device"));
		strategist = ElectrolyzerStrategist.newStrategist(input.getGroup("Strategy"), electrolyzer);

		call(this::forwardClearingTimes).on(Products.PpaInformationRequest).use(DayAheadMarket.Products.GateClosureInfo);
		call(this::assignDispatch).on(PowerPlantScheduler.Products.DispatchAssignment)
				.use(VariableRenewableOperator.Products.PpaInformation);
		call(this::sellProducedGreenHydrogen).on(FuelsTrader.Products.FuelBid);
		call(this::digestSaleReturns).on(FuelsMarket.Products.FuelBill).use(FuelsMarket.Products.FuelBill);
		call(this::payoutClient).on(PowerPlantScheduler.Products.Payout)
				.use(VariableRenewableOperator.Products.PpaInformation);
	}

	/** Forwards one ClearingTimes to connected clients
	 * 
	 * @param input a single ClearingTimes message
	 * @param contracts connected client */
	private void forwardClearingTimes(ArrayList<Message> input, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(input);
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		ClearingTimes clearingTimes = message.getDataItemOfType(ClearingTimes.class);
		fulfilNext(contract, clearingTimes);
	}

	/** Determine capacity to be dispatched based on maximum consumption
	 * 
	 * @param messages not used
	 * @param contracts with the {@link VariableRenewablePlantOperator} to send it the dispatch assignment */
	private void assignDispatch(ArrayList<Message> messages, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(messages);
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		PpaInformation ppaInformation = message.getDataItemOfType(PpaInformation.class);
		strategist.updateMaximumConsumption(ppaInformation.validAt, ppaInformation.yieldPotential);
		AmountAtTime dispatch = new AmountAtTime(ppaInformation.validAt, strategist.getMaximumConsumption());
		fulfilNext(contract, dispatch);
	}

	/** Sell Hydrogen according to production schedule following the contracted renewable power plant
	 * 
	 * @param messages not used
	 * @param contracts a contract with one {@link FuelsMarket} */
	private void sellProducedGreenHydrogen(ArrayList<Message> messages, List<Contract> contracts) {
		DispatchSchedule schedule = strategist.getValidSchedule(now());
		TimeStamp deliveryTime = schedule.getTimeOfFirstElement();
		double chargingPowerInMWH = schedule.getScheduledChargingPowerInMW(deliveryTime);
		double costs = ppaPriceInEURperMWH * chargingPowerInMWH;
		double producedHydrogenInThermalMWH = electrolyzer.calcProducedHydrogenOneHour(chargingPowerInMWH,
				deliveryTime);
		strategist.updateProducedHydrogenTotal(producedHydrogenInThermalMWH);
		sendHydrogenSellMessage(contracts, producedHydrogenInThermalMWH, deliveryTime);

		store(Outputs.AwardedEnergyInMWH, chargingPowerInMWH);
		store(Outputs.ProducedHydrogenInMWH, producedHydrogenInThermalMWH);
		store(FlexibilityTrader.Outputs.VariableCostsInEUR, costs);
	}

	/** Sends a single {@link FuelData} message to one contracted {@link FuelsMarket} to sell the given amount of hydrogen */
	private void sendHydrogenSellMessage(List<Contract> contracts, double producedHydrogenInThermalMWH,
			TimeStamp deliveryTime) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		FuelBid fuelBid = new FuelBid(deliveryTime, producedHydrogenInThermalMWH, BidType.Supply, FuelType.HYDROGEN);
		sendFuelBid(contract, fuelBid);
	}

	/** Evaluate revenues (i.e. negative purchase cost) from selling hydrogen at the fuels market
	 * 
	 * @param messages one AmountAtTime message from fuels market
	 * @param contracts ignored */
	private void digestSaleReturns(ArrayList<Message> messages, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(messages);
		double cost = readFuelBillMessage(message);
		store(FlexibilityTrader.Outputs.ReceivedMoneyInEUR, -cost);
	}

	/** Pay client according to PPA specification
	 * 
	 * @param messages contain PPA information
	 * @param contracts payment to client from PPA */
	private void payoutClient(ArrayList<Message> messages, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(messages);
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		PpaInformation ppaInformation = message.getDataItemOfType(PpaInformation.class);
		double payment = strategist.getMaximumConsumption() * ppaInformation.price;
		AmountAtTime paymentToClient = new AmountAtTime(ppaInformation.validAt, payment);
		fulfilNext(contract, paymentToClient);
		store(FlexibilityTrader.Outputs.ReceivedMoneyInEUR, -payment);
	}

	@Override
	protected double getInstalledCapacityInMW() {
		return electrolyzer.getPeakPower(now());
	}

	@Override
	protected Strategist getStrategist() {
		return strategist;
	}

}
