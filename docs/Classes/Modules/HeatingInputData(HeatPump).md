# In short

Time series data required for the calculation of electricity demand of heat pumps.

# Input

* `temperatureProfile` Time series of ambient temperature at a specific location in °C.
* `solarRadiation` Time series of total radiation on a vertical southern surface at a specific location in kW/m2.
  Required for strategist types [StrategistInflexibleRC](./StrategistInflexibleRC(HeatPump))
  and [StrategistMinCostRC](./StrategistMinCostRC(HeatPump)) only.
* `heatDemandProfile` Time series of heat demand at an aggregate level (e.g. Germany) in MWh/h. The share of the heating
  demand given in the input *heatPumpPenetrationFactor* (see [HeatPump](./HeatPump)) is assumed to be covered by heat
  pumps. Required for strategist types [StrategistInflexibleFile](./StrategistInflexibleFile(HeatPump))
  and [StrategistMinCostFile](./StrategistMinCostFile(HeatPump)) only.
* `pvProfile` Time series of pv yield at a specific location in kW/kWp.
