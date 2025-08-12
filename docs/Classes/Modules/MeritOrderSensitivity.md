# In Short

`MeritOrderSensitivities` represents changes of a merit-order derived value (e.g. electricity price or system cost) when the awarded power for supply or demand are changed, e.g., due to a modification of the merit order by (dis-)charging a storage device.

# Details

Abstract base class for specialised sensitivities representing a specific aspect (e.g. electricity price) to change when the merit order is changed in demand or supply.
`MeritOrderSensitivities` contain [SensitivityItems](./SensitivityItem.md) for charging and discharging.
`MeritOrderSensitivities` are used to optimise dispatch strategies of flexibility options, i.e. in [ArbitrageStrategists](./ArbitrageStrategist.md).

# Subclasses
* [MarginalCostSensitivity](./MarginalCostSensitivity.md): Represents changes in marginal cost of the awarded merit order items
* [PriceSensitivity](./PriceSensitivity.md): Represents changes in the wholesale electricity price
* [PriceNoSensitivity](./PriceNoSensitivity.md): No representation of changes, but stores only one electricity price

# See als
* [SensitivityItems](./SensitivityItem.md)
* [ArbitrageStrategist](./ArbitrageStrategist.md)