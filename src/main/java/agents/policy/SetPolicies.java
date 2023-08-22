// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.policy;

import java.util.EnumMap;
import agents.plantOperator.RenewablePlantOperator.SetType;
import agents.policy.PolicyItem.SupportInstrument;
import agents.policy.SupportPolicy.EnergyCarrier;
import communications.message.TechnologySet;
import communications.portable.SupportData;

/** Controls all {@link SetType} specific policy items
 * 
 * @author Christoph Schimeczek */
public class SetPolicies {
	static final String ERR_POLICY_UNCONFIGURED = ": Policy not configured for instrument ";

	/** Holds set-specific support policies
	 * 
	 * @author Johannes Kochems, Christoph Schimeczek */
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

	/** Maps each set to its support data */
	private EnumMap<SetType, SetPolicyItems> policyItemsPerSet = new EnumMap<>(SetType.class);
	/** Maps each set to its energy carrier */
	private EnumMap<SetType, EnergyCarrier> energyCarrierPerSet = new EnumMap<>(SetType.class);

	public void addSetPolicyItem(SetType set, PolicyItem policyItem) {
		SetPolicyItems supportData = policyItemsPerSet.computeIfAbsent(set, __ -> new SetPolicyItems());
		supportData.addPolicyItem(policyItem);
	}

	public void register(TechnologySet technologySet) {
		energyCarrierPerSet.put(technologySet.setType, technologySet.energyCarrier);
		if (getPolicyItem(technologySet.setType, technologySet.supportInstrument) == null) {
			throw new RuntimeException(technologySet.setType + ERR_POLICY_UNCONFIGURED + technologySet.supportInstrument);
		}
	}

	public EnergyCarrier getEnergyCarrier(SetType set) {
		return energyCarrierPerSet.get(set);
	}

	public SupportData getSupportData(TechnologySet technologySet) {
		SetType set = technologySet.setType;
		return new SupportData(set, getPolicyItem(set, technologySet.supportInstrument));
	}

	/** Fetches {@link PolicyItem} for given {@link SetType} and {@link SupportInstrument}
	 * 
	 * @param set associated with the returned {@link PolicyItem}
	 * @param instrument used by the given set and associated with the returned {@link PolicyItem}
	 * @return PolicyItem for given set and support instrument */
	public PolicyItem getPolicyItem(SetType set, SupportInstrument instrument) {
		return policyItemsPerSet.get(set).getPolicyFor(instrument);
	}
}
