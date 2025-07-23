# In short

`SensitivityBasedAssessment` is an abstract type of [AssessmentFunction](./AssessmentFunction.md) that uses [Sensitivity](../Comms/Sensitivity.md) forecasts provided by a [SensitivityForecaster](../Agents/SensitivityForecaster.md) to assess state transitions within a dynamic programming scheduling algorithm applied for scheduling a [GenericDevice](./GenericDevice.md).

# Details

`SensitivityBasedAssessment` stores future `Sensitivities` to derive forecasts.
Based on these forecasts, state transitions are assessed.
Depending on the child class, the assessment can consider the feedback of the transition on the assessment, and even approximate the impact of competitors using dispatch multipliers.

## Assumptions

Dispatch multipliers are taken from the latest `Sensitivity` update provided by the `SensitivityForecaster`.
Thus, always the latest dispatch multiplier is used during assessment, even if previous `Sensitivity` messages contained a different dispatch multiplier.

See also [AssessmentFunction](./AssessmentFunction.md)

## Operations

see [AssessmentFunction](./AssessmentFunction.md)

# Input from file

see [AssessmentFunction](./AssessmentFunction.md)

# Input from Environment

* [Sensitivity](../Comms/Sensitivity.md) messages

# Child classes

* [MaxProfitPriceTaker](./MaxProfitPriceTaker.md)
* [MaxProfit](./MaxProfit.md)
* [MinSystemCost](./MinSystemCost.md)

# See also

* [AssessmentFunction](./AssessmentFunction.md)
* [Sensitivity](../Comms/Sensitivity.md)
* [SensitivityForecaster](../Agents/SensitivityForecaster.md)