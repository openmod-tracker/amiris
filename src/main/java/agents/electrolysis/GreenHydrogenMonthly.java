// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.electrolysis;

import java.util.ArrayList;
import java.util.TreeMap;
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

	private final TreeMap<TimePeriod, PpaInformation> ppaForesight = new TreeMap<>();

	protected GreenHydrogenMonthly(ParameterData input) throws MissingDataException {
		super(input);
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
	protected void updateSchedule(TimePeriod timePeriod) {
		// TODO Auto-generated method stub
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

}
