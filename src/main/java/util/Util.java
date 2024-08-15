// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import communications.message.PointInTime;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.time.TimeStamp;

/** Collection of general static methods used across packages
 * 
 * @author Christoph Schimeczek, Martin Klein, Marc Deissenroth */
public final class Util {
	static final String INVALID_RANGE = "Interpolation: minValue must be lower equal to maxValue";
	static final String INVALID_STEPS = "Interpolation steps must not be negative!";
	static final String NO_INSTANCE = "Do not instantiate class: ";
	static final String COUNT_MISMATCH = "No equal number of entries for in list of messages for types %s %s";
	static final String TIME_DUPLICATE = "More than one message of type %s found for same time %s";
	static final String TIME_UNMATCHED = "Time %s in messages of type %s could not be matched to times in messages of type %s";

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
	 * @throws IllegalArgumentException if steps are negative or if minValue &gt; maxValue */
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

	public static <T extends PointInTime, U extends PointInTime> HashMap<TimeStamp, MessagePair<T, U>> matchMessagesByTime(
			ArrayList<Message> messages, Class<T> firstType, Class<U> otherType) {
		List<Message> messagesOfFirstType = CommUtils.extractMessagesWith(messages, firstType);
		List<Message> messagesOfOtherType = CommUtils.extractMessagesWith(messages, otherType);
		if (messagesOfFirstType.size() != messagesOfOtherType.size()) {
			throw new RuntimeException(String.format(COUNT_MISMATCH, firstType, otherType));
		}
		HashMap<TimeStamp, MessagePair<T, U>> result = new HashMap<>(messages.size() / 2);
		for (var message : messagesOfFirstType) {
			T dataItem = message.getDataItemOfType(firstType);
			if (result.containsKey(dataItem.validAt)) {
				throw new RuntimeException(String.format(TIME_DUPLICATE, firstType, dataItem.validAt));
			}
			result.put(dataItem.validAt, new MessagePair<T, U>(dataItem));
		}
		for (var message : messagesOfOtherType) {
			U dataItem = message.getDataItemOfType(otherType);
			if (!result.containsKey(dataItem.validAt)) {
				throw new RuntimeException(String.format(TIME_UNMATCHED, dataItem.validAt, otherType, firstType));
			}
			result.get(dataItem.validAt).setOtherItem(dataItem);
		}
		return result;
	}

	public static class MessagePair<T extends PointInTime, U extends PointInTime> {
		private T itemOne;
		private U otherItem;

		private MessagePair(T itemOne) {
			this.itemOne = itemOne;
		}

		private void setOtherItem(U otherItem) {
			this.otherItem = otherItem;
		}

		public T getItemOne() {
			return itemOne;
		}

		public U getOtherItem() {
			return otherItem;
		}
	}
}