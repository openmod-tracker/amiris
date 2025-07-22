# 42 words

The `VariableRenewableOperator` is a [RenewablePlantOperator](./RenewablePlantOperator.md) for renewable plants with a variable yield profile.

# Details

VariableRenewableOperator uses a yield profile to depict fluctuating maximum power production capabilities, caused by e.g. changes in wind speed, irradiation or water flows.
It operates units within a TechnologySet and a specified energy carrier (one of `PV`, `WindOn`, `WindOff`, `RunOfRiver`).
VariableRenewableOperator can market their electricity via a type of [AggregatorTrader](./AggregatorTrader.md).

# Dependencies

see [RenewablePlantOperator](./RenewablePlantOperator.md)

# Input from file

* `YieldProfile`: TimeSeries of actual power production; values are relative, where "0" represents no production possibility and "1" resembles possibility to deliver peak power (i.e. all the currently installed power).

see also [RenewablePlantOperator](./RenewablePlantOperator.md)

# Input from environment

see [RenewablePlantOperator](./RenewablePlantOperator.md)

# Simulation outputs

see [RenewablePlantOperator](./RenewablePlantOperator.md)

# Contracts

see [RenewablePlantOperator](./RenewablePlantOperator.md)

# Available Products

see also [RenewablePlantOperator](./RenewablePlantOperator.md)

# Submodules

None

# Derived Agents

* [VariableRenewableOperatorPpa](./VariableRenewableOperatorPpa.md)

# Messages

see also [RenewablePlantOperator](./RenewablePlantOperator.md)

# See also

* [RenewablePlantOperator](./RenewablePlantOperator.md)
* [AggregatorTrader](./AggregatorTrader.md)
* [GreenHydrogenOperator](./GreenHydrogenTrader.md)