# In short

Builds [BidScheduler](./BidScheduler) from given input parameters

# Input from file

* `Type`: enum, name of the assessment function that is to be instantiated
* `SchedulingHorizonInHours`: double value, the time length of dispatch schedules to be created

# Available Types

* `ENSURE_DISPATCH`: Ensure planned dispatch is fulfilled by bidding at technical price limits; high-risk strategy, requires high-precision forecasts [EnsureDispatch](./EnsureDispatch)
* `STORAGE_CONTENT_VALUE`: Use the estimated value changes of storage content to calculate bidding price; medium-risk strategy, requires forecasts with reasonable precision [StorageContentValue](./StorageContentValue)

# See also

* [BidScheduler](./BidScheduler)