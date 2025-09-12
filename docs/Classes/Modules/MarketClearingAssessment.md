# In short

`MarketClearingAssessment` is an interface that allows to assess a market clearing result and return its sensitivity to changes in demand or supply.

# Details

## Assumptions

A [MarketClearingResult](./MarketClearingResult.md) is provided first before information is extracted from this interface.

## Operations

The `build(type)` method helps to construct the correct implementation of `MarketClearingAssessment` depending on the required type of assessment.
Then, a `MarketClearingResult` is provided with the `assess()` method.
Finally, the assessment results may be extracted using `getDemandSensitivityPowers()`, `getDemandSensitivityValues()`, `getSupplySensitivityPowers()`, and `getSupplySensitivityValues()`.

# Implementations

* [CostInsensitive](./CostInsensitive.md)
* [CostSensitive](./CostSensitive.md)

# See also

* [MarketClearingResult](./MarketClearingResult.md)