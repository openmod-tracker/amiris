package agents.flexibility.dynamicProgramming.bidding;

import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;

public class BidSchedulerBuilder {
	public static final Tree parameters = Make.newTree()
			.add(Make.newEnum("Type", Type.class), Make.newDouble("SchedulingHorizonInHours")).buildTree();

	/** Available {@link BidScheduler}s */
	enum Type {
		/** Ensure planned dispatch is fulfilled by bidding at technical price limits */
		EnsureDispatch,
	}

	public static final String ERR_NOT_IMPLEMENTED = "Bid Scheduler is not implemented: ";

	public static BidScheduler build(ParameterData input) throws MissingDataException {
		Type type = input.getEnum("Type", Type.class);
		switch (type) {
			case EnsureDispatch:
				return new EnsureDispatch(input.getDouble("SchedulingHorizonInHours"));
			default:
				throw new RuntimeException(ERR_NOT_IMPLEMENTED + type);
		}
	}
}
