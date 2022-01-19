package agents.policy;

import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;
import de.dlr.gitlab.fame.data.TimeSeries;

/** Holds the information of a capacity premium (CP) support model needed by the SupportPolicy and part of a CPData
 * 
 * @author Johannes Kochems */
public class CPInfo extends PolicyInfo {
	public static final Tree parameters = Make.newTree().add(Make.newSeries("Premium").optional()).buildTree();

	/** The capacity premium in EUR/MW installed */
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

	/** @return time series capacity premium in EUR/MW */
	public TimeSeries getPremium() {
		return premium;
	}
}
