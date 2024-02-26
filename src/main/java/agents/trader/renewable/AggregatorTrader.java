// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader.renewable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import agents.forecast.MarketForecaster;
import agents.forecast.PowerForecastError;
import agents.markets.DayAheadMarket;
import agents.markets.DayAheadMarketTrader;
import agents.plantOperator.PowerPlantOperator;
import agents.plantOperator.RenewablePlantOperator;
import agents.plantOperator.RenewablePlantOperator.SetType;
import agents.policy.SupportPolicy;
import agents.policy.SupportPolicy.EnergyCarrier;
import agents.trader.ClientData;
import agents.trader.PowerPlantScheduler;
import agents.trader.Trader;
import agents.trader.TraderWithClients;
import communications.message.AmountAtTime;
import communications.message.AwardData;
import communications.message.BidData;
import communications.message.ClearingTimes;
import communications.message.MarginalCost;
import communications.message.SupportRequestData;
import communications.message.SupportResponseData;
import communications.message.TechnologySet;
import communications.message.YieldPotential;
import communications.portable.SupportData;
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
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Aggregates supply capacity and administers support payments to plant operators
 * 
 * @author Johannes Kochems, Christoph Schimeczek, Felix Nitsch, Farzad Sarfarazi, Kristina Nienhaus */
public abstract class AggregatorTrader extends TraderWithClients {
	@Input private static final Tree parameters = Make.newTree().addAs("ForecastError", PowerForecastError.parameters)
			.buildTree();

	static final String ERR_NO_MESSAGE_FOUND = "No client data received for client: ";
	static final String ERR_SUPPORT_INFO = "Support info not implemented: ";
	static final String ERR_NO_CLIENT_FOR_SET = " has no client with technology set type: ";
	static final String ERR_TIMESTAMP_LEFTOVER = "Accounting period mismatch; No payout was obtained for dispatch at time stamp: ";

	/** Columns of the output file */
	@Output
	protected static enum OutputColumns {
		/** amount of energy offered */
		OfferedEnergyInMWH,
		/** amount of energy awarded */
		AwardedEnergyInMWH,
		/** overall received support payments from policy agent */
		ReceivedSupportInEUR,
		/** overall support refunded to policy agent (in CFD scheme) */
		RefundedSupportInEUR,
		/** overall received market revenues from marketing power plants */
		ReceivedMarketRevenues,
		/** actual electricity generation potential */
		TrueGenerationPotentialInMWH
	};

	/** Products of this Agent */
	@Product
	public static enum Products {
		/** Request for support information for contracted technology set(s) */
		SupportInfoRequest,
		/** Request to obtain support payments for contracted technology set(s) */
		SupportPayoutRequest,
		/** Yield potential of contracted technology set(s) */
		YieldPotential
	};

	/** Submitted Bids */
	protected final TreeMap<TimeStamp, ArrayList<BidData>> submittedBidsByTime = new TreeMap<>();
	/** Map to store all client, i.e. {@link RenewablePlantOperator}, specific data */
	protected final HashMap<Long, ClientData> clientMap = new HashMap<>();
	/** Stores the power prices from {@link DayAheadMarket} */
	protected final TreeMap<TimeStamp, Double> powerPrices = new TreeMap<>();
	/** Adds random errors (normally distributed) to the amount of offered power */
	protected PowerForecastError errorGenerator;

	/** Creates an {@link AggregatorTrader}
	 * 
	 * @param dataProvider provides input from config */
	public AggregatorTrader(DataProvider dataProvider) {
		super(dataProvider);
		ParameterData inputData = parameters.join(dataProvider);
		try {
			errorGenerator = new PowerForecastError(inputData.getGroup("ForecastError"), getNextRandomNumberGenerator());
		} catch (MissingDataException e) {
			errorGenerator = null;
		}

		call(this::registerClient).on(RenewablePlantOperator.Products.SetRegistration)
				.use(RenewablePlantOperator.Products.SetRegistration);
		call(this::requestSupportInfo).on(Products.SupportInfoRequest);
		call(this::digestSupportInfo).on(SupportPolicy.Products.SupportInfo).use(SupportPolicy.Products.SupportInfo);
		call(this::prepareForecastBids).on(Trader.Products.BidsForecast)
				.use(PowerPlantOperator.Products.MarginalCostForecast);
		call(this::prepareBids).on(DayAheadMarketTrader.Products.Bids).use(PowerPlantOperator.Products.MarginalCost);
		call(this::sendYieldPotentials).on(Products.YieldPotential).use(DayAheadMarket.Products.GateClosureInfo);
		call(this::assignDispatch).on(PowerPlantScheduler.Products.DispatchAssignment).use(DayAheadMarket.Products.Awards);
		call(this::requestSupportPayout).on(Products.SupportPayoutRequest);
		call(this::digestSupportPayout).on(SupportPolicy.Products.SupportPayout).use(SupportPolicy.Products.SupportPayout);
		call(this::payoutClients).on(TraderWithClients.Products.Payout);
	}

	/** Extract information on {@link TechnologySet} and add it to the client data collection
	 * 
	 * @param messages client registration information: {@link TechnologySet}
	 * @param contracts not used */
	private void registerClient(ArrayList<Message> messages, List<Contract> contracts) {
		for (Contract contract : contracts) {
			long clientId = contract.getSenderId();
			ClientData clientData = searchClientData(messages, clientId);
			clientMap.put(clientId, clientData);
		}
	}

	/** Find client data based on client's Id */
	private ClientData searchClientData(ArrayList<Message> messages, long clientId) {
		for (Message message : messages) {
			if (message.senderId == clientId) {
				TechnologySet technologySet = message.getDataItemOfType(TechnologySet.class);
				double installedPowerInMW = message.getDataItemOfType(AmountAtTime.class).amount;
				return new ClientData(technologySet, installedPowerInMW);
			}
		}
		throw new RuntimeException(ERR_NO_MESSAGE_FOUND + clientId);
	}

	/** Request support details by informing it of the {@link TechnologySet}s and their instrument
	 * 
	 * @param messages not used
	 * @param contracts single partner (typically {@link SupportPolicy}) */
	private void requestSupportInfo(ArrayList<Message> messages, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		for (ClientData clientData : clientMap.values()) {
			TechnologySet technologySet = clientData.getTechnologySet();
			fulfilNext(contract, technologySet);
		}
	}

	/** Processes the received support information and adds it to the associated client's data
	 * 
	 * @param messages support information, typically from {@link SupportPolicy}
	 * @param contracts not used */
	private void digestSupportInfo(ArrayList<Message> messages, List<Contract> contracts) {
		for (Message message : messages) {
			SupportData supportData = message.getFirstPortableItemOfType(SupportData.class);
			SetType technologySetType = supportData.getSetType();
			for (ClientData clientData : getClientDataForSetType(technologySetType)) {
				clientData.setSupportData(supportData);
			}
		}
	}

	/** Return all data of clients that match the given setType
	 * @param setType to search for
	 * @return client data for given set type */
	protected List<ClientData> getClientDataForSetType(SetType setType) {
		List<ClientData> clients = new ArrayList<>();
		for (ClientData clientData : clientMap.values()) {
			if (clientData.getTechnologySet().setType == setType) {
				clients.add(clientData);
			}
		}
		if (clients.isEmpty()) {
			throw new RuntimeException(this + ERR_NO_CLIENT_FOR_SET + setType);
		}
		return clients;
	}

	/** Sends supply {@link BidData bid} forecasts
	 * 
	 * @param messages marginal cost forecasts to process - typically from {@link RenewablePlantOperator}
	 * @param contractsToFulfill one partner to send bid forecasts to, typically a {@link MarketForecaster} */
	private void prepareForecastBids(ArrayList<Message> messages, List<Contract> contractsToFulfill) {
		Contract contract = CommUtils.getExactlyOneEntry(contractsToFulfill);
		TreeMap<TimeStamp, ArrayList<Message>> messagesByTimeStamp = sortMarginalsByTimeStamp(messages);
		for (TimeStamp deliveryTime : messagesByTimeStamp.keySet()) {
			ArrayList<Message> timeMessages = messagesByTimeStamp.get(deliveryTime);
			ArrayList<MarginalCost> sortedMarginalsForecast = getSortedMarginalList(timeMessages);
			submitHourlyBids(deliveryTime, contract, sortedMarginalsForecast);
		}
	}

	/** Prepares hourly bids based on given marginals and sends them to the contracted partner
	 * 
	 * @param time at which to calculate bids
	 * @param contract to fulfil
	 * @param sortedMarginals to be used for bid calculation
	 * @return submitted bids */
	protected abstract ArrayList<BidData> submitHourlyBids(TimeStamp time, Contract contract,
			ArrayList<MarginalCost> sortedMarginals);

	/** Sends supply {@link BidData} bids to {@link DayAheadMarket}
	 * 
	 * @param messages marginal cost to process - typically from {@link RenewablePlantOperator} - possibly for multiple times
	 * @param contracts one {@link DayAheadMarket} to send bids to */
	private void prepareBids(ArrayList<Message> messages, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TreeMap<TimeStamp, ArrayList<MarginalCost>> marginalsByTimeStamp = splitMarginalsByTimeStamp(messages);
		for (TimeStamp targetTime : marginalsByTimeStamp.keySet()) {
			ArrayList<MarginalCost> marginals = marginalsByTimeStamp.get(targetTime);
			marginals.sort(MarginalCost.byCostAscending);
			ArrayList<MarginalCost> marginalsWithError = addPowerForecastErrors(marginals);
			ArrayList<BidData> submittedBids = submitHourlyBids(targetTime, contract, marginalsWithError);
			submittedBidsByTime.put(targetTime, submittedBids);
			storeYieldPotentials(submittedBids);
			storeOfferedEnergy(submittedBids);
		}
	}

	/** @return HashMap with marginals split with respect to their deliveryTime */
	private TreeMap<TimeStamp, ArrayList<MarginalCost>> splitMarginalsByTimeStamp(ArrayList<Message> messages) {
		TreeMap<TimeStamp, ArrayList<MarginalCost>> marginalsByTimeStamp = new TreeMap<>();
		for (Message message : messages) {
			MarginalCost marginalCost = message.getDataItemOfType(MarginalCost.class);
			TimeStamp time = marginalCost.deliveryTime;
			if (!marginalsByTimeStamp.containsKey(time)) {
				marginalsByTimeStamp.put(time, new ArrayList<MarginalCost>());
			}
			marginalsByTimeStamp.get(time).add(marginalCost);
		}
		return marginalsByTimeStamp;
	}

	/** Returns list of marginals that include power forecast errors based on the given marginals (without errors) */
	private ArrayList<MarginalCost> addPowerForecastErrors(ArrayList<MarginalCost> marginals) {
		if (errorGenerator != null) {
			ListIterator<MarginalCost> iterator = marginals.listIterator();
			while (iterator.hasNext()) {
				MarginalCost item = iterator.next();
				double powerForecastWithError = errorGenerator.calcPowerWithError(item.powerPotentialInMW);
				iterator.set(new MarginalCost(item, powerForecastWithError));
			}
		}
		return marginals;
	}

	/** Store {@link YieldPotential}s for RES market value calculation **/
	private void storeYieldPotentials(ArrayList<BidData> bids) {
		for (BidData bid : bids) {
			ClientData clientData = clientMap.get(bid.producerUuid);
			clientData.appendYieldPotential(bid.deliveryTime, bid.powerPotentialInMW);
		}
	}

	/** Store the amount of energy offered */
	private void storeOfferedEnergy(ArrayList<BidData> bids) {
		double totalEnergyOffered = 0.;
		for (BidData bid : bids) {
			totalEnergyOffered += bid.offeredEnergyInMWH;
		}
		store(OutputColumns.OfferedEnergyInMWH, totalEnergyOffered);
	}

	/** Forward yield potential information from clients
	 * 
	 * @param messages one ClearingTimes message
	 * @param contracts one partner, typically {@link SupportPolicy} */
	private void sendYieldPotentials(ArrayList<Message> messages, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		ClearingTimes clearingTimes = CommUtils.getExactlyOneEntry(messages).getDataItemOfType(ClearingTimes.class);
		for (TimeStamp time : clearingTimes.getTimes()) {
			for (ClientData clientData : clientMap.values()) {
				double yieldPotential = clientData.getYieldPotential().get(time);
				EnergyCarrier energyCarrier = clientData.getTechnologySet().energyCarrier;
				fulfilNext(contract, new YieldPotential(time, yieldPotential, energyCarrier));
			}
		}
	}

	/** Determine capacity to be dispatched based on {@link AwardData} from {@link DayAheadMarket}; dispatch is assigned in
	 * ascending order of bid prices, thus accounting for marginal cost + expected policy payments
	 * 
	 * @param messages single award message from {@link DayAheadMarket}
	 * @param contracts clients (typically {@link RenewablePlantOperator}s) to receive their dispatch assignment */
	private void assignDispatch(ArrayList<Message> messages, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(messages);
		AwardData award = message.getDataItemOfType(AwardData.class);
		store(OutputColumns.AwardedEnergyInMWH, award.supplyEnergyInMWH);
		double energyToDispatch = award.supplyEnergyInMWH;
		ArrayList<BidData> submittedBids = submittedBidsByTime.remove(award.beginOfDeliveryInterval);
		submittedBids.sort(BidData.BY_PRICE_ASCENDING);
		double actualProductionPotentialInMWH = 0;
		for (BidData bid : submittedBids) {
			Contract matchingContract = getMatchingContract(contracts, bid.producerUuid);
			double dispatchedEnergy = Math.min(energyToDispatch, bid.powerPotentialInMW);
			actualProductionPotentialInMWH += bid.powerPotentialInMW;
			logClientDispatchAndRevenues(bid, dispatchedEnergy, award.powerPriceInEURperMWH);
			fulfilNext(matchingContract, new AmountAtTime(award.beginOfDeliveryInterval, dispatchedEnergy));
			energyToDispatch = Math.max(0, energyToDispatch - dispatchedEnergy);
		}
		powerPrices.put(award.beginOfDeliveryInterval, award.powerPriceInEURperMWH);
		store(OutputColumns.TrueGenerationPotentialInMWH, actualProductionPotentialInMWH);
	}

	/** Logs actual dispatch and revenue for client of given BidData at its delivery time
	 * 
	 * @param bid original bid sent for clearing
	 * @param dispatchedEnergy assigned to this bid
	 * @param powerPrice awarded price */
	protected void logClientDispatchAndRevenues(BidData bid, double dispatchedEnergy, double powerPrice) {
		ClientData clientData = clientMap.get(bid.producerUuid);
		double stepRevenue = powerPrice * dispatchedEnergy;
		clientData.appendStepDispatchAndRevenue(bid.deliveryTime, dispatchedEnergy, stepRevenue);
	}

	/** Request support pay-out - one message per client
	 * 
	 * @param messages not used
	 * @param contracts one partner to ask for policy pay-out, i.e. {@link SupportPolicy} */
	private void requestSupportPayout(ArrayList<Message> messages, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TimePeriod accountingPeriod = SupportPolicy.extractAccountingPeriod(now(), contract, 1800L);
		for (Entry<Long, ClientData> entries : clientMap.entrySet()) {
			SupportRequestData supportData = new SupportRequestData(entries, accountingPeriod);
			fulfilNext(contract, supportData);
		}
		powerPrices.headMap(accountingPeriod.getLastTime()).clear();
	}

	/** Collect and store the support revenues per client, i.e. operator of the {@link TechnologySet}
	 * 
	 * @param messages one pay-out message per client
	 * @param contracts not used */
	private void digestSupportPayout(ArrayList<Message> messages, List<Contract> contracts) {
		double receivedSupport = 0.0;
		double refundedSupport = 0.0;
		for (Message message : messages) {
			SupportResponseData supportDataResponse = message.getDataItemOfType(SupportResponseData.class);
			ClientData clientData = clientMap.get(supportDataResponse.clientId);
			clientData.appendSupportRevenue(supportDataResponse.accountingPeriod, supportDataResponse.payment);
			clientData.appendMarketPremium(supportDataResponse.accountingPeriod, supportDataResponse.marketPremium);
			receivedSupport += Math.max(0, supportDataResponse.payment);
			refundedSupport -= Math.min(0, supportDataResponse.payment);
		}
		store(OutputColumns.ReceivedSupportInEUR, receivedSupport);
		store(OutputColumns.RefundedSupportInEUR, refundedSupport);
	}

	/** Forward support pay-out to clients
	 * 
	 * @param messages not used
	 * @param contracts with clients to pay-out, typically {@link RenewablePlantOperator}s */
	private void payoutClients(ArrayList<Message> messages, List<Contract> contracts) {
		double receivedMarketRevenues = 0;
		for (Contract contract : contracts) {
			long plantOperatorId = contract.getReceiverId();
			TimePeriod accountingPeriod = SupportPolicy.extractAccountingPeriod(now(), contract, 1796L); // -3L
			double marketRevenue = calcMarketRevenue(plantOperatorId, accountingPeriod);
			receivedMarketRevenues += marketRevenue;
			double payout = applyPayoutStrategy(plantOperatorId, accountingPeriod, marketRevenue);
			AmountAtTime contractualPayout = new AmountAtTime(now(), payout);
			fulfilNext(contract, contractualPayout);
			clientMap.get(plantOperatorId).clearBefore(now());
		}
		store(OutputColumns.ReceivedMarketRevenues, receivedMarketRevenues);
	}

	/** Define a pay-out strategy for the contractual pay-out (in child classes)
	 * 
	 * @param plantOperatorId the operator to pay
	 * @param accountingPeriod to be considered
	 * @param marketRevenue earned with bids associated with that operator
	 * @return value to pay out to that plant operator */
	protected abstract double applyPayoutStrategy(long plantOperatorId, TimePeriod accountingPeriod,
			double marketRevenue);

	/** Calculate and return the sum of market revenues during a given accounting period for a given client */
	private double calcMarketRevenue(long plantOperatorId, TimePeriod accountingPeriod) {
		ClientData clientData = clientMap.get(plantOperatorId);
		double marketRevenue = 0;
		Iterator<Entry<TimeStamp, Double>> iterator = clientData.getMarketRevenue().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<TimeStamp, Double> entry = iterator.next();
			if (entry.getKey().isLessThan(accountingPeriod.getStartTime())) {
				throw new RuntimeException(ERR_TIMESTAMP_LEFTOVER + entry.getKey());
			}
			if (entry.getKey().isLessEqualTo(accountingPeriod.getLastTime())) {
				marketRevenue += entry.getValue();
				iterator.remove();
			}
		}
		return marketRevenue;
	}
}
