package agents.trader;

import de.dlr.gitlab.fame.communication.Product;

public interface PowerPlantScheduler {
	/** Products of {@link PowerPlantScheduler} */
	@Product
	public static enum Products {
		/** Electricity production assignment to clients */
		DispatchAssignment
	};
}
