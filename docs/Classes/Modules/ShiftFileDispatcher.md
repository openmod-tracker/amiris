# In short

Creates [BidSchedule](./BidSchedule.md) from file for a connected [LoadShiftingPortfolio](./LoadShiftingPortfolio.md).

# Details

Uses one TimeSeries read from file to define load shifting power dispatch.
The TimeSeries contains the power **relative** to the maximum power of load shifting portfolio eligible for shifting.
Thus, dispatch TimeSeries only can be shared among load shifting portfolios with **similar energy resolutions** and an identical **relative initial energy level**.
In addition, another TimeSeries defines the pattern for the current shift time.

In case the targeted dispatch would violate the load shifting portfolios energy, power or shift time bounds, a warning is issued.
Since the dispatch specified by file shall be "enforced", the created BidSchedule will use **minimal / maximal allowed prices** at the energy exchange to maximise its chance of implementation.