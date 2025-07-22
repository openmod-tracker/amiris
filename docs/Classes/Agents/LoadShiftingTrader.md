# 42 words

LoadShiftingTrader is a kind of [FlexibilityTrader](./FlexibilityTrader.md) that markets the load shifting capability of a [LoadShiftingPortfolio](../Modules/LoadShiftingPortfolio) at the [DayAheadMarket](./DayAheadMarket.md).
Different dispatch strategies can be used to fulfil different targets, e.g., to maximise profits, or to minimise system cost.
Depending on its strategy, it might require for forecasts of electricity prices or the merit order.

# Details

## Strategies

Different types of dispatch strategists are available for the LoadShiftingTrader:

* `DISPATCH_FILE`: Creates a [BidSchedule](../Modules/BidSchedule.md) following a given load shifting energy storage level *TimeSeries* as well as a given current shifting time *TimeSeries* read from input file.
* `SINGLE_AGENT_MIN_SYSTEM_COST`: Uses the load shifting portfolio in order to minimise the total system cost. Full information is used to consider marginal cost of the bids instead of the bidding price itself which maybe distorted with mark-ups and mark-downs.
* `SINGLE_AGENT_MAX_PROFIT`: Optimises the dispatch to maximise the profits of the LoadShiftingTrader. Considers any changes in price caused by its own dispatch.
* `SINGLE_AGENT_MAX_PROFIT_TARIFFS `: Optimises the dispatch to maximise the profits of the LoadShiftingTrader. Considers any changes in price caused by its own dispatch. Compared to the ShiftProfitMaximiser, it also considers changes incurring from additional consumer price elements, see [EndUserTariff](../Modules/EndUserTariff.md).
* `SINGLE_AGENT_MIN_CONSUMER_COST_EXTERNAL`: Optimises the dispatch to minimise the total power payment obligations of the customers in the load shifting portfolio. It calls an external optimisation micro-model via an [UrlModelService](../Util/UrlModelService.md) Thus, it also allows for changes in peak load compared to any other strategy which parts of network tariffs may be levied onto.

| StrategistType                                                                              | Used Forecast | Provide Forecast | Flex agent count | Robustness             | Consumer Tariffs Integration | Scheduling Model |
|---------------------------------------------------------------------------------------------|---------------|------------------|------------------|------------------------|------------------------------|------------------|
| [DISPATCH_FILE](../Modules/ShiftFileDispatcher.md)                                          | none          | not implemented  | multiple         | n/a                    | no                           | internal         |
| [SINGLE_AGENT_MIN_SYSTEM_COST](../Modules/ShiftSystemCostMinimiser.md)                      | merit order   | no               | single           | only perfect foresight | no                           | internal         |
| [SINGLE_AGENT_MAX_PROFIT](../Modules/ShiftProfitMaximiser.md)                               | merit order   | no               | single           | only perfect foresight | no                           | internal         |
| [SINGLE_AGENT_MAX_PROFIT_TARIFFS](../Modules/ShiftProfitMaximiserTariffs.md)                | merit order   | no               | single           | only perfect foresight | yes                          | internal         |
| [SINGLE_AGENT_MIN_CONSUMER_COST_EXTERNAL](../Modules/ShiftConsumerCostMinimiserExternal.md) | price         | no               | single           | only perfect foresight | yes                          | external         |

In general, the load shifting modelling is similar to the storages modeling, see: [StorageTrader](../Agents/StorageTrader.md).

* For the strategies `SINGLE_AGENT_MIN_SYSTEM_COST`, `SINGLE_AGENT_MAX_PROFIT` and `SINGLE_AGENT_MAX_PROFIT_TARIFFS`, a dynamic programming approach is used to find an optimal dispatch pattern. Hereby, the state is defined as a two-dimensional state consisting of a current shift time, i.e. the time that has been shifted for so far, as well as a current energy storage state. The latter is an integer value that is corresponding to a fictitious load shifting energy storage level which in turn is greater than 0 when loads are advanced compared to a baseline load pattern and smaller than 0 when loads are delayed.
  The current shift time has to be smaller than a maximum shift time which indicates the maximum duration for a shift. Some prolonging option is also allowed, see [LoadShiftStateManager](../Modules/LoadShiftStateManager.md).
  The load shifting process only considers deviations from a baseline demand. To keep track of the load shifting state, the [LoadShiftingStrategist](../Modules/LoadShiftingStrategist) chosen makes use of a [LoadShiftStateManager](../Modules/LoadShiftStateManager.md). Parameterization-wise, it must be ensured that the overall demand of the [DemandTrader](./DemandTrader.md) is decreased by the baseline demand of load shifting portfolio units.
* The `SINGLE_AGENT_MIN_CONSUMER_COST_EXTERNAL` calls an external optimization model written in python (pyomo) and thus given as a full algebraic description of the target function and its constraints that is in turn solved by an external solver (e.g. gurobi, CPLEX, GLPK or CBC). The model formulation itself relates to modelling a storage with additional inter-temporal constraints (i.e. balancing requirements). The full formulation is laid down in Kochems (2024), pp. 101-105 and 135-136 and based on Gils (2015), pp. 67-70.

# Dependencies

* [MarketForecaster](./MarketForecaster.md)

also see [FlexibilityTrader](./FlexibilityTrader.md)

# Input from file

* LoadShiftingPortfolio: Group, defined in [LoadShiftingPortfolio](../Modules/LoadShiftingPortfolio.md), technical details of the load shifting portfolio to administrate
* `Strategy`: Group
    * `StrategistType`: declares the Strategist to be used for Bid calculation. Possible types are `DISPATCH_FILE`, `SINGLE_AGENT_MIN_SYSTEM_COST`, `SINGLE_AGENT_MAX_PROFIT`, `SINGLE_AGENT_MAX_PROFIT_TARIFFS` and `SINGLE_AGENT_MIN_CONSUMER_COST_EXTERNAL` (see details above).
    * `ForecastPeriodInHours`: Must be smaller or equal to that of the MarketForecaster.
    * `ScheduleDurationInHours`: Number of hours each created schedule is viable. Must be smaller or equal to the `ForecastPeriodInHours`.
    * `SingleAgent`: Group, use with "SINGLE_AGENT" Strategists, e.g. `SINGLE_AGENT_MIN_SYSTEM_COST` and `SINGLE_AGENT_MAX_PROFIT` or `SINGLE_AGENT_MAX_PROFIT_TARIFFS`
        * `PurchaseLeviesAndTaxes`: Levies and taxes to be applied when purchasing energy &rarr; Note that this is a rather simplistic approach. If you are interested to study the effects of end user tariff designs, you should use the ShiftProfitMaximiserTariffs together with the specifications in the group `Policy`.
    * `FixedDispatch`: Group, use with `DISPATCH_FILE` Strategist
        * `EnergySchedule`: a TimeSeries that determines the upshifting / charging (positive values) or downshifting / discharging (negative values) activities of the connected LoadShiftingPortfolio, relative to the load shifting portfolios' power - i.e. "+1.0" resembles maximum upshift (charging) power, while "-1.0" resembles maximum downshift (discharging) power.
        * `ShiftTimeSchedule`: a TimeSeries defining the pattern of the current shift time to be applied with the given energy schedule. Make sure that both are consistent with each other and that neither the maximum shift time nor any energy or power bounds are violated. If this is the case, an Exception is raised. It is recommended to create a consistent pattern from a previous run where another strategy was applied.
* `Policy`: Group, defined in [EndUserTariff](../Modules/EndUserTariff.md), details of the consumer tariff structure to apply

# Input from environment

see [FlexibilityTrader](./FlexibilityTrader.md)

# Simulation outputs

* `OfferedUpshiftPowerInMW`: Power offered to be shifted in upwards direction (load increase) compared to baseline load consumption in MW.
* `OfferedDownshiftPowerInMW`: Power offered to be shifted in downwards direction (load reduction) compared to baseline load consumption in MW.
* `OfferedPriceInEURperMWH`: The offered bid price to sell / purchase power at in Euro per MWh.
* `AwardedUpshiftPowerInMW`: The power awarded to be shifted in upwards direction (load increase) compared to baseline load consumption in MW.
* `AwardedDownshiftPowerInMW`: The power awarded to be shifted in downwards direction (load reduction) compared to baseline load consumption in MW.
* `NetAwardedPowerInMW`: The net power awarded to be shifted compared to baseline load consumption in MW.
* `StoredMWH`: The (fictitious) load shifting energy storage level in MWh.
* `CurrentShiftTimeInH`: The number of consecutive hours for which the load shifting portfolio has already been shifted into one direction (upwards or downwards), i.e. for which the (fictitious) load shifting energy storage level is already unbalanced.
* `RevenuesInEUR`: The product of power price and awarded discharged power in Euro.
* `CostsInEUR`: The total costs for load shifting comprising costs for purchasing power in the course of an upshift (load increase) as well as variable costs for load shifting.
* `VariableShiftingCostsInEUR`: The variable shifting costs (from the awarded charged power + the awarded discharge power + the prolonging portfolio costs in Euro).
* `ProfitInEUR`: The revenues minus the costs in Euro.
* `VariableShiftingCostsFromOptimiserInEUR`: The variable shifting costs variable costs for load shifting from optimization micro-model, called for strategist `ShiftConsumerCostMinimiserExternal` in Euro.

# Contracts

* MarketForecaster: Send requests, get price forecasts, send bids forecasts
* DayAheadMarket: Send bids & asks, get awards

# Available Products

see [FlexibilityTrader](./FlexibilityTrader.md)

# Submodules

* [LoadShiftingPortfolio](../Modules/LoadShiftingPortfolio.md)
* [LoadShiftStateManager](../Modules/LoadShiftStateManager.md)
* [BidSchedule](../Modules/BidSchedule.md)
* [ShiftFileDispatcher](../Modules/ShiftFileDispatcher.md)
* [ShiftSystemCostMinimiser](../Modules/ShiftSystemCostMinimiser.md)
* [ShiftProfitMaximiser](../Modules/ShiftProfitMaximiser.md)
* [ShiftProfitMaximiserTariffs](../Modules/ShiftProfitMaximiserTariffs.md)
* [ShiftConsumerCostMinimiserExternal](../Modules/ShiftConsumerCostMinimiserExternal.md)
* [EndUserTariff](../Modules/EndUserTariff.md)

# Messages

* [BidsAtTime](../Comms/BidsAtTime.md)
* [PointInTime](../Comms/PointInTime.md): `MeritOrderForecastRequest` or `PriceForecastRequest` sent to MarketForecaster
* [MeritOrderMessage](../Comms/MeritOrderMessage.md): received `MeritOrderForecast` from MeritOrderForecaster
* [AmountAtTime](../Comms/AmountAtTime.md): received `PriceForecast` from PriceForecaster

# Literature

* Gils, Hans Christian (2015): Balancing of Intermittent Renewable Power Generation by Demand Response and Thermal Energy Storage. Dissertation. University of Stuttgart, Stuttgart. DOI: [10.18419/opus-6888](http://dx.doi.org/10.18419/opus-6888).
* Kochems, Johannes (2024): Lastmanagementpotenziale im deutschen Stromsystem. Einzelwirtschaftliche Bewertung gesamtwirtschaftlicher Potenzialschätzungen. Dissertation. TU Berlin, Berlin. DOI: [10.14279/depositonce-22008](https://doi.org/10.14279/depositonce-22008)