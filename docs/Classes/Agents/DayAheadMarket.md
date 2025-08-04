# 42 words

DayAheadMarket represents the day-ahead energy market with one clearing per time segment (typically hourly clearing).

# Details

The DayAheadMarket is an abstract base class for day-ahead energy markets.
It covers common definitions of such market agents, like Inputs and Products. 
Its only action covers the sending of gate closure information to connected trading clients - to tell them when the market will clear.
Any other actions are defined in the corresponding child classes.
Simultaneous market clearing for more than one time period (compared to reality: 24 hours cleared at once) is not yet supported.

# Dependencies

see child classes

# Input from file

* `Clearing` see [MarketClearing](../Modules/MarketClearing.md)
* `GateClosureInfoOffsetInSeconds` time delay between sending out `GateClosureInfo` and actual market clearing

see also child classes

# Input from environment

see child classes 

# Simulation outputs

* `AwardedEnergyInMWH` Total power awarded at last market clearing
* `ElectricityPriceInEURperMWH` Market clearing price achieved at last market clearing
* `DispatchSystemCostInEUR` System cost for generating the power awarded at last market clearing

# Contracts

* DayAheadMarketTraders to receive `GateClosureInfo`

see child classes

# Available Products

* GateClosureInfo
* Awards

# Submodules

see child classes

# Messages

* [ClearingTimes](../Comms/ClearingTimes.md) sent out

see also child classes

# See also

Child classes:
* [DayAheadMarketSingleZone](./DayAheadMarketSingleZone.md)
* [DayAheadMarketMultiZone](./DayAheadMarketMultiZone.md)