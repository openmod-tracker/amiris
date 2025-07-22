# 42 words

DayAheadMarketSingleZone (DAMSZ) is a type of [DayAheadMarket](./DayAheadMarket.md) covering a single market zone.
It digests the requested and offered energy from connected [DayAheadMarketTraders](../Abilities/DayAheadMarketTrader.md).
It then finds a uniform market clearing price in its market zone and returns the price and their specific awarded energy to the DayAheadMarketTraders.

# Details

The DAMSZ performs regular market clearings at given times.
Each clearing is performed individually.
A simultaneous market clearing for more than one time period (compared to reality: 24 hours cleared at once) is not supported, yet.
Based on the received [Bids](../Comms/BidsAtTime.md) from the DayAheadMarketTraders, DAMSZ fillsthe [Demand & Supply book](../Modules/OrderBook.md).
The [MeritOrderKernel](../Modules/MeritOrderKernel.md) is then employed to clear the market for the current TimeSegment under investigation.
Then, for each contracted Agent, the total amount of awarded supply and demand energy is calculated and sent out as [Award](../Comms/AwardData.md) message.
Such messages do not require a previous bid - if no bid was placed before the clearing, the awarded energy will equal Zero.

# History

This agent was known as `EnergyExchange` before version 2.0.0-alpha.14.

# Dependencies

* [DayAheadMarket](./DayAheadMarket.md): Parent class defining sending of GateClosureInfos
* [DayAheadMarketTraders](../Abilities/DayAheadMarketTrader.md): to send their supply and demand bids

# Input from file

see [DayAheadMarket](./DayAheadMarket.md)

# Input from environment

* Bids from DayAheadMarketTraders

# Simulation outputs

see [DayAheadMarket](./DayAheadMarket.md)

# Contracts

* DayAheadMarketTraders to send their Bids
* DayAheadMarketTraders to receive Awards

see also [DayAheadMarket](./DayAheadMarket.md)

# Available Products

see [DayAheadMarket](./DayAheadMarket.md)

# Submodules

* [MarketClearing](../Modules/MarketClearing.md)
* [MeritOrderKernel](../Modules/MeritOrderKernel.md)
* [OrderBook](../Modules/OrderBook.md)
* [MarketClearingResult](../Modules/MarketClearingResult.md)

# Messages

* [AwardData](../Comms/AwardData.md) sent out
* [BidsAtTime](../Comms/BidsAtTime.md) received

see also [DayAheadMarket](./DayAheadMarket.md)

# See also

* [DayAheadMarket](./DayAheadMarket.md)
* [DayAheadMarketTrader](../Abilities/DayAheadMarketTrader.md)