# In short

`MinSystemCost` is a type of [AssessmentFunction](./AssessmentFunction.md) that minimises the total system cost due to its transitions using a merit order forecast.
It takes into account the impact of the dispatch of the considered [GenericDevice](./GenericDevice.md) onto the system costs and can approximate that of other flexibility options.

# Details

operates on cost-based [Sensitivity](../Comms/Sensitivity.md) provided by a [SensitivityForecaster](../Agents/SensitivityForecaster.md).
This type of `Sensitivity` utilises knowledge of the merit order to estimate the impact of the own dispatch (and that of the competitors) on marginal costs and uses whatever market power it has to minimise the total system cost.
Its own profits are disregarded.

## Assumptions

To model competition, all competing flexibility options should register to the same `SensitivityForecaster` and also send it their energy amounts awarded at the corresponding [DayAheadMarket](../Agents/DayAheadMarket.md).

## Operation

`MinSystemCost` operates on the "true" marginal cost of bids within the merit order and ignore the actual prices.
It will assess and minimise total system costs from selling and buying electricity utilising the provided merit order forecasts.

See also [SensitivityBasedAssessment](./SensitivityBasedAssessment.md)

# Input from file

See [SensitivityBasedAssessment](./SensitivityBasedAssessment.md)

# Input from Environment

`MinSystemCost` requires `Sensitivities` of type [MarginalCostSensitivity](./MarginalCostSensitivity(Forecast).md).

See [SensitivityBasedAssessment](./SensitivityBasedAssessment.md)

# See also

* [Sensitivity](../Comms/Sensitivity.md)
* [MarginalCostSensitivity](./MarginalCostSensitivity(Forecast).md)
* [SensitivityBasedAssessment](./SensitivityBasedAssessment.md)
* [SensitivityForecaster](../Agents/SensitivityForecaster.md)