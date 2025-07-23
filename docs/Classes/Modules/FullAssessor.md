# In short

`FullAssessor` is an implementation of [MarketClearingAssessment](./MarketClearingAssessment.md).
`FullAssessor` is still an abstract class - the actual sensitivity assessment depends on the child classes.
`FullAssessor` inspects the order books of a provided [MarketClearingResult](./MarketClearingResult.md) and derives changes in value associated with changes in supply or demand.

# Details

## Assumptions

See [MarketClearingAssessment](./MarketClearingAssessment.md)

## Operations

First, `FullAssessor` will extract all orders in the [SupplyBook](./SupplyOrderBook.md) and [DemandBook](./DemandOrderBook.md).
Then, it will split the items according to their awarded status and assign them to the charging / discharging side.
After sorting all items according to the value (depending on the implementation), cumulative values are calculated.

# Child classes

* [CostSensitivity](./CostSensitivity.md)
* [MarginalCostSensitivity](./MarginalCostSensitivity(Forecast).md)

# See also

* [MarketClearingAssessment](./MarketClearingAssessment.md)