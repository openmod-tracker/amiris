# In short

Builds [AssessmentFunctions](./AssessmentFunction) from given input parameters

# Input from file

* `Type`: enum, name of the assessment function that is to be instantiated

# Available Types

* `MAX_PROFIT_PRICE_TAKER`: Maximise profit using an electricity price forecast neglecting any price impact of bids, see [MaxProfitPriceTaker](./MaxProfitPriceTaker)
* `MAX_PROFIT`: Maximise profit using merit order forecasts, considering price impact of bids from all GenericFlexibilityTraders, see [MaxProfit](./MaxProfit)
* `MIN_SYSTEM_COST`: Minimise system cost using merit order forecasts, considering impact of bids from all GenericFlexibilityTraders, see [MinSystemCost](./MinSystemCost)

The latter two type names indicate that the agents can only operate properly as a single flexibility entity.
This, however, is no longer true - these strategies can be applied to competing flexibility options.
To avoid a breaking change the original names were kept, but will be changed in the next major release.

# See also

* [AssessmentFunction](./AssessmentFunction)
* [MaxProfitPriceTaker](./MaxProfitPriceTaker)
* [MaxProfit](./MaxProfit)
* [MinSystemCost](./MinSystemCost)