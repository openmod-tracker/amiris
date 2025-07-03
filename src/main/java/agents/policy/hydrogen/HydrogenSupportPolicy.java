// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.policy.hydrogen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import agents.policy.hydrogen.PolicyItem.SupportInstrument;
import agents.trader.renewable.AggregatorTrader;
import communications.message.AmountAtTime;
import communications.message.HydrogenPolicyRegistration;
import communications.message.TechnologySet;
import communications.portable.HydrogenSupportData;
import de.dlr.gitlab.fame.agent.Agent;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.message.Message;

public class HydrogenSupportPolicy extends Agent implements HydrogenSupportProvider {
	static final String ERR_SET_UNKNOWN = "Hydrogen policy set not configured: ";
	static final String ERR_INSTRUMENT_UNKNOWN = " is an unknown support instrument in hydrogen policy set: ";

	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newGroup("SetSupportData").list().add(setParameter).addAs(SupportInstrument.MPFIX.name(),
					Mpfix.parameters))
			.buildTree();

	public class InstrumentPolicies {
		private final EnumMap<SupportInstrument, PolicyItem> policies = new EnumMap<>(SupportInstrument.class);

		public void addPolicyItem(PolicyItem policyItem) {
			if (policyItem != null) {
				policies.put(policyItem.getSupportInstrument(), policyItem);
			}
		}

		public PolicyItem getPolicyFor(SupportInstrument instrument) {
			return policies.get(instrument);
		}
	}

	public class PolicyChoice {
		public final String set;
		public final SupportInstrument supportInstrument;

		public PolicyChoice(String set, SupportInstrument supportInstrument) {
			this.set = set;
			this.supportInstrument = supportInstrument;
		}
	}

	private final HashMap<String, InstrumentPolicies> policyItemsPerSet = new HashMap<>();
	private final HashMap<Long, PolicyChoice> clientPolicyChoice = new HashMap<>();

	public HydrogenSupportPolicy(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData inputData = parameters.join(dataProvider);
		loadSetSupportData(inputData.getGroupList("SetSupportData"));

		call(this::sendSupportInfo).on(Products.SupportInfo).use(HydrogenSupportClient.Products.SupportInfoRequest);
		call(this::calcSupportPayout).on(Products.SupportPayout).use(HydrogenSupportClient.Products.SupportPayoutRequest);
	}

	/** loads all set-specific support instrument configurations from given groupList */
	private void loadSetSupportData(List<ParameterData> groupList) throws MissingDataException {
		for (ParameterData group : groupList) {
			String setType = HydrogenSupportProvider.readSet(group);
			for (SupportInstrument instrument : SupportInstrument.values()) {
				addSetPolicyItem(setType, PolicyItem.buildPolicy(instrument, group));
			}
		}
	}

	private void addSetPolicyItem(String policySet, PolicyItem policyItem) {
		InstrumentPolicies setPolicyItems = policyItemsPerSet.computeIfAbsent(policySet, __ -> new InstrumentPolicies());
		setPolicyItems.addPolicyItem(policyItem);
	}

	/** Send {@link TechnologySet}-specific and technology-neutral support data to contracted partner that sent request(s)
	 * 
	 * @param messages incoming request(s) from contracted partners, containing type of technology set the want to be informed of
	 * @param contracts with partners (typically {@link AggregatorTrader}s) to send set-specific support policy details to */
	private void sendSupportInfo(ArrayList<Message> messages, List<Contract> contracts) {
		for (Contract contract : contracts) {
			for (Message message : CommUtils.extractMessagesFrom(messages, contract.getReceiverId())) {
				HydrogenPolicyRegistration registration = message.getDataItemOfType(HydrogenPolicyRegistration.class);
				PolicyItem policyItem = getPolicyData(registration);
				clientPolicyChoice.put(contract.getReceiverId(),
						new PolicyChoice(registration.setType, policyItem.getSupportInstrument()));
				fulfilNext(contract, new HydrogenSupportData(registration.setType, policyItem));
			}
		}
	}

	private PolicyItem getPolicyData(HydrogenPolicyRegistration registration) {
		InstrumentPolicies setType = policyItemsPerSet.get(registration.setType);
		if (setType == null) {
			throw new RuntimeException(ERR_SET_UNKNOWN + registration.setType);
		}
		PolicyItem policyItem = setType.getPolicyFor(registration.supportInstrument);
		if (policyItem == null) {
			throw new RuntimeException(registration.supportInstrument + ERR_INSTRUMENT_UNKNOWN + registration.setType);
		}
		return policyItem;
	}

	private void calcSupportPayout(ArrayList<Message> messages, List<Contract> contracts) {
		for (Contract contract : contracts) {
			for (Message message : CommUtils.extractMessagesFrom(messages, contract.getReceiverId())) {
				AmountAtTime amountAtTime = message.getDataItemOfType(AmountAtTime.class);
				double payoutInEUR = calcSupportPerRequest(contract.getReceiverId(), amountAtTime);
				fulfilNext(contract, new AmountAtTime(amountAtTime.validAt, payoutInEUR));
			}
		}
	}

	private double calcSupportPerRequest(long clientId, AmountAtTime amountAtTime) {
		PolicyChoice policyChoice = clientPolicyChoice.get(clientId);
		PolicyItem policyItem = policyItemsPerSet.get(policyChoice.set).getPolicyFor(policyChoice.supportInstrument);
		return amountAtTime.amount * policyItem.calcInfeedSupportRate(amountAtTime.validAt);
	}
}
