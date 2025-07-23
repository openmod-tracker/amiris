# In Short

A type of [BiddingStrategy](./BiddingStrategy.md) employed by [RenewableTrader](../Agents/RenewableTrader.md) that calculates bid prices for a [Mpfix](./Mpfix.md) policy regime. 

# Details

FixedPremium extends upon the [PremiumBased](./PremiumBased.md) template class that aids with calculating bids for market-based premium regimes.
Bid price is set to match the difference of the [Marginal](../Modules/Marginal.md) cost and the fixed market premium.

# See also

* [RenewableTrader](../Agents/RenewableTrader.md)
* [PremiumBased](./PremiumBased.md)
* [BiddingStrategy](./BiddingStrategy.md)
* [Mpfix](./Mpfix.md)