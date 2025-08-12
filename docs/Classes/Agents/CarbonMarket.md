# 42 words

The CarbonMarket sells CO2 emission allowances, determines their prices and accounts for total quantity of sold CO2 allowances.
Currently, it can be operated in the mode `FIXED` where prices are given exogenously.

# Detail

Conventional power plant technologies emit CO2 for which emission allowances are required.
The accompanying costs to these certificates are determined by the CarbonMarket which is based on the European Union Emissions Trading System (EU ETS).
Requests for prices of CO2 emission allowances at a certain time are answered and forwarded to the respective agents.
In order to determine the prices, one mode (`FIXED`) is currently implemented.
Hereby, the CarbonMarket reads a given price time series and supplies the price information to requesting parties.
This mode, however, does not allow a feedback on CO2 prices in time of high conventional power generation.

# Dependencies

None.

# Input from file

* `OperationMode` specifies the way the prices for CO2 emission allowances are determined and can be set to `FIXED` or `DYNAMIC` (`DYNAMIC` mode is yet to be implemented)
* `Co2Prices` is an exogenously defined price time series for CO2 prices

# Input from environment

None.

# Simulation outputs

* `Co2EmissionsInTons` CO2 emissions in tons
* `Co2PriceInEURperTon` CO2 price in EUR per ton

# Contracts

* [ConventionalPlantOperator](./ConventionalPlantOperator.md) send `Co2PriceForecastRequest` and receive `Co2PriceForecast`
* [ConventionalPlantOperator](./ConventionalPlantOperator.md) send `Co2PriceRequest` and receives `Co2Price`
* [ConventionalPlantOperator](./ConventionalPlantOperator.md) send `Co2Emissions` and receive `CertificateBill`

# Available Products

* `Co2PriceForecast` forecast of Co2 price(s)
* `Co2Price` actual co2 price
* `CertificateBill` costs for ordered CO2-certificates

# Submodules

None.

# Messages

* [co2Cost](../Comms/Co2Cost.md): sent `Co2Price` & `Co2PriceForecast`
* [ClearingTimes](../Comms/ClearingTimes.md): received `Co2PriceForecastRequest` & `Co2PriceRequest`
* [AmountAtTime](../Comms/AmountAtTime.md): received `Co2Emissions` & send `CertificateBill`
* 
