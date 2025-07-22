# 42 words

The `RenewablePlantOperator` is a [PowerPlantOperator](./PowerPlantOperator.md) for renewable plants.
`RenewablePlantOperator` sends [MarginalsAtTime](../Comms/MarginalsAtTime.md) and their forecasts to an associated [AggregatorTrader](./AggregatorTrader.md), e.g. [SystemOperatorTrader](./SystemOperatorTrader.md).
The agent operates its assigned [TechnologySet](../Comms/TechnologySet.md).

# Details

Initially, the `RenewablePlantOperator` registers its [TechnologySet](../Comms/TechnologySet.md) at the [AggregatorTrader](./AggregatorTrader.md) who is in charge of marketing it.
Given the input parameters of variable costs and installed power, the `RenewablePlantOperator` sends supply marginals and their forecasts to Traders.
It is an abstract class and cannot be instantiated.
Please see below for a list of derived agents that can be instantiated.
The [MarginalCost](../Comms/MarginalsAtTime.md) are based upon the variable cost of operation and comprise any power available according to the currently installed peak capacity and the given yield profile.

**Important**: if InstalledPowerInMW is not constant, this value will be linearly interpolated between given data points in the TimeSeries leading to "non-constant" energy production capabilities.

# Dependencies

None

# Input from file

- `Set` optional name of the [TechnologySet](../Comms/TechnologySet.md) the `RenewablePlantOperator` operates (enum) - **renamed** to `PolicySet` in AMIRIS v3.0
- `EnergyCarrier` EnergyCarrier provided by this plant operator
- `SupportInstrument` specifies SupportInstrument of [SupportPolicy](./SupportPolicy.md) to be used 
- `InstalledPowerInMW` TimeSeries of total peak production capacity
- `OpexVarInEURperMWH` TimeSeries of variable cost of operation

# Input from environment

see [PowerPlantOperator](./PowerPlantOperator.md)

# Simulation outputs

see [PowerPlantOperator](./PowerPlantOperator.md)

# Contracts

* Register PolicySet at [AggregatorTrader](./AggregatorTrader.md)
* Receive GateClosureForward from Trader
* Send MarginalCostsForecasts and MarginalCosts to [RenewableTrader](./RenewableTrader.md), [SystemOperatorTrader](./SystemOperatorTrader.md) or [NoSupportTrader](./NoSupportTrader.md)
* Receive DispatchAssignment and Payout from [RenewableTrader](./RenewableTrader.md), [SystemOperatorTrader](./SystemOperatorTrader.md) or [NoSupportTrader](./NoSupportTrader.md)

Also see [PowerPlantOperator](./PowerPlantOperator.md)

# Available Products

* `SetRegistration`: covers important information of the power plant regarding support instrument eligibility

see [PowerPlantOperator](./PowerPlantOperator.md)

# Submodules

None

# Derived Agents

* [VariableRenewableOperator](./VariableRenewableOperator.md)
* [Biogas](./Biogas.md)

# Messages

* [TechnologySet](../Comms/TechnologySet.md): sent `SetRegistration` to AggregatorTrader
* [AmountAtTime](../Comms/AmountAtTime.md): also sent with `SetRegistration` to AggregatorTrader
* [MarginalsAtTime](../Comms/MarginalsAtTime.md): sent `MarginalCostForecast` and `MarginalCost` to AggregatorTrader 
* [ClearingTimes](../Comms/ClearingTimes.md): received `GateClosureForward` from Trader

Also see [PowerPlantOperator](./PowerPlantOperator.md)

# See also

* [SupportPolicy](./SupportPolicy.md)