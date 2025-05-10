// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.flexibility.dynamicProgramming.assessment;

import java.util.ArrayList;
import java.util.TreeMap;
import communications.message.PointInTime;
import communications.portable.Sensitivity;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.time.TimeStamp;

/** {@link AssessmentFunction} that is based on {@link Sensitivity}
 * 
 * @author Christoph Schimeczek */
public abstract class SensitivityBasedAssessment implements AssessmentFunction {
	private final TreeMap<TimeStamp, Sensitivity> sensitivityForecasts = new TreeMap<>();
	private Sensitivity currentSensitivity;

	@Override
	public void prepareFor(TimeStamp time) {
		currentSensitivity = sensitivityForecasts.get(time);
	}

	@Override
	public double assessTransition(double externalEnergyDeltaInMWH) {
		return currentSensitivity.getValue(externalEnergyDeltaInMWH);
	}

	@Override
	public void clearBefore(TimeStamp time) {
		Util.clearMapBefore(sensitivityForecasts, time);
	}

	@Override
	public ArrayList<TimeStamp> getMissingForecastTimes(ArrayList<TimeStamp> planningTimes) {
		return Util.findMissingKeys(sensitivityForecasts, planningTimes);
	}

	@Override
	public void storeForecast(ArrayList<Message> messages) {
		double multiplier = 0;
		for (Message inputMessage : messages) {
			Sensitivity sensitivity = inputMessage.getAllPortableItemsOfType(Sensitivity.class).get(0);
			multiplier = sensitivity.getMultiplier();
			TimeStamp time = inputMessage.getDataItemOfType(PointInTime.class).validAt;
			sensitivityForecasts.put(time, sensitivity);
		}
		for (var entry : sensitivityForecasts.entrySet()) {
			entry.getValue().updateMultiplier(multiplier);
		}
	}
}
