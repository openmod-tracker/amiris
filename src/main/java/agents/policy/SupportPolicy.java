// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.policy;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import agents.markets.EnergyExchange;
import agents.plantOperator.RenewablePlantOperator.SetType;
import agents.trader.AggregatorTrader;
import communications.message.AwardData;
import communications.message.SupportRequestData;
import communications.message.SupportResponseData;
import communications.message.TechnologySet;
import communications.message.YieldPotential;
import communications.portable.SupportData;
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
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.service.output.Output;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeSpan;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Coordinates various different support policies - distributes according information and assigns support pay-outs.
 * 
 * @author Johannes Kochems, Christoph Schimeczek, Felix Nitsch, Farzad Sarfarazi, Kristina Nienhaus */
public class SupportPolicy extends Agent {
	static final String ERR_MISSING_INSTRUMENT = "SupportInstrument is not implemented: ";
	static final String ERR_MISSING_CARRIER = "Energy carrier is not implemented: ";

	@Product
	public enum Products {
		/** Info on the support scheme to be applied to a set of plants */
		SupportInfo,
		/** Actual pay-out of the support */
		SupportPayout,
		/** Trigger for market value calculation */
		MarketValueCalculation
	}

	/** Available support instruments */
	public enum SupportInstrument {
		/** A feed-in-tariff (FIT) */
		FIT,
		/** A variable market premium (MPVAR); as in the German Renewable Energies Act */
		MPVAR,
		/** A fixed market premium (MPFIX) */
		MPFIX,
		/** A contracts for differences (CFD) scheme, building on MPVAR */
		CFD,
		/** A capacity premium (CP) scheme */
		CP
	}

	/** Available energy carriers eligible for support - and "Other" */
	public enum EnergyCarrier {
		PV, WindOn, WindOff, RunOfRiver, Biogas,
		/** Not eligible for support */
		Other
	}

	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newGroup("SetSupportData").list().add(Make.newEnum("Set", SetType.class))
					.addAs("FIT", FitInfo.parameters).addAs("MPVAR", MpvarInfo.parameters)
					.addAs("MPFIX", MpfixInfo.parameters).addAs("CFD", CfdInfo.parameters).addAs("CP", CPInfo.parameters))
			.buildTree();

	@Output
	private enum OutputColumns {
		MarketValuePV, MarketValueWindOn, MarketValueWindOff, MarketValueBase
	};

	/** Maps each set to its support data */
	private EnumMap<SetType, SetSupportData> setTypeSupportData = new EnumMap<>(SetType.class);
	/** Maps each set to its energy carrier */
	private EnumMap<SetType, EnergyCarrier> setTypeEnergyCarrier = new EnumMap<>(SetType.class);
	/** Tracks the feed-in potential per energy carrier */
	private EnumMap<EnergyCarrier, TreeMap<TimeStamp, Double>> energyCarrierInfeeds = initializeMap(EnergyCarrier.class,
			TimeStamp.class);
	/** Stores the market values per energy carrier for multiple time periods */
	private EnumMap<EnergyCarrier, TreeMap<TimePeriod, Double>> energyCarrierMarketValues = initializeMap(
			EnergyCarrier.class, TimePeriod.class);
	/** Stores the market premia for each set */
	private EnumMap<SetType, TreeMap<TimePeriod, Double>> setTypeMarketPremia = initializeMap(SetType.class,
			TimePeriod.class);
	/** Stores electricity market prices for multiple times */
	private TreeMap<TimeStamp, Double> powerPrices = new TreeMap<>();

	/** Initializes an EnumMap with a TreeMap as value and both keys of arbitrary types
	 * 
	 * @param <X> Enum class of arbitrary type
	 * @param <Y> class of arbitrary type
	 * @param enumType enum instance type as key of outer EnumMap
	 * @param keyType key type of inner TreeMap
	 * @return an EnumMap with all enum values of <X> as key and an empty TreeMap as value */
	private <X extends Enum<X>, Y> EnumMap<X, TreeMap<Y, Double>> initializeMap(Class<X> enumType, Class<Y> keyType) {
		EnumMap<X, TreeMap<Y, Double>> map = new EnumMap<>(enumType);
		for (X x : enumType.getEnumConstants()) {
			map.put(x, new TreeMap<>());
		}
		return map;
	}

	/** Creates a {@link SupportPolicy}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public SupportPolicy(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData inputData = parameters.join(dataProvider);
		for (ParameterData group : inputData.getGroupList("SetSupportData")) {
			SetType setType = group.getEnum("Set", SetType.class);
			FitInfo fitInfo = PolicyInfo.buildPolicyInfo(FitInfo.class, "FIT", group);
			MpvarInfo mpvarInfo = PolicyInfo.buildPolicyInfo(MpvarInfo.class, "MPVAR", group);
			MpfixInfo mpfixInfo = PolicyInfo.buildPolicyInfo(MpfixInfo.class, "MPFIX", group);
			CfdInfo cfdInfo = PolicyInfo.buildPolicyInfo(CfdInfo.class, "CFD", group);
			CPInfo capacityPremiumInfo = PolicyInfo.buildPolicyInfo(CPInfo.class, "CP", group);
			setTypeSupportData.put(setType, new SetSupportData(fitInfo, mpvarInfo, mpfixInfo, cfdInfo, capacityPremiumInfo));
		}

		call(this::sendSupportInfo).on(Products.SupportInfo).use(AggregatorTrader.Products.SupportInfoRequest);
		call(this::addYieldPotentials).on(AggregatorTrader.Products.YieldPotential)
				.use(AggregatorTrader.Products.YieldPotential);
		call(this::trackPowerPrice).on(EnergyExchange.Products.Awards).use(EnergyExchange.Products.Awards);
		call(this::calcSupportPayout).on(Products.SupportPayout).use(AggregatorTrader.Products.SupportPayoutRequest);
		call(this::calculateAndStoreMarketValues).on(Products.MarketValueCalculation);
	}

	/** Send {@link TechnologySet}-specific and technology-neutral support data to contracted partner that sent request(s); sent
	 * information dependends on support type:
	 * <ul>
	 * <li>FIT: FitData containing FIT and suspension volume share</li>
	 * <li>MPVAR or CFD: MpVarOrCfDData containing the LCOE</li>
	 * <li>MPFIX: MpFixInfo containing the fixed market premium</li>
	 * <li>CP: CPInfo containing the capacity premium</li>
	 * </ul>
	 * 
	 * @param messages incoming request(s) from contracted partners, containing type of technology set the want to be informed of
	 * @param contracts with partners (typically {@link AggregatorTrader}s) to send set-specific support policy details to */
	private void sendSupportInfo(ArrayList<Message> messages, List<Contract> contracts) {
		for (Contract contract : contracts) {
			ArrayList<Message> contractMessages = CommUtils.extractMessagesFrom(messages, contract.getReceiverId());
			for (Message message : contractMessages) {
				TechnologySet technologySet = message.getDataItemOfType(TechnologySet.class);
				SetType set = technologySet.setType;
				setTypeEnergyCarrier.put(set, technologySet.energyCarrier);
				PolicyInfo policyInfo = setTypeSupportData.get(set).getInfoFor(technologySet.supportInstrument);
				fulfilNext(contract, new SupportData(set, policyInfo));
			}
		}
	}

	/** Store {@link YieldPotential}s for RES market value calculation
	 * 
	 * @param input incoming reported yield potentials
	 * @param contracts not used */
	private void addYieldPotentials(ArrayList<Message> input, List<Contract> contracts) {
		for (Message message : input) {
			YieldPotential yieldPotential = message.getDataItemOfType(YieldPotential.class);
			addYieldValue(yieldPotential);
		}
	}

	/** Increment feed-in potential info per {@link EnergyCarrier} by feed-in potential of {@link TechnologySet} */
	private void addYieldValue(YieldPotential yieldPotential) {
		EnergyCarrier carrier = yieldPotential.energyCarrier;
		energyCarrierInfeeds.putIfAbsent(carrier, new TreeMap<>());
		double yieldPerEnergyCarrier = 0;
		TreeMap<TimeStamp, Double> yieldMap = energyCarrierInfeeds.get(carrier);
		if (yieldMap.containsKey(yieldPotential.validAt)) {
			yieldPerEnergyCarrier = yieldMap.get(yieldPotential.validAt);
		}
		yieldPerEnergyCarrier += yieldPotential.amount;
		yieldMap.put(yieldPotential.validAt, yieldPerEnergyCarrier);
	}

	/** Extract and store power prices and volumes reported from {@link EnergyExchange}
	 * 
	 * @param input single power price message to read
	 * @param contracts not used */
	private void trackPowerPrice(ArrayList<Message> input, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(input);
		AwardData award = message.getDataItemOfType(AwardData.class);
		powerPrices.put(award.beginOfDeliveryInterval, award.powerPriceInEURperMWH);
	}

	/** Calculate the support pay-out and distribute it to {@link AggregatorTrader}s; Add market premium information for MPVAR,
	 * MPFIX and CFD scheme
	 * 
	 * @param messages incoming pay-out requests from contracted partners
	 * @param contracts receivers get one pay-out message per incoming pay-out request */
	private void calcSupportPayout(ArrayList<Message> messages, List<Contract> contracts) {
		for (Contract contract : contracts) {
			ArrayList<Message> contractMessages = CommUtils.extractMessagesFrom(messages, contract.getReceiverId());
			for (Message message : contractMessages) {
				SupportRequestData supportData = message.getDataItemOfType(SupportRequestData.class);
				EnergyCarrier energyCarrier = setTypeEnergyCarrier.get(supportData.setType);
				double marketValue = calcMarketValue(energyCarrier, supportData.accountingPeriod);
				double supportPayout = calcSupportPerMessage(supportData, marketValue);
				double marketPremium = 0;
				if (supportData.supportInstrument == SupportInstrument.MPVAR
						|| supportData.supportInstrument == SupportInstrument.MPFIX
						|| supportData.supportInstrument == SupportInstrument.CFD) {
					marketPremium = setTypeMarketPremia.get(supportData.setType).get(supportData.accountingPeriod);
				}
				SupportResponseData supportDataResponse = new SupportResponseData(supportData, supportPayout, marketPremium);
				fulfilNext(contract, supportDataResponse);
			}
		}
	}

	/** Calculate market value based on energy carrier-specific RES infeed (wind and PV) or base price
	 * 
	 * @return the market value of the given energy carrier */
	private double calcMarketValue(EnergyCarrier energyCarrier, TimePeriod interval) {
		double numerator = 0;
		double denominator = 0;
		Iterator<Entry<TimeStamp, Double>> iterator = powerPrices.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<TimeStamp, Double> entry = iterator.next();
			TimeStamp powerPriceTime = entry.getKey();
			if (powerPriceTime.isGreaterEqualTo(interval.getStartTime())
					&& powerPriceTime.isLessEqualTo(interval.getLastTime())) {
				double[] marketValQuotient = calcEnergyCarrierSpecificValue(energyCarrier, powerPriceTime, denominator,
						numerator);
				denominator = marketValQuotient[0];
				numerator = marketValQuotient[1];
			}
		}
		return numerator / denominator;
	}

	/** Calculate the market value elements (denominator and numerator) dependent on RES type:
	 * <ul>
	 * <li>For wind and PV, an average price weighted by the energy carrier feed-in potential (before curtailment) is used.</li>
	 * <li>For all other RES sources, the unweighted average price (base price) is used.</li>
	 * </ul>
	 * 
	 * @param energyCarrier to calculate market value for
	 * @param time at which to calculate the market value element
	 * @param denominator running total of the energyCarrier-specific market value calculation
	 * @param numerator running total of the energyCarrier-specific market value calculation
	 * @return double array with the first entry being the denominator and the second the numerator for the market value
	 *         calculation */
	private double[] calcEnergyCarrierSpecificValue(EnergyCarrier energyCarrier, TimeStamp time, double denominator,
			double numerator) {
		switch (energyCarrier) {
			case PV:
			case WindOn:
			case WindOff:
				denominator += energyCarrierInfeeds.get(energyCarrier).get(time);
				numerator += energyCarrierInfeeds.get(energyCarrier).get(time) * powerPrices.get(time);
				return new double[] {denominator, numerator};
			case RunOfRiver:
			case Biogas:
			case Other:
				denominator += 1;
				numerator += powerPrices.get(time);
				return new double[] {denominator, numerator};
			default:
				throw new RuntimeException(ERR_MISSING_CARRIER + energyCarrier);
		}
	}

	/** Extract set-specific support information and calculate support for that set; negative payments may occur for a CFD scheme
	 * and indicate an obligation of the {@link AggregatorTrader} to pay to the {@link SupportPolicy}. The calculation of the
	 * support rate (in EUR/MWh or EUR/MW for a CP) is dependent on the support instrument:
	 * <ul>
	 * <li>FIT: the feed-in tariff itself</li>
	 * <li>MPVAR: the difference between the LCOE (value applied) and the market value of the respective energy carrier; 0 at
	 * minimum</li>
	 * <li>MPFIX: the fixed market premium itself on a MWh basis</li>
	 * <li>CFD: Same as MPVAR, but with an obligation to pay back if market value > value applied</li>
	 * <li>CP: The capacity premium on a MW basis</li>
	 * </ul>
	 * 
	 * @param supportData reported data to calculated support the pay-out from
	 * @param marketValue in the accounting period associated with the support data request
	 * @return the support to be payed out for the currently evaluated set */
	private double calcSupportPerMessage(SupportRequestData supportData, double marketValue) {
		double supportRate;
		SetType setType = supportData.setType;
		SetSupportData setSupportData = setTypeSupportData.get(setType);
		TimeStamp startTime = supportData.accountingPeriod.getStartTime();
		PolicyInfo policyInfo = setSupportData.getInfoFor(supportData.supportInstrument);
		switch (supportData.supportInstrument) {
			case FIT:
				supportRate = ((FitInfo) policyInfo).getTsFit().getValueLowerEqual(startTime);
				break;
			case MPVAR:
				TimeSeries lcoe = ((MpvarInfo) policyInfo).getLcoe();
				double valueApplied = lcoe.getValueLowerEqual(startTime);
				supportRate = Math.max(0, valueApplied - marketValue);
				break;
			case MPFIX:
				TimeSeries marketPremium = ((MpfixInfo) policyInfo).getPremium();
				supportRate = marketPremium.getValueLowerEqual(supportData.accountingPeriod.getStartTime());
				break;
			case CFD:
				lcoe = ((CfdInfo) policyInfo).getLcoe();
				valueApplied = lcoe.getValueLowerEqual(startTime);
				supportRate = valueApplied - marketValue;
				break;
			case CP:
				TimeSeries capacityPremium = ((CPInfo) policyInfo).getPremium();
				supportRate = capacityPremium.getValueLowerEqual(supportData.accountingPeriod.getStartTime());
				break;
			default:
				throw new InvalidParameterException(ERR_MISSING_INSTRUMENT + supportData.supportInstrument);
		}
		setTypeMarketPremia.get(setType).put(supportData.accountingPeriod, supportRate);
		return supportData.amount * supportRate;
	}

	/** Calculates and stores market values per energy carrier */
	private void calculateAndStoreMarketValues(ArrayList<Message> messages, List<Contract> contracts) {
		Contract contract = contracts.get(0);
		TimePeriod accountingInverval = extractAccountingPeriod(now(), contract, 1796L);
		HashSet<EnergyCarrier> uniqueEnergyCarriers = new HashSet<>(setTypeEnergyCarrier.values());
		for (EnergyCarrier energyCarrier : uniqueEnergyCarriers) {
			double marketValue = calcMarketValue(energyCarrier, accountingInverval);
			energyCarrierMarketValues.get(energyCarrier).put(accountingInverval, marketValue);
			switch (energyCarrier) {
				case PV:
					store(OutputColumns.MarketValuePV, marketValue);
					break;
				case WindOn:
					store(OutputColumns.MarketValueWindOn, marketValue);
					break;
				case WindOff:
					store(OutputColumns.MarketValueWindOff, marketValue);
					break;
				default:
					store(OutputColumns.MarketValueBase, marketValue);
			}
		}
	}

	/** Extract the accounting period for premia schemes from contract duration and shift it by given steps
	 * 
	 * @param time to start the search for next delivery time
	 * @param contract to create the accounting period from
	 * @param stepsToShift seconds to shift the time: positive values shift towards the future, negative ones towards the past
	 * @return a TimePeriod matching next delivery period of given contract shifted by given time steps */
	public static TimePeriod extractAccountingPeriod(TimeStamp time, Contract contract, long stepsToShift) {
		TimeStamp endTime;
		TimeStamp nextContractExecutionTime = contract.getNextTimeOfDeliveryAfter(time);
		if (stepsToShift >= 0) {
			endTime = nextContractExecutionTime.laterBy(new TimeSpan(stepsToShift));
		} else {
			endTime = nextContractExecutionTime.earlierBy(new TimeSpan(-stepsToShift));
		}
		TimeSpan duration = contract.getDeliveryInterval();
		TimeStamp startTime = endTime.earlierBy(duration);
		return new TimePeriod(startTime, duration);
	}
}
