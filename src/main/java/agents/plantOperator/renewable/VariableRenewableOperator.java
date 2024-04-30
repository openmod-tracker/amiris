// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.plantOperator.renewable;

import java.util.ArrayList;
import java.util.List;
import agents.electrolysis.GreenHydrogenOperator;
import agents.plantOperator.RenewablePlantOperator;
import communications.message.ClearingTimes;
import communications.message.Marginal;
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
		PpaInformation
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

		call(this::sendPpaInformation).on(Products.PpaInformation)
				.use(GreenHydrogenOperator.Products.PpaInformationRequest);
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

	/** send price and yield potential at given time */
	private void sendPpaInformation(ArrayList<Message> input, List<Contract> contracts) {
		if (ppaPriceInEURperMWH == null) {
			throw new RuntimeException(ERR_PPA_PRICE_MISSING + this);
		}
		Message message = CommUtils.getExactlyOneEntry(input);
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TimeStamp time = message.getDataItemOfType(ClearingTimes.class).getTimes().get(0);
		double ppaPrice = ppaPriceInEURperMWH.getValueLowerEqual(time);
		double availablePower = getInstalledPowerAtTimeInMW(time) * getYieldAtTime(time);
		fulfilNext(contract, new PpaInformation(time, ppaPrice, availablePower));
	}
}