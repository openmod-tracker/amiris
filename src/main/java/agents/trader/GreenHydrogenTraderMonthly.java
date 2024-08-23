// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.trader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import agents.electrolysis.GreenHydrogen;
import agents.electrolysis.GreenHydrogenMonthly;
import agents.flexibility.DispatchSchedule;
import agents.flexibility.Strategist;
import agents.markets.meritOrder.Bid;
import agents.plantOperator.renewable.VariableRenewableOperator;
import communications.message.PointInTime;
import communications.message.PpaInformation;
import communications.portable.BidsAtTime;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

public class GreenHydrogenTraderMonthly extends ElectrolysisTrader implements GreenHydrogen {

	public GreenHydrogenTraderMonthly(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);

		call(this::requestPpaForecast).on(GreenHydrogen.Products.PpaInformationForecastRequest);
		call(this::updatePpaForecast).on(VariableRenewableOperator.Products.PpaInformationForecast)
				.use(VariableRenewableOperator.Products.PpaInformationForecast);
	}

	private void requestPpaForecast(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TimePeriod nextTime = new TimePeriod(now().laterBy(electricityForecastRequestOffset),
				Strategist.OPERATION_PERIOD);
		GreenHydrogenMonthly strategist = (GreenHydrogenMonthly) getStrategist();
		ArrayList<TimeStamp> missingForecastTimes = strategist.getTimesMissingPpaForecastTimes(nextTime);
		for (TimeStamp missingForecastTime : missingForecastTimes) {
			PointInTime pointInTime = new PointInTime(missingForecastTime);
			fulfilNext(contract, pointInTime);
		}
	}

	private void updatePpaForecast(ArrayList<Message> input, List<Contract> contracts) {
		for (Message inputMessage : input) {
			PpaInformation ppaForecast = inputMessage.getDataItemOfType(PpaInformation.class);
			TimePeriod timeSegment = new TimePeriod(ppaForecast.validAt, Strategist.OPERATION_PERIOD);
			GreenHydrogenMonthly strategist = (GreenHydrogenMonthly) getStrategist();
			strategist.storePpaForecast(timeSegment, ppaForecast);
		}
	}

	@Override
	protected void prepareBids(ArrayList<Message> input, List<Contract> contracts) {
		Contract contractToFulfil = CommUtils.getExactlyOneEntry(contracts);
		for (TimeStamp targetTime : extractTimesFromGateClosureInfoMessages(input)) {
			DispatchSchedule schedule = getStrategist().getValidSchedule(targetTime);
			Bid demandBid = prepareHourlyDemandBid(targetTime, schedule);
			Bid supplyBid = prepareHourlySupplyBid(targetTime, schedule);
			fulfilNext(contractToFulfil,
					new BidsAtTime(targetTime, getId(), Arrays.asList(supplyBid), Arrays.asList(demandBid)));
		}
	}

	private Bid prepareHourlyDemandBid(TimeStamp targetTime, DispatchSchedule schedule) {
		double demandPower = schedule.getScheduledChargingPowerInMW(targetTime);
		double price = schedule.getScheduledBidInHourInEURperMWH(targetTime);
		store(OutputColumns.RequestedEnergyInMWH, demandPower);
		return new Bid(demandPower, price, Double.NaN);
	}

	private Bid prepareHourlySupplyBid(TimeStamp targetTime, DispatchSchedule schedule) {
		double supplyPower = schedule.getScheduledDischargingPowerInMW(targetTime);
		double price = schedule.getScheduledBidInHourInEURperMWH(targetTime);
		store(OutputColumns.OfferedEnergyInMWH, supplyPower);
		return new Bid(supplyPower, price, Double.NaN);
	}

}
