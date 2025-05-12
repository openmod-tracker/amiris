// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import communications.message.AmountAtTime;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Assesses flexibility dispatch history to derive bid multipliers. These multipliers determine how the dispatch of an individual
 * flexibility option behaves compared to the overall dispatch.
 * 
 * @author Christoph Schimeczek, Johannes Kochems */
public class FlexibilityAssessor {
	static final String WARN_MISSING_REGISTRATION = "Agent with ID %s was not registered at SensitivityForecaster; it is recommended that all or no clients register.";
	private static Logger logger = LoggerFactory.getLogger(FlexibilityAssessor.class);

	private final double factorLimit;

	private final HashMap<Long, Double> installedPowerPerClient = new HashMap<>();
	private final TreeMap<TimeStamp, HashMap<Long, Double>> multiplierHistory = new TreeMap<>();
	private final TreeMap<TimeStamp, HashMap<Long, Double>> awards = new TreeMap<>();
	private final HashSet<TimeStamp> updatesRequiredAt = new HashSet<>();
	private final HashMap<Long, Double> sumOfMultipliers = new HashMap<>();
	private final HashMap<Long, Integer> numberOfPreviousSummands = new HashMap<>();

	/** Instantiate a new {@link FlexibilityAssessor}
	 * 
	 * @param factorLimit maximum factor that is to be logged - higher ratios will be capped at this value */
	public FlexibilityAssessor(double factorLimit) {
		this.factorLimit = factorLimit;
	}

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
		updatesRequiredAt.add(award.validAt);
	}

	/** Process all previously stored awards and update each client's multiplier history */
	public void processAwards() {
		for (TimeStamp time : updatesRequiredAt) {
			double sum = sumValuesInMap(awards.get(time));
			multiplierHistory.putIfAbsent(time, new HashMap<>());
			var multiplierPerClient = multiplierHistory.get(time);
			for (var entry : awards.get(time).entrySet()) {
				double factor = Math.abs(entry.getValue()) > 0 ? sum / entry.getValue() : Double.NaN;
				multiplierPerClient.put(entry.getKey(), Math.max(-factorLimit, Math.min(factorLimit, factor)));
			}
		}
		updatesRequiredAt.clear();
	}

	/** @return sum of double values in given map */
	private double sumValuesInMap(HashMap<?, Double> map) {
		double sum = 0;
		for (double value : map.values()) {
			sum += value;
		}
		return sum;
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
		int numberOfValidMultipliers = numberOfPreviousSummands.getOrDefault(clientId, 0);
		for (var multiplierPerClient : allMultipliers.values()) {
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
		if (!installedPowerPerClient.containsKey(clientId) || installedPowerPerClient.get(clientId) <= 0) {
			if (installedPowerPerClient.size() > 0) {
				logger.warn(String.format(WARN_MISSING_REGISTRATION, clientId));
			}
			return 1.0;
		}
		double installedCapacityTotal = 0;
		for (double value : installedPowerPerClient.values()) {
			installedCapacityTotal += value;
		}
		double multiplier = installedCapacityTotal / installedPowerPerClient.get(clientId);
		return Math.max(-factorLimit, Math.min(factorLimit, multiplier));
	}

	/** Remove stored award data that precede the given time and compress the bid history
	 * 
	 * @param time elements associated with previous times are removed */
	public void clearBefore(TimeStamp time) {
		awards.headMap(time).clear();
		var multipliersToDelete = multiplierHistory.headMap(time);
		var uniqueClients = getKeysFromEntries(multipliersToDelete);
		for (long clientId : uniqueClients) {
			double[] result = calcMultiplierComponents(clientId, multipliersToDelete);
			sumOfMultipliers.put(clientId, result[0]);
			numberOfPreviousSummands.put(clientId, (int) Math.round(result[1]));
		}
		multipliersToDelete.clear();
	}

	/** @return list of all key present in the entries of the given map */
	private HashSet<Long> getKeysFromEntries(Map<?, HashMap<Long, Double>> multipliers) {
		HashSet<Long> clients = new HashSet<>();
		for (var value : multipliers.values()) {
			clients.addAll(value.keySet());
		}
		return clients;
	}
}
