// SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.policy;

import java.lang.reflect.InvocationTargetException;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.communication.transfer.Portable;

/** Unify construction and transmission of support policy info objects */
public abstract class PolicyInfo implements Portable {
	static final String ERR_CONSTRUCTOR = "Ensure class has accessible default constructor: ";
	static final String ERR_CONFIG = "Incomplete config data for PolicyInfo: ";

	/** Initialises an empty PolicyInfo with its associated config data
	 * 
	 * @param group DataProvider group matching type of PolicyInfo to provide input from config
	 * @throws MissingDataException if any required data is not provided */
	abstract public void setDataFromConfig(ParameterData group) throws MissingDataException;

	/** Instantiates a PolicyInfo object of given class type by feeding it with the associated config data
	 * 
	 * @param <T> type of policy info to instantiate
	 * @param infoClass class to policy info to instantiate
	 * @param subGroupName associated name of matching parameter sub-group in given group
	 * @param group data from config; group should contain requested subGroup - if not: null is returned
	 * @return PolicyInfo object initialised with matching config data; null if group contains no parameters for requested subGroup
	 * @throws RuntimeException if config data for given type is incomplete or if default constructor is missing / inaccessible */
	public static <T extends PolicyInfo> T buildPolicyInfo(Class<T> infoClass, String subGroupName, ParameterData group) {
		T info = null;
		ParameterData subGroup = getSubGroupOrNull(group, subGroupName);
		if (subGroup != null) {
			info = instantiatePolicyInfo(infoClass);
			initialisePolicyInfo(info, subGroup);
		}
		return info;
	}

	/** @return subgroup of given name for given group if it is present, null otherwise */
	private static ParameterData getSubGroupOrNull(ParameterData group, String subGroupName) {
		try {
			return group.getGroup(subGroupName);
		} catch (MissingDataException e) {
			return null;
		}
	}

	/** @return newly instantiated PolicyInfo based on required class */
	private static <T extends PolicyInfo> T instantiatePolicyInfo(Class<T> infoClass) {
		try {
			return infoClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(PolicyInfo.ERR_CONSTRUCTOR + infoClass);
		}
	}

	private static void initialisePolicyInfo(PolicyInfo info, ParameterData parameters) {
		try {
			info.setDataFromConfig(parameters);
		} catch (MissingDataException e) {
			throw new RuntimeException(PolicyInfo.ERR_CONFIG + info.getClass().getSimpleName());
		}		
	}
}
