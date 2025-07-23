# In Short

The LoadShiftingStrategist is an abstract base class for the different strategists that can be instantiated and create a [BidSchedules](./BidSchedule) for an associated [LoadShiftingPortfolio](./LoadShiftingPortfolio).
It contains basic functionality used by most child classes, i.e., storing of [MeritOrderSensitivities](./MeritOrderSensitivity), and creating [BidSchedules](./BidSchedule).

# Details

The following types of load shifting strategists are implemented:

* [ShiftFileDispatcher](./ShiftFileDispatcher)
* [ShiftSystemCostMinimiser](./ShiftSystemCostMinimiser)
* [ShiftProfitMaximiser](./ShiftProfitMaximiser)
* [ShiftProfitMaximiserTariffs](./ShiftProfitMaximiserTariffs)
* [ShiftConsumerCostMinimiserExternal](./ShiftConsumerCostMinimiserExternal)

# See also

* [Strategist(Flexibility)](./Strategist(Flexibility))
* [LoadShiftingPortfolio](./LoadShiftingPortfolio)
* [LoadShiftingTrader](../Agents/LoadShiftingTrader)
* [MeritOrderSensitivity](./MeritOrderSensitivity)