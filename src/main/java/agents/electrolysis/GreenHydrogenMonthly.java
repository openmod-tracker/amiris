// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.electrolysis;

import java.util.ArrayList;
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
	static final String ERR_NOT_INTENDED = "Method not intended for strategist type ";

	private final TreeMap<TimePeriod, PpaInformation> ppaForesight = new TreeMap<>();
	private double greenElectricitySurplusTotal = 0;
	private TimePeriod lastTimePeriodInCurrentMonth;

	private double[] scheduledGreenElectricitySurplus;
	private double[] purchasedElectricityInMWH;
	private double[] bidPricesInEURperMWH;

	protected GreenHydrogenMonthly(ParameterData input) throws MissingDataException {
		super(input);
		scheduledGreenElectricitySurplus = new double[scheduleDurationPeriods];
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
	protected double[] getInternalEnergySchedule() {
		return scheduledGreenElectricitySurplus;
	}

	@Override
	protected void updateSchedule(TimePeriod timePeriod) {
		clearPlanningArrays();
		// TODO: Set schedule Arrays to 0 for TimePeriods after lastTimePeriodInCurrentMonth
		updateScheduleArrays(greenElectricitySurplusTotal);
	}

	private void clearPlanningArrays() {
		for (int index = 0; index < forecastSteps; index++) {
			purchasedElectricityInMWH[index] = 0;
			bidPricesInEURperMWH[index] = 0;
		}
	}

	/** @return Number of planning steps limited to forecastSteps or end of month */
	private int calcNumberOfPlanningSteps(TimePeriod firstPeriod) {
		long stepDelta = lastTimePeriodInCurrentMonth.getStartTime().getStep() - firstPeriod.getStartTime().getStep();
		int stepsUntilEndOfMonth = (int) (stepDelta / OPERATION_PERIOD.getSteps()) + 1;
		return Math.min(forecastSteps, stepsUntilEndOfMonth);
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

	/** transfer optimised dispatch to schedule arrays */
	private void updateScheduleArrays(double greenElectricitySurplus) {
		for (int hour = 0; hour < scheduleDurationPeriods; hour++) {
			demandScheduleInMWH[hour] = purchasedElectricityInMWH[hour];
			priceScheduleInEURperMWH[hour] = bidPricesInEURperMWH[hour];
			scheduledGreenElectricitySurplus[hour] = greenElectricitySurplus;
			greenElectricitySurplus -= purchasedElectricityInMWH[hour];
		}
	}
}
