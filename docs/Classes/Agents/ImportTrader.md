# In Short

Offers imported energy at the `EnergyExchange` as supply according to a given `TimeSeries` of energy imports.

# Details

The ImportTrader can be used to represent exogenous imports into a market zone.
For this purpose, the `ImportTrader` agent is designed similar to a `DemandTrader`, but with energy supply. More precisely, it holds a list of energy imports. An `Import` consists of a time series of imported energy in MWH per time segment and a value in EUR for unsold imported MWH energy. 
The `ImportTrader` tries to sell its `Import` to the market according to its value of unsold imported energy.  

In case the imports in a market zone exceed the demand (plus exports), it is advisable to implement the ImportTrader and avoid "negative" DemandBids in the EnergyExchange. This is also relevant for coupled market zones when using the MarketCoupling Agent.

# Input

List of input parameters:

* `AvailableEnergyForImport`: A time series that contains imported energy bids to be offered in each time step	
* `ImportCostInEURperMWH`: The minimum price of offered imported energy bids 

# Simulation outputs

see [Trader](./Trader.md)
		
# Contracts

* `ImportTrader` receives a `ForecastRequest` from the `MarketForecaster` and in return sends its `BidsForecast` forecast.
* `ImportTrader` sends bids to the `EnergyExchange` and gets awards.

# See Also

* [DemandTrader](./DemandTrader.md)
* [EnergyExchange](./EnergyExchange.md)
* [MarketForecaster](./MarketForecaster.md)
