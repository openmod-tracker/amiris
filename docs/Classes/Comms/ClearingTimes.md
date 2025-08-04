# In Short

Transfers one or multiple TimeStamps.
ClearingTimes is used during bidding.
DayAheadMarket sends them to Traders to inform them about the next event of market clearing covering one or multiple time periods.
Similarly, MarketForecasters asks Traders to deliver their future bids for the requested time periods using ClearingTimes messages.

# Details

ClearingTimes contains an arbitrary amount of TimeStamps - but at least one.
If no TimeStamp is provided during construction, a RuntimeException is thrown.

# See also

* [DayAheadMarket](../Agents/DayAheadMarket.md)
* [MarketForecaster](../Agents/MarketForecaster.md)