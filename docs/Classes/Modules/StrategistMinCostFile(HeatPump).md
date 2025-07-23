# In short

Creates a cost-optimal [HeatPumpSchedule](./HeatPumpSchedule) from a given heat demand input time series. Flexibility is
provided by a thermal storage.

# Details

StrategistMinCostFile is a type of [HeatPumpStrategist](./HeatPumpStrategist). A single TimeSeries read from a file
contains the heat demand of any unit, e.g. the heat demand profile of all buildings in the market zone.
The share of the heating demand given in the input *heatPumpPenetrationFactor* is assumed to be covered by heat pumps.
The model uses a [PriceSensitivity](./PriceSensitivity) to find the optimal dispatch path that minimizes the electricity
cost for heat pump operation. Flexibility is provided by a hypothetical, aggregated thermal storage of
type [Device](./Device) that is designed to meet the aggregated demand of the given TimeSeries at any time.

## General strategy

All possible dispatches within the forecast period are evaluated using dynamic programming,
considering the price changes induced in the merit order when the heat pumps are dispatched.
For this, the StrategistMinCostFile needs "perfect foresight" of the merit order for all time periods
within the forecast period. Thus, StrategistMinCostFile is **incompatible** with other agents that modify the
merit order dynamically.

# See also

[Device](./Device)
