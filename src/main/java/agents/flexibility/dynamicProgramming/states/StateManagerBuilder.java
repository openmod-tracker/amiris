package agents.flexibility.dynamicProgramming.states;

import agents.flexibility.GenericDevice;
import agents.flexibility.dynamicProgramming.assessment.AssessmentFunction;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;

public class StateManagerBuilder {
	public static final Tree parameters = Make.newTree().add(Make.newEnum("Type", Type.class),
			Make.newInt("PlanningHorizonInHours"), Make.newDouble("EnergyResolutionInMWH")).buildTree();

	/** Available {StateManager}s */
	enum Type {
		/** Manages energy states of an electricity storage device */
		Storage,
	}

	public static final String ERR_NOT_IMPLEMENTED = "StateManager is not implemented: ";

	public static StateManager build(GenericDevice device, AssessmentFunction assessment, ParameterData input)
			throws MissingDataException {
		Type type = input.getEnum("Type", Type.class);
		switch (type) {
			case Storage:
				return new StorageStateManager(device, assessment, input.getDouble("PlanningHorizonInHours"),
						input.getDouble("EnergyResolutionInMWH"));
			default:
				throw new RuntimeException(ERR_NOT_IMPLEMENTED + type);
		}
	}
}
