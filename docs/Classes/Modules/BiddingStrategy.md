# In Short

BiddingStrategy is an interface that [RenewableTrader](../Agents/RenewableTrader.md)s use to calculate bidding prices for marketing of renewable energy.
Several BiddingStrategies exist and consider characteristics of their connected [PolicyItem](./PolicyItem.md) to maximise the profits of marketing renewable energy.

# Details

The actual calculation depend on the BiddingStrategy implementing this interface.  

# Implemented by

* [AtMarginalCost](./AtMarginalCost.md)
* [ContractForDifferences](./ContractForDifferences.md)
* [FixedPremium](./FixedPremium.md)
* [VariablePremium](./VariablePremium.md)

# See also

* [PolicyItem](./PolicyItem.md)
* [RenewableTrader](../Agents/RenewableTrader.md)
