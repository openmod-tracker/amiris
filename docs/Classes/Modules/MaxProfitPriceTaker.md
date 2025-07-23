# In short

`MaxProfitPriceTaker` is a type of [AssessmentFunction](./AssessmentFunction.md) that assesses the value of state transitions using an electricity price forecast.
It neglects any price impact (from own dispatch of considered [GenericDevice](./GenericDevice.md) or dispatch of other flexibility options).

# Details

`MaxProfitPriceTaker` operates on electricity price forecasts as provided by a [PriceForecaster](../Agents/PriceForecaster.md).

## Assumptions

`MaxProfitPriceTaker` interprets the provided electricity price forecasts as "perfect foresight" and does not consider any potential impact onto the price forecasts, such as from the own dispatch.
Thus, to obtain reasonable results with `MaxProfitPriceTaker`, the electricity price forecast should be adequately good, i.e. the impact of agent behaviour not included in the price forecast should be negligible.
If no price forecast is available to `MaxProfitPriceTaker`, it will assume an electricity price of 0 EUR/MWh.

## Operation

`MaxProfitPriceTaker` will assess and maximise profits from selling and buying electricity utilising the provided electricity price forecasts.
See also [SensitivityBasedAssessment](./SensitivityBasedAssessment.md)

# Input from file

`MaxProfitPriceTaker` requires `Sensitivities` of type [CostInsensitive](./CostInsensitive.md).

See [SensitivityBasedAssessment](./SensitivityBasedAssessment.md)

# Input from Environment

See [SensitivityBasedAssessment](./SensitivityBasedAssessment.md)

# See also

* [Sensitivity](../Comms/Sensitivity.md)
* [CostInsensitive](./CostInsensitive.md)
* [SensitivityBasedAssessment](./SensitivityBasedAssessment.md)
* [SensitivityForecaster](../Agents/SensitivityForecaster.md)