# In short

An ability to be implemented by an Agent to be able to trade with the [FuelsMarket](../Agents/FuelsMarket.md). 

# Details

The `FuelsTrader` class offers a few very basic implementation of how to communicate with a FuelsMarket.

# Available Products

* `FuelPriceForecastRequest`: Request for fuel price forecast at a given time and for a given fuel 
* `FuelPriceRequest`: Request for fuel price at a given time and for a given fuel
* `FuelBid`: Total amount of fuel offered to / requested from market 

# Messages

* [FuelData](../Comms/FuelData.md) & [ClearingTimes](../Comms/ClearingTimes.md): `FuelPriceForecastRequest` or `FuelPriceRequest` sent to FuelsMarket
* [FuelCost](../Comms/FuelCost.md): `FuelPriceForecast` or `FuelPrice` received from FuelsMarket
* [FuelBid](../Comms/FuelBid.md): `FuelBid` sent to FuelsMarket
* [AmountAtTime](../Comms/AmountAtTime.md): `FuelBill` received from FuelsMarket

# See also

* [FuelsMarket](../Agents/FuelsMarket.md)