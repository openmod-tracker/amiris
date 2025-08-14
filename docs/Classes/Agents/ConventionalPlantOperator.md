# 42 words

The `ConventionalPlantOperator` is a type of [PowerPlantOperator](./PowerPlantOperator.md).
It holds a [Portfolio](../Modules/Portfolio.md) of conventional power plants.
As [FuelsTrader](../Abilities/FuelsTrader.md) it can trade fuels at a [FuelsMarket](./FuelsMarket.md).
`ConventionalPlantOperator`s send [MarginalsAtTime](../Comms/MarginalsAtTime.md) (and their forecasts) to an associated [ConventionalTrader](./ConventionalTrader.md).

# Details

The `ConventionalPlantOperator` receives a Portfolio of power plants from the [PlantBuildingManager](./PlantBuildingManager.md).
Based on this Portfolio and information about CO<sub>2</sub> prices and fuel costs, the `ConventionalPlantOperator` calculates actual and forecasted supply marginal costs.
These are sent to an associated Trader (organised by its super class [PowerPlantOperator](./PowerPlantOperator.md)).
Along with marginal costs, `ConventionalPlantOperator` can report a must-run power to its associated [ConventionalTrader](./ConventionalTrader.md).

Forecasting of marginals is triggered by connected Traders sending a `GateClosureForward`.

The `ConventionalPlantOperator` assigns its associated power plants to dispatch.
First, awarded power is used to serve must-run restrictions.
If awarded power is insufficient to cover must-run restrictions of all power plants, those with lower marginal costs are served first. 
Once must-run production is allocated: if awarded power remains, it is allocated to power plants in the order of increasing marginal costs.
If a power plant is assigned no power, it is shut down.

# Dependencies

* [PlantBuildingManager](./PlantBuildingManager.md)
* [CarbonMarket](./CarbonMarket.md)
* [FuelsMarket](./FuelsMarket.md)

# Input from file

None

# Input from environment

* `PowerPlantPortfolio` from PlantBuildingManager
* `GateClosureForward` from Trader
* `Co2PriceForecast` and `Co2Price` from CarbonMarket
* `FuelPriceForecast` and `FuelPrice` from FuelsMarket

# Simulation outputs

* `DispatchedEnergyInMWHperPlant` complex output; energy production in MWh per time interval and power plant
* `VariableCostsInEURperPlant` complex output; variable operation costs in EUR per time interval and power plant
* `ReceivedMoneyInEURperPlant` complex output; money assigned to each power plant in EUR per time interval
* `Co2EmissionsInT` total CO2 emissions for all power plants per time interval
* `FuelConsumptionInThermalMWH` total fuel consumption for all power plants per time interval

see [PowerPlantOperator](./PowerPlantOperator.md)

# Contracts

* Send ForecastRequest and to Co2PriceRequest `CarbonMarket` and receives Co2PriceForecast and Co2Price
* Send ForecastRequest and FuelPriceRequest to `FuelsMarket` and receives FuelPriceForecast and FuelPrice
* Receives GateClosureForward from `Trader`

see also [PowerPlantOperator](./PowerPlantOperator.md)

# Available Products

* `Co2Emissions`: total actual emissions produced during power generation
* `Co2PriceForecastRequest`: Requests the Co2 price forecast from the carbon market for a given time
* `FuelPriceForecastRequest`: Requests the fuel price forecast from the fuel market for a given fuel and time
* `FuelPriceRequest`: Requests the fuel price from the fuel market for a given fuel and time
* `Co2PriceRequest`: Request the Co2 price from the carbon market for a given time
* `ConsumedFuel`: Total actual fuel consumption

see also [PowerPlantOperator](./PowerPlantOperator.md)

# Submodules

None

# Messages

* [FuelData](../Comms/FuelData.md): sent `FuelPriceForecastRequest` and `FuelPriceRequest`
* [ClearingTimes](../Comms/ClearingTimes.md): sent `Co2PriceForecastRequest`, `Co2PriceRequest`, `FuelPriceRequest` and `FuelPriceForecastRequest`
* [AmountAtTime](../Comms/AmountAtTime.md): sent `Co2Emissions` and `ConsumedFuel`
* [Portfolio](../Modules/Portfolio.md): received `PowerPlantPortfolio`
* [MarginalsAtTime](../Comms/MarginalsAtTime.md): sent `MarginalCost` and `MarginalCostForecast`
* [FuelCost](../Comms/FuelCost.md): received `FuelPrice`
* [Co2Cost](../Comms/Co2Cost.md): received `Co2Price`

# See also

* [PowerPlantOperator](./PowerPlantOperator.md)
* [FuelsTrader](../Abilities/FuelsTrader.md)