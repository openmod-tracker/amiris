# In short
Creates an inflexible [HeatPumpSchedule](./HeatPumpSchedule) from a given heat demand input time series that is independent of electricity prices.

# Details
StrategistInflexibleFile is a type of [HeatPumpStrategist](./HeatPumpStrategist).
It uses one TimeSeries read from file to calculate heat pump dispatch. 
This TimeSeries contains the heat demand of any unit, e.g. the heat demand profile of all buildings in the market zone.
The share of the heating demand given in the input *heatPumpPenetrationFactor* is assumed to be covered by heat pumps.
The model calculates the corresponding power demand of heat pumps by dividing the heating demand by a coefficient of performance.
