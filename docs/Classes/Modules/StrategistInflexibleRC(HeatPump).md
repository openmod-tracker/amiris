# In short

Creates an inflexible [HeatPumpSchedule](./HeatPumpSchedule.md) that satisfies a space heating demand that is endogenously calculated by a [ThermalResponse](./ThermalResponse.md) model.
Heat pump operation is only oriented at current space heat demand, not at prices.

# Details

StrategistInflexibleRC is a type of [HeatPumpStrategist](./HeatPumpStrategist.md).
A simple 1R1C [ThermalResponse](./ThermalResponse.md) model, which represents a **single, prototype building**, is used for calculating space heat demand.
The resulting schedule is inflexible, meaning that the temperature inside the building is kept constant.