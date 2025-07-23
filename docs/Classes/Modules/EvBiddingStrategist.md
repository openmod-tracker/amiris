# In Short

`EvBiddingStrategist` is a machine learning (ML) based strategist used by [EvTraderExternal](../Agents/EvTraderExternal).  
It predicts optimized aggregated net load for electric vehicles using an external ML model.
The predicted load can be used to create market bids.

# Details

`EvBiddingStrategist` leverages an ML-based strategy to:

- Predict the EV load based on:
    - Electricity price forecast
    - Available charging power
    - Electricity consumption
    - Past load behavior
- Generate a prediction request for a remote ML prediction service using these input variables
- Return the net load prediction to the trader class, i.e. [EvTraderExternal](../Agents/EvTraderExternal).

# Dependencies

* [UrlModelService](../Util/UrlModelService)
* [SeriesManipulation](../Util/SeriesManipulation)
* [PredictionRequest](../Comms/PredictionRequest)
* [PredictionResponse](../Comms/PredictionResponse)

# Input from file

* `ServiceUrl`: String - Endpoint for the ML prediction service
* `ModelId`: String - Identifier for the ML model version
* `ForecastPeriodInHours`: Integer - How many hours ahead to predict
* `AvailableChargingPowerInMW`: TimeSeries - Fleet’s available charging power
* `ElectricityConsumptionInMWH`: TimeSeries - Fleet’s electricity consumption
* `PredictionWindows`: Group - Configuration of time windows for features used in prediction:
    * `ElectricityPriceBackwardWindow`: Integer - Number of past electricity price values to consider
    * `ElectricityPriceForwardWindow`: Integer - Number of future electricity price values to consider
    * `ElectricityConsumptionForwardWindow`: Integer - Number of past electricity consumption values to consider
    * `ElectricityConsumptionBackwardWindow`: Integer - Number of future electricity consumption values to consider
    * `ChargingPowerForwardWindow`: Integer - Number of future charging power values to consider
    * `ChargingPowerBackwardWindow`: Integer - Number of past charging power values to consider
    * `LoadPredictionBackwardWindow`: Integer - Number of past load values to consider

# Outputs

* `NetLoadPredictionInMWH`: Predicted net load for a requested time
* `UpdatedLoadHistory`: Historical load updated from awarded energy data

# Messages with ML service

* [PredictionRequest](../Comms/PredictionRequest): Sent to external ML service
* [PredictionResponse](../Comms/PredictionResponse): Received from ML service

# See also

* [EvTraderExternal](../Agents/EvTraderExternal)
