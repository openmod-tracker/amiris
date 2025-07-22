# 42 words

A type of [MarketForecaster](./MarketForecaster) and [SensitivityForecastProvider](../Abilities/SensitivityForecastProvider) for the day-ahead electricity market.
It provides electricity price forecasts to connected agents.
These price forecasts are compiled by external models considered as "extensions" to AMIRIS namely [AMIRIS-PriceForecast](https://gitlab.com/dlr-ve/esy/amiris/extensions/priceforecast).
To reduce the frequency of API calls, `PriceForecasterApi` may request additional time steps of forecasts from the API that extend beyond the specified forecast period.

# Details

Communication between Java and Python modules is facilitated through the use of the [UrlModelService](../Util/UrlModelService).

## Available Forecasting Models

At present, four naive forecasting methods are available at the `amiris-priceforecaster`: a StaticPredictor and three variants of a TimeShiftPredictor.
Further, a `SimpleNN` as well as a `Temporal Fusion Transformer` are available.
However, the prediction model cannot be selected by the AMIRIS agent but has to be selected by the modeller when starting `amiris-priceforecaster`.

## Forecast Window Extension

While calling the api on the local computer adds only a moderate overhead of a few seconds to the simulation time, calculating price predictions via a machine learning (ML) model is likely to take some time and might be a bottleneck for the simulation progress.
`ForecastWindowExtensionInHours` should be __set to 0 for naive predictors__, since otherwise an inappropriate time might occur in the predictions.

`ForecastWindowExtensionInHours` should only be set to non-zero values for ML-based predictors.
In order to reduce the number of calls to ML prediction models, `PriceForecasterApi` can ask for number of forecasts "extended" by a given window.
This additional forecasts are stored and can be sent to the forecast clients at one of their next calls to the forecaster.
Thus, to reduce the number of calls to the prediction model by a factor of e+1, add a `ForecastWindowExtensionInHours` of e.
Mind that this can reduce the forecast precision though as the prediction model needs to predict values further away in time.

## Forecast Error Tolerance

Since forecasts created from the `amiris-priceforecaster` may vary in quality and may also have less quality if referring to more distant future times, quality of the forecasts may not be sufficient for the simulation.
Thus, if the error of a previous forecast exceeds the configured forecast error tolerance, a new call to the remote forecasting model is issued to update the forecast and *hopefully* provide better forecasts.
Again, this is only useful for ML-based predictors, and should __not be used for naive predictors__.

## Forecasting Types

`PriceForecasterApi` can provide `MeritOrderForecasts` (inherited from [MarketForecaster](./MarketForecaster)), `PriceForecasts`, and `SensitivityForecasts`.
For sake of clarity, it is recommended to **not use it for `MeritOrderForecasts`** - instead, add a `MarketForecaster`.

`PriceForecasts` and `SensitivityForecasts` are both based on the external prediction service.
It is not possible to obtain `PriceForecasts` that are created from the pre-timed market clearing mechanism employed by `MarketForecaster`.
For `SensitivityForecasts`, only the subtype `CostInsensitive` is available, thus, ensure to have a compatible [AssessmentFunction](../Modules/AssessmentFunction) selected.

# Dependencies

An externally running `amiris-priceforecaster` service endpoint.

# Input from file

* `ServiceURL`: A URL pointing to an external `amiris-priceforecaster` service whose prediction model actually calculates the forecasts
* `LookBackWindowInHours`: optional (default=`ForecastPeriodInHours`); Number of time steps sent to the prediction model for past data, e.g., previous electricity prices
* `ForecastWindowExtensionInHours`: optional (default=0); number of time steps (in addition to the `ForecastPeriodInHours`) of the forecast requested from the remote prediction model
* `ForecastErrorToleranceInEURperMWH`: optional (default=-1); maximum tolerance for deviations between forecasted and realized electricity prices. If tolerance is exceeded, a new prediction is obtained from the remote model. Thus, small tolerances may result in many API calls. If set to negative values, no error checks are performed.
* `ResidualLoadInMWh`: optional; Load time series derived from total electricity demand minus all renewable energy supply

see also [MarketForecaster](./MarketForecaster)

# Input from environment

* `PriceForecastRequest` from FlexibilityTraders

see also [MarketForecaster](./MarketForecaster)

# Simulation outputs

* `ElectricityPriceForecastInEURperMWH`: The forecasted value for the electricity price.
* `ElectricityPriceForecastVarianceInEURperMWH`: Variance of forecasted electricity price in EUR per MWh.

# Contracts

* `SensitivityForecastClient`: receive `SensitivityRequest`s
* `SensitivityForecastClient`: send `SensitivityForecast`s of type `CostInsensitive`

`PriceForecasterApi` can tolerate all messages from [SensitivityForecastClients](../Abilities/SensitivityForecastClient), although the `ForecastRegistration` and `NetAward` messages are not required.
This allows easy switching between `PriceForecasterApi` and [SensitivityForecaster](./SensitivityForecaster), without the need to change contracts.

see also [MarketForecaster](./MarketForecaster)

# Available Products

see [MarketForecaster](./MarketForecaster) and [SensitivityForecastProvider](../Abilities/SensitivityForecastProvider)

# Submodules

* [ForecastApiRequest](../Modules/ForecastApiRequest): Requests sent to the external `amiris-priceforecaster` prediction model
* [ForecastApiResponse](../Modules/ForecastApiResponse): Responses received from the external `amiris-priceforecaster` prediction model
* [CostInsensitive](../Modules/CostInsensitive)

# Messages

* [AmountAtTime](../Comms/AmountAtTime) as PriceForecast
* [Sensitivity](../Comms/Sensitivity) as SensitivityForecast

# See also

* [MarketForecaster](./MarketForecaster)
* [SensitivityForecastProvider](../Abilities/SensitivityForecastProvider)
* [SensitivityForecastClient](../Abilities/SensitivityForecastClient)
* [PriceForecaster](./PriceForecaster)
* [FlexibilityTrader](./FlexibilityTrader)
* [ForecastApiRequest](../Modules/ForecastApiRequest)
* [ForecastApiResponse](../Modules/ForecastApiResponse)
* [CostInsensitive](../Modules/CostInsensitive)
* [AssessmentFunction](../Modules/AssessmentFunction)
* [SensitivityForecaster](./SensitivityForecaster)