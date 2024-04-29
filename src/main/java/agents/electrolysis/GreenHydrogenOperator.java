// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.electrolysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import agents.markets.DayAheadMarket;
import agents.markets.FuelsMarket;
import agents.markets.FuelsMarket.FuelType;
import agents.markets.FuelsTrader;
import agents.plantOperator.renewable.VariableRenewableOperator;
import agents.trader.ElectrolysisTrader;
import agents.trader.PowerPlantScheduler;
import communications.message.AmountAtTime;
import communications.message.ClearingTimes;
import communications.message.FuelBid;
import communications.message.FuelBid.BidType;
import communications.message.FuelData;
import communications.message.PpaInformation;
import de.dlr.gitlab.fame.agent.Agent;
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
 * @author Johannes Kochems, Leonard Willeke, Christoph Schimeczek */
public class GreenHydrogenOperator extends Agent implements FuelsTrader, PowerPlantScheduler {
	@Input private static final Tree parameters = Make.newTree().addAs("Device", Electrolyzer.parameters).buildTree();

	@Output
	private static enum Outputs {
		ConsumedElectricityInMWH, ProducedHydrogenInMWH, VariableCostsInEUR, ReceivedMoneyInEUR
	};

	@Product
	public static enum Products {
		PpaInformationRequest
	};

	private final Electrolyzer electrolyzer;
	private final TreeMap<TimeStamp, Double> dispatch = new TreeMap<>();

	/** Creates a new {@link ElectrolysisTrader} based on given input parameters
	 * 
	 * @param data configured input
	 * @throws MissingDataException if any required input is missing */
	public GreenHydrogenOperator(DataProvider data) throws MissingDataException {
		super(data);
		ParameterData input = parameters.join(data);
		electrolyzer = new Electrolyzer(input.getGroup("Device"));

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
		fulfilNext(contract, message.getDataItemOfType(ClearingTimes.class));
	}

	/** Determine capacity to be dispatched based on maximum consumption
	 * 
	 * @param messages not used
	 * @param contracts with the {@link VariableRenewablePlantOperator} to send it the dispatch assignment */
	private void assignDispatch(ArrayList<Message> messages, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(messages);
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		PpaInformation ppa = message.getDataItemOfType(PpaInformation.class);
		double usableElectricityInMWH = electrolyzer.calcCappedElectricDemandInMW(ppa.yieldPotential, ppa.validAt);
		dispatch.put(ppa.validAt, usableElectricityInMWH);
		fulfilNext(contract, new AmountAtTime(ppa.validAt, usableElectricityInMWH));
	}

	/** Sell hydrogen according to production schedule following the contracted renewable power plant
	 * 
	 * @param messages not used
	 * @param contracts a contract with one {@link FuelsMarket} */
	private void sellProducedGreenHydrogen(ArrayList<Message> messages, List<Contract> contracts) {
		SortedMap<TimeStamp, Double> pastDispatch = dispatch.headMap(now());
		double totalConsumedElectricityInMWH = 0;
		double totalProducedHydrogenInThermalMWH = 0;
		for (Entry<TimeStamp, Double> entry : pastDispatch.entrySet()) {
			totalConsumedElectricityInMWH += entry.getValue();
			double producedHydrogenInThermalMWH = electrolyzer.calcProducedHydrogenOneHour(entry.getValue(), entry.getKey());
			totalProducedHydrogenInThermalMWH += producedHydrogenInThermalMWH;
			sendHydrogenSellMessage(contracts, producedHydrogenInThermalMWH, entry.getKey());
		}
		pastDispatch.clear();
		store(Outputs.ConsumedElectricityInMWH, totalConsumedElectricityInMWH);
		store(Outputs.ProducedHydrogenInMWH, totalProducedHydrogenInThermalMWH);
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
		store(Outputs.ReceivedMoneyInEUR, -cost);
	}

	/** Pay client according to PPA specification
	 * 
	 * @param messages contain PPA information
	 * @param contracts payment to client from PPA */
	private void payoutClient(ArrayList<Message> messages, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(messages);
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		PpaInformation ppa = message.getDataItemOfType(PpaInformation.class);
		double consumedElectricityInMWH = electrolyzer.calcProducedHydrogenOneHour(ppa.yieldPotential, ppa.validAt);
		double payment = consumedElectricityInMWH * ppa.price;
		fulfilNext(contract, new AmountAtTime(ppa.validAt, payment));
		store(Outputs.VariableCostsInEUR, payment);
	}
}
