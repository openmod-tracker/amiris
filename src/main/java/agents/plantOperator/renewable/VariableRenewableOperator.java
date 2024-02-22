// SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.plantOperator.renewable;

import java.util.ArrayList;
import java.util.List;
<<<<<<< Upstream, based on origin/dev
=======

>>>>>>> 1a6db84 Prepare data exchange between agents and classes
import agents.plantOperator.RenewablePlantOperator;
<<<<<<< Upstream, based on origin/dev
import agents.trader.ElectrolysisTrader;
import communications.message.ClearingTimes;
=======
import communications.message.AmountAtTime;
>>>>>>> 1a6db84 Prepare data exchange between agents and classes
import communications.message.MarginalCost;
import communications.message.PpaInformation;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
<<<<<<< Upstream, based on origin/dev
<<<<<<< Upstream, based on origin/dev
=======
>>>>>>> 1a6db84 Prepare data exchange between agents and classes
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;
<<<<<<< Upstream, based on origin/dev
=======
import de.dlr.gitlab.fame.communication.Product;
>>>>>>> 9c181f2 Start implementation in VarREOperator and ElectrolysisTrader
=======
>>>>>>> 1a6db84 Prepare data exchange between agents and classes
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimeStamp;

/**
 * An operator of variable renewable energy sources plants that depend on a
 * yield profile.
 * 
 * @author Christoph Schimeczek, Johannes Kochems
 */
public class VariableRenewableOperator extends RenewablePlantOperator {
<<<<<<< Upstream, based on origin/dev
<<<<<<< Upstream, based on origin/dev
	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newSeries("YieldProfile"), Make.newSeries("PpaPriceInEURperMWH").optional()).buildTree();

	/** Products of {@link VariableRenewableOperator}s */
	@Product
	public static enum Products {
		/** Price set in PPA and the current yield potential */
		PpaInformation
	};

=======
	@Input private static final Tree parameters = Make.newTree().add(Make.newSeries("YieldProfile")).buildTree();
	
=======
	@Input
	private static final Tree parameters = Make.newTree()
			.add(Make.newSeries("YieldProfile"), Make.newDouble("PpaPriceInEURperMWH").optional()).buildTree();

>>>>>>> 6006af8 VariableRenewableOperator - Create new product PpaPrice - Read variable PpaPriceInEURperMWH from schema file and store locally - Send PPA price as message via new function sendPpaPrice
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
<<<<<<< Upstream, based on origin/dev
	
>>>>>>> 9c181f2 Start implementation in VarREOperator and ElectrolysisTrader
	private TimeSeries tsYieldProfile;
	private TimeSeries ppaPriceInEURperMWH;
=======
>>>>>>> 6006af8 VariableRenewableOperator - Create new product PpaPrice - Read variable PpaPriceInEURperMWH from schema file and store locally - Send PPA price as message via new function sendPpaPrice

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
<<<<<<< Upstream, based on origin/dev
<<<<<<< Upstream, based on origin/dev
		ppaPriceInEURperMWH = input.getTimeSeriesOrDefault("PpaPriceInEURperMWH", null);

		call(this::sendPpaInformation).on(Products.PpaInformation).use(ElectrolysisTrader.Products.PpaInformationRequest);
=======
		
=======
		ppaPriceInEURperMWH = input.getDoubleOrDefault("PpaPriceInEURperMWH", null);

		call(this::sendPpaPrice).on(Products.PpaPrice);
>>>>>>> 6006af8 VariableRenewableOperator - Create new product PpaPrice - Read variable PpaPriceInEURperMWH from schema file and store locally - Send PPA price as message via new function sendPpaPrice
		call(this::sendAvailablePowerAtTime).on(Products.YieldPotential);
>>>>>>> 1a6db84 Prepare data exchange between agents and classes
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

	/** @return send price and yield potential at given time */
	private void sendPpaInformation(ArrayList<Message> input, List<Contract> contracts) {
		Message message = CommUtils.getExactlyOneEntry(input);
		Contract contract = CommUtils.getExactlyOneEntry(contracts);
		TimeStamp time = message.getDataItemOfType(ClearingTimes.class).getTimes().get(0);
		double ppaPrice = ppaPriceInEURperMWH.getValueLowerEqual(time);
		double availablePower = getInstalledPowerAtTimeInMW(time) * getYieldAtTime(time);
		PpaInformation ppaInformation = new PpaInformation(time, ppaPrice, availablePower);
		fulfilNext(contract, ppaInformation);
	}
}