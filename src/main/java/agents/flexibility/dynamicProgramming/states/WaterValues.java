package agents.flexibility.dynamicProgramming.states;

import java.util.List;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.time.TimeStamp;

public class WaterValues {
	public static final Tree parameters = Make.newTree().list().optional().add(
			Make.newDouble("StoredEnergyInMWH"), Make.newSeries("WaterValueInEUR")).buildTree();

	private final boolean hasData;

	public WaterValues(List<ParameterData> inputs) throws MissingDataException {
		hasData = !(inputs == null || inputs.isEmpty());
	}

	/** Returns interpolated water value for specified time and energy content, or 0 if no has is available
	 * 
	 * @param time at which to get the water value for
	 * @param energyContentInMWH for which to get the water value for
	 * @return water value in EUR */
	public double getValueInEUR(TimeStamp time, double energyContentInMWH) {
		if (!hasData) {
			return 0;
		}

		return 0; // TODO: implement
	}

	/** Returns true if any water value interpolation can be provided, false otherwise
	 * 
	 * @return true if water value interpolation is possible */
	public boolean hasData() {
		return hasData;
	}
}
