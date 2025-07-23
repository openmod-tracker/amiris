# In Short

Creates [BidSchedules](./BidSchedule.md) for an associated [LoadShiftingPortfolio](./LoadShiftingPortfolio.md) and thereby maximising the profits considering end user tariff components in a detailed manner.

# Details

ShiftProfitMaximiserTariffs is a [LoadShiftingStrategist](./LoadShiftingStrategist.md).
It uses [MeritOrderSensitivity](./MeritOrderSensitivity) to find the optimal dispatch path that maximises the load shifting portfolio's profits.
Hereby, end user tariff components are considered and included in the dispatch optimization.
The price components included may be static or dynamic placing different incentives.
The ShiftProfitMaximiserTariffs uses price information from an [EndUserTariff](./EndUserTariff.md) for the dispatch optimization.
The underlying portfolio is assumed to be homogeneous in terms of pricing.

## General strategy

All possible dispatches within the forecast period are evaluated using dynamic programming considering the price changes induced to the merit order when the load shifting portfolio is dispatched.
For this, the ShiftProfitMaximiserTariffs needs "perfect foresight" of the merit order for all time periods within the forecast period. 
Thus, ShiftProfitMaximiserTariffs is **incompatible** with other agents that modify the merit order dynamically. 
Also, only **one** load shifting portfolio can be used when ShiftProfitMaximiserTariffs is used.
In case the merit order is not "what it has been promised to the ShiftProfitMaximiserTariffs to be" (due to other agents interfering with the merit order) the resulting dispatch of ShiftProfitMaximiserTariffs performs very bad.
For the optimization, the ShiftProfitMaximiserTariffs uses the sum of the wholesale day-ahead price and the additional end user tariff components.

## Bidding

The Bids are made to resemble the expected price within the merit order after applying the changed demand or supply by ShiftProfitMaximiserTariffs.
A small price tolerance is included to force the scheduled dispatch.