# In Short
A type of [BiddingStrategy](./BiddingStrategy) employed by [RenewableTrader](../Agents/RenewableTrader) that calculates bid prices for a [Mpvar](./Mpvar) policy regime. 

# Details
VariablePremium extends upon the [PremiumBased](./PremiumBased) template class that aids with calculating bids for market-based premium regimes. 
Bid price is set to match the difference of the [Marginal](../Modules/Marginal) cost and the predicted market premium. 
The market premium is the difference between the (expected) monthly market value and the levelised costs of electricity (LCOE) (== "value to be applied").
If the market value of a particular month exceeds the LCOE, no market premium is paid.

# See also
* [RenewableTrader](../Agents/RenewableTrader)
* [PremiumBased](./PremiumBased)
* [BiddingStrategy](./BiddingStrategy)
* [Mpvar](./Mpvar)