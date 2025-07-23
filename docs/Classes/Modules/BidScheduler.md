# In short

`BidSchedulers` is an interface that creates [BidSchedules](./BidSchedule.md) from a given dispatch schedule.

## Details

`BidSchedulers` store the information about the time length of schedules.
Using `getScheduleHorizonInHours()` one can obtain this information from `BidSchedulers`.

Based on a given starting time and a planned dispatch, `BidSchedulers` create the `BidSchedule` using `createBidSchedule()`.

# Input from file

See [BidSchedulerBuilder](./BidSchedulerBuilder.md)

# Child classes

* [EnsureDispatch](./EnsureDispatch.md)
* [StorageContentValue](./StorageContentValue.md)

# See also

* [GenericFlexibilityTrader](../Agents/GenericFlexibilityTrader.md)
* [BidSchedule](./BidSchedule.md)