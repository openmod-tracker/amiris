package util;

import java.security.InvalidParameterException;
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
	/** Returns a newly created List of doubles, interpolating between given minimum and maximum
	 * 
	 * @param minValue minimum value, if steps > 1 also included in the returned list
	 * @param maxValue maximum value, if steps > 1 also included in the returned list, requires maxValue > minValue
	 * @param steps integer number of interpolation steps [0..]
	 * @return a list of interpolated values; in case of
	 *         <ul>
	 *         <li>steps = 0: an empty list</li>
	 *         <li>steps = 1: average of minValue and maxValue</li>
	 *         <li>steps = 2: minValue and maxValue</li>
	 *         <li>steps > 2: minValue, maxValue + (steps - 2) intermediate values, splitting the interval in (steps - 1)
	 *         equidistant segments</li>
	 *         </ul>
	 */
	public static ArrayList<Double> linearInterpolation(double minValue, double maxValue, int steps) {
		ensureValidRange(minValue, maxValue);
		ArrayList<Double> interpolatedValues = new ArrayList<>();
		if (steps < 0) {
			throw new IllegalArgumentException("Interpolation steps must not be negative!");
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

	/** throws an {@link InvalidParameterException} in case minValue > maxValue */
	public static void ensureValidRange(double minValue, double maxValue) {
		if (minValue > maxValue) {
			throw new InvalidParameterException("minValue has to be lower than or equal to maxValue");
		}
	}

	/** @return a {@link DescriptiveStatistics} filled with entries copied from the provided array */
	public static DescriptiveStatistics createStatisticsFrom(double[] array) {
		DescriptiveStatistics stat = new DescriptiveStatistics();
		for (int i = 0; i < array.length; i++) {
			stat.addValue(array[i]);
		}
		return stat;
	}

	/** Searches given list of messages for the given type of DataItem. If at least one such message is found: Removes first message
	 * with that payload from given list and returns the payload. Further messages with that payload still reside in the message
	 * list. If no message with matching payload is found: returns null.
	 * 
	 * @param <T> type of searched & returned DataItem
	 * @param payload class type of DataItem to search for
	 * @param messages list of messages to search; message with found data item is removed
	 * @return first matching DataItem found */
	public static <T extends DataItem> T removeFirstMessageWithDataItem(Class<T> payload, List<Message> messages) {
		Iterator<Message> iterator = messages.iterator();
		while (iterator.hasNext()) {
			Message message = iterator.next();
			if (message.containsType(payload)) {
				iterator.remove();
				return message.getDataItemOfType(payload);
			}
		}
		return null;
	}
}