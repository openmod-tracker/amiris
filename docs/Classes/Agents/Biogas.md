# 42 words

The Biogas Agent is a renewable [RenewablePlantOperator](./RenewablePlantOperator.md).
It burns biogas produced in local bioreactors using combustion engines to generate electricity.
The plant is assumed to have either a constant electricity output, or to have flexible electricity generation utilising short-term biogas buffer storages.

# Details

## Strategies

There are 3 OperationModes available for the Biogas agent.
Common to most modes is the base power output according to the currently InstalledPowerInMW scaled by `FullLoadHoursPerYear` / 8760.
This scaling factor is not applied in the `FROM_FILE` mode, however.

### CONTINUOUS

Biogas offers (the more or less) constant base power output to its associated Trader, e.g. [SystemOperatorTrader](./SystemOperatorTrader.md).

### DAY_NIGHT

The Agent offers 50% of its base power output at night (from 19h to 6h) and 150% at daylight (from 7h to 18h).

### FROM_FILE

Biogas offers power according to the specified DispatchTimeSeries.
This TimeSeries needs to contain values between 0 and 1 reflecting the power output relative to the installed power.
Here, the `FullLoadHoursPerYear` are *not* applied as a scaling factor.

# Dependencies

none

# Input from file

see [RenewablePlantOperator](./RenewablePlantOperator.md)

* `OperationMode`: see [Strategies](#strategies) above
* `FullLoadHoursPerYear`: determines scaling factor (by dividing with 8760) applied to output power for all strategies except `FROM_FILE`
* `DispatchTimeSeries`: only used in mode `FROM_FILE`: determines the available power output by multiplication with `InstalledPowerInMW`

# Input from environment

see [RenewablePlantOperator](./RenewablePlantOperator.md)

# Simulation outputs

see [RenewablePlantOperator](./RenewablePlantOperator.md)

# Contracts

see [RenewablePlantOperator](./RenewablePlantOperator.md)

## Receiver

see [RenewablePlantOperator](./RenewablePlantOperator.md)

## Sender

see [RenewablePlantOperator](./RenewablePlantOperator.md)

# Available Products

see [RenewablePlantOperator](./RenewablePlantOperator.md)

# Submodules

none

# Messages

see [RenewablePlantOperator](./RenewablePlantOperator.md)