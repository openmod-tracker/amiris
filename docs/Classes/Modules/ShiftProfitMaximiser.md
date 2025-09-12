# In Short

Creates [BidSchedules](./BidSchedule.md) for an associated [LoadShiftingPortfolio](./LoadShiftingPortfolio.md) and thereby maximising the profits optionally considering purchase taxes and levies in a highly simplified manner.

# Details

ShiftProfitMaximiser is a [LoadShiftingStrategist](./LoadShiftingStrategist.md).
It uses [MeritOrderSensitivity](./MeritOrderSensitivity.md) to find the optimal dispatch path that maximises the load shifting portfolio's profits.
Hereby, purchase taxes and levies are considered as a static adder that can be specified optionally.
In case, end user tariffs should be studied in detail, the strategist [ShiftProfitMaximiserTariffs](./ShiftProfitMaximiserTariffs.md) should be used instead.

## General strategy

All possible dispatches within the forecast period are evaluated using dynamic programming considering the price changes induced to the merit order when the load shifting portfolio is dispatched.
For this, the ShiftProfitMaximiser needs "perfect foresight" of the merit order for all time periods within the forecast period.
Thus, ShiftProfitMaximiser is **incompatible** with other agents that modify the merit order dynamically.
Also, only **one** load shifting portfolio can be used when ShiftProfitMaximiser is used.
In case the merit order is not "what it has been promised to the ShiftProfitMaximiser to be" (due to other agents interfering with the merit order) the resulting dispatch of ShiftProfitMaximiser performs very bad.

## Bidding

The Bids are made to resemble the expected price within the merit order after applying the changed demand or supply by ShiftProfitMaximiser.
A small price tolerance is included to force the scheduled dispatch.