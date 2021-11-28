package agents.trader;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import agents.plantOperator.RenewablePlantOperator;
import communications.message.TechnologySet;
import communications.portable.SupportData;
import de.dlr.gitlab.fame.time.TimePeriod;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Holds data on the client of an AggregatorTrader (i.e. a {@link RenewablePlantOperator})
 * 
 * @author Johannes Kochems, Christoph Schimeczek */
public class ClientData {
	private TechnologySet technologySet;
	private double installedCapacity;
	private final TreeMap<TimeStamp, Double> yieldPotential = new TreeMap<>();
	private final TreeMap<TimeStamp, Double> dispatch = new TreeMap<>();
	private final TreeMap<TimeStamp, Double> marketRevenue = new TreeMap<>();
	/** Total amount of support payed in accounting interval in € */
	private final TreeMap<TimePeriod, Double> supportRevenueInEUR = new TreeMap<>();
	/** The market premium for the accounting interval in €/MWh */
	private final TreeMap<TimePeriod, Double> marketPremiaInEURperMWH = new TreeMap<>();
	private SupportData supportInfo;

	/** Create a client data object and initialize it only with the technology set */
	public ClientData(TechnologySet technologySet) {
		this.technologySet = technologySet;
	}

	/** Removes any internal data with TimeStamp before given TimeStamp */
	public void clearBefore(TimeStamp timeStamp) {
		yieldPotential.headMap(timeStamp).clear();
		dispatch.headMap(timeStamp).clear();
		marketRevenue.headMap(timeStamp).clear();
		clearBefore(timeStamp, supportRevenueInEUR);
		clearBefore(timeStamp, marketPremiaInEURperMWH);
	}

	/** Removes any element of a TreeMap with TimeSegment key if last time before given TimeStamp */
	public void clearBefore(TimeStamp timeStamp, TreeMap<TimePeriod, Double> map) {
		Iterator<Entry<TimePeriod, Double>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<TimePeriod, Double> entry = iterator.next();
			TimePeriod timeSegment = entry.getKey();
			if (timeSegment.getLastTime().isLessThan(timeStamp)) {
				iterator.remove();
			} else if (timeSegment.getStartTime().isGreaterEqualTo(timeStamp)) {
				break;
			}
		}
	}

	public void setInstalledCapacity(double installedCapacity) {
		this.installedCapacity = installedCapacity;
	}

	public void appendYieldPotential(TimeStamp time, double stepPotential) {
		yieldPotential.put(time, stepPotential);
	}

	/** Append dispatch and revenue for given time stamps to maps for tracking it */
	public void appendStepDispatchAndRevenue(TimeStamp time, double stepDispatch, double stepRevenue) {
		dispatch.put(time, stepDispatch);
		marketRevenue.put(time, stepRevenue);
	}

	public void appendSupportRevenue(TimePeriod accountingPeriod, double amount) {
		supportRevenueInEUR.put(accountingPeriod, amount);
	}

	public void appendMarketPremium(TimePeriod accountingPeriod, double marketPremium) {
		marketPremiaInEURperMWH.put(accountingPeriod, marketPremium);
	}

	public TechnologySet getTechnologySet() {
		return technologySet;
	}

	public double getInstalledCapacity() {
		return installedCapacity;
	}

	public TreeMap<TimeStamp, Double> getDispatch() {
		return dispatch;
	}

	public TreeMap<TimeStamp, Double> getMarketRevenue() {
		return marketRevenue;
	}

	public TreeMap<TimeStamp, Double> getYieldPotential() {
		return yieldPotential;
	}

	public TreeMap<TimePeriod, Double> getSupportRevenueInEUR() {
		return supportRevenueInEUR;
	}

	public TreeMap<TimePeriod, Double> getMarketPremiaInEURperMWH() {
		return marketPremiaInEURperMWH;
	}

	public void setSupportInfo(SupportData info) {
		this.supportInfo = info;
	}

	public SupportData getSupportInfo() {
		return supportInfo;
	}
}
