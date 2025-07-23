# 42 words

The `VariableRenewableOperatorPpa` is a [VariableRenewableOperator](./VariableRenewableOperator.md) that can also sell its electricity directly via a Power Purchase Agreement (PPA).

# Details

VariableRenewableOperatorPpa sell their power via a Power Purchase Agreement (PPA).
Currently, electricity can be sold via PPA only to a single [GreenHydrogenTrader](./GreenHydrogenTrader.md) or [GreenHydrogenTraderMonthly](./GreenHydrogenTraderMonthly.md).

VariableRenewableOperatorPpa can also market their electricity via a type of [AggregatorTrader](./AggregatorTrader.md), but is recommended to use the VariableRenewableOperator class for this task.
PPA marketing cannot be mixed with selling electricity via an `AggregatorTrader`.

# Dependencies

see [VariableRenewableOperator](./VariableRenewableOperator.md)

# Input from file

* `PpaPriceInEURperMWH`: Price for selling power via a bilateral PPA (optional)

see [VariableRenewableOperator](./VariableRenewableOperator.md)

# Input from environment

see [VariableRenewableOperator](./VariableRenewableOperator.md)

# Simulation outputs

see [VariableRenewableOperator](./VariableRenewableOperator.md)

# Contracts

see [VariableRenewableOperator](./VariableRenewableOperator.md)

# Available Products

* [PpaInformation](../Comms/PpaInformation.md): Produced electricity and PPA-Price at a given time
* [PpaInformationForecast](../Comms/PpaInformation.md): Produced electricity and PPA-Price at a given future time 

see [VariableRenewableOperator](./VariableRenewableOperator.md)

# Submodules

None

# Derived Agents

None

# Messages

* [PpaInformation](../Comms/PpaInformation.md): sent to GreenHydrogenTrader on `PpaInformationRequest` and `PpaInformationForecastRequest`

see [VariableRenewableOperator](./VariableRenewableOperator.md)

# See also

* [VariableRenewableOperator](./VariableRenewableOperator.md)
* [AggregatorTrader](./AggregatorTrader.md)
* [GreenHydrogenOperator](./GreenHydrogenTrader.md)
* [GreenHydrogenTraderMonthly](./GreenHydrogenTraderMonthly.md)