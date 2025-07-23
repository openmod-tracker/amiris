# In short

`MaxProfit` is a type of [SensitivityBasedAssessment](./SensitivityBasedAssessment.md) that maximises the profits of state transitions using a merit order forecast.
It takes into account the price impact from the own dispatch of the considered [GenericDevice](./GenericDevice.md), and can approximate that of other flexibility options.

# Details

`MaxProfit` operates on cost-based [Sensitivity](../Comms/Sensitivity.md) provided by a [SensitivityForecaster](../Agents/SensitivityForecaster.md).
This type of `Sensitivity` utilises knowledge of the merit order to estimate the impact of the own dispatch (and that of the competitors) on the market price and thus the costs and profits.
Thus, this assessment will try to utilise whatever market power it has to maximise its profits.

## Assumptions

To model competition, all competing flexibility options should register to the same `SensitivityForecaster` and also send it their energy amounts awarded at the corresponding [DayAheadMarket](../Agents/DayAheadMarket.md).

## Operation

`MaxProfit` will assess and maximise profits from selling and buying electricity utilising the provided sensitivity forecasts.

See also [SensitivityBasedAssessment](./SensitivityBasedAssessment.md)

# Input from file

See [SensitivityBasedAssessment](./SensitivityBasedAssessment.md)

# Input from Environment

`MaxProfit` requires `Sensitivities` of type [CostSensitivity](./CostSensitivity.md).

See also [SensitivityBasedAssessment](./SensitivityBasedAssessment.md)

# See also

* [Sensitivity](../Comms/Sensitivity.md)
* [CostSensitivity](./CostSensitivity.md)
* [SensitivityBasedAssessment](./SensitivityBasedAssessment.md)
* [SensitivityForecaster](../Agents/SensitivityForecaster.md)