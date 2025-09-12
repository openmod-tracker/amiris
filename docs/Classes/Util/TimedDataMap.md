# In Short

`TimedDataMap` is a container for data that is associated with a TimeStamp but also mapped to another key, e.g., a client ID.

# Details

`TimedDataMap` is a TreeMap linking different TimeStamps to HashMaps of user-defined type.
It deals offers convenience methods like:
 
* `set(time, key, value)`: create a time entry to the TreeMap if it not yet exists; then add the key-value pair to inner HashMap
* `clearBefore(time)`: remove all mappings before the given time
* `get(time, key)`: return the value for the given time, or null if not entry for time or key is defined.
* `computeIfAbsent(time, key, function)`: calculate and set the value for the specified key and time - only if no value is yet present
* `getValuesOf(key)`: return all values linked to the given key in ascending order of their time
* `getDataAt(time)`: return the inner HashMap associated with the given time, or an empty map if no data is associated
* `getValuesBefore(time, key)`: return all values associated with the key and times before the given time
* `getKeysBefore(time)`: return all keys defined at times before the given time
