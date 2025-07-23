# In Short
BiddingStrategy is an interface that [RenewableTrader](../Agents/RenewableTrader)s use to calculate bidding prices for marketing of renewable energy.
Several BiddingStrategies exist and consider characteristics of their connected [PolicyItem](./PolicyItem) to maximise the profits of marketing renewable energy.

# Details
The actual calculation depend on the BiddingStrategy implementing this interface.  

# Implemented by
* [AtMarginalCost](./AtMarginalCost)
* [ContractForDifferences](./ContractForDifferences)
* [FixedPremium](./FixedPremium)
* [VariablePremium](./VariablePremium)

# See also
* [PolicyItem](./PolicyItem)
* [RenewableTrader](../Agents/RenewableTrader)
