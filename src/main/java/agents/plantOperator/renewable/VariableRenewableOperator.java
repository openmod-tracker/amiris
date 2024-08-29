// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.plantOperator.renewable;

import java.util.ArrayList;
import java.util.List;
import agents.electrolysis.GreenHydrogenProducer;
import agents.plantOperator.Marginal;
import agents.plantOperator.RenewablePlantOperator;
import communications.message.PointInTime;
import communications.message.PpaInformation;
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
import de.dlr.gitlab.fame.time.TimeStamp;

/** An operator of variable renewable energy sources plants that depend on a yield profile.
 * 
 * @author Christoph Schimeczek, Johannes Kochems */
public class VariableRenewableOperator extends RenewablePlantOperator {
	static final String ERR_PPA_PRICE_MISSING = "PPA was requested, but no PPA price found in inputs of ";

	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newSeries("YieldProfile"), Make.newSeries("PpaPriceInEURperMWH").optional()).buildTree();

	/** Products of {@link VariableRenewableOperator}s */
	@Product
	public static enum Products {
		/** Price set in PPA and the current yield potential */
		PpaInformation, PpaInformationForecast
	};

	private TimeSeries tsYieldProfile;
	private TimeSeries ppaPriceInEURperMWH;

	/** Creates an {@link VariableRenewableOperator}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public VariableRenewableOperator(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		tsYieldProfile = input.getTimeSeries("YieldProfile");
		ppaPriceInEURperMWH = input.getTimeSeriesOrDefault("PpaPriceInEURperMWH", null);

		call(this::sendPpaInformationForecast).on(Products.PpaInformationForecast)
				.use(GreenHydrogenProducer.Products.PpaInformationForecastRequest);
		call(this::sendPpaInformation).on(Products.PpaInformation).use(GreenHydrogenProducer.Products.PpaInformationRequest);
	}

	/** @return single {@link Marginal} considering variable yield */
	@Override
	protected Marginal calcSingleMarginal(TimeStamp time) {
		if (tsYieldProfile == null) {
			throw new RuntimeException("Yield profile is missing for " + this);
		}
		double availablePower = getInstalledPowerAtTimeInMW(time) * getYieldAtTime(time);
		double marginalCost = getVariableOpexAtTime(time);
		return new Marginal(availablePower, marginalCost);
	}

	/** @return yield [0..1] relative to peak capacity at given time */
	private double getYieldAtTime(TimeStamp time) {
		return tsYieldProfile.getValueLinear(time);
	}

	private void sendPpaInformationForecast(ArrayList<Message> input, List<Contract> contracts) {
		sendPpaMultipleTimes(input, contracts);
	}

	/** send price and yield potential at given time */
	private double sendPpaMultipleTimes(ArrayList<Message> input, List<Contract> contracts) {
		if (ppaPriceInEURperMWH == null) {
			throw new RuntimeException(ERR_PPA_PRICE_MISSING + this);
		}
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		double totalOfferedEnergyInMWH = 0;
		for (var message : input) {
			var time = message.getDataItemOfType(PointInTime.class).validAt;
			double ppaPrice = ppaPriceInEURperMWH.getValueLowerEqual(time);
			double availablePower = getInstalledPowerAtTimeInMW(time) * getYieldAtTime(time);
			fulfilNext(contract, new PpaInformation(time, ppaPrice, availablePower, getVariableOpexAtTime(time)));
			totalOfferedEnergyInMWH += availablePower;
		}
		return totalOfferedEnergyInMWH;
	}

	private void sendPpaInformation(ArrayList<Message> input, List<Contract> contracts) {
		double totalOfferedEnergyInMHW = sendPpaMultipleTimes(input, contracts);
		store(OutputFields.OfferedEnergyInMWH, totalOfferedEnergyInMHW);
	}
}