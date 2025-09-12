## In Short

A pair of power-production potential with associated marginal costs for production created by [PowerPlantOperators](../Agents/PowerPlantOperator.md).
These are not sent out individually by the `PowerPlantOperators`, but summarized in a single [MarginalsAtTime](../Comms/MarginalsAtTime.md) message.

## Content

* `powerPotentialInMW` the actual net electricity production potential of the power plant
* `marginalCostInEURperMWH` the actual marginal cost for electricity production 

## See also

### Related Classes

* [PowerPlantOperator](../Agents/PowerPlantOperator.md)
* [MarginalsAtTime](../Comms/MarginalsAtTime.md)
