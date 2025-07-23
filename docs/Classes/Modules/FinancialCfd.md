# In Short

FinancialCfd is a type of [PolicyItem](./PolicyItem.md) holding all the [TechnologySet](../Comms/TechnologySet.md)-specific information for a `FINANCIAL_CFD` scheme, i.e. a two-sided contracts for differences scheme  according to the proposal from [Schlecht et al. 2023](https://www.econstor.eu/handle/10419/268370).
Support payments consist of two different money flows:
* a capacity premium payment from the [SupportPolicy](../Agents/SupportPolicy.md) and
* a payback obligation for the market revenues of a reference plant, paid from the [AggregatorTrader](../Agents/AggregatorTrader.md) to the SupportPolicy.

Payments are evaluated after the accounting period.
Payback obligation excludes hours with negative prices which may occurred during the accounting period.

# Input from file

Included parameters are
* `Premium`: The capacity premium for the [TechnologySet](../Comms/TechnologySet.md) which (by assumption) equals to the share of the total full costs (investment and (expected) operational expenditures) for the accounting period given in EUR/MW.
* `ReferenceYieldProfile`: Yield profile of the reference plant to calculate payback obligation from its market revenues.

# See also

[PolicyItem](./PolicyItem.md)