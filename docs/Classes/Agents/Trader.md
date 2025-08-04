# 42 words

The Trader is parent to all Agents trading at the [DayAheadMarket](./DayAheadMarket.md).
Some Traders link [PowerPlantOperators](./PowerPlantOperator.md) to the energy exchange by placing [Bids](../Comms/BidsAtTime.md).
In this case, bids are created based upon connected power plants' [Marginals](../Comms/MarginalsAtTime.md).
These traders coordinate the actual production of their clients based on received [Awards](../Comms/AwardData.md).
Other types of Traders can create bids and digest awards on their own without using clients.

# Details

As abstract base class, Trader cannot be instantiated.
Instead, Trader provides all relevant Products and some inherited methods for its derived agents.
Trader also implements the [DayAheadMarketTrader](../Abilities/DayAheadMarketTrader.md) ability.
All actual actions, especially Bids creation, are defined in child classes.

# Dependencies

* [DayAheadMarket](./DayAheadMarket.md)
* [MarketForecaster](./MarketForecaster.md)
* [DayAheadMarketTrader](../Abilities/DayAheadMarketTrader.md)

# Input from file

None in general - input parameters are specific to each subclass.

# Input from environment

Not addressed in this class - all type of Traders, however, have additional actions to interact with the environment via, e.g.,

* Bid creation for DayAheadMarket and MarketForecaster (if applicable)
* Awards from DayAheadMarket
* Marginal cost and MarginalCostForecast from PlantOperators (if existent)
* Forecasts from MarketForecaster (if required)

# Simulation outputs

see [DayAheadMarketTrader](../Abilities/DayAheadMarketTrader.md)

# Contracts

Trader has no contracts of its own, but derived Agents typically have Contracts with

* [MarketForecaster](./MarketForecaster.md) receives `BidsForecast`, `MeritOrderForecastRequest` or `PriceForecastRequest` and send `ForecastRequest` `MeritOrderForecast` or `PriceForecast`
* [DayAheadMarket](./DayAheadMarket.md) receives `Bids` and returns `Awards`; sends `GateClosureInfo`

# Available Products

* `BidsForecast`: A forecast of the own bids to be placed at later times at the associated DayAheadMarket

see also [DayAheadMarketTrader](../Abilities/DayAheadMarketTrader.md)

# Submodules

None

# Messages

* [AwardData](../Comms/AwardData.md): `Awards` received from DayAheadMarket

see also [DayAheadMarketTrader](../Abilities/DayAheadMarketTrader.md)

# See also

Abilities:

* [DayAheadMarketTrader](../Abilities/DayAheadMarketTrader.md)

Derived agents:

* [TraderWithClients](./TraderWithClients.md)
* [DemandTrader](./DemandTrader.md)
* [FlexibilityTrader](./FlexibilityTrader.md)
* [ImportTrader](./ImportTrader.md)
* [GenericFlexibilityTrader](./GenericFlexibilityTrader.md)