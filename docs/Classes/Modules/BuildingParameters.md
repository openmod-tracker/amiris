# In Short

Parameters for Buildings used e.g. by the [HeatPumpTrader](../Agents/HeatPumpTrader).

# Input

BuildingParameters defines the input parameters to parameterise a building. The corresponding ParameterTree can be used
in Agents that require a building to add the following Parameters to their set of required input data:

* `Ria`, `Ci`, `Ai` factors characterizing the thermodynamics of a specific building type. Can be looked up
  here: [(https://www.sciencedirect.com/science/article/abs/pii/S037877881933378X)].
  See [ThermalResponse](./ThermalResponse) for details.
* `heatingLimitTemperatureInC` The temperature above which buildings are not heated. Usually amounts to 10 - 15 °C,
  depending on the buildings standard
* `internalHeatGainsInKW` Internal heat gains in buildings (e.g. due to humans or electronic devices)