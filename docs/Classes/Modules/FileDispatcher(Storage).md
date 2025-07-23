# In short
Creates [BidSchedule](./BidSchedule) from file for a connected storage [Device](./Device).

# Details
Uses a TimeSeries read from file to define storage dispatch. 
The TimeSeries contains the charging and discharging power **relative** to the **internal** (dis-)charging power of the device. 
Thus, dispatch TimeSeries only can be shared among storage devices with **similar E2P** and identical **relative initial energy level**. 
In case the targeted dispatch would create a "more than empty" or "more than full" energy storage, a warning is issued. 
Since the dispatch specified by file shall be "enforced", the created BidSchedule will use **minimal / maximal allowed prices** for its bids to maximise their chance of implementation.

# See also
[ArbitrageStrategist](./ArbitrageStrategist)