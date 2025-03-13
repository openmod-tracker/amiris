package agents.flexibility.dynamicProgramming;

import de.dlr.gitlab.fame.time.TimePeriod;

/** A function to assess states and transitions
 * 
 * @author Christoph Schimeczek, Felix Nitsch, Johannes Kochems */
public interface AssessmentFunction {
	/** @param timePeriod currently evaluated */
	void prepareFor(TimePeriod timePeriod);

	double getTransitionValueFor(int initialStateIndex, int finalStateIndex);
}
