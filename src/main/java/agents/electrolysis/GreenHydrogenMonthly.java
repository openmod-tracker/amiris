// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.electrolysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import agents.flexibility.DispatchSchedule;
import agents.markets.meritOrder.books.DemandOrderBook;
import agents.markets.meritOrder.books.SupplyOrderBook;
import agents.markets.meritOrder.sensitivities.MeritOrderSensitivity;
import agents.markets.meritOrder.sensitivities.PriceNoSensitivity;
import communications.message.PpaInformation;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

public class GreenHydrogenMonthly extends ElectrolyzerStrategist {
	static final String ERR_PPA_MISSING = "PpaInformationForecast missing for time period: ";
	static final String ERR_NOT_INTENDED = "Method not intended for strategist type: ";
	static final double TOLERANCE = 1E-3;

	private final TreeMap<TimePeriod, PpaInformation> ppaForesight = new TreeMap<>();
	private double greenElectricitySurplusTotal = 0;
	private TimePeriod lastTimePeriodInCurrentMonth;
	private double[] scheduledGreenElectricitySurplus;

	private PpaInformation[] ppaInformationForecast;
	private double[] purchasedElectricityInMWH;
	private double[] bidPricesInEURperMWH;

	protected GreenHydrogenMonthly(ParameterData input) throws MissingDataException {
		super(input);
		scheduledGreenElectricitySurplus = new double[scheduleDurationPeriods];
		ppaInformationForecast = new PpaInformation[forecastSteps];
		purchasedElectricityInMWH = new double[forecastSteps];
		bidPricesInEURperMWH = new double[forecastSteps];
	}

	public ArrayList<TimeStamp> getTimesMissingPpaForecastTimes(TimePeriod firstTime) {
		return getMissingForecastTimes(ppaForesight, firstTime);
	}

	public void storePpaForecast(TimePeriod timePeriod, PpaInformation ppaInformation) {
		ppaForesight.put(timePeriod, ppaInformation);
	}

	public PpaInformation getPpaForPeriod(TimePeriod timePeriod) {
		return ppaForesight.get(timePeriod);
	}

	@Override
	/** @return an empty {@link MeritOrderSensitivity} item of the type used by this {@link Strategist}-type */
	protected MeritOrderSensitivity createBlankSensitivity() {
		return new PriceNoSensitivity();
	}

	@Override
	public void storeMeritOrderForesight(TimePeriod timePeriod, SupplyOrderBook supplyForecast,
			DemandOrderBook demandForecast) {
		throw new RuntimeException(ERR_USE_MERIT_ORDER_FORECAST + StrategistType.DISPATCH_FILE);
	}

	@Override
	public DispatchSchedule getValidSchedule(TimeStamp targetTime) {
		if (schedule == null || !schedule.isApplicable(targetTime, greenElectricitySurplusTotal)) {
			clearSensitivitiesBefore(targetTime);
			TimePeriod targetTimeSegment = new TimePeriod(targetTime, OPERATION_PERIOD);
			schedule = createSchedule(targetTimeSegment);
		}
		return schedule;
	}

	@Override
	protected void updateSchedule(TimePeriod timePeriod) {
		clearPlanningArrays();
		updateElectricityPriceForecasts(timePeriod);
		updateOpportunityCosts(timePeriod);
		updatePpaInformationForecast(timePeriod);
		updateStepTimes(timePeriod);
		schedulePpaProductionWithPositiveOpportunity(timePeriod);
		scheduleGreyElectricityPurchase(timePeriod);
		rescheduleForExtremelyNegativePrices(timePeriod);
		updateScheduleArrays(greenElectricitySurplusTotal);
	}

	private void clearPlanningArrays() {
		Arrays.fill(purchasedElectricityInMWH, 0);
		Arrays.fill(bidPricesInEURperMWH, 0);
		Arrays.fill(electricDemandOfElectrolysisInMW, 0);
	}

	private void updatePpaInformationForecast(TimePeriod startTime) {
		for (int period = 0; period < forecastSteps; period++) {
			TimePeriod timePeriod = startTime.shiftByDuration(period);
			var ppa = getPpaForPeriod(timePeriod);
			if (ppa == null) {
				throw new RuntimeException(ERR_PPA_MISSING + timePeriod);
			}
			ppaInformationForecast[period] = ppa;
		}
	}

	/** Schedule production of electrolyzer and market offers for times with renewable electricity provision from PPA */
	private void schedulePpaProductionWithPositiveOpportunity(TimePeriod firstPeriod) {
		for (int index = 0; index < calcNumberOfPlanningSteps(firstPeriod); index++) {
			double ppaProduction = ppaInformationForecast[index].yieldPotentialInMWH;
			if (electricityPriceForecasts[index] > hydrogenSaleOpportunityCostsPerElectricMWH[index]) {
				purchasedElectricityInMWH[index] = -ppaProduction;
				bidPricesInEURperMWH[index] = hydrogenSaleOpportunityCostsPerElectricMWH[index];
			} else {
				electricDemandOfElectrolysisInMW[index] = electrolyzer.calcCappedElectricDemandInMW(ppaProduction,
						stepTimes[index]);
				double surplus = ppaProduction - electricDemandOfElectrolysisInMW[index];
				if (electricityPriceForecasts[index] > 0) {
					purchasedElectricityInMWH[index] = -surplus;
					bidPricesInEURperMWH[index] = 0;
				}
			}
		}
	}

	/** @return Number of planning steps limited to forecastSteps or end of month */
	private int calcNumberOfPlanningSteps(TimePeriod firstPeriod) {
		long stepDelta = lastTimePeriodInCurrentMonth.getStartTime().getStep() - firstPeriod.getStartTime().getStep();
		int stepsUntilEndOfMonth = (int) (stepDelta / OPERATION_PERIOD.getSteps()) + 1;
		return Math.min(forecastSteps, stepsUntilEndOfMonth);
	}

	/** Plan grey electricity purchases based on projected green electricity sales */
	private void scheduleGreyElectricityPurchase(TimePeriod firstPeriod) {
		double surplusPosition = calcSurplusPosition(firstPeriod);
		if (surplusPosition >= 0) {
			schedulePurchaseForFullLoadOperation(firstPeriod);
		} else {
			schedulePurchaseForSurplusDeficit(firstPeriod);
			scheduleSalesForNegativePrices(firstPeriod);
		}
	}

	/** @return surplus from selling renewable energy minus purchasing grey energy to run electrolyzer at full capacity */
	private double calcSurplusPosition(TimePeriod firstPeriod) {
		double surplusPosition = greenElectricitySurplusTotal;
		for (int index = 0; index < calcNumberOfPlanningSteps(firstPeriod); index++) {
			surplusPosition -= purchasedElectricityInMWH[index];
			if (electricityPriceForecasts[index] < hydrogenSaleOpportunityCostsPerElectricMWH[index]) {
				surplusPosition += -getRemainingPowerInMW(index);
			}
		}
		return surplusPosition;
	}

	/** Assuming an overall surplus of sold green electricity, maximize hydrogen production */
	private void schedulePurchaseForFullLoadOperation(TimePeriod firstPeriod) {
		for (int index = 0; index < calcNumberOfPlanningSteps(firstPeriod); index++) {
			if (electricityPriceForecasts[index] < hydrogenSaleOpportunityCostsPerElectricMWH[index]) {
				purchasedElectricityInMWH[index] = getRemainingPowerInMW(index);
				bidPricesInEURperMWH[index] = hydrogenSaleOpportunityCostsPerElectricMWH[index];
				electricDemandOfElectrolysisInMW[index] += purchasedElectricityInMWH[index];
			}
		}
	}

	/** Assuming an overall deficit of sold green electricity, maximize profits */
	private void schedulePurchaseForSurplusDeficit(TimePeriod firstPeriod) {
		double netSurplus = calcNetSurplus(firstPeriod);
		while (netSurplus > 0) {
			int bestHour = getHourWithHighestEconomicPotential(calcNumberOfPlanningSteps(firstPeriod));
			if (bestHour < 0) {
				break;
			}
			double powerToPurchase = Math.min(netSurplus, getRemainingPowerInMW(bestHour));
			netSurplus -= powerToPurchase;
			purchasedElectricityInMWH[bestHour] += powerToPurchase;
			bidPricesInEURperMWH[bestHour] = hydrogenSaleOpportunityCostsPerElectricMWH[bestHour];
			electricDemandOfElectrolysisInMW[bestHour] += powerToPurchase;
		}
	}

	/** @return surplus from selling renewable energy */
	private double calcNetSurplus(TimePeriod firstPeriod) {
		double netSurplus = greenElectricitySurplusTotal;
		for (int index = 0; index < calcNumberOfPlanningSteps(firstPeriod); index++) {
			netSurplus -= purchasedElectricityInMWH[index];
		}
		return netSurplus;
	}

	/** Schedule sales of renewable energy at negative electricity prices if corresponding purchasing option exists that has higher
	 * economic potential compared to selling at negative prices */
	private void scheduleSalesForNegativePrices(TimePeriod firstPeriod) {
		int endOfHorizon = calcNumberOfPlanningSteps(firstPeriod);
		int bestBuyHour = getHourWithHighestEconomicPotential(endOfHorizon);
		int bestSellHour = getHourWithLowestSaleCosts(endOfHorizon);
		while (bestBuyHour >= 0 && bestSellHour >= 0) {
			double economicPotential = getEconomicHydrogenPotential(bestBuyHour);
			if (-electricityPriceForecasts[bestSellHour] < economicPotential) {
				double toPurchaseAndSell = Math.min(getUnusedResPotential(bestSellHour), getRemainingPowerInMW(bestBuyHour));
				purchasedElectricityInMWH[bestBuyHour] += toPurchaseAndSell;
				electricDemandOfElectrolysisInMW[bestBuyHour] += toPurchaseAndSell;
				bidPricesInEURperMWH[bestBuyHour] = hydrogenSaleOpportunityCostsPerElectricMWH[bestBuyHour];
				purchasedElectricityInMWH[bestSellHour] -= toPurchaseAndSell;
				bidPricesInEURperMWH[bestSellHour] = -economicPotential;
			} else {
				break;
			}
			bestBuyHour = getHourWithHighestEconomicPotential(endOfHorizon);
			bestSellHour = getHourWithLowestSaleCosts(endOfHorizon);
		}
	}

	/** @returns hour with the highest negative price with remaining renewable capacity, -1 if no such hour exists */
	private int getHourWithLowestSaleCosts(int endOfHorizon) {
		int bestHour = -1;
		double bestPrice = -Double.MAX_VALUE;
		for (int hour = 0; hour < endOfHorizon; hour++) {
			if (electricityPriceForecasts[hour] < 0 && getUnusedResPotential(hour) > 0) {
				if (electricityPriceForecasts[hour] > bestPrice) {
					bestPrice = electricityPriceForecasts[hour];
					bestHour = hour;
				}
			}
		}
		return bestHour;
	}

	private double getUnusedResPotential(int hour) {
		double yieldPotential = ppaInformationForecast[hour].yieldPotentialInMWH;
		return Math.max(0, Math.min(yieldPotential, yieldPotential - electricDemandOfElectrolysisInMW[hour]
				+ purchasedElectricityInMWH[hour]));
	}

	@Override
	public void updateProducedHydrogenTotal(double producedHydrogenInMWH) {
		throw new RuntimeException(ERR_NOT_INTENDED + this.getClass().getSimpleName());
	}

	public void updateGreenElectricitySurplus(double greenElectricitySurplusInMWH) {
		greenElectricitySurplusTotal += greenElectricitySurplusInMWH;
	}

	public void resetMonthly(TimeStamp beginOfNextMonth) {
		schedule = null;
		greenElectricitySurplusTotal = 0;
		lastTimePeriodInCurrentMonth = new TimePeriod(beginOfNextMonth.earlierBy(OPERATION_PERIOD), OPERATION_PERIOD);
	}

	/** Curtail renewable generation and buy at exchange for extremely negative prices exceeding negative PPA price */
	private void rescheduleForExtremelyNegativePrices(TimePeriod firstPeriod) {
		int endOfHorizon = calcNumberOfPlanningSteps(firstPeriod);
		double netSurplus = calcNetSurplus(firstPeriod);
		while (netSurplus > 0) {
			int bestHour = getBestCurtailmentHour(endOfHorizon);
			if (bestHour < 0) {
				break;
			}
			double usedResPotential = getUsedResPotentialFor(bestHour);
			double curtailedEnergy = Math.min(usedResPotential, netSurplus);
			purchasedElectricityInMWH[bestHour] += curtailedEnergy;
			// TODO: Need multiple bids with different price limits
			bidPricesInEURperMWH[bestHour] = hydrogenSaleOpportunityCostsPerElectricMWH[bestHour];
			netSurplus = calcNetSurplus(firstPeriod);
		}
		if (netSurplus <= TOLERANCE) {
			int bestCurtailmentHour = getBestCurtailmentHour(endOfHorizon);
			int lowestEconomicPotentialHour = getHourWithLowestEconomicPotential(endOfHorizon);
			while (bestCurtailmentHour >= 0 && lowestEconomicPotentialHour >= 0) {
				double purchasedElectricity = purchasedElectricityInMWH[lowestEconomicPotentialHour];
				double usedRenewableElectricity = getUsedResPotentialFor(bestCurtailmentHour);
				double rescheduledAmount = Math.min(purchasedElectricity, usedRenewableElectricity);
				purchasedElectricityInMWH[lowestEconomicPotentialHour] -= rescheduledAmount;
				electricDemandOfElectrolysisInMW[lowestEconomicPotentialHour] -= rescheduledAmount;
				purchasedElectricityInMWH[bestCurtailmentHour] += rescheduledAmount;
				// TODO: Need multiple bids with different price limits
				bidPricesInEURperMWH[bestCurtailmentHour] = hydrogenSaleOpportunityCostsPerElectricMWH[bestCurtailmentHour];
				bestCurtailmentHour = getBestCurtailmentHour(endOfHorizon);
				lowestEconomicPotentialHour = getHourWithLowestEconomicPotential(endOfHorizon);
			}
		}
	}

	/** @returns hour with highest difference between electricity price and negative PPA price, -1 if no such hour exists */
	private int getBestCurtailmentHour(int endOfHorizon) {
		int bestHour = -1;
		double bestPriceDifference = 0;
		for (int hour = 0; hour < endOfHorizon; hour++) {
			double priceDifference = getCurtailmentPriceDifferenceFor(hour);
			if (priceDifference > 0 && getUsedResPotentialFor(hour) > 0) {
				if (priceDifference > bestPriceDifference) {
					bestPriceDifference = priceDifference;
					bestHour = hour;
				}
			}
		}
		return bestHour;
	}

	private double getUsedResPotentialFor(int hour) {
		return ppaInformationForecast[hour].yieldPotentialInMWH - getUnusedResPotential(hour);
	}

	private double getCurtailmentPriceDifferenceFor(int hour) {
		return -electricityPriceForecasts[hour] - ppaInformationForecast[hour].priceInEURperMWH;
	}

	/** @param endOfHorizon last step of planning horizon
	 * @return Hour with lowest, but positive economic potential among those with scheduled purchase; -1 if no such hour exists */
	protected int getHourWithLowestEconomicPotential(int endOfHorizon) {
		int bestHour = -1;
		double lowestPotential = Double.MAX_VALUE;
		for (int hour = 0; hour < endOfHorizon; hour++) {
			double economicPotential = getEconomicHydrogenPotential(hour);
			if (economicPotential < lowestPotential && purchasedElectricityInMWH[hour] > 0) {
				lowestPotential = economicPotential;
				bestHour = hour;
			}
		}
		return bestHour;
	}

	/** transfer optimised dispatch to schedule arrays */
	private void updateScheduleArrays(double greenElectricitySurplus) {
		for (int hour = 0; hour < scheduleDurationPeriods; hour++) {
			demandScheduleInMWH[hour] = purchasedElectricityInMWH[hour];
			priceScheduleInEURperMWH[hour] = bidPricesInEURperMWH[hour];
			scheduledGreenElectricitySurplus[hour] = greenElectricitySurplus;
			greenElectricitySurplus -= purchasedElectricityInMWH[hour];
		}
	}

	@Override
	protected double[] getInternalEnergySchedule() {
		return scheduledGreenElectricitySurplus;
	}
}
