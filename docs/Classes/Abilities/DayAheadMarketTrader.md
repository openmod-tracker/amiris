# In short

An ability to be implemented by an Agent to be able to trade with the [DayAheadMarket](../Agents/DayAheadMarket.md). 

# Details

The `DayAheadMarketTrader` class offers but a very basic implementation of how to communicate with the DayAheadMarket.

# Available Products

* `Bids`: Sell/Buy orders to be placed at the DayAheadMarket

# Outputs

* `OfferedEnergyInMWH`: Energy offered to energy exchange in MWh
* `AwardedEnergyInMWH`: Energy awarded by energy exchange in MWh
* `RequestedEnergyInMWH`: Energy requested at energy exchange in MWh

# Messages

* [BidsAtTime](../Comms/BidsAtTime.md): `Bids` sent to DayAheadMarket
* [ClearingTimes](../Comms/ClearingTimes.md): `GateClosureInfo` received from DayAheadMarket

# See also

* [DayAheadMarket](../Agents/DayAheadMarket.md)