# In Short

`PredictionRequest` encapsulates a prediction request for a general prediction model.

# Details

`PredictionRequest` is designed primarily for time series prediction, where each variable's value is associated with a time reference (or an ordered index).  
A `PredictionRequest` object packages all information needed to call an external prediction service:

- `modelId`: Identifier of the prediction model to use
- `predictionStart`: Time step (or index) at which to begin prediction
- `inputVars`: A list of `InputVariable` instances, each containing a named time series

The class is JSONable, i.e., implements the `.toJson()` method of the `JSONable` interface for custom serialization to `JSONObject`.

# Dependencies

* [`JSONable`](../Util/JSONable.md)
* [`InputVariable`](../Comms/InputVariable.md)

# See also

* [`InputVariable`](../Comms/InputVariable.md)
* [`PredictionResponse`](../Comms/PredictionResponse.md)
