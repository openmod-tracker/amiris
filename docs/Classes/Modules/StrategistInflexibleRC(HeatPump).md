# In short

Creates an inflexible [HeatPumpSchedule](./HeatPumpSchedule) that satisfies a space heating demand that is endogenously
calculated by a [ThermalResponse](./ThermalResponse) model.
Heat pump operation is only oriented at current space heat demand, not at prices.

# Details

StrategistInflexibleRC is a type of [HeatPumpStrategist](./HeatPumpStrategist).
A simple 1R1C [ThermalResponse](./ThermalResponse) model, which represents a **single, prototype building**, is used for
calculating space heat demand.
The resulting schedule is inflexible, meaning that the temperature inside the building is kept constant. 