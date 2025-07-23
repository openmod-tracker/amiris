# In Short

This interface contracts that an Object can be translated to JSON.
It is used by [UrlModelService](./UrlModelService.md) to serialise requests.

# Details

Its only method asks to serialise the implementing object to JSON.
A default implementation using the implementing object's getter-methods is provided.
Thus, when requests of UrlModelService implement this interface, no extra code beside the getter-methods is required.
For complex request object, however, an own serialisation implementation can be implemented.

# See Also

* [UrlModelService](./UrlModelService.md)