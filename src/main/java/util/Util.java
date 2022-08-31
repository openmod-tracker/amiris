// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.communication.message.Message;

/** Collection of general static methods used across packages
 * 
 * @author Christoph Schimeczek, Martin Klein, Marc Deissenroth */
public final class Util {
	static final String INVALID_RANGE = "Interpolation: minValue must be lower equal to maxValue";
	static final String INVALID_STEPS = "Interpolation steps must not be negative!";
	static final String NO_INSTANCE = "Do not instantiate class: ";
	
	Util() {
		throw new IllegalStateException(NO_INSTANCE + getClass().getCanonicalName());
	}
	
	/** Returns a newly created List of doubles, interpolating between given minimum and maximum
	 * 
	 * @param minValue minimum value, if steps &gt; 1 also included in the returned list
	 * @param maxValue maximum value, if steps &gt; 1 also included in the returned list, requires maxValue &gt; minValue
	 * @param steps integer number of interpolation steps [0..]
	 * @return a list of interpolated values; in case of
	 *         <ul>
	 *         <li>steps = 0: an empty list</li>
	 *         <li>steps = 1: average of minValue and maxValue</li>
	 *         <li>steps = 2: minValue and maxValue</li>
	 *         <li>steps &gt; 2: minValue, maxValue + (steps - 2) intermediate values, splitting the interval in (steps - 1)
	 *         equidistant segments</li>
	 *         </ul>
	 * @throws IllegalArgumentException if steps are negative or if minValue > maxValue */
	public static ArrayList<Double> linearInterpolation(double minValue, double maxValue, int steps) {
		ensureValidRange(minValue, maxValue);
		ArrayList<Double> interpolatedValues = new ArrayList<>();
		if (steps < 0) {
			throw new IllegalArgumentException(INVALID_STEPS);
		} else if (steps == 1) {
			double averageValue = (minValue + maxValue) / 2.;
			interpolatedValues.add(averageValue);
		} else if (steps > 1) {
			double currentValue = minValue;
			double stepWidth = (maxValue - minValue) / (steps - 1);
			for (int idx = 0; idx < steps; idx++) {
				interpolatedValues.add(currentValue);
				currentValue += stepWidth;
			}
		}
		return interpolatedValues;
	}

	/** Ensures that given minValue is smaller than or equal to given maxValue
	 * 
	 * @param minValue must be smaller than or equal to maxValue
	 * @param maxValue must be larger than or equal to minValue
	 * @throws IllegalArgumentException in case minValue &gt; maxValue */
	public static void ensureValidRange(double minValue, double maxValue) {
		if (minValue > maxValue) {
			throw new IllegalArgumentException(INVALID_RANGE);
		}
	}

	/** Calculates median of given data using {@link DescriptiveStatistics}
	 * 
	 * @param data to be analysed
	 * @return median of the given data or NaN if given data was null or empty */
	public static double calcMedian(double... data) {
		return (new DescriptiveStatistics(data)).getPercentile(50);
	}

	/** Searches given list of messages for the given type of DataItem. If at least one such message is found: Removes first message
	 * with that payload from given list and returns the payload. Further messages with that payload still reside in the message
	 * list. If no message with matching payload is found: returns null.
	 * 
	 * @param <T> type of searched and returned DataItem
	 * @param payload class type of DataItem to search for
	 * @param messages list of messages to search; message with found data item is removed
	 * @return first matching DataItem found */
	public static <T extends DataItem> T removeFirstMessageWithDataItem(Class<T> payload, List<Message> messages) {
		if (messages != null) {
			Iterator<Message> iterator = messages.iterator();
			while (iterator.hasNext()) {
				Message message = iterator.next();
				if (message.containsType(payload)) {
					iterator.remove();
					return message.getDataItemOfType(payload);
				}
			}
		}
		return null;
	}
}