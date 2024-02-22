// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.plantOperator.renewable;

import java.util.ArrayList;
import java.util.List;

import agents.plantOperator.RenewablePlantOperator;
import communications.message.AmountAtTime;
import communications.message.MarginalCost;
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

/**
 * An operator of variable renewable energy sources plants that depend on a
 * yield profile.
 * 
 * @author Christoph Schimeczek, Johannes Kochems
 */
public class VariableRenewableOperator extends RenewablePlantOperator {
	@Input
	private static final Tree parameters = Make.newTree()
			.add(Make.newSeries("YieldProfile"), Make.newDouble("PpaPriceInEURperMWH").optional()).buildTree();

	/** Products of {@link VariableRenewableOperator}s */
	@Product
	public static enum Products {
		/**
		 * Yield potential to inform the ElectrolysisTrader of the amount of electricity
		 */
		YieldPotential,
		/**
		 * Price set in PPA between renewable plant and electrolyzer
		 */
		PpaPrice
	};

	private TimeSeries tsYieldProfile;
	private double ppaPriceInEURperMWH;

	/**
	 * Creates an {@link VariableRenewableOperator}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided
	 */
	public VariableRenewableOperator(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		tsYieldProfile = input.getTimeSeries("YieldProfile");
		ppaPriceInEURperMWH = input.getDoubleOrDefault("PpaPriceInEURperMWH", null);

		call(this::sendPpaPrice).on(Products.PpaPrice);
		call(this::sendAvailablePowerAtTime).on(Products.YieldPotential);
	}

	/** @return single {@link MarginalCost} considering variable yield */
	@Override
	protected MarginalCost calcSingleMarginal(TimeStamp time) {
		if (tsYieldProfile == null) {
			throw new RuntimeException("Yield profile is missing for " + this);
		}
		double availablePower = getInstalledPowerAtTimeInMW(time) * getYieldAtTime(time);
		double marginalCost = getVariableOpexAtTime(time);
		return new MarginalCost(getId(), availablePower, marginalCost, time);
	}

	/** @return yield [0..1] relative to peak capacity at given time */
	private double getYieldAtTime(TimeStamp time) {
		return tsYieldProfile.getValueLinear(time);
	}

	/** @return send price set in PPA at given time */
	private void sendPpaPrice(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TimeStamp time = now();
		fulfilNext(contract, new AmountAtTime(time, ppaPriceInEURperMWH));
	}
	
	/** @return send available power at given time */
	private void sendAvailablePowerAtTime(ArrayList<Message> input, List<Contract> contracts) {
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TimeStamp time = now();
		double availablePower = getInstalledPowerAtTimeInMW(time) * getYieldAtTime(time);
		fulfilNext(contract, new AmountAtTime(time, availablePower));
	}

}