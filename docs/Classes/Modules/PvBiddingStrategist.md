# In Short

`PvBiddingStrategist` is a machine learning (ML) based strategist used by [HouseholdPvTraderExternal](../Agents/HouseholdPvTraderExternal.md).
It predicts optimized aggregated net load or supply for household PV using an external ML model.
The predicted load / supply can be used to create market bids.

# Details

`PvBiddingStrategist` leverages an ML-based strategy to:

- Predict the household PV load based on:
    - Electricity price forecast
    - Available charging power
    - Electricity consumption
    - Past grid interaction (bids/asks) behavior
- Generate a prediction request for a remote ML prediction service using these input variables
- Return the net load / supply prediction to the trader class, i.e., [HouseholdPvTraderExternal](../Agents/HouseholdPvTraderExternal.md).

# Dependencies

* [UrlModelService](../Util/UrlModelService.md)
* [SeriesManipulation](../Util/SeriesManipulation.md)
* [PredictionRequest](../Comms/PredictionRequest.md)
* [PredictionResponse](../Comms/PredictionResponse.md)

# Input from file

* `serviceUrl`: String - Endpoint for the ML prediction service
* `modelId`: String - Identifier for the ML model version
* `installedGenerationPowerInMW`: Integer - installed PV generation capacity
* `tsGenerationProfile`: TimeSeries - PV electricity yield profile
* `tsLoadInMW`: TimeSeries - Household own consumption 
* `storage`: Device - Battery storage
* `forecastPeriodInHours`: Integer - How many hours ahead to predict
* `predictionWindows`: Group - Configuration of time windows for features used in prediction:
    * `ElectricityPriceBackwardWindow`: Integer - Number of past electricity price values to consider
    * `ElectricityPriceForwardWindow`: Integer - Number of future electricity price values to consider
    * `ElectricityConsumptionBackwardWindow`: Integer - Number of past electricity consumption values to consider
    * `ElectricityConsumptionForwardWindow`: Integer - Number of future electricity consumption values to consider
    * `EnergyGenerationBackwardWindow`: Integer - Number of past PV generation efficiency values to consider
    * `EnergyGenerationForwardWindow`: Integer - Number of future PV generation efficiency values to consider
    * `StoredEnergyBackwardWindow`: Integer - Number of past stored energy values to consider
    * `StoredEnergyForwardWindow`: Integer - Number of future stored energy values to consider
    * `GridInteractionBackwardWindow`: Integer - Number of past grid interaction values to consider

# Outputs

* `NetLoadPredictionInMWH`: Predicted net load for a requested time
* `UpdatedLoadHistory`: Historical load updated from awarded energy data

# Messages with ML service

* [PredictionRequest](../Comms/PredictionRequest.md): Sent to external ML service
* [PredictionResponse](../Comms/PredictionResponse.md): Received from ML service

# See also

* [HouseholdPvTraderExternal](../Agents/HouseholdPvTraderExternal.md)
