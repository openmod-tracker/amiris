// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package util;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.function.Supplier;
import de.dlr.gitlab.fame.time.TimeStamp;

public class TimedDataMap<U, V> {
	private final TreeMap<TimeStamp, HashMap<U, V>> dataPerTime;

	public TimedDataMap() {
		dataPerTime = new TreeMap<>();
	}

	public void set(TimeStamp time, U key, V value) {
		ensurePresent(time);
		dataPerTime.get(time).put(key, value);
	}

	private void ensurePresent(TimeStamp time) {
		if (!dataPerTime.containsKey(time)) {
			dataPerTime.put(time, new HashMap<>());
		}
	}

	public void clearBefore(TimeStamp time) {
		dataPerTime.headMap(time).clear();
	}

	public V get(TimeStamp time, U key) {
		if (!dataPerTime.containsKey(time)) {
			return null;
		}
		return dataPerTime.get(time).get(key);
	}

	public void computeIfAbsent(TimeStamp time, U key, Supplier<? extends V> mappingFunction) {
		ensurePresent(time);
		var data = dataPerTime.get(time);
		if (!data.containsKey(key)) {
			data.put(key, mappingFunction.get());
		}
	}
}
