# In Short

A type of [BiddingStrategy](./BiddingStrategy.md) employed by [RenewableTrader](../Agents/RenewableTrader.md) that calculates bid prices for a [Mpvar](./Mpvar.md) policy regime.

# Details

VariablePremium extends upon the [PremiumBased](./PremiumBased.md) template class that aids with calculating bids for market-based premium regimes.
Bid price is set to match the difference of the [Marginal](../Modules/Marginal.md) cost and the predicted market premium.
The market premium is the difference between the (expected) monthly market value and the levelised costs of electricity (LCOE) (== "value to be applied").
If the market value of a particular month exceeds the LCOE, no market premium is paid.

# See also

* [RenewableTrader](../Agents/RenewableTrader.md)
* [PremiumBased](./PremiumBased.md)
* [BiddingStrategy](./BiddingStrategy.md)
* [Mpvar](./Mpvar.md)