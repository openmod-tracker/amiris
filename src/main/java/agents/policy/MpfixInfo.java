package agents.policy;

import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;
import de.dlr.gitlab.fame.data.TimeSeries;

/** Holds the information of a fixed market premium (MPFIX) support model needed by the SupportPolicy and part of a MpFixData
 * 
 * @author Johannes Kochems */
public class MpfixInfo extends PolicyInfo {
	public static final Tree parameters = Make.newTree().add(Make.newSeries("Premium").optional()).buildTree();

	/** The fixed market premium in EUR/MWh */
	private TimeSeries premium;

	@Override
	public void setDataFromConfig(ParameterData group) throws MissingDataException {
		premium = group.getTimeSeries("Premium");
	}

	/** required for {@link Portable}s */
	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeTimeSeries(premium);
	}

	/** required for {@link Portable}s */
	@Override
	public void populate(ComponentProvider provider) {
		premium = provider.nextTimeSeries();
	}

	/** @return fixed market premium in EUR/MWh */
	public TimeSeries getPremium() {
		return premium;
	}

}
