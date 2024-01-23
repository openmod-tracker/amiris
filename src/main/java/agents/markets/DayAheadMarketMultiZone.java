// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.markets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import agents.markets.meritOrder.Bid;
import agents.markets.meritOrder.ClearingResult;
import agents.markets.meritOrder.MarketClearing;
import agents.markets.meritOrder.MarketClearingResult;
import agents.markets.meritOrder.MeritOrderKernel;
import agents.markets.meritOrder.MeritOrderKernel.MeritOrderClearingException;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.OrderBook.DistributionMethod;
import agents.markets.meritOrder.books.SupplyOrderBook;
import agents.markets.meritOrder.books.TransferOrderBook;
import agents.markets.meritOrder.books.TransmissionBook;
import communications.message.AwardData;
import communications.message.BidData;
import communications.message.CouplingData;
import communications.message.TransmissionCapacity;
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
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.logging.Logging;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Energy exchange performs local market clearing for day-ahead energy market.
 * 
 * @author Christoph Schimeczek, A. Achraf El Ghazi, Felix Nitsch, Johannes Kochems */
public class DayAheadMarketMultiZone extends DayAheadMarket {
	static final String REGION_MISSING = "No region value found";
	static final String TIME_SERIES_MISSING = "No transmission TIME_SERIES found for region: ";

	/** All available market regions */
	public static enum Region {
		DE, FR, PL, AT, BE, NL, NO, LU, SE, DKW, DKO, CZ, CH
	};

	@Product
	public static enum Products {
		/** Transmission capacities and bids from local exchange */
		TransmissionAndBids,
	};

	@Output
	private static enum OutputFields {
		PreCouplingTotalAwardedPowerInMW, PreCouplingElectricityPriceInEURperMWH, PreCouplingDispatchSystemCostInEUR
	};

	@Input private static final Tree parameters = Make.newTree()
			.add(
					Make.newEnum("DistributionMethod", DistributionMethod.class),
					Make.newEnum("Region", Region.class).optional(),
					Make.newGroup("Transmission").list()
							.add(
									Make.newEnum("Region", Region.class).optional(),
									Make.newSeries("CapacityInMW").optional()))
			.buildTree();

	private final MarketClearing marketClearing;
	/** Market region of this energy exchange instance */
	private final Region region;
	private DemandOrderBook demandBook = new DemandOrderBook();
	private SupplyOrderBook supplyBook = new SupplyOrderBook();
	private final HashMap<Region, TimeSeries> transmissionCapacities = new HashMap<>();
	private TransferOrderBook importBook = new TransferOrderBook();
	private TransferOrderBook exportBook = new TransferOrderBook();

	/** Holds net export / import to other regions */
	private class NetEnergyTransfer {
		public double netImportsInMWH;
		public double netExportsInMWH;

		public NetEnergyTransfer(double netImportsInMWH, double netExportsInMWH) {
			this.netImportsInMWH = netImportsInMWH;
			this.netExportsInMWH = netExportsInMWH;
		}
	}

	public DayAheadMarketMultiZone(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);

		marketClearing = new MarketClearing(input);
		region = input.getEnumOrDefault("Region", Region.class, null);
		if (region != null) {
			loadTransmissionCapacities(input.getGroupList("Transmission"));
		}

		call(this::digestBids).on(DayAheadMarketTrader.Products.Bids).use(DayAheadMarketTrader.Products.Bids);
		call(this::provideTransmissionAndBids).on(Products.TransmissionAndBids);
		call(this::clearMarket).on(DayAheadMarket.Products.Awards).use(MarketCoupling.Products.MarketCouplingResult);
	}

	/** Loads all transmission capacity time-series and stores them with the corresponding target region as key
	 * 
	 * @param transmissions list of all available transmission time-series with the current EnergyExchange as origin region of
	 *          supply */
	private void loadTransmissionCapacities(List<ParameterData> transmissions) {
		for (ParameterData dataItem : transmissions) {
			Region targetRegion = null;
			try {
				targetRegion = dataItem.getEnum("Region", Region.class);
			} catch (MissingDataException e) {
				logger.error(REGION_MISSING);
			}
			try {
				transmissionCapacities.put(targetRegion, dataItem.getTimeSeries("CapacityInMW"));
			} catch (MissingDataException e) {
				logger.error(TIME_SERIES_MISSING + targetRegion);
			}
		}
	}

	/** Collects the received trader bids in {@link EnergyExchange #demandBook} and {@link EnergyExchange #supplyBook}, according to
	 * their type.
	 * 
	 * @param input messages specifying the trader bids
	 * @param contracts with the Trader Agent's */
	private void digestBids(ArrayList<Message> input, List<Contract> contracts) {
		fillOrderBooksWithTraderBids(input, supplyBook, demandBook);
	}

	/** Builds a CouplingRequest and sends it to the contracted MarketCoupling Agent. The CouplingRequest contains: the local
	 * DemandOrderBook, the local SupplyOrderBook, and the TransmissionCapacity's from the Region of this EnergyExchange to all
	 * EnergyExchange's, that are coupled with it.
	 * 
	 * @param input not-used, not-expected
	 * @param contracts with the MarketCoupling Agent */
	private void provideTransmissionAndBids(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);

		TransmissionBook transmissionBook = new TransmissionBook(region);
		for (Region targetRegion : transmissionCapacities.keySet()) {
			transmissionBook.add(getTransmissionCapacity(targetRegion, now()));
		}

		ArrayList<Message> inputCopy = new ArrayList<>();
		for (Message msg : input) {
			inputCopy.add(msg.deepCopy());
		}
		MarketClearingResult result = marketClearing.calculateMarketClearing(inputCopy, this.toString() + " " + now());

		store(OutputFields.PreCouplingElectricityPriceInEURperMWH, result.getMarketPriceInEURperMWH());
		store(OutputFields.PreCouplingTotalAwardedPowerInMW, result.getTradedEnergyInMWH());
		store(OutputFields.PreCouplingDispatchSystemCostInEUR, result.getSystemCostTotalInEUR());

		fulfilNext(contract, new CouplingData(demandBook, supplyBook, transmissionBook));
	}

	/** Returns the TransmissionCapacity for a given target Region and a given TimeStamp
	 * 
	 * @param targetRegion given target Region
	 * @param time given TimeStamp
	 * @return TransmissionCapacity for a given target Region and TimeStamp */
	private TransmissionCapacity getTransmissionCapacity(Region targetRegion, TimeStamp time) {
		double amount = getTransmissionCapacityAmount(targetRegion, time);
		TransmissionCapacity transmissionCapacity = new TransmissionCapacity(targetRegion, amount);
		return transmissionCapacity;
	}

	/** Returns the TransmissionCapacityAmount for a given target Region and a given TimeStamp
	 * 
	 * @param targetRegion given target Region
	 * @param time given TimeStamp
	 * @return transmission capacity amount for a given target Region and TimeStamp */
	private double getTransmissionCapacityAmount(Region targetRegion, TimeStamp time) {
		TimeSeries transmissionCapacityOverTime = transmissionCapacities.get(targetRegion);
		if (transmissionCapacityOverTime == null) {
			throw Logging.logFatalException(logger, TIME_SERIES_MISSING + targetRegion);
		}
		return transmissionCapacityOverTime.getValueLowerEqual(time);
	}

	/** Clears the local market and sends the Awards to the contracted Trader Agents. Depending on whether this EnergyExchange is
	 * involved in a coupled super-market (via contract to a MarketCoupling Agent) or not, it used the local or the coupled
	 * DemandOrderBook and SupplyOrderBook.
	 * 
	 * @param input one message from the MarketCoupling Agent containing the result of the market coupling
	 * @param contracts with the Trader Agents */
	private void clearMarket(ArrayList<Message> input, List<Contract> contracts) {
		if (input.size() > 0) {
			Message message = CommUtils.getExactlyOneEntry(input);
			CouplingData coupledData = message.getFirstPortableItemOfType(CouplingData.class);
			demandBook = coupledData.getDemandOrderBook();
			supplyBook = coupledData.getSupplyOrderBook();
			importBook = coupledData.getImportOrderBook();
			exportBook = coupledData.getExportOrderBook();
		} else {
			importBook = new TransferOrderBook();
			exportBook = new TransferOrderBook();
		}
		ClearingResult clearingResult;
		String clearingEventId = this.toString() + " " + now();
		try {
			clearingResult = MeritOrderKernel.clearMarketSimple(supplyBook, demandBook);
		} catch (MeritOrderClearingException e) {
			clearingResult = new ClearingResult(0.0, Double.NaN);
			logger.error(clearingEventId + ": Market clearing failed due to: " + e.getMessage());
		}
		MarketClearingResult marketClearingResult = new MarketClearingResult(clearingResult, demandBook, supplyBook);
		marketClearingResult.setBooks(supplyBook, demandBook, marketClearing.distributionMethod);
		NetEnergyTransfer energyTransfer = computeNetEngergyTransfer(importBook, exportBook);

		store(DayAheadMarket.OutputFields.ElectricityPriceInEURperMWH, marketClearingResult.getMarketPriceInEURperMWH());
		store(DayAheadMarket.OutputFields.AwardedEnergyInMWH, marketClearingResult.getTradedEnergyInMWH());
		store(DayAheadMarket.OutputFields.DispatchSystemCostInEUR, marketClearingResult.getSystemCostTotalInEUR());

		store(DayAheadMarket.OutputFields.AwardedNetEnergyFromImportInMWH, energyTransfer.netImportsInMWH);
		store(DayAheadMarket.OutputFields.AwardedNetEnergyToExportInMWH, energyTransfer.netExportsInMWH);

		sendAwardsToTraders(contracts, marketClearingResult, importBook, exportBook);
	}

	/** Computes the net accumulated export and import of energy according to the given import and export TransferOrderBook. Thereby
	 * the accumulated export and import are offset together. So only one of their net results is different that zero.
	 * 
	 * @param importBook import TransferOrderBook
	 * @param exportBook export TransferOrderBook
	 * @return a EnergyTransfer of the result */
	private NetEnergyTransfer computeNetEngergyTransfer(TransferOrderBook importBook, TransferOrderBook exportBook) {
		double netImportPower = importBook.getAccumulatedEnergyInMWH();
		double netExportPower = exportBook.getAccumulatedEnergyInMWH();
		if (netImportPower > netExportPower) {
			return new NetEnergyTransfer(netImportPower - netExportPower, 0.0);
		}
		return new NetEnergyTransfer(0.0, netExportPower - netImportPower);
	}

	/** Computes and sends the Awards for each contracted Trader Agent.
	 *
	 * @param contracts with the contracted Trader Agents
	 * @param result the result of the market clearing process
	 * @param importBook imports from other markets
	 * @param exportBook exports to other markets */
	private void sendAwardsToTraders(List<Contract> contracts, MarketClearingResult result, TransferOrderBook importBook,
			TransferOrderBook exportBook) {
		double powerPrice = result.getMarketPriceInEURperMWH();
		for (Contract contract : contracts) {
			long receiverId = contract.getReceiverId();

			double energyFromImports = importBook.getEnergySumForTrader(receiverId);
			double supplyPower = result.getSupplyBook().getTradersSumOfPower(receiverId);
			double demandPower = result.getDemandBook().getTradersSumOfPower(receiverId) + energyFromImports;

			List<TimeStamp> clearingTimeList = clearingTimes.getTimes();
			if (clearingTimeList.size() > 1) {
				throw new RuntimeException(LONE_LIST + clearingTimeList);
			}
			for (TimeStamp clearingTime : clearingTimeList) {
				AwardData awardData = new AwardData(supplyPower, demandPower, powerPrice, clearingTime);
				fulfilNext(contract, awardData);
			}
		}
	}

	/** Fills received Bids into provided demand or supply OrderBook
	 * 
	 * @param input unsorted messages containing demand and supply bids
	 * @param supplyBook to be filled with supply bids
	 * @param demandBook to be filled with demand bids */
	private void fillOrderBooksWithTraderBids(ArrayList<Message> input, SupplyOrderBook supplyBook,
			DemandOrderBook demandBook) {
		demandBook.clear();
		supplyBook.clear();
		for (Message message : input) {
			BidData bidData = message.getDataItemOfType(BidData.class);
			if (bidData == null) {
				throw new RuntimeException("No BidData in message from " + message.getSenderId());
			}
			Bid bid = bidData.getBid();
			switch (bid.getType()) {
				case Supply:
					supplyBook.addBid(bid);
					break;
				case Demand:
					demandBook.addBid(bid);
					break;
				default:
					throw new RuntimeException("Bid type unknown.");
			}
		}
	}
}
