# 42 words

The TraderWithClients is parent to Trader Agents trading at the [DayAheadMarket](./DayAheadMarket.md) and requiring clients in order to prepare their bids.
TraderWithClients are, e.g., [AggregatorTrader](./AggregatorTrader.md) or [ConventionalTrader](./ConventionalTrader.md).

# Details

As abstract base class, TraderWithClients cannot be instantiated.
Instead, it provides relevant Products and methods to deal with its clients.
Bids creation is based upon more or less complex strategies which might depend on marketing channels, remuneration policies, taxation, etc.

# Dependencies

* [PlantOperators](./PowerPlantOperator.md)

see also [Trader](./Trader.md)

# Input from file
None in general - input parameters are specific to each subclass.

see also [Trader](./Trader.md)

# Input from environment

Trader forwards GateClosureInfo (from DayAheadMarket) and ForecastRequest (from MarketForecaster) to connected clients.

see also [Trader](./Trader.md)

# Simulation outputs

see [Trader](./Trader.md)

# Contracts

* [MarketForecaster](./MarketForecaster.md) sends `ForecastRequest`
* [DayAheadMarket](./DayAheadMarket.md) sends `GateClosureInfo`
* [PlantOperators](./PowerPlantOperator.md) receives `GateClosureForward` and `ForecastRequestForward`

see also [Trader](./Trader.md)

# Available Products

* `GateClosureForward`: Forwarded bidding times request from DayAheadMarket
* `ForecastRequestForward`: Forwarded forecasting times request from MarketForecaster

see also
* [PowerPlantScheduler](../Abilities/PowerPlantScheduler.md)
* [Trader](./Trader.md)

# Submodules

None

# Messages

* [ClearingTimes](../Comms/ClearingTimes.md): `ForecastRequestForward` & `GateClosureForward` received from MarketForecaster or DayAheadMarket and forwarded to clients

see also [Trader](./Trader.md)

# See also

[Trader](./Trader.md)

Derived agents:

* [AggregatorTrader](./AggregatorTrader.md)
  * [NoSupportTrader](./NoSupportTrader.md)
  * [RenewableTrader](./RenewableTrader.md)
  * [SystemOperatorTrader](./SystemOperatorTrader.md)
* [ConventionalTrader](./ConventionalTrader.md)
