# In Short

`ForecastApiRequest` holds relevant information for an external forecast model creating a forecast at runtime.

# Details

The [PriceForecasterApi](../Agents/PriceForecasterApi.md) sends a `ForecastApiRequest` including

* `forecastStartTime`: start time of forecast
* `forecastWindow`: window of forecast
* `pastTargets`: realized electricity prices with time steps
* `residualLoad`: residual load with time steps
