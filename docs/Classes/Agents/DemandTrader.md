# 42 words

DemandTrader is a [Trader](./Trader.md), which is responsible for purchasing the electricity demand at the [DayAheadMarket](./DayAheadMarket.md). 

# Details

DemandTrader is responsible to acquire the electricity demand from the [DayAheadMarket](./DayAheadMarket.md).
The demand is given as time series.
Hereby, multiple time series are used and attributed with a `ValueOfLostLoad`, i.e. the price  level at which the respective consumer cluster is willing to shed (parts of) their load. 
The non-sheddable baseline demand is marketed at the technical price maximum.
To market the demand, this agent sends [BidsAtTime](../Comms/BidsAtTime.md) to the energy exchange.
In order to ensure that the electricity demand is met, the price of the demand bids is as high as the specified `ValueOfLostLoad`.

# Dependencies

None

# Input from file

* `Loads`: List of groups containing 
  * `DemandSeries`: A time series that contains demand values, which should be covered in each time step
  * `ValueOfLostLoad`: The maximum price of offered demand bids

# Input from environment

None

# Simulation outputs

* `RequestedEnergyInMWH`: Energy demanded from energy exchange in MWh
* `AwardedEnergyInMWH`: Energy awarded by energy exchange in MWh

# Contracts

* DemandTrader receives a `ForecastRequest` from the [MarketForecaster](./MarketForecaster.md) and in return sends its `BidsForecast` forecast. 
* DemandTrader sends bids to the [DayAheadMarket](./DayAheadMarket.md) and gets awards.

# Available Products

see [Trader](./Trader.md)

# Submodules
None

# Messages

see [Trader](./Trader.md)

# See also

* [Trader](./Trader.md)