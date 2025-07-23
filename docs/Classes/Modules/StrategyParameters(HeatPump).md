# In Short
StrategyParameters encapsulate strategy-related input parameters for the [HeatPumpTrader](../Agents/HeatPumpTrader).

# Inputs
StrategyParameters define the strategy input parameters in a ParameterTree. Required parameters are:
* `modelledChargingSteps` Steps in the dynamic programming model (only applies for strategist [StrategistMinCostFile](./StrategistMinCostFile(HeatPump)) and [StrategistMinCostRC](./StrategistMinCostRC(HeatPump)).
* `heatPumpStrategistType` One of the following [Strategist](./HeatPumpStrategist) types: MIN_COST_RC, INFLEXIBLE_RC, MIN_COST_FILE, INFLEXIBLE_FILE, EXTERNAL.
* `minimalRoomTemperatureInC` Minimum accepted indoor air temperature during the heating period in Celsius. Not applicable for strategists [StrategistMinCostFile](./StrategistMinCostFile(HeatPump)) and [StrategistInflexibleFile](./StrategistInflexibleFile(HeatPump)).
* `maximalRoomTemperatureInC` Maximum accepted indoor air temperature during the heating period in Celsius.
* `meanRoomTemperatureInC` Mean accepted indoor air temperature during the heating period in Celsius.
* `ApiParameters`: Only applicable for Strategist [StrategistExternal](./StrategistExternal(HeatPump))
  * `ServiceUrl`: Url for API
  * `StaticParameterFolder`: Folder that encapsulates all input data required for the external GAMS heat pump dispatch optimization model.

