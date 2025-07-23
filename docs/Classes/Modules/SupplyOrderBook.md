# Short Description
Supply [Bids](./Bid) are managed in the `SupplyOrderBook`. It sorts its [OrderBookItems](./OrderBookItem) in ascending order of their offering price.

# Details
`SupplyOrderBook` extends the [OrderBook](./OrderBook) and adds a few extra functionalities, i.e. to get the last *awarded item*, i.e. with the highest bidding price or the item with the highest offering price *at all*. 

# See also 
* [DemandOrderBook](./DemandOrderBook)
* [OrderBook](./OrderBook)