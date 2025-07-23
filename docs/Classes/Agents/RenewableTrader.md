# 42 words

The RenewableTrader is an [AggregatorTrader](./AggregatorTrader.md) which markets plants eligible for support other than `FIT`.
It trades energy of associated power plants at the [DayAheadMarket](./DayAheadMarket.md).
It is a kind of direct marketer which markets generation that is either under a market premium scheme (`MPVAR`, `MPFIX`), a capacity premium scheme (`CP`), 
a two-sided contracts for differences scheme (`CFD`) or a financial contracts for differences scheme (`FINANCIAL_CFD`).

# Details

The RenewableTrader creates [Bids](../Comms/BidsAtTime.md) based on the power production potential contained in [MarginalsAtTime](../Comms/MarginalsAtTime.md) data from linked PowerPlantOperators.
The bid price depends on the support instrument and its connected [BiddingStrategy](../Modules/BiddingStrategy.md). 
The RenewableTrader passes the received support premium fully to its clients.
The received market revenues may be split whereby the RenewableTrader may keep a fixed share of the market revenues.

# Dependencies

see [AggregatorTrader](./AggregatorTrader.md)

# Input from file

* `ShareOfRevenues`: The share of the market revenues, the RenewableTrader may keep (double $`\in [0,1]`$)
* `MarketValueForecastMethod`: Choose either `PREVIOUS_MONTH` and `FROM_FILE` to forecast market values, latter requires also `MarketValueForecasts` to be defined.
* `MarketValueForecasts`: A list of subgroups, each containing a market value forecast timeseries and an associated energy carrier.
  * `EnergyCarrier`: Energy carrier as defined in [SupportPolicy](./SupportPolicy.md)
  * `Forecast`: Forecast timeseries of market value for an associated energy carrier

also see [AggregatorTrader](./AggregatorTrader.md)

# Input from environment

see [AggregatorTrader](./AggregatorTrader.md)

# Simulation outputs

see [AggregatorTrader](./AggregatorTrader.md)

# Contracts

see [AggregatorTrader](./AggregatorTrader.md)

# Available Products

see [AggregatorTrader](./AggregatorTrader.md)

# Submodules

* various [BiddingStrategies](../Modules/BiddingStrategy.md)

see also [AggregatorTrader](./AggregatorTrader.md)

# Messages

see [AggregatorTrader](./AggregatorTrader.md)