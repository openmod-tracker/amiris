# Short Description

`MarketClearing` organises day-ahead market clearing activities for the [DayAheadMarket](../Agents/DayAheadMarket.md) and [MarketForecaster](../Agents/MarketForecaster.md) agents.

# Details

`MarketClearing` connects incoming messages with [BidsAtTime](../Comms/BidsAtTime.md) content, assigns them to either its [DemandOrderBook](./DemandOrderBook.md) or [SupplyOrderBook](./SupplyOrderBook.md) and then performs the actual market clearing via a [MeritOrderKernel](./MeritOrderKernel.md).
Finally, it returns a [MarketClearingResult](./MarketClearingResult.md).
Given OrderBooks are updated according to the market clearing result.
The price in case of scarcity, i.e. when valuable demand cannot be served by a prematurely ending supply curve, depends on the selected `ShortagePrice`.

## Shortage Price

Market prices set by MarketClearing depend on the parameter `ShortagePrice` which can take two values:
* `ScarcityPrice` (default) the energy price on a market with scarcity equals the maximum allowed price (scarcity price)
* `LastSupplyPrice` the market price is capped at the highest supply bid

The latter option is intended to avoid market disturbances for simulations where scarcity occurs, but is not deemed "reasonable".
In this case, demand will still be shed, but the market price will stay at the highest supply bid and have less impact on the refinancing of market actors. 

# Input from file

* `DistributionMethod` the energy assignment strategy for price-setting bids with the same price, see [OrderBook](../Modules/OrderBook.md#distribution-methods).
* `ShortagePrice` which price to use in case of scarcity (i.e. when valuable demand is shed due to missing capacity), see above 

# Submodules

* [MeritOrderKernel](./MeritOrderKernel.md)
* [MarketClearingResult](./MarketClearingResult.md)

# See also

* [OrderBook](./OrderBook.md)
