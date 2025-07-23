# In short

Creates a cost-optimal [HeatPumpSchedule](./HeatPumpSchedule) that satisfies a space heating demand that is endogenously
calculated by a [ThermalResponse](./ThermalResponse) model.
Flexibility is provided by varying the indoor temperature of the building.

# Details

StrategistMinCostRC is a type of [HeatPumpStrategist](./HeatPumpStrategist).
A simple 1R1C [ThermalResponse](./ThermalResponse) model, which represents a **single, prototype building**, is used for
calculating space heat demand.
The model use a [PriceSensitivity](./PriceSensitivity) to find the optimal dispatch path that minimizes the electricity
cost for heat pump operation for space heating.

## General strategy

All possible dispatches within the forecast period are evaluated using dynamic programming,
considering the price changes induced in the merit order when the heat pumps are dispatched.
For this, the StrategistMinCostRC needs "perfect foresight" of the merit order for all time periods
within the forecast period. Thus, StrategistMinCostRC is **incompatible** with other agents that modify the
merit order dynamically.
