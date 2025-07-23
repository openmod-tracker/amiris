# 42 words

HeatPumpTrader is a type of [FlexibilityTrader](./FlexibilityTrader.md) that aggregates heat pumps in buildings and arranges procurement at the [DayAheadMarket](./DayAheadMarket.md).
Different strategies can be used to calculate the electricity demand of heat pumps.
Heat pumps can be either dispatched according to heat demand or optimised according to spot market price forecasts.

# Details

## Strategies

Different dispatch Strategists are available for the HeatPumpTrader:

* [StrategistInflexibleRC](../Modules/StrategistInflexibleRC(HeatPump).md): Inflexible dispatch strategy; the space heating demand of a prototype building is calculated endogenously by a [ThermalResponse](../Modules/ThermalResponse.md) model. The indoor air temperature in the building is kept at a constant level.
* [StrategistInflexibleFile](../Modules/StrategistInflexibleFile(HeatPump).md): Inflexible dispatch strategy; the electricity demand schedule is calculated following a given *TimeSeries* read from input file.
* [StrategistMinCostRC](../Modules/StrategistMinCostRC(HeatPump).md): Flexible dispatch strategy; the space heating demand of a prototype building is calculated endogenously by a 1R1C [ThermalResponse](../Modules/ThermalResponse.md) model. Flexibility is provided by the variation of the indoor air temperature in the building.
* [StrategistMinCostFile](../Modules/StrategistMinCostFile(HeatPump).md): Flexible dispatch strategy; the space heating demand profile is imported from a file. Flexibility is provided by using an active hot water storage tank.
* [StrategistExternal](../Modules/StrategistExternal(HeatPump).md): Flexible dispatch strategy; calls an external GAMS-based heat pump dispatch optimization model. The GAMS model encapsulates more complex thermal response models, a more complex heat pump representation, includes generation of domestic hot water, and is able to optimize a large portfolio of decentral heat pumps in different building types simultaneously.

# Contracts

Contracts of the HeatPumpTrader depend on its dispatch strategy:

* `StrategistMinCostRC` and `StragistMinCostFile` require merit order forecasts,
* `StrategistExternal` requires price forecasts,
* `StrategistInflexibleRC` and `StragistInflexibleFile` do use any forecast since their dispatch does not depend on prices.

When merit-order forecasts are used, it must be ensured that **no other flexibility option agents** are active - otherwise, they will interfere and the merit-order forecasts will be spoiled (with the dispatch of the other flexibility agent(s)).
Once the forecasts are obtained, the HeatPumpTrader uses its [Strategist](../Modules/HeatPumpStrategist.md) to calculate a suitable [BidSchedule](../Modules/BidSchedule.md).
This schedule contains the heat pump's electricity demand over time.
Following the DispatchSchedule, the HeatPumpTrader places demand bids at the [DayAheadMarket](./DayAheadMarket.md).
The HeatPumpTrader bids at the maximum allowed bidding price in order to assure that the demand is always met.

# Dependencies

* [MarketForecaster](./MarketForecaster.md)

also see [FlexibilityTrader](./FlexibilityTrader.md)

# Input from file

* `Device`: Group, defined in [Device](../Modules/Device.md). Watch out that parametrization must be for a thermal system, not an electric one.
* `HeatPump`: Group, defined in [HeatPump](../Modules/HeatPump.md)
* `Strategy`: Group, defined in [StrategyParameters](../Modules/StrategyParameters(HeatPump).md)
* `StrategyBasic`: Group, defined in [HeatPumpStrategist](../Modules/HeatPumpStrategist.md)
* `Building`: Group, defined in [BuildingParameters](../Modules/BuildingParameters.md)
* `HeatingInputData`: Group, defined in [HeatingInputData](../Modules/HeatingInputData(HeatPump).md)
* `Policy`: Group, defined in [EndUserTariff](../Modules/EndUserTariff.md)
* `BusinessModel`: Group, defined in [EndUserTariff](../Modules/EndUserTariff.md)

Note that not all inputs are used for each Strategist type.
The table below shows which inputs are actually used per Strategist type.
However, you must parameterize all inputs since all Strategist instantiate them, even if unused.

| Input Parameter Group        | InflexibleFile | MinCostFile | InflexibleRC | MinCostRC | External |
|------------------------------|----------------|-------------|--------------|-----------|----------|
| **Heatpump**                 |                |             |              |           |          |
| minElectricHeatPumpPowerInKW | +              | +           | +            | +         | -        |
| maxElectricHeatPumpPowerInKW | +              | +           | +            | +         | -        |
| minCOP                       | +              | +           | +            | +         | -        |
| maxCOP                       | +              | +           | +            | +         | -        |
| heatPumpPenetrationFactor    | +              | +           | -            | -         | -        |
| installedUnits               | -              | -           | +            | +         | -        |
| **HeatingInputData**         |                |             |              |           |          |
| temperatureProfile           | +              | +           | +            | +         | -        |
| solarRadiation               | -              | -           | +            | +         | -        |
| pvProfile                    | -              | -           | +            | +         | -        |
| heatDemandProfile            | +              | +           | -            | -         | -        |
| **Strategy**                 |                |             |              |           |          |
| modelledChargingSteps        | -              | +           | -            | +         | -        |
| scheduleDurationInHours      | +              | +           | +            | +         | +        |
| heatPumpStrategistType       | +              | +           | +            | +         | +        |
| minimalRoomTemperature       | -              | -           | +            | +         | -        |
| maximalRoomTemperature       | -              | -           | +            | +         | -        |
| meanRoomTemperature          | -              | -           | -            | -         | -        |
| ApiParameters                | -              | -           | -            | -         | +        |
| **Building**                 | -              | -           | +            | +         | -        |
| **Device**                   | -              | +           | -            | -         | -        |
| **Policy**                   | -              | -           | -            | -         | +        |
| **BusinessModel**            | -              | -           | -            | -         | +        |

# Input from environment

`MeritOrderForecast`s or `PriceForecast`s from MeritOrderForecaster or PriceForecaster, respectively

# Simulation outputs

Outputs depend on the Strategist type. Available outputs are:

* `AwardedEnergyInMWH`: The awarded energy in MWh
* `COP`: The coefficient of performance, i.e. efficiency of the heat pump
* `FinalRoomTemperatureInCelsius`: The final room temperature in Celsius
* `StoredEnergyInMWH`: The stored heat in MWh

also see [FlexibilityTrader](./FlexibilityTrader.md)

# Available Products

see [FlexibilityTrader](./FlexibilityTrader.md)

# Submodules

* [HeatPumpSchedule](../Modules/HeatPumpSchedule.md)
* [ThermalResponse](../Modules/ThermalResponse.md)
* [HeatPump](../Modules/HeatPump.md)
* [StrategyParameters](../Modules/StrategyParameters(HeatPump).md)
* [BuildingParameters](../Modules/BuildingParameters.md)
* [HeatingInputData](../Modules/HeatingInputData(HeatPump).md)
* [HeatPumpStrategist](../Modules/HeatPumpStrategist.md)
* [EndUserTariff](../Modules/EndUserTariff.md)

# Messages

* [PointInTime](../Comms/PointInTime.md): `MeritOrderForecastRequest` or `PriceForecastRequest` sent to MarketForecaster
* [MeritOrderMessage](../Comms/MeritOrderMessage.md): received `MeritOrderForecast` from MeritOrderForecaster
* [AmountAtTime](../Comms/AmountAtTime.md): received `PriceForecast` from PriceForecaster

* see [FlexibilityTrader](./FlexibilityTrader.md)