# Short Description

`OrderBookItems` are the central component of [OrderBooks](./OrderBook.md).
They are based on a [Bid](./Bid.md) and enhance them by additional information related to their position within the merit order of the [DayAheadMarket](../Agents/DayAheadMarket.md).
Bids with negative energy are disallowed for OrderBookItems.

# Details

`OrderBookItems` feature a BY_PRICE Comparator and can thus be sorted easily according to their offered price.
Ordering direction depends on the type of OrderBook containing the `OrderBookItems`.
An `OrderBookItem` encapsules a [Bid](./Bid.md) and may contain information about what portion of this [Bid](./Bid.md) has been accepted by the market clearing algorithm (i.e. its `awardedPower`).
It also contains a link to the original Trader submitting the bid.
In addition, the `cumulatedPowerUpperValue` reflects the position of the associated `Bid` within the merit order, i.e. the sum of the power of all elements "before" this `Bid` within the merit order plus the own offered power.
Note that for items with the same offering price the order within the OrderBook depends on the DistributionMethod (see [OrderBook](./OrderBook.md)).

`OrderBookItems` are Portable and can thus be contained in messages.

# See also

* [DayAheadMarket](../Agents/DayAheadMarket.md)
* [Bid](./Bid.md)
* [OrderBook](./OrderBook.md)