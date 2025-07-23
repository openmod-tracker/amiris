# In short

`SensitivityBasedAssessment` is an abstract type of [AssessmentFunction](./AssessmentFunction) that uses [Sensitivity](../Comms/Sensitivity) forecasts provided by a [SensitivityForecaster](../Agents/SensitivityForecaster) to assess state transitions within a dynamic programming scheduling algorithm applied for scheduling a [GenericDevice](./GenericDevice).

# Details

`SensitivityBasedAssessment` stores future `Sensitivities` to derive forecasts.
Based on these forecasts, state transitions are assessed.
Depending on the child class, the assessment can consider the feedback of the transition on the assessment, and even approximate the impact of competitors using dispatch multipliers.

## Assumptions

Dispatch multipliers are taken from the latest `Sensitivity` update provided by the `SensitivityForecaster`.
Thus, always the latest dispatch multiplier is used during assessment, even if previous `Sensitivity` messages contained a different dispatch multiplier.

See also [AssessmentFunction](./AssessmentFunction)

## Operations

see [AssessmentFunction](./AssessmentFunction)

# Input from file

see [AssessmentFunction](./AssessmentFunction)

# Input from Environment

* [Sensitivity](../Comms/Sensitivity) messages

# Child classes

* [MaxProfitPriceTaker](./MaxProfitPriceTaker)
* [MaxProfit](./MaxProfit)
* [MinSystemCost](./MinSystemCost)

# See also

* [AssessmentFunction](./AssessmentFunction)
* [Sensitivity](../Comms/Sensitivity)
* [SensitivityForecaster](../Agents/SensitivityForecaster)