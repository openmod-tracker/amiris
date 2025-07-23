# In Short
A type of [BiddingStrategy](./BiddingStrategy) employed by [RenewableTrader](../Agents/RenewableTrader) that calculates bid prices for a [Mpfix](./Mpfix) policy regime. 

# Details
FixedPremium extends upon the [PremiumBased](./PremiumBased) template class that aids with calculating bids for market-based premium regimes.
Bid price is set to match the difference of the [Marginal](../Modules/Marginal) cost and the fixed market premium.

# See also
* [RenewableTrader](../Agents/RenewableTrader)
* [PremiumBased](./PremiumBased)
* [BiddingStrategy](./BiddingStrategy)
* [Mpfix](./Mpfix)