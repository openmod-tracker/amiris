# In Short

The LoadShiftingStrategist is an abstract base class for the different strategists that can be instantiated and create a [BidSchedules](./BidSchedule.md) for an associated [LoadShiftingPortfolio](./LoadShiftingPortfolio.md).
It contains basic functionality used by most child classes, i.e., storing of [MeritOrderSensitivities](./MeritOrderSensitivity.md), and creating [BidSchedules](./BidSchedule.md).

# Details

The following types of load shifting strategists are implemented:

* [ShiftFileDispatcher](./ShiftFileDispatcher.md)
* [ShiftSystemCostMinimiser](./ShiftSystemCostMinimiser.md)
* [ShiftProfitMaximiser](./ShiftProfitMaximiser.md)
* [ShiftProfitMaximiserTariffs](./ShiftProfitMaximiserTariffs.md)
* [ShiftConsumerCostMinimiserExternal](./ShiftConsumerCostMinimiserExternal.md)

# See also

* [Strategist(Flexibility)](./Strategist(Flexibility).md)
* [LoadShiftingPortfolio](./LoadShiftingPortfolio.md)
* [LoadShiftingTrader](../Agents/LoadShiftingTrader.md)
* [MeritOrderSensitivity](./MeritOrderSensitivity.md)