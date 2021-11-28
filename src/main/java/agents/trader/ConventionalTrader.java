package agents.trader;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import agents.forecast.MarketForecaster;
import agents.markets.EnergyExchange;
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
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimeStamp;
import de.dlr.gitlab.fame.logging.Logging;
import util.Util;

/** Sells energy of one conventional PowerPlantOperator at the EnergyExchange
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
	private double totalOfferedPowerInMW;

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

		call(this::sendBids).on(Trader.Products.Bids).use(PowerPlantOperator.Products.MarginalCost);
		call(this::assignDispatch).on(Trader.Products.DispatchAssignment).use(EnergyExchange.Products.Awards);
		call(this::prepareForecastBids).on(Trader.Products.BidsForecast)
				.use(PowerPlantOperator.Products.MarginalCostForecast);
		call(this::payout).on(Trader.Products.Payout).use(EnergyExchange.Products.Awards);
	}

	/** @throws RuntimeException if {@link #minMarkup} > {@link #maxMarkup} */
	private void ensureValidMarkups() {
		try {
			Util.ensureValidRange(minMarkup, maxMarkup);
		} catch (InvalidParameterException e) {
			throw Logging.logFatalException(logger, "Markups of " + this + ":: " + e.getMessage());
		}
	}

	/** Sends supply {@link BidData bids} to partner and stores offered power
	 * 
	 * @param messages marginal cost data from client
	 * @param contracts single contract with typically {@link EnergyExchange} */
	private void sendBids(ArrayList<Message> messages, List<Contract> contracts) {
		Contract contractToFulfil = CommUtils.getExactlyOneEntry(contracts);
		prepareBids(messages, contractToFulfil);
		store(OutputFields.OfferedPowerInMW, totalOfferedPowerInMW);
	}

	/** Sends supply {@link BidData bids} to contracted partner
	 * 
	 * @param input marginal costs from associated PowerPlantOperators
	 * @param contractToFulfil contracted parter */
	private void prepareBids(ArrayList<Message> input, Contract contractToFulfil) {
		ArrayList<MarginalCost> marginals = getSortedMarginalList(input);
		ArrayList<Double> markups = Util.linearInterpolation(minMarkup, maxMarkup, marginals.size());
		TimeStamp deliveryTime = input.get(0).getDataItemOfType(MarginalCost.class).deliveryTime;

		totalOfferedPowerInMW = 0;
		for (int i = 0; i < marginals.size(); i++) {
			MarginalCost marginal = marginals.get(i);
			totalOfferedPowerInMW += marginal.powerPotentialInMW;
			double markup = markups.get(i);
			double offeredPriceInEURperMWH = marginal.marginalCostInEURperMWH + markup;
			BidData bid = new BidData(marginal.powerPotentialInMW, offeredPriceInEURperMWH, marginal.marginalCostInEURperMWH,
					getId(), Type.Supply, deliveryTime);
			fulfilNext(contractToFulfil, bid);
		}
	}

	/** Prepares forecast bids grouped by time stamps
	 * 
	 * @param messages marginal cost forecasts from associated PowerPlantOperators
	 * @param contractsToFulfill single contract, typically with a {@link MarketForecaster} */
	private void prepareForecastBids(ArrayList<Message> messages, List<Contract> contracts) {
		Contract contractToFulfil = CommUtils.getExactlyOneEntry(contracts);
		TreeMap<TimeStamp, ArrayList<Message>> messagesByTimeStamp = sortMarginalsByTimeStamp(messages);
		for (ArrayList<Message> messagesAtTime : messagesByTimeStamp.values()) {
			prepareBids(messagesAtTime, contractToFulfil);
		}
	}

	/** Assigns dispatch from {@link EnergyExchange} to power plant operators and writes information to output
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