# In Short

A type of [BiddingStrategy](./BiddingStrategy.md) employed by [RenewableTrader](../Agents/RenewableTrader.md) that calculates bid prices for a [Cfd](./Cfd.md) policy regime. 

# Details

ContractForDifferences extends upon the [PremiumBased](./PremiumBased.md) template class that aids with calculating bids for market-based premium regimes. 
Bid price is set to match the difference of the [Marginal](../Modules/Marginal.md) cost and the predicted market premium. 
The market premium is the difference between the (expected) monthly market value and the levelised costs of electricity (LCOE) (== "value to be applied").
In case the market value of a month exceeds the LCOE, a negative market premium results which means there is an obligation to pay back.
If a negative market premium is prognosed, the resulting bid price (variable costs - prognosed premium) can be higher than actual marginal costs.

# See also

* [RenewableTrader](../Agents/RenewableTrader.md)
* [PremiumBased](./PremiumBased.md)
* [BiddingStrategy](./BiddingStrategy.md)
* [Cfd](./Cfd.md)
