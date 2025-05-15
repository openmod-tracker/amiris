// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import communications.message.AmountAtTime;
import de.dlr.gitlab.fame.time.TimeStamp;
import util.TimedDataMap;

/** Assesses flexibility dispatch history to derive bid multipliers. These multipliers determine how the dispatch of an individual
 * flexibility option behaves compared to the total dispatch of power plants.
 * 
 * @author Christoph Schimeczek, Johannes Kochems */
public class FlexibilityAssessor {
	static final String WARN_MISSING_REGISTRATION = "Agent with ID %s was not registered with SensitivityForecaster; it is recommended that all clients register, or none.";
	private static Logger logger = LoggerFactory.getLogger(FlexibilityAssessor.class);

	private final double cutOffFactor;

	private final HashMap<Long, Double> installedPowerPerClient = new HashMap<>();
	private final TimedDataMap<Long, Double> multiplierHistory = new TimedDataMap<>();
	private final TimedDataMap<Long, Double> awardHistory = new TimedDataMap<>();
	private final HashSet<TimeStamp> updatesRequiredAt = new HashSet<>();
	private final HashMap<Long, Double> sumOfMultipliers = new HashMap<>();
	private final HashMap<Long, Integer> numberOfPreviousSummands = new HashMap<>();
	private final HashMap<Long, Double> largestAwards = new HashMap<>();

	/** Instantiate a new {@link FlexibilityAssessor}
	 * 
	 * @param cutOffFactor ignore awards that have lower energy than this factor * largest previous award */
	public FlexibilityAssessor(double cutOffFactor) {
		this.cutOffFactor = cutOffFactor;
	}

	/** Register a client's installed power to provide a first guess for its power multiplier
	 * 
	 * @param clientId id of agent whose installed power is to be registered
	 * @param installedPowerInMW of the client valid at the beginning of the simulation */
	public void registerInstalledPower(long clientId, double installedPowerInMW) {
		installedPowerPerClient.put(clientId, installedPowerInMW);
		sumOfMultipliers.put(clientId, 0.);
		largestAwards.put(clientId, 0.);
	}

	/** Store a client's net awarded energy at a given time
	 * 
	 * @param clientId id of agent to store the net awarded energy for
	 * @param award telling the net awarded energy for a specific clearing time */
	public void saveAward(long clientId, AmountAtTime award) {
		awardHistory.set(award.validAt, clientId, award.amount);
		double largestPreviousAward = largestAwards.getOrDefault(clientId, 0.);
		double largestAward = Math.abs(award.amount) > largestPreviousAward ? Math.abs(award.amount) : largestPreviousAward;
		largestAwards.put(clientId, largestAward);
		updatesRequiredAt.add(award.validAt);
	}

	/** Process all previously stored awards and update each client's multiplier history */
	public void processAwards() {
		for (TimeStamp time : updatesRequiredAt) {
			HashMap<Long, Double> awards = awardHistory.getDataAt(time);
			double sum = sumValuesInMap(awards);
			for (var entry : awards.entrySet()) {
				double factor = calcFactor(entry.getKey(), entry.getValue(), sum);
				multiplierHistory.set(time, entry.getKey(), factor);
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

	/** @return factor for given client based on the client's award and the sum of all awards */
	private double calcFactor(long clientId, double award, double sum) {
		double largestAward = largestAwards.get(clientId);
		if (largestAward < 1E-3 || Math.abs(award) * cutOffFactor < largestAward) {
			return Double.NaN;
		}
		return sum / award;
	}

	/** Return a client's average multiplier derived from award history or installed power
	 * 
	 * @param clientId id of agent to obtain the multiplier for
	 * @return an estimate of the client's bid multiplier */
	public double getMultiplier(long clientId) {
		double[] result = calcMultiplierComponents(clientId, multiplierHistory.getValuesOf(clientId));
		return result[1] > 0 ? result[0] / result[1] : getInitialEstimate(clientId);
	}

	/** @return the sum of a client's multiplier history and the number of elements in that history */
	private double[] calcMultiplierComponents(long clientId, ArrayList<Double> multipliers) {
		double sum = sumOfMultipliers.getOrDefault(clientId, 0.);
		int numberOfValidMultipliers = numberOfPreviousSummands.getOrDefault(clientId, 0);
		for (double multiplier : multipliers) {
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
		return Math.max(-cutOffFactor, Math.min(cutOffFactor, multiplier));
	}

	/** Remove any stored award data from before the given time and compress the bid history
	 * 
	 * @param time elements associated with previous times are removed */
	public void clearBefore(TimeStamp time) {
		awardHistory.clearBefore(time);
		HashSet<Long> uniqueClients = multiplierHistory.getKeysBefore(time);
		for (long clientId : uniqueClients) {
			double[] result = calcMultiplierComponents(clientId, multiplierHistory.getValuesBefore(time, clientId));
			sumOfMultipliers.put(clientId, result[0]);
			numberOfPreviousSummands.put(clientId, (int) Math.round(result[1]));
		}
		multiplierHistory.clearBefore(time);
	}
}
