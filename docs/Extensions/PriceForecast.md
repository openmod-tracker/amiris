## At a glance

AMIRIS-PriceForecast is an extension to the agent-based electricity market model [AMIRIS](https://helmholtz.software/software/amiris).
Specifically, it provides electricity price forecasts to the [`PriceForecasterApi`](../Classes/Agents/PriceForecasterApi.md) agent.

## Requirements
In order to use this extension to AMIRIS, add an agent to your scenario with the following (or similar) attributes:

```yaml
  - Type: PriceForecasterApi
    Id: 6
    Attributes:
      Clearing: *clearingParameters
      ServiceURL: "http://127.0.0.1:8001/forecast"  # this URL has to match AMIRIS-PriceForecast --host/-ho and --port/-po and point to /forecast
      LookBackWindowInHours: 24  # number of previous time steps are visible to the forecasting model
      ForecastPeriodInHours: 24  # number of time steps you want to forecast
      ForecastWindowExtensionInHours: 12  # number of additional time steps you want to forecast in order to reduce calls of the external forecast model 
      ForecastErrorToleranceInEURperMWH: 10  # threshold for forecasting error. if previous forecasts exceed this value, the current forecasts in the memory are discarded and a new samples is requested
      ResidualLoadInMWh: "./timeseries/residual_load.csv"  # optional additional time series for some forecast models, e.g. Transformers
 ```

The corresponding AMIRIS-PriceForecast call could be 

    amiris-priceforecast run --host 127.0.0.1 --port 8001 --model Transformer --config_path path/to/transformer_config.yaml

## Installation

We suggest to install AMIRIS-PriceForecast in your dedicated [AMIRIS Python environment](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Get-Started#install-and-run-amiris).

    pip install amiris-priceforecast

## Usage

### Suggested workflow

1. Define a scenario with a [PriceForecasterApi](../Classes/Agents/PriceForecasterApi.md) agent. Make sure that its [attributes](../Classes/Agents/PriceForecasterApi.md#input-from-file) match the settings of AMIRIS-PriceForecast.
1. Select one of the AMIRIS-PriceForecast forecast algorithms, see table below for all available models
1. Start AMIRIS-PriceForecast in your Python environment via `amiris-priceforecast run --host <your.host.adress[default: 127.0.0.1]> --port <your.port[default: 8001]> --model <ModelName>` and, if necessary, also specify `--config_path <path/to/ml_model_config.yaml>`.
1. Wait for the server to set up (takes a few seconds depending on the size of the defined model)
1. Run your AMIRIS simulation and get your results as usual
1. Optional: analyse the communication between AMIRIS and AMIRIS-PriceForecast using the `request.csv` and `response.csv` files written to your working directory.

### Command-line interface

Currently, there is one command available:

- `amiris-priceforecast run`: Runs AMIRIS-PriceForecast

#### `amiris-priceforecast run`

Starts AMIRIS-PriceForecast based on the following settings.
Awaits requests and returns responses to the AMIRIS `PriceForecasterApi` agent.

| Option                         | Action                                                                 |
|--------------------------------|------------------------------------------------------------------------|
| `--host` or `-ho`              | Provide host (default: 127.0.0.1)                                      |
| `--port` or `-po`              | Provide port (default: 8000)                                           |
| `--model` or `-m`              | Provide your model from available models (see table above)             |
| `--config_path` or `-cp`       | Provide path to your model config. Not necessary for naive models.     |
| `--log_communication` or `-lc` | If provided, all requests and responses are logged and written to disk |

### Help

You reach the help menu at any point using `-h` or `--help` which gives you a list of all available options, e.g.:

`amiris-priceforecast --help`

## Details

### Available forecast models

| Model Name     | Description                                                                                   | Approach                     | Provides Means | Provides Variances | Applied Features        | Requires model config | 
|----------------|-----------------------------------------------------------------------------------------------|------------------------------|----------------|--------------------|-------------------------|-----------------------|
| `Static`       | Static predictor always returning `0`                                                         | Naive                        | `0`            | `0`                | None                    | No                    |
| `TimeShift1`   | Predictor using past realized electricity prices as forecast                                  | Naive                        | Last Hour      | `0`                | Past Electricity Prices | No                    |                
| `TimeShift24`  | Predictor using past realized electricity prices as forecast                                  | Naive                        | Last 24 Hours  | `0`                | Past Electricity Prices | No                    |
| `TimeShift168` | Predictor using past realized electricity prices as forecast                                  | Naive                        | Last 168 Hours | `0`                | Past Electricity Prices | No                    |
| `SimpleNN`     | Basic Neural Network                                                                          | Neural Network               | Forecasted     | `0`                | Past & Future           | Yes                   |
| `Transformer`  | Temporal Fusion Transformer, based on [Lim et al. (2021)](https://arxiv.org/abs/1912.09363)   | Neural Network (Transformer) | Forecasted     | Can provide skew   | Past & Future           | Yes                   |

Please find specific templates for the respective model configs below.

#### `SimpleNN`

This simple neural network requires the following configuration:

```yaml
name: SimpleNN
batch_size: 1024
epochs: 200
hidden_units: 70
features:
  - column_name: "AwardedEnergyInMWH_Exchange"
    past_lags: [3, 2, 1,]
  - column_name: "AwardedEnergyInMWH_Storage"
    future_lags: [0, 1, 2,]
  - column_name: "ElectricityPriceInEURperMWH"
    past_lags: [3, 2, 1,]
targets:
  - column_name: "ElectricityPriceInEURperMWH"
learning_rate: 0.01
forecast_window: 24
weight_decay: 0.001
```

#### `Transformer`

This implements a [Temporal Fusion Transformer](https://doi.org/10.1016/j.ijforecast.2021.03.012) from the `darts` library.
See the official [documentation](https://unit8co.github.io/darts/generated_api/darts.models.forecasting.tft_model.html) for the possible arguments.

It can be called multiple times predicting `n_forecast` samples of the predicted target time series.
These can be interpreted as probabilistic forecasts.
Additionally, `ForecastApiResponse.forecastVariances` is provided with [`skew`](https://en.wikipedia.org/wiki/Skewness) when `n_forecast > 1`.

```yaml
name: Transformer
epochs: 3
targets:
  - column_name: ElectricityPriceInEURperMWH
features:
  - column_name: ElectricityPriceInEURperMWH
    past_lags: [24, 12, 1]
  - column_name: AwardedEnergyInMWH_Exchange
    future_lags: [24, 12, 1]
forecast_window: 24
n_forecast: 3  # optional parameter specifying number of forecasts to generate (default: 1). if greater than 1 (default), `skew` is provided in `ForecastApiResponse.forecastVariances` 
kwargs:  # optional kwargs for darts TFT implementation
  add_relative_index: True
  lstm_layers: 1
```

## Citing

If you use AMIRIS-PriceForecast in an academic context please cite [doi: 10.5281/zenodo.14907870](https://doi.org/10.5281/zenodo.14907870) and [doi: 10.21105/joss.05041](https://doi.org/10.21105/joss.05041).
In other contexts, please include a link to our repositories [AMIRIS-PriceForecast](https://gitlab.com/dlr-ve/esy/amiris/extensions/priceforecast) and [AMIRIS](https://gitlab.com/dlr-ve/esy/amiris/amiris).

## Links

- [Repository](https://gitlab.com/dlr-ve/esy/amiris/extensions/priceforecast)
