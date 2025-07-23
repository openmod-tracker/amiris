# In short

Includes basic calculation functions for the current coefficient of performance and the electric heat pump power.

# Input

A dedicated ParameterTree is given that can be re-used in agents that feature heat pumps.

* `minElectricHeatPumpPowerInKW` Rated power at the design ambient temperature (typically -14 °C for air/water heat
  pumps) in kW. Derived from manufacturer data. In the model, the current power is interpolated based on the ambient
  temperature. For brine/water heat pumps, just set min and max values identically.
* `maxElectricHeatPumpPowerInKW` Rated power at the upper ambient temperature for heat pump specification (typically +10
  °C for air/water heat pumps) in kW. Derived from manufacturer data. In the model, the current power is interpolated
  based on the ambient temperature. For brine/water heat pumps, just set min and max values identically.
* `minCOP` Coefficient of performance at the design ambient temperature (typically -14 °C for air/water heat pumps).
  Derived from manufacturer data. In the model, the current COP is interpolated based on the ambient temperature.
* `maxCOP` Coefficient of performance at the upper ambient temperature for heat pump specification (typically +10 °C for
  air/water heat pumps). Derived from manufacturer data. In the model, the current COP is interpolated based on the
  ambient temperature.
* `heatPumpPenetration` Share of given heatDemandProfile that should be covered by heat pumps. Only used for strategist
  types [StrategistInflexibleFile](./StrategistInflexibleFile(HeatPump))
  and [StrategistMinCostFile](./StrategistMinCostFile(HeatPump)).
* `installedUnits` Total number of installed heat pumps. Only used for strategist
  types [StrategistInflexibleRC](./StrategistInflexibleRC(HeatPump))
  and [StrategistMinCostRC](./StrategistMinCostRC(HeatPump)).
