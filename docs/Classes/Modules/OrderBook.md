# Short Description
Represents a list of Bids at the [DayAheadMarket](../Agents/DayAheadMarket) for specific time of energy trading. 

# Details
The `OrderBook` is abstract and cannot be instantiated. An `OrderBook` represents either Bids or Asks in form of a list of [OrderBookItems](./OrderBookItem) - depending on whether it is a [DemandOrderBook](./DemandOrderBook) or [SupplyOrderBook](./SupplyOrderBook). It can sort itself according to the offered price of the items it contains. Once sorted, no more items can be added before the `OrderBook` has been "cleared" again. If the market has been cleared using the [MeritOrderKernel](./MeritOrderKernel), the `OrderBook` can be updated with the clearing price. It then automatically awards supply bids below and demand bids above the clearing price. For bids equal to the market clearing price, see the following text.

## Distribution Methods
While the assignment of awarded energy is simple for bids that are below / above the actual clearing price, the DayAheadMarket provides no direct rule on how to proceed with bids that **match** the clearing price, i.e. price-setting bids. In case one bid is price-setting it is assigned the "left-over" energy. However, in case multiple bids have the same price matching the price for the OrderBook after clearing, OrderBook supports three energy assignment strategies:
* `FIRST_COME_FIRST_SERVE`: Bids are awarded in the order they appear after sorting the order book. NOTE: This is discouraged as it might cause unreliable results or introduce biases.
* `RANDOMIZE`: Bids with the same price are awarded in a random order.
* `SAME_SHARES`: Bids with the same price are all awarded their same relative share.

# Submodules
* [OrderBookItem](./OrderBookItem)

# Derived classes
* [SupplyOrderBook](./SupplyOrderBook)
* [DemandOrderBook](./DemandOrderBook)

# See also
* [MarketClearing](./MarketClearing)
* [MarketClearingResult](./MarketClearingResult)