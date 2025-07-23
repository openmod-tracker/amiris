# Short Description
Demand [Bids](./Bid) are managed in the `DemandOrderBook`. It sorts its [OrderBookItems](./OrderBookItem) in descending order of their offering price.

# Details
`DemandOrderBook` extends the [OrderBook](./OrderBook) and adds a few extra functionalities:
* `getUnsheddableDemand`: the sum of all items' power that has been asked for and that is not sheddable, i.e. that has a value of lost load greater or equal to the scarcity price.
* `getAmountOfPowerShortage`: the amount of power that the supply is short, i.e. the sum of all demand power not awarded that has higher prices than the most expensive supply offer.

# See also 
* [SupplyOrderBook](./SupplyOrderBook)
* [OrderBook](./OrderBook)