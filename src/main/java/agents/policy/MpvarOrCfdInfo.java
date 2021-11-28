package agents.policy;

import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;
import de.dlr.gitlab.fame.data.TimeSeries;

/** Holds the information of a variable market premium (MPVAR) or a contracts for differences (CFD) support model needed by the
 * SupportPolicy and part of a MpVarOrCfdData
 * 
 * @author Johannes Kochems */
public class MpvarOrCfdInfo extends PolicyInfo {
	public static final Tree parameters = Make.newTree().add(Make.newSeries("Lcoe").optional()).buildTree();

	/** The levelised cost of electricity (value applied) */
	private TimeSeries lcoe;

	@Override
	public void setDataFromConfig(ParameterData group) throws MissingDataException {
		lcoe = group.getTimeSeries("Lcoe");
	}

	/** required for {@link Portable}s */
	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeTimeSeries(lcoe);
	}

	/** required for {@link Portable}s */
	@Override
	public void populate(ComponentProvider provider) {
		lcoe = provider.nextTimeSeries();
	}

	/** @return levelised cost of electricity in EUR/MWh */
	public TimeSeries getLcoe() {
		return lcoe;
	}
}
