# In Short
Creates [BidSchedules](./BidSchedule) for an associated [StorageDevice](./Device) and thereby minimises the overall dispatch-based system cost.

# Details
SystemCostMinimiser is an [ArbitrageStrategist](./ArbitrageStrategist). 
It uses "merit order sensitivities" of type [MarginalCostSensitivity](./MarginalCostSensitivity) to find the optimal storage utilisation that minimises the overall system cost for dispatch.

## General strategy
All possible dispatches within the forecast period are evaluated using dynamic programming  considering the changes of system cost induced to the merit order when the storage is dispatched. 
For this, the SystemCostMinimiser needs "perfect foresight" of the merit order for all time periods within the forecast period.
Thus, SystemCostMinimiser is **incompatible** with other agents that modify the merit order dynamically.
Also, only **one** storage can be used when SystemCostMinimiser is used. 
In case the merit order is not "what it has been promised to the SystemCostMinimiser to be" (due to other agents interfering with the merit order) the resulting dispatch of SystemCostMinimiser performs bad.

## Bidding strategy
Since the resulting electricity price is of no relevance to SystemCostMinimiser, the offered bid prices are fictionally and placed at the technical price limits to maximise chances of awarding the bids from the planned [BidSchedules](./BidSchedule).

# See also
[ArbitrageStrategist](./ArbitrageStrategist)
