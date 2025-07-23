# In short

`MaxProfitPriceTaker` is a type of [AssessmentFunction](./AssessmentFunction) that assesses the value of state transitions using an electricity price forecast.
It neglects any price impact (from own dispatch of considered [GenericDevice](./GenericDevice) or dispatch of other flexibility options).

# Details

`MaxProfitPriceTaker` operates on electricity price forecasts as provided by a [PriceForecaster](../Agents/PriceForecaster).

## Assumptions

`MaxProfitPriceTaker` interprets the provided electricity price forecasts as "perfect foresight" and does not consider any potential impact onto the price forecasts, such as from the own dispatch.
Thus, to obtain reasonable results with `MaxProfitPriceTaker`, the electricity price forecast should be adequately good, i.e. the impact of agent behaviour not included in the price forecast should be negligible.
If no price forecast is available to `MaxProfitPriceTaker`, it will assume an electricity price of 0 EUR/MWh.

## Operation

`MaxProfitPriceTaker` will assess and maximise profits from selling and buying electricity utilising the provided electricity price forecasts.
See also [SensitivityBasedAssessment](./SensitivityBasedAssessment)

# Input from file

`MaxProfitPriceTaker` requires `Sensitivities` of type [CostInsensitive](./CostInsensitive).

See [SensitivityBasedAssessment](./SensitivityBasedAssessment)

# Input from Environment

See [SensitivityBasedAssessment](./SensitivityBasedAssessment)

# See also

* [Sensitivity](../Comms/Sensitivity)
* [CostInsensitive](./CostInsensitive)
* [SensitivityBasedAssessment](./SensitivityBasedAssessment)
* [SensitivityForecaster](../Agents/SensitivityForecaster)