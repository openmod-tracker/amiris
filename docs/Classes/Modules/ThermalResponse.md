# In short

The ThermalResponse model calculates the heat demand of a specific building type at given weather conditions in order to obtain a desired minimal room temperature inside the building.

# Details

Depending on the [HeatPumpStrategist](./HeatPumpStrategist.md) underlying the simulations, it is either used to simulate the heat demand ([StrategistInflexibleRC](./StrategistInflexibleRC(HeatPump).md) or to optimize the heat pump dispatch ([StrategistMinCostRC](./StrategistMinCostRC(HeatPump).md)) according to electricity prices.
In the latter case, flexibility is provided by activation of the structural thermal mass of a building.
Thus, the indoor room temperature is varied within given comfort limits in order to store or withdraw energy.
For the [StrategistInflexibleRC](./StrategistInflexibleRC(HeatPump).md), a hysteresis is implemented in order to avoid a continuous operation of heat pumps at low power.

Building thermodynamics are represented by lumped parameter models based on RC-networks in electric analogy.
Thereby, assumptions about thermodynamics are transformed into a network of lumped thermal resistances (R) and capacitances (C).
AMIRIS currently only uses the 1R1C model internally, meaning that only one R and C used are used in the system equation.
More complex RC models are underlying the GAMS model that is called via the [StrategistExternal](./StrategistExternal(HeatPump).md).

The parameters of the models have been identified through grey-box modeling.
Details on the method, validation as well as model parameters can be found in Sperber et al. (2020).

The differential equations found in this publication have been Euler discretised for the integration into AMIRIS.

# Inputs

* `Ria` Thermal resistance between the building interior and the ambient in °C/kW.
* `Ci` Capacitance of the interior of the building in kWh/°C.
* `Ai` Effective window area for absorption of solar gains on internal air in the building in m2.
* `heatingLimitTemperatureInC` Ambient temperature above which heating is turned off in °C.
* `internalHeatGainsInKW` Internal heat gains inside the building in kW.

# Literature

Sperber E., Frey U., Bertsch V.: Reduced-order models for assessing demand response with heat pumps - Insights from the
German energy system. Energy and Buildings 2020;223. https://doi.org10.1016/j.enbuild.2020.110144.