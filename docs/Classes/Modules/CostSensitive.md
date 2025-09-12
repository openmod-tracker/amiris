# In short

`CostSensitivity` is an implementation of [MarketClearingAssessment](./MarketClearingAssessment.md).
`CostSensitivity` inspects the order books of a provided [MarketClearingResult](./MarketClearingResult.md) and derives changes in prices associated with changes in supply or demand.
From price changes, `CostSensitivity` calculates the implications on increased costs / reduced revenues.

# Details

## Assumptions

See [MarketClearingAssessment](./MarketClearingAssessment.md)

## Operations

First, `FullAssessor` will extract all orders in the [SupplyBook](./SupplyOrderBook.md) and [DemandBook](./DemandOrderBook.md).
Then, it will split the items according to their awarded status and assign them to the charging / discharging side.
After sorting all items according to the value (depending on the implementation), cumulative values are calculated.

# See also

* [MarketClearingAssessment](./MarketClearingAssessment.md)
