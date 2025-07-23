# 42 words

StorageTrader is a kind of [FlexibilityTrader](./FlexibilityTrader.md) that uses a storage device to trade electricity at the energy exchange.
Different dispatch strategies can be used to fulfil different targets, e.g. to maximise profits or to minimise system cost.
Depending on its strategy, it might require for (or contribute to) electricity price or merit order forecasts.

# Details

## Strategies

Different operation Strategists are available for the StorageTrader:
* `FileDispatcher`: Creates a [BidSchedule](../Modules/BidSchedule.md) following a given *TimeSeries* read from input file. It can provide forecasts regarding its behaviour.
* `SystemCostMinimiser`: Uses the storage device in order to minimise the total system cost. Full information is used to consider marginal cost of the bids instead of the bidding price itself which maybe distorted with mark-ups and mark-downs.
* `ProfitMaximiser`: Optimises the dispatch to maximise the profits of the StorageTrader. **Considers** any changes in price caused by its own dispatch.
* `ProfitMaximiserPriceTaker`: Optimises the dispatch to maximise the profits of the StorageTrader. **Ignores** any impact on prices caused by its own dispatch.
* `MultiAgentMedian`: Uses the median of the forecasted price to estimate a good dispatch strategy. This strategy is robust and can be used in combination with other flexibility options (e.g. multiple storage agents).

| Strategist                 | Used Forecast | Provide Forecast  | Flex agent count | Robustness                   |
|----------------------------|---------------|-------------------|------------------|------------------------------|
| FileDispatcher             | none          | yes               | multiple         | n/a                          |
| SystemCostMinimiser        | merit order   | no                | single           | only perfect foresight       |
| ProfitMaximiser            | merit order   | no                | single           | only perfect foresight       |
| ProfitMaximiserPriceTaker  | price         | no                | multiple (small) | only perfect foresight       |
| MultiAgentMedian           | price         | no                | multiple         | erroneous foresight possible |

## Contracts

Contracts of the StorageTrader depend on its dispatch strategy: 
* `SystemCostMinimiser` and `ProfitMaximiser` require merit order forecasts
* `MultiAgentMedian` and `ProfitMaximiserPriceTaker` require price forecasts
* `FileDispatcher` provides bid forecasts 

Except strategy `FileDispatcher`, the StorageTrader requests forecasts from the [MarketForecaster](./MarketForecaster.md).
Depending on the employed [Strategist](../Modules/ArbitrageStrategist.md), these can be either forecasts for the electricity price (maybe even with errors), or perfect foresight forecasts for the whole merit order. 
When merit-order forecasts are used, it must be ensured that **no other flexibility option agents** are active - otherwise, they will interfere and the merit-order forecasts will be spoiled (with the dispatch of the other flexibility agent(s)). 
With strategy `FileDispatcher`, the StorageTrader provides a BidsForecast to the MarketForecaster, instead.

Once the forecasts are obtained, the StorageTrader uses its [Strategists](../Modules/ArbitrageStrategist.md) to calculate a suitable [BidSchedule](../Modules/BidSchedule.md).
The [BidSchedule](../Modules/BidSchedule.md) can be of any duration between 1 and the foresight period.
It will be based upon the specifications of the attached [Device](../Modules/Device.md). 
Following the DispatchSchedule, the StorageTrader places supply and demand bids at the [DayAheadMarket](./DayAheadMarket.md).

# Dependencies

* [MarketForecaster](./MarketForecaster.md)

also see [FlexibilityTrader](./FlexibilityTrader.md)

# Input from file

* `Device`: Group, defined in [Device](../Modules/Device.md), technical details of the storage device to administrate
* `Strategy`: Group
  * `StrategistType`: declares the Strategist to be used for Bid calculation.
    Possible types are `DISPATCH_FILE`, `SINGLE_AGENT_MIN_SYSTEM_COST`, `SINGLE_AGENT_MAX_PROFIT` and `MULTI_AGENT_MEDIAN` 
  * `ForecastPeriodInHours`: Must be smaller or equal to that of the MarketForecaster; recommendation to be at least twice the E2P ratio of the [Device](../Modules/Device.md).
  * `ScheduleDurationInHours`: Number of hours each created schedule is viable. Must be smaller or equal to the `ForecastPeriodInHours`.
  * `ForecastUpdateType`: Defines the mode which should be applied to request electricity price forecasts. Either `ALL` for all time steps, discarding previously received electricity price forecasts, or `INCREMENTAL` for missing time steps only.
  * `SingleAgent`: Group, use with "SINGLE_AGENT_" Strategists, e.g. SystemCostMinimiser, ProfitMaximiser and ProfitMaximiserPriceTaker
    * `ModelledChargingSteps`: number of charging steps (per E2P) that is used to discretize the storage's state of charge (SOC)
  * `MultiAgent`: Group, use with "MULTI_AGENT_" Strategists, e.g. MultiAgentMedian
    * `AssessmentFunctionPrefactors`: List of doubles associated with the assessment polynomial with rising order (e.g. 4.0, 2.0, 3.0 correspond to $`4 + 2 \cdot x, 3 \cdot x^2`$. See [MultiAgentMedian](../Modules/MultiAgentMedian(Storage).md) for an explanation of the assessment function).
  * `FixedDispatch`: Group, use with FileDispatcher Strategist
    * `Schedule`: a TimeSeries that determines the charging (positive values) or discharging (negative values) activities of the connected device, relative to the device's power - i.e. "+1.0" resembles maximum charging power, while "-1.0" resembles maximum discharging power.
    * `DispatchTolerance`: Accepted tolerance for dispatch deviations in MWh from given storage schedule. If storage schedule violates this tolerance, a warning is raised.

# Input from environment

see [FlexibilityTrader](./FlexibilityTrader.md)

# Simulation outputs

* `OfferedChargePriceInEURperMWH`: Offered price for charging in EUR per MWh
* `OfferedDischargePriceInEURperMWH`: Offered price for discharging in EUR per MWh
* `AwardedChargeEnergyInMWH`: Total amount of charge energy awarded in MWh
* `AwardedDischargeEnergyInMWH`: Total amount of discharge energy awarded in MWh
* `StoredEnergyInMWH`: Amount of energy in storage in MWh after executing (dis-)charging actions in the current time step

see also [FlexibilityTrader](./FlexibilityTrader.md)

# Contracts

* MarketForecaster: Send requests, get price forecasts, send bids forecasts
* DayAheadMarket: Send bids & asks, get awards

# Available Products

see [FlexibilityTrader](./FlexibilityTrader.md)

# Submodules

* [Device](../Modules/Device.md)
* [BidSchedule](../Modules/BidSchedule.md)
* [ArbitrageStrategist](../Modules/ArbitrageStrategist.md)

# Messages

* [PointInTime](../Comms/PointInTime.md): `MeritOrderForecastRequest` or `PriceForecastRequest` sent to MarketForecaster
* [MeritOrderMessage](../Comms/MeritOrderMessage.md): received `MeritOrderForecast` from MeritOrderForecaster
* [AmountAtTime](../Comms/AmountAtTime.md): received `PriceForecast` from PriceForecaster

see [FlexibilityTrader](./FlexibilityTrader.md)

# See also

* [FlexibilityTrader](./FlexibilityTrader.md)
* [ElectrolysisTrader](./ElectrolysisTrader.md)
* [Strategists](../Modules/ArbitrageStrategist.md)