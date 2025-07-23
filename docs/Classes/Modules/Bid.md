# In Short
Bids represent a a single bidding element for the DayAheadMarket.
It contains an offered / requested amount of electricity with an associated minimum / maximum price and the associated true marginal costs.

# Details
Bids are Portable.
They are not submitted individually to their corresponding market, but as part of a summary message ([BidsAtTime](./BidsAtTime)) containing all bids of the trader for a given time. 
Bid contains the following information:

* `energyAmountInMWH` the requested or offered energy; positive value
* `offerPriceInEURperMWH` minimum price for supply bids, or maximum price for demand bids
* `marginalCostInEURperMWH` specific generation cost of the associated power plant; optional value

# See also
* [BidsAtTime](./BidsAtTime)