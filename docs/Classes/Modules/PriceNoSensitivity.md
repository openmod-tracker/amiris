# In Short

PriceNoSensitivity are [MeritOrderSensitivities](./MeritOrderSensitivity.md) that store no sensitivity but only a price.

# Details

`PriceNoSensitivity` **stores no sensitivity at all**, but only a single electricity price.
Any access to sensitivity functions, e.g. "calcMonetaryValue" or "getValuesInSteps", will cause a RuntimeException and abort of the simulation.

# See also

* [MeritOrderSensitivity](./MeritOrderSensitivity.md)