# In short

Response of the [SupportPolicy](../Agents/SupportPolicy.md) agent to the [SupportRequest](./SupportRequestData.md) message it received.
In case, the payment is negative, the concerned [AggregatorTrader](../Agents/AggregatorTrader.md) has to pay to the [SupportPolicy](../Agents/SupportPolicy.md).

# Details

Contained information:

* `clientId` UUID of the power plant operator connected to this support request response
* `setType` the name of the [TechnologySet](./TechnologySet.md) (enum)
* `accountingPeriod` the accounting period (TimeSegment) for which the support is to be paid
* `payment` the amount of payment for the accounting period
* `marketPremium` the market premium for the accounting period - for energy-based market premia only (`MPFIX`, `MPVAR` and `CFD`).

see also [SupportRequestData](./SupportRequestData.md)