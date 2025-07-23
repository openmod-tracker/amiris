# In Short

Creates [BidSchedules](./BidSchedule) for an associated [LoadShiftingPortfolio](./LoadShiftingPortfolio) and thereby minimises the dispatch-based system cost.

# Details

ShiftSystemCostMinimiser is a [LoadShiftingStrategist](./LoadShiftingStrategist).
It uses [MarginalCostSensitivity](./MarginalCostSensitivity) to find the optimal dispatch path that minimises the overall system cost for dispatch.

## General strategy

All possible dispatches within the forecast period are evaluated using dynamic programming considering the changes of system cost induced to the merit order when the load shifting portfolio is dispatched.
For this, the SystemCostMinimiser needs "perfect foresight" of the merit order for all time periods within the forecast period.
Thus, ShiftSystemCostMinimiser is **incompatible** with other agents that modify the merit order dynamically.
Also, only **one** load shifting portfolio can be used when ShiftSystemCostMinimiser is used.
In case the merit order is not "what it has been promised to the ShiftSystemCostMinimiser to be" (due to other agents interfering with the merit order) the resulting dispatch of SystemCostMinimiser performs very bad.

## Bidding

Since the resulting electricity price is of no relevance to ShiftSystemCostMinimiser, the offered bid prices are fictionally (lower or higher price limits) to force dispatch.