# In Short

Creates [BidSchedules](./BidSchedule.md) for an associated [StorageDevice](./Device.md) and thereby maximises profits of the storage operator.

# Details

ProfitMaximiser is an [ArbitrageStrategist](./ArbitrageStrategist.md).
It uses "merit order sensitivities" of type [MarginalCostSensitivity](./MarginalCostSensitivity.md) to find the optimal storage utilisation that maximises its profits.

## General strategy

All possible dispatches within the forecast period are evaluated using dynamic programming considering the changes of price induced to the merit order when the storage is dispatched.
For this, the ProfitMaximiser needs "perfect foresight" of the merit order for all time periods within the forecast period.
Thus, ProfitMaximiser is **incompatible** with other agents that modify the merit order dynamically.
Also, only **one** storage can be used when ProfitMaximiser is used.
In case the merit order is not "what it has been promised to the ProfitMaximiser to be" (due to other agents interfering with the merit order) the resulting dispatch of ProfitMaximiser performs bad.

## Bidding strategy

ProfitMaximiser bids slightly above (below) the expected electricity price when charging (discharging).
This includes its own expected impact on the electricity price.

# See also

[ArbitrageStrategist](./ArbitrageStrategist.md)
