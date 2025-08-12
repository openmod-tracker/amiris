# 42 words

FuelsMarket determines market prices for power plant fuels.
These may be conventional fuels or others (e.g. Biogas). 
So far, FuelsMarket does not apply any market mechanisms to create fuel prices, but provides fuel price data from configured TimeSeries.

# Details

Fuel prices are required for the calculation of [Marginals](../Modules/Marginal.md) for [ConventionalPlantOperator](./ConventionalPlantOperator.md).
Technically, it could also provide biogas prices / biomass to renewable plants.
However, the [Biogas](./Biogas.md) agent does not rely on the FuelsMarket, yet.
FuelsMarket receives requests for each specific fuel cost at a specific time and forwards the corresponding prices (`FuelPrice`) to the
requesting party, e.g. a ConventionalPlantOperator.
FuelsMarket can also send a bill (`FuelBill`) to the contracted  power plants based on their fuel orders as reported in a `FuelBid` message.

# Dependencies

Any Agent that wants to trade at the FuelsMarket should implement the [FuelsTrader](../Abilities/FuelsTrader.md) ability. 

# Input from file

* `FuelPrices` List Group
    * `FuelType` Any of `NATURAL_GAS`, `NUCLEAR`, `LIGNITE`, `HARD_COAL`, `OIL`, `WASTE`, `HYDROGEN`, `BIOMASS`, or `OTHER`
    * `Price` TimeSeries for price of matching fuel type
    * `ConversionFactor` for the associated fuel price time series to *€/Thermal_MWh* (in case the fuel price is given
      in another unit). Example: if the fuel price is given in k€/Thermal_MWh, the conversion factor needs to be set to 1000.

# Input from environment

None

# Simulation outputs

None

# Contracts

* [FuelsTrader](../Abilities/FuelsTrader.md) send `FuelPriceForecastRequest` and receive `FuelPriceForecast`, respectively.
* [FuelsTrader](../Abilities/FuelsTrader.md) send `FuelPriceRequest` and receive `FuelPrice`, respectively.
* [FuelsTrader](../Abilities/FuelsTrader.md) send `FuelBid` and receive `FuelBill`, respectively.

# Available Products

* `FuelPriceForecast`: Forecast of the fuel price
* `FuelPrice`: actual fuel price
* `FuelBill`: sum of cost for purchased fuels

# Submodules

None

# Messages

* [FuelData](../Comms/FuelData.md): received `FuelPriceForecastRequest` &  `FuelPriceRequest`
* [FuelCosts](../Comms/FuelCost.md): sent `FuelPriceForecast` and `FuelPrice`
* [AmountAtTime](../Comms/AmountAtTime.md): sent `FuelsBill` & received `ConsumedFuel`

# See also

* [FuelsTrader](../Abilities/FuelsTrader.md)