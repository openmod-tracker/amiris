# In Short

`EvTraderExternal` is a [FlexibilityTrader](./FlexibilityTrader.md) that manages energy trading for electric vehicle (EV) fleets at the energy market [DayAheadMarket](./DayAheadMarket.md).
It delegates the calculation of how much energy is required to an external prediction model [EvBiddingStrategist](../Modules/EvBiddingStrategist.md).

# Details

## Strategy

The `EvTraderExternal` uses for its bidding a prediction-based strategy [EvBiddingStrategist](../Modules/EvBiddingStrategist.md).
It:

- Predicts the optimized aggregated load for EVs using electricity price forecasts, load, charging power
- Generates the corresponding bids/asks and sends them to the [DayAheadMarket](./DayAheadMarket.md)
- Update the EVs status (charging power)

## Schedule

- **Forecasts**: Requests electricity price forecasts from [Forecaster](./MarketForecaster.md)
- **Optimization**: Predict the optimized aggregated net load of EVs using the prediction model [EvBiddingStrategist](../Modules/EvBiddingStrategist.md)
- **Bidding**: Generates and submits bids to the [DayAheadMarket](./DayAheadMarket.md)
- **Awards**: Updates status according to the received awards

## External prediction model

# Dependencies

* [MarketForecaster](./MarketForecaster.md)
* [DayAheadMarket](./DayAheadMarket.md)
* [EvBiddingStrategist](../Modules/EvBiddingStrategist.md)

See also [FlexibilityTrader](./FlexibilityTrader.md)

# Input from file

* `ServiceUrl`: String - Endpoint for external ML prediction service
* `ModelId`: String (optional) - Identifier for ML model version
* `ForecastPeriodInHours`: Integer - Prediction horizon
* `AggregatedAvailableChargingPowerInMW`: TimeSeries - Fleet's available charging power
* `AggregatedElectricConsumptionInMWH`: TimeSeries - Fleet's baseline consumption
* `PredictionWindows`: Group, see [EvBiddingStrategist](../Modules/EvBiddingStrategist.md)

# Input from environment

See [FlexibilityTrader](./FlexibilityTrader.md)

# Simulation outputs

* `OfferedChargePriceInEURperMWH`: Price for offered charging capacity
* `OfferedDischargePriceInEURperMWH`: Price for offered discharging capacity
* `AwardedChargeEnergyInMWH`: Cleared charging energy
* `AwardedDischargeEnergyInMWH`: Cleared discharging energy
* `StoredEnergyInMWH`: Virtual "storage" representing fleet's net stored energy

Also see [FlexibilityTrader](./FlexibilityTrader.md)

# Contracts

* MarketForecaster: Request & receive electricity price forecasts
* DayAheadMarket: Submit bids & receive awards

# Available Products

See [FlexibilityTrader](./FlexibilityTrader.md)

# Submodules

* [EvBiddingStrategist](../Modules/EvBiddingStrategist.md)

# Messages

* [PointInTime](../Comms/PointInTime.md): PriceForecastRequest to `MarketForecaster`
* [MeritOrderMessage](../Comms/AmountAtTime.md): Received price forecasts
* [BidsAtTime](../Comms/BidsAtTime.md): Submitted bids to `DayAheadMarket`
* [AwardData](../Comms/AwardData.md): Cleared market results

See [FlexibilityTrader](./FlexibilityTrader.md)

# See also

* [EvBiddingStrategist](../Modules/EvBiddingStrategist.md)
* [FlexibilityTrader](./FlexibilityTrader.md)
* [DayAheadMarket](./DayAheadMarket.md)
