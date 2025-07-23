# 42 words

`PowerPlantOperator`s are Agents that generate electricity - be it conventionally or from renewable sources.
They supply connected [TraderWithClients](./TraderWithClients.md) with [Marginal](../Modules/Marginal.md) cost information.
These Traders then send dispatch assignments after market clearing, specifying how much electricity needs to be generated.

# Details

`PowerPlantOperator` is abstract and cannot be instantiated.
Instead, PowerPlantOperator provides relevant Products for derived [renewable](./RenewablePlantOperator.md) and [conventional](./ConventionalPlantOperator.md) plant operators.
The base class `PowerPlantOperator` organises the communication with associated Traders for its child classes.
Action definition of MarginalCost send-out and their calculation are handled in child classes, since conventional / renewable plant operators need to process different kinds of incoming data.

# Dependencies

None

# Input from file

see derived agents and [AnnualCostCalculator](../Modules/AnnualCostCalculator.md)

# Input from environment

* `DispatchAssignment`s from associated TraderWithClients

Also see derived agents.

# Simulation outputs

* `AwardedEnergyInMWH` Awarded energy in MWh
* `OfferedEnergyInMWH` Offered energy in MWh
* `ReceivedMoneyInEUR` Received money in EUR
* `VariableCostsInEUR` Variable cost in EUR
* `FixedCostsInEUR` fixed operation and maintenance cost in EUR
* `InvestmentAnnuityInEUR` annuity of invest cost over expected lifetime in EUR

# Contracts

* TraderWithClients send DispatchAssignment and Payout data.

Also see derived agents.

# Available Products

* `MarginalCost` the marginal cost(s) of the power plant(s) - associated actions defined in child classes.
* `MarginalCostForecast` a forecast for the marginal cost(s) of the power plant(s) - associated actions defined in child classes.
* `AnnualCostReport` report annual costs (not sent to other agents, but used for internal calculations)

Also see derived agents.

# Submodules

None, see derived agents

# Messages

* [AmountAtTime](../Comms/AmountAtTime.md): received `Payout` and `DispatchAssignment` from TraderWithClients(s)
* [MarginalsAtTime](../Comms/MarginalsAtTime.md): sent `MarginalCost` and `MarginalCostForecast` to associated Trader

Also see derived agents.

# See also

## Derived Agents

* [ConventionalPlantOperator](./ConventionalPlantOperator.md)
* [RenewablePlantOperator](./RenewablePlantOperator.md)

## Related Classes

* [TraderWithClients](./TraderWithClients.md)
