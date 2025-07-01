// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.policy.hydrogen;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import agents.policy.hydrogen.PolicyItem.SupportInstrument;
import de.dlr.gitlab.fame.agent.Agent;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;

public class HydrogenSupportPolicy extends Agent implements HydrogenSupportProvider {

	@Input private static final Tree parameters = Make.newTree()
			.add(Make.newGroup("SetSupportData").list().add(setParameter).addAs(SupportInstrument.MPFIX.name(),
					Mpfix.parameters))
			.buildTree();

	public class SetPolicyItems {
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

	private HashMap<String, SetPolicyItems> policyItemsPerSet = new HashMap<>();

	public HydrogenSupportPolicy(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData inputData = parameters.join(dataProvider);
		loadSetSupportData(inputData.getGroupList("SetSupportData"));
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
		SetPolicyItems setPolicyItems = policyItemsPerSet.computeIfAbsent(policySet, __ -> new SetPolicyItems());
		setPolicyItems.addPolicyItem(policyItem);
	}
}
