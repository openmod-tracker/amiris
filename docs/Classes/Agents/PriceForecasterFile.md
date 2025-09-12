# 42 words

A [DamForecastProvider](../Abilities/DamForecastProvider.md) that provides electricity price forecasts to connected agents.
These price forecasts are predetermined before the simulation begins, therefore defined exogenous, making them static in nature.
For a dynamic forecasting of electricity prices, see [MarketForecaster](./MarketForecaster.md).

# Details

None

# Dependencies

None

# Input from file

* `PriceForecastsInEURperMWH`: timeseries of electricity price forecasts

# Input from environment

* `PriceForecastRequest` from Traders

# Simulation outputs

* `ElectricityPriceForecastInEURperMWH`: The forecasted value for the electricity price.

# Contracts

* Trader: send `PriceForecasts` as response to `PriceForecastRequest`s

# Available Products

* PriceForecast: Electricity price at requested hour as defined by input file

# Submodules

None

# Messages

* [AmountAtTime](../Comms/AmountAtTime.md) as PriceForecast

# See also

* [MarketForecaster](./MarketForecaster.md)
