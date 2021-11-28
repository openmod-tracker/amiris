package agents.policy;

import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.communication.transfer.Portable;
import de.dlr.gitlab.fame.data.TimeSeries;

/** Holds the information of a feed-in tariff (FIT) support model needed by the SupportPolicy and part of a FitData
 * 
 * @author Johannes Kochems */
public class FitInfo extends PolicyInfo {
	static final Tree parameters = Make.newTree()
			.add(Make.newSeries("TsFit").optional(), Make.newDouble("SuspensionVolumeShare").optional()).buildTree();

	/** The time series containing the FIT applicable in EUR/MWh */
	private TimeSeries tsFit;
	/** The share of installed infeed capacity at which the FIT is capped */
	private double suspensionVolumeShare;

	@Override
	public void setDataFromConfig(ParameterData group) throws MissingDataException {
		tsFit = group.getTimeSeries("TsFit");
		suspensionVolumeShare = group.getDoubleOrDefault("SuspensionVolumeShare", 1.0);
	}

	/** required for {@link Portable}s */
	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeTimeSeries(tsFit);
		collector.storeDoubles(suspensionVolumeShare);
	}

	/** required for {@link Portable}s */
	@Override
	public void populate(ComponentProvider provider) {
		tsFit = provider.nextTimeSeries();
		suspensionVolumeShare = provider.nextDouble();
	}

	/** @return time series of feed-in tariff in EUR/MWh */
	public TimeSeries getTsFit() {
		return tsFit;
	}

	/** @return share of installed in-feed capacity at which the FIT is capped */
	public double getSuspensionVolumeShare() {
		return suspensionVolumeShare;
	}
}
