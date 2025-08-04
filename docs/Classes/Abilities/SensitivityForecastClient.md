# In short

`SensitivityForecastClient` is a client to a [SensitivityForecastProvider](./SensitivityForecastProvider.md).

# Available Products

* `ForecastRegistration`: A registration message specifying the type of forecast needed and the client's installed power
* `NetAward`: A report on the client's awarded energy at the local [DayAheadMarket](../Agents/DayAheadMarket.md) at a previous time
* `SensitivityRequest`: A request for a sensitivity forecast, specifying a time for which the forecast is required

# See also

* [SensitivityForecastProvider](./SensitivityForecastProvider.md)