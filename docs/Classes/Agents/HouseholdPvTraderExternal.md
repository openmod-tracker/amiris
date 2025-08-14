# In Short

`HouseholdPvTraderExternal` is a [FlexibilityTrader](./FlexibilityTrader.md) agent that manages electricity trading for clusters of households equipped with photovoltaic (PV) systems and battery storage. 
It delegates the calculation of how much energy is required or produced by the households to an external prediction model ([PvBiddingStrategist](../Modules/PvBiddingStrategist.md)).

# Details

## Strategy

The `HouseholdPvTraderExternal` uses [PvBiddingStrategist](../Modules/PvBiddingStrategist.md) to predict the electricity demand or production from its associated households.
To this end, `PvBiddingStrategist` is provided with electricity price forecasts, load, and PV generation.
Based on the demand or supply prediction `HouseholdPvTraderExternal` generates the corresponding bids/asks and sends them to the [DayAheadMarket](./DayAheadMarket.md).
Once the awards are provided by the `DayAheadMarket`, `HouseholdPvTraderExternal` updates the PV household status (stored energy).

## Schedule

- **Forecasts**: Requests electricity price forecasts from [Forecaster](./MarketForecaster.md)
- **Optimization**: Predict the optimized aggregated net load of households using the prediction model [PvBiddingStrategist](../Modules/PvBiddingStrategist.md)
- **Bidding**: Submits bids to the [DayAheadMarket](./DayAheadMarket.md) using ML-based net load predictions
- **Awards**: Updates status according to the received awards

## External prediction model

# Dependencies

* [PvBiddingStrategist](../Modules/PvBiddingStrategist.md)
* [Forecaster](./MarketForecaster.md)
* [DayAheadMarket](./DayAheadMarket.md)

Also see [FlexibilityTrader](./FlexibilityTrader.md)

# Input from file

* `InstalledGenerationPowerInMW`: PV generation capacity in MW
* `LoadInMW`: Time series of electricity load
* `GenerationProfile`: Time series of PV generation
* `ServiceUrl`: URL of the prediction service
* `ModelId` *(optional)*: Identifier of the ML model
* `ForecastPeriodInHours`: How far ahead the forecast should reach
* `PredictionWindows`: ML model input window parameters, see [PvBiddingStrategist](../Modules/PvBiddingStrategist.md)
* `Device`: Battery storage parameters, see [Device](../Modules/Device.md)
* `Policy`: Tariff policy parameters, see [EndUserTariff](../Modules/EndUserTariff.md)
* `BusinessModel`: Tariff business model, see [EndUserTariff](../Modules/EndUserTariff.md)

# Input from environment

See [FlexibilityTrader](./FlexibilityTrader.md)

# Simulation outputs

* `AwardedDemandInMWh`: Awarded demand-side energy
* `AwardedSupplyInMWh`: Awarded supply-side energy

Also see [FlexibilityTrader](./FlexibilityTrader.md) for inherited outputs

# Contracts

* MarketForecaster: Request & receive electricity price forecasts
* DayAheadMarket: Submit bids & receive awards

# Available Products

See [FlexibilityTrader](./FlexibilityTrader.md)

# Submodules

* [PvBiddingStrategist](../Modules/PvBiddingStrategist.md)
* [Device](../Modules/Device.md)

# Messages

* [PointInTime](../Comms/PointInTime.md): PriceForecastRequest to MarketForecaster
* [AmountAtTime](../Comms/AmountAtTime.md): Received price forecasts

See also [FlexibilityTrader](./FlexibilityTrader.md)

# See also

* [PvBiddingStrategist](../Modules/PvBiddingStrategist.md)
* [FlexibilityTrader](./FlexibilityTrader.md)
* [Forecaster](./MarketForecaster.md)
* [DayAheadMarket](./DayAheadMarket.md)
* [Device](../Modules/Device.md)
* [EndUserTariff](../Modules/EndUserTariff.md)