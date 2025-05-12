// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import communications.message.AmountAtTime;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Assesses flexibility dispatch history to derive bid multipliers. These multipliers determine how the dispatch of an individual
 * flexibility option behaves compared to the overall dispatch.
 * 
 * @author Christoph Schimeczek, Johannes Kochems */
public class FlexibilityAssessor {
	static final double IGNORE_THRESHOLD_IN_MWH = 1E-1;
	static final double FACTOR_LIMIT = 100;

	private final HashMap<Long, Double> installedPowerPerClient = new HashMap<>();
	private final TreeMap<TimeStamp, HashMap<Long, Double>> multiplierHistory = new TreeMap<>();

	private final TreeMap<TimeStamp, HashMap<Long, Double>> awards = new TreeMap<>();
	private final TreeMap<TimeStamp, Boolean> updatesRequiredAt = new TreeMap<>();
	private final TreeMap<TimeStamp, Double> awardTotals = new TreeMap<>();
	private int numberOfPreviousSummands = 0;
	private HashMap<Long, Double> sumOfMultipliers = new HashMap<>();

	/** Register a client's installed power to provide a first guess for its power multiplier
	 * 
	 * @param clientId whose installed power is to be registered
	 * @param installedPowerInMW of the client valid at the beginning of the simulation */
	public void registerInstalledPower(long clientId, double installedPowerInMW) {
		installedPowerPerClient.put(clientId, installedPowerInMW);
		sumOfMultipliers.put(clientId, 0.);
	}

	/** Store a client's net awarded energy at a given time
	 * 
	 * @param clientId to store the net awarded energy for
	 * @param award telling the net awarded energy for a specific clearing time */
	public void saveAward(long clientId, AmountAtTime award) {
		awards.putIfAbsent(award.validAt, new HashMap<Long, Double>());
		awards.get(award.validAt).put(clientId, award.amount);
		updatesRequiredAt.put(award.validAt, true);
	}

	/** Process all previously stored awards and update each client's multiplier history */
	public void processAwards() {
		for (TimeStamp time : updatesRequiredAt.keySet()) {
			var allAwards = awards.get(time);
			double sum = 0;
			for (double value : allAwards.values()) {
				sum += value;
			}
			awardTotals.put(time, sum);
			var multiplierPerClient = multiplierHistory.getOrDefault(time, new HashMap<>());
			for (var entry : awards.get(time).entrySet()) {
				double factor = Math.abs(sum) < IGNORE_THRESHOLD_IN_MWH ? Double.NaN : entry.getValue() / sum;
				multiplierPerClient.put(entry.getKey(), Math.max(-FACTOR_LIMIT, Math.min(FACTOR_LIMIT, factor)));
			}
			multiplierHistory.put(time, multiplierPerClient);
		}
		updatesRequiredAt.clear();
	}

	/** Return a client's average multiplier derived from award history or installed power
	 * 
	 * @param clientId to obtain the multiplier for
	 * @return an estimate of the client's bid multiplier */
	public double getMultiplier(long clientId) {
		double[] result = calcMultiplierComponents(clientId, multiplierHistory);
		return result[1] > 0 ? result[0] / result[1] : getInitialEstimate(clientId);
	}

	/** @return the sum of a client's multiplier history and the number of elements in that history */
	private double[] calcMultiplierComponents(long clientId, SortedMap<TimeStamp, HashMap<Long, Double>> allMultipliers) {
		double sum = sumOfMultipliers.getOrDefault(clientId, 0.);
		int numberOfValidMultipliers = numberOfPreviousSummands;
		for (var multiplierPerClient : multiplierHistory.values()) {
			double multiplier = multiplierPerClient.get(clientId);
			if (!Double.isNaN(multiplier)) {
				sum += multiplier;
				numberOfValidMultipliers++;
			}
		}
		return new double[] {sum, numberOfValidMultipliers};
	}

	/** @return an estimate of a client's bid multiplier based on its installed power */
	private double getInitialEstimate(Long clientId) {
		double installedCapacityTotal = 0;
		for (double value : installedPowerPerClient.values()) {
			installedCapacityTotal += value;
		}
		return installedCapacityTotal > 0 ? installedPowerPerClient.get(clientId) / installedCapacityTotal : 1;
	}

	/** Remove stored award data that precede the given time and compress the bid history
	 * 
	 * @param time elements associated with previous times are removed */
	public void clearBefore(TimeStamp time) {
		awards.headMap(time).clear();
		awardTotals.headMap(time).clear();
		var multipliersToDelete = multiplierHistory.headMap(time);
		for (long clientId : sumOfMultipliers.keySet()) {
			double[] result = calcMultiplierComponents(clientId, multipliersToDelete);
			sumOfMultipliers.put(clientId, result[0]);
			numberOfPreviousSummands = (int) Math.round(result[1]);
		}
		multipliersToDelete.clear();
	}
}
