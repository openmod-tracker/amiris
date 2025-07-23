# In Short

`ForecastApiResponse` holds relevant information from an external forecast model providing a forecast at runtime.

# Details

The [PriceForecasterApi](../Agents/PriceForecasterApi.md) receives a `ForecastApiResponse` including

* `forecastMeans`: mean values of the forecast target
* `forecastVariances`: variances of the forecast target

Depending on the applied forecast model, `ForecastApiResponse` can contain multiple `forecastMeans` and `forecastVariances` enabling the consideration of probabilistic forecast strategies.
