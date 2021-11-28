package agents.policy;

import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.communication.transfer.Portable;

/** Unify construction and transmission of support policy info objects */
public abstract class PolicyInfo implements Portable {
	static final String ERR_CONSTRUCTOR = "Ensure classes has accessible default constructor: ";
	static final String ERR_CONFIG = "Config data for PolicyInfo incomplete: ";

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
		try {
			ParameterData subGroup = group.getGroup(subGroupName);
			try {
				info = infoClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(PolicyInfo.ERR_CONSTRUCTOR + infoClass);
			}
			try {
				info.setDataFromConfig(subGroup);
			} catch (MissingDataException e) {
				throw new RuntimeException(PolicyInfo.ERR_CONFIG + infoClass);
			}
		} catch (MissingDataException e) {} // do nothing here -> returns null
		return info;
	}
}
