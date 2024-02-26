package agents.electrolysis;

import agents.markets.meritOrder.sensitivities.MeritOrderSensitivity;
import communications.message.AmountAtTime;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.time.TimePeriod;

public class GreenHydrogen extends ElectrolyzerStrategist {
	public static enum TemporalCorrelationPeriod {
		HOURLY, MONTHLY,
	}

	static final String ERR_CORRELATION_PERIOD_NOT_IMPLEMENTED = "Correlation period is not implemented yet: ";

	/** Inputs specific to {@link GreenHydrogen} electrolyzer strategists */
	public static final Tree parameters = Make.newTree()
			.add(Make.newEnum("TemporalCorrelationPeriod", TemporalCorrelationPeriod.class).help(
					"Period in which electrolyzer production must match production of associated renewable plant"))
			.buildTree();

	private final TemporalCorrelationPeriod temporalCorrelationPeriod;
	private double maximumConsumption;

	/** Create new {@link GreenHydrogen}
	 * 
	 * @param generalInput parameter group associated with flexibility strategists in general
	 * @param specificInput parameter group associated with this strategist in specific
	 * @throws MissingDataException if any required input data is missing */
	public GreenHydrogen(ParameterData generalInput, ParameterData specificInput) throws MissingDataException {
		super(generalInput);
		temporalCorrelationPeriod = specificInput.getEnum("TemporalCorrelationPeriod", TemporalCorrelationPeriod.class);
		if (temporalCorrelationPeriod == TemporalCorrelationPeriod.MONTHLY) {
			throw new RuntimeException(ERR_CORRELATION_PERIOD_NOT_IMPLEMENTED + temporalCorrelationPeriod);
		}
	}

	@Override
	public void calcMaximumConsumption(AmountAtTime yieldPotential) {
		maximumConsumption = electrolyzer.calcCappedElectricDemandInMW(yieldPotential.amount, yieldPotential.validAt);
	}

	@Override
	public double getMaximumConsumption() {
		return maximumConsumption;
	}

	@Override
	protected MeritOrderSensitivity createBlankSensitivity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateSchedule(TimePeriod timePeriod) {
		updateScheduleArrays(actualProducedHydrogen);

	}

	/** transfer optimised dispatch to schedule arrays */
	private void updateScheduleArrays(double initialHydrogenProductionInMWH) {
		for (int hour = 0; hour < scheduleDurationPeriods; hour++) {
			demandScheduleInMWH[hour] = maximumConsumption;
			scheduledChargedHydrogenTotal[hour] = initialHydrogenProductionInMWH;
			initialHydrogenProductionInMWH += electrolyzer.calcHydrogenEnergy(demandScheduleInMWH[hour]);
		}
	}

}
