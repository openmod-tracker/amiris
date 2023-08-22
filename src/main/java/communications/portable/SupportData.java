// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.portable;

import agents.plantOperator.RenewablePlantOperator.SetType;
import agents.policy.PolicyItem;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;

/** Transmits a SetType-specific PolicyInfo
 * 
 * @author Johannes Kochems, Christoph Schimeczek */
public final class SupportData implements Portable {
	static final String WRONG_TYPE = "SupportData item does not contain PolicyInfo of type: ";

	private SetType setType;
	/** setType-specific policy */
	private PolicyItem policyItem;

	/** required for {@link Portable}s */
	public SupportData() {}

	public SupportData(SetType setType, PolicyItem policyInfo) {
		this.setType = setType;
		this.policyItem = policyInfo;
	}

	/** required for {@link Portable}s */
	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeInts(setType.ordinal());
		collector.storeComponents(policyItem);
	}

	/** required for {@link Portable}s */
	@Override
	public void populate(ComponentProvider provider) {
		setType = SetType.values()[provider.nextInt()];
		policyItem = provider.nextComponent(PolicyItem.class);
	}

	/** @return SetType associated with the also contained PolicyInfo */
	public SetType getSetType() {
		return setType;
	}

	/** Returns PolicyInfo of requested class type - if types do not match an Exception is thrown
	 * 
	 * @param <T> requested type of PolicyInfo
	 * @param type class of requested PolicyInfo
	 * @return object of requested type of PolicyInfo type
	 * @throws RuntimeException if contained PolicyInfo is of other than requested type */
	@SuppressWarnings("unchecked")
	public <T> T getPolicyOfType(Class<T> type) {
		if (type.isInstance(policyItem)) {
			return (T) policyItem;
		} else {
			throw new RuntimeException(WRONG_TYPE + type);
		}
	}
}
