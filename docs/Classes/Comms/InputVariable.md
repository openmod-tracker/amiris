# In Short

`InputVariable` is a [JSONable](../Util/JSONable.md) data class that holds a series of values associated with a time stamp.

# Details

`InputVariable` models an input or target variable of a general prediction model.
`InputVariable` has a custom implementation of the `.toJson()` method to serialise its content.

# Dependencies

* [`JSONable`](../Util/JSONable.md)

# See also

* [`PredictionRequest`](./PredictionRequest.md)
* [`JSONable`](../Util/JSONable.md)
