package agents.electrolysis;

import agents.markets.meritOrder.sensitivities.MeritOrderSensitivity;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.time.TimePeriod;
import util.Polynomial;

public class GreenHydrogen extends ElectrolyzerStrategist {
	public static enum TemporalCorrelationPeriod {
		HOURLY, MONTHLY,
	}

	/** Inputs specific to {@link GreenHydrogen} electrolyzer strategists */
	public static final Tree parameters = Make.newTree()
			.add(Make.newEnum("TemporalCorrelationPeriod", TemporalCorrelationPeriod.class).help(
					"Period in which electrolyzer production must match production of associated renewable plant"))
			.buildTree();
	
	private final TemporalCorrelationPeriod temporalCorrelationPeriod;

	/** Create new {@link GreenHydrogen}
	 * 
	 * @param generalInput parameter group associated with flexibility strategists in general
	 * @param specificInput parameter group associated with this strategist in specific
	 * @throws MissingDataException if any required input data is missing */
	public GreenHydrogen(ParameterData generalInput, ParameterData specificInput) throws MissingDataException {
		super(generalInput);
		temporalCorrelationPeriod = specificInput.getEnum("TemporalCorrelationPeriod", TemporalCorrelationPeriod.class);
	}
	
	@Override
	protected MeritOrderSensitivity createBlankSensitivity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateSchedule(TimePeriod timePeriod) {
		// TODO Auto-generated method stub

	}

}
