// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import agents.forecast.MarketForecaster;
import agents.markets.DayAheadMarket;
import agents.markets.DayAheadMarketTrader;
import agents.markets.meritOrder.Bid.Type;
import agents.plantOperator.ConventionalPlantOperator;
import agents.plantOperator.PowerPlantOperator;
import communications.message.AmountAtTime;
import communications.message.AwardData;
import communications.message.BidData;
import communications.message.MarginalCost;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.logging.Logging;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimeStamp;
import util.Util;

/** Sells energy of one conventional PowerPlantOperator at the {@link DayAheadMarket}
 *
 * @author Christoph Schimeczek, Marc Deissenroth, Ulrich Frey */
public class ConventionalTrader extends Trader {
	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newDouble("minMarkup"), Make.newDouble("maxMarkup")).buildTree();

	@Output
	private static enum OutputFields {
		OfferedPowerInMW, AwardedPower
	}

	private double minMarkup;
	private double maxMarkup;

	/** Creates a {@link ConventionalTrader}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not */
	public ConventionalTrader(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		minMarkup = input.getDouble("minMarkup");
		maxMarkup = input.getDouble("maxMarkup");
		ensureValidMarkups();

		call(this::sendForecastBids).on(Trader.Products.BidsForecast)
				.use(PowerPlantOperator.Products.MarginalCostForecast);
		call(this::sendBids).on(DayAheadMarketTrader.Products.Bids).use(PowerPlantOperator.Products.MarginalCost);
		call(this::assignDispatch).on(Trader.Products.DispatchAssignment).use(DayAheadMarket.Products.Awards);
		call(this::payout).on(Trader.Products.Payout).use(DayAheadMarket.Products.Awards);
	}

	/** @throws RuntimeException if {@link #minMarkup} > {@link #maxMarkup} */
	private void ensureValidMarkups() {
		try {
			Util.ensureValidRange(minMarkup, maxMarkup);
		} catch (InvalidParameterException e) {
			throw Logging.logFatalException(logger, "Markups of " + this + ":: " + e.getMessage());
		}
	}

	/** Sends supply {@link BidData bids} to {@link DayAheadMarket} and stores offered power
	 * 
	 * @param messages marginal cost data from client
	 * @param contracts single {@link DayAheadMarket} to send bids to */
	private void sendBids(ArrayList<Message> messages, List<Contract> contracts) {
		Contract contractToFulfil = CommUtils.getExactlyOneEntry(contracts);
		double totalOfferedPowerInMW = 0;
		for (BidData bid : prepareBids(messages)) {
			totalOfferedPowerInMW += bid.offeredEnergyInMWH;
			sendDayAheadMarketBids(contractToFulfil, bid);
		}
		store(OutputFields.OfferedPowerInMW, totalOfferedPowerInMW);
	}

	/** Create {@link BidData bids} from given marginals
	 * 
	 * @param input marginal costs from PowerPlantOperators */
	private List<BidData> prepareBids(ArrayList<Message> input) {
		ArrayList<MarginalCost> marginals = getSortedMarginalList(input);
		ArrayList<Double> markups = Util.linearInterpolation(minMarkup, maxMarkup, marginals.size());
		TimeStamp deliveryTime = input.get(0).getDataItemOfType(MarginalCost.class).deliveryTime;
		List<BidData> bids = new ArrayList<>(marginals.size());
		for (int i = 0; i < marginals.size(); i++) {
			MarginalCost marginal = marginals.get(i);
			double markup = markups.get(i);
			double offeredPriceInEURperMWH = marginal.marginalCostInEURperMWH + markup;
			bids.add(new BidData(marginal.powerPotentialInMW, offeredPriceInEURperMWH, marginal.marginalCostInEURperMWH,
					getId(), Type.Supply, deliveryTime));
		}
		return bids;
	}

	/** Prepares forecast bids grouped by time stamps
	 * 
	 * @param messages marginal cost forecasts from associated PowerPlantOperators
	 * @param contractsToFulfill single contract, typically with a {@link MarketForecaster} */
	private void sendForecastBids(ArrayList<Message> messages, List<Contract> contracts) {
		Contract contractToFulfil = CommUtils.getExactlyOneEntry(contracts);
		TreeMap<TimeStamp, ArrayList<Message>> messagesByTimeStamp = sortMarginalsByTimeStamp(messages);
		for (ArrayList<Message> messagesAtTime : messagesByTimeStamp.values()) {
			for (BidData bid : prepareBids(messagesAtTime)) {
				fulfilNext(contractToFulfil, bid);
			}
		}
	}

	/** Assigns dispatch from {@link DayAheadMarket} to power plant operators and writes information to output
	 * 
	 * @param messages one single message containing award information (price, awarded power)
	 * @param contracts single contract with associated PowerPlantOperator to order the energy to deliver */
	private void assignDispatch(ArrayList<Message> messages, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(messages);
		Contract contract = CommUtils.getExactlyOneEntry(contracts);

		AwardData award = message.getDataItemOfType(AwardData.class);
		fulfilNext(contract, new AmountAtTime(award.beginOfDeliveryInterval, award.supplyEnergyInMWH));
		store(OutputFields.AwardedPower, award.supplyEnergyInMWH);
	}

	/** Sends pay-out to {@link ConventionalPlantOperator}
	 * 
	 * @param messages one single message containing award information (price, awarded power)
	 * @param contracts single contract with associated PowerPlantOperator to send its pay-out */
	private void payout(ArrayList<Message> messages, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(messages);
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		AwardData award = message.getDataItemOfType(AwardData.class);
		double payoutInEUR = award.supplyEnergyInMWH * award.powerPriceInEURperMWH;
		fulfilNext(contract, new AmountAtTime(award.beginOfDeliveryInterval, payoutInEUR));
	}
}