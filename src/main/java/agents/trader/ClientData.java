// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
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
	private final TreeMap<TimeStamp, Double> yieldPotentials = new TreeMap<>();
	private final TreeMap<TimeStamp, Double> dispatches = new TreeMap<>();
	private final TreeMap<TimeStamp, Double> marketRevenues = new TreeMap<>();
	/** Total amount of support payed in accounting interval in € */
	private final TreeMap<TimePeriod, Double> supportRevenueInEUR = new TreeMap<>();
	/** The market premium for the accounting interval in €/MWh */
	private final TreeMap<TimePeriod, Double> marketPremiaInEURperMWH = new TreeMap<>();
	private SupportData supportData;

	/** Create a client data object and initialise it with the given technology set
	 * 
	 * @param technologySet to be assigned */
	public ClientData(TechnologySet technologySet) {
		this.technologySet = technologySet;
	}

	/** Removes any internal data with TimeStamp before given TimeStamp; other arrays are cleared as well
	 * 
	 * @param timeStamp any stored data associated with earlier times are removed */
	public void clearBefore(TimeStamp timeStamp) {
		yieldPotentials.headMap(timeStamp).clear();
		dispatches.headMap(timeStamp).clear();
		marketRevenues.headMap(timeStamp).clear();
		clearBefore(timeStamp, supportRevenueInEUR);
		clearBefore(timeStamp, marketPremiaInEURperMWH);
	}

	/** Removes any element of a TreeMap with TimeSegment key if last time before given TimeStamp */
	private void clearBefore(TimeStamp timeStamp, TreeMap<TimePeriod, Double> map) {
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
		yieldPotentials.put(time, stepPotential);
	}

	/** Append dispatch and revenue for given time stamps to maps for tracking it
	 * 
	 * @param time at which to save dispatch and revenue
	 * @param dispatch at given time to save
	 * @param revenue at given time to save */
	public void appendStepDispatchAndRevenue(TimeStamp time, double dispatch, double revenue) {
		dispatches.put(time, dispatch);
		marketRevenues.put(time, revenue);
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
		return dispatches;
	}

	public TreeMap<TimeStamp, Double> getMarketRevenue() {
		return marketRevenues;
	}

	public TreeMap<TimeStamp, Double> getYieldPotential() {
		return yieldPotentials;
	}

	public TreeMap<TimePeriod, Double> getSupportRevenueInEUR() {
		return supportRevenueInEUR;
	}

	public TreeMap<TimePeriod, Double> getMarketPremiaInEURperMWH() {
		return marketPremiaInEURperMWH;
	}

	public void setSupportData(SupportData supportData) {
		this.supportData = supportData;
	}

	public SupportData getSupportData() {
		return supportData;
	}
}
