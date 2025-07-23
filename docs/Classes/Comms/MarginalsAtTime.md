## In Short

`MarginalsAtTime` is a Portable sent to [TradersWithClients](../Agents/TraderWithClients.md).
It is a summary of multiple [Marginals](../Modules/Marginal.md) associated with the same producer (typically a [PowerPlantOperator](../Agents/PowerPlantOperator.md)) and for the same delivery time.

## Content

* `producerUuid` which power plant agent is associated with these marginals
* `deliveryTime` begin of the delivery interval
* `marginals` `Marginals` of electricity production associated with its producer

## See also

### Related Classes

* [Marginals](../Modules/Marginal.md)
