package agents.policy;

import java.util.TreeMap;
import communications.message.SupportRequestData;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.transfer.ComponentCollector;
import de.dlr.gitlab.fame.communication.transfer.ComponentProvider;
import de.dlr.gitlab.fame.data.TimeSeries;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Set-specific realisation of a financial contract for difference according to <a
 * href=https://www.econstor.eu/handle/10419/268370>Schlecht et al. 2023</a>
 * 
 * @author Johannes Kochems, Christoph Schimeczek */
public class FinancialCfd extends PolicyItem {
	public static final Tree parameters = Make.newTree()
			.add(premiumParam, Make.newSeries("ReferenceYieldProfile").optional())
			.buildTree();

	private TimeSeries referenceYieldProfile;
	private TimeSeries premiumPerMW;

	@Override
	protected void setDataFromConfig(ParameterData group) throws MissingDataException {
		referenceYieldProfile = group.getTimeSeries("ReferenceYieldProfile");
		premiumPerMW = group.getTimeSeries("Premium");
	}

	@Override
	public void addComponentsTo(ComponentCollector collector) {
		collector.storeTimeSeries(referenceYieldProfile, premiumPerMW);
	}

	@Override
	public void populate(ComponentProvider provider) {
		referenceYieldProfile = provider.nextTimeSeries();
		premiumPerMW = provider.nextTimeSeries();
	}

	@Override
	public SupportInstrument getSupportInstrument() {
		return SupportInstrument.FINANCIAL_CFD;
	}

	/** Does not return the actual infeed, but the negative profit total of the reference power plant in the accounting period */
	@Override
	public double calcEligibleInfeed(TreeMap<TimeStamp, Double> powerPrices, SupportRequestData request) {
		double referenceProfit = 0;
		for (TimeStamp time : request.infeed.keySet()) {
			double powerPrice = powerPrices.get(time);
			if (powerPrice > 0) {
				double referenceYield = referenceYieldProfile.getValueLinear(time);
				referenceProfit += powerPrice * referenceYield;
			}
		}
		return -request.installedCapacityInMW * referenceProfit;
	}

	/** Does not return the per MWH support, but 1 */
	@Override
	public double calcInfeedSupportRate(TimePeriod accountingPeriod, double marketValue) {
		return 1;
	}

	@Override
	public double calcEligibleCapacity(SupportRequestData request) {
		return request.installedCapacityInMW;
	}

	@Override
	public double calcCapacitySupportRate(TimePeriod accountingPeriod) {
		return premiumPerMW.getValueLowerEqual(accountingPeriod.getStartTime());
	}

	@Override
	public boolean isTypeOfMarketPremium() {
		return false;
	}
}
