# In Short

Creates a cost-optimal [HeatPumpSchedule](./HeatPumpSchedule.md) according to real-time prices, which is endogenously calculated by a heat pump dispatch model.

# Details

StrategistExternal is a type of [HeatPumpStrategist](./HeatPumpStrategist.md).
It uses merit order sensitivities of type [PriceNoSensitivity](./PriceNoSensitivity.md) to find the optimal dispatch path that minimizes the cost of heat pump operation.
End user electricity price components are considered and included in the dispatch optimization.
The StrategistExternal uses price information from an [EndUserTariff](./EndUserTariff.md) for the dispatch optimization.

The necessary AMIRIS extension that connects the StrategistExternal to the heat pump dispatch optimization model is available at https://gitlab.com/dlr-ve/esy/amiris/extensions/heatpump.

## General strategy

The dispatch strategy is decided by calling an external optimization model making use of [UrlModelService](../Util/UrlModelService.md).
A bottom-up model in GAMS is used to minimize the total cost of electricity for residential heat pumps used for space heating and domestic hot water generation under dynamic electricity prices.
Using real time prices, weather data, domestic hot water tapping profiles and building and heat pump parameters as inputs, the model calculates the aggregated cost-optimal grid electricity consumption schedule for typical single-family houses in Germany, in a quarter-hourly resolution.
The GAMS model allows for the combination of active and passive thermal storage, considers the thermal comfort of users, and the utilization of local PV generation.
To calculate the space heating demand and corresponding indoor air temperatures of buildings within the GAMS model, a set of validated bottom-up reduced-order models of building thermodynamics are used from *Sperber et al., 2020*.
Input parameters for the GAMS model are given in the StaticParameterFolder as defined in [StrategyParameters](./StrategyParameters(HeatPump).md).
Dynamic data is transferred to the GAMS model via [OptimisationInputs](./OptimisationInputs(HeatPump).md) messages, while its outputs are returned in form of [OptimisationOutputs](./OptimisationOutputs(HeatPump).md) messages.

All details on the GAMS model can be found in *Sperber et al. 2025*.

# Literature

* Sperber E., Frey U., Bertsch V.: Reduced-order models for assessing demand response with heat pumps - Insights from
  the German energy system; Energy and Buildings 2020; 223. https://doi.org10.1016/j.enbuild.2020.110144
* Sperber E., Schimeczek C., Cao K.K., Frey U., Bertsch V.: Aligning heat pump operation with market signals: A win-win
  scenario for users and the electricity market? Energy Reports, vol 13, 2025; https://doi.org/10.1016/j.egyr.2024.12.028