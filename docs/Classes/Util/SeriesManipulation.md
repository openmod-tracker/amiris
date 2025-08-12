# In Short

`SeriesManipulation` is a utility class providing static methods to slice and pad time series around a pivot timestamp.
Missing values are extrapolated using the nearest available entry or a default value (`0.0` if no data exists).

# Details

`SeriesManipulation` enables consistent extraction of time series windows for modeling, forecasting, or machine learning preprocessing.
It can handle both `TimeSeries` and `TreeMap<Long, Double>` types and provides a static method `sliceWithPadding()` that supports:

- Backward slicing (before pivot) excluding the pivot
- Forward slicing (from pivot onward) including the pivot
- Extrapolation using:
  - `getValueEarlierEqual()` / `getValueLaterEqual()` for `TimeSeries`
  - `floorEntry()` / `ceilingEntry()` for `TreeMap`
- Use of `DEFAULT_VALUE = 0.0` if no values exist
- Protection against negative slice ranges
