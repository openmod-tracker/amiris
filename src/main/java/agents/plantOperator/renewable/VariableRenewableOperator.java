package agents.plantOperator.renewable;

import agents.plantOperator.RenewablePlantOperator;
import communications.message.MarginalCost;
import de.dlr.gitlab.fame.agent.input.DataProvider;
import de.dlr.gitlab.fame.agent.input.Input;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimeStamp;
import de.dlr.gitlab.fame.agent.input.Tree;

/** An operator of variable renewable energy sources plants that depend on a yield profile.
 * 
 * @author Christoph Schimeczek, Johannes Kochems */
public class VariableRenewableOperator extends RenewablePlantOperator {
	@Input private static final Tree parameters = Make.newTree().add(Make.newSeries("YieldProfile")).buildTree();

	private TimeSeries tsYieldProfile;

	/** Creates an {@link VariableRenewableOperator}
	 * 
	 * @param dataProvider provides input from config
	 * @throws MissingDataException if any required data is not provided */
	public VariableRenewableOperator(DataProvider dataProvider) throws MissingDataException {
		super(dataProvider);
		ParameterData input = parameters.join(dataProvider);
		tsYieldProfile = input.getTimeSeries("YieldProfile");
	}

	/** @return single {@link MarginalCost} considering variable yield */
	@Override
	protected MarginalCost calcSingleMarginal(TimeStamp time) {
		if (tsYieldProfile == null) {
			throw new RuntimeException("Yield profile is missing for " + this);
		}
		double availablePower = getInstalledPowerAtTime(time) * getYieldAtTime(time);
		double marginalCost = getVariableOpexAtTime(time);
		return new MarginalCost(getId(), availablePower, marginalCost, time);
	}

	/** @return yield [0..1] relative to peak capacity at given time */
	private double getYieldAtTime(TimeStamp time) {
		return tsYieldProfile.getValueLinear(time);
	}

}