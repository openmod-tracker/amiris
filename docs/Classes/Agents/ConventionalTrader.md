# 42 words

The `ConventionalTrader` is a [TraderWithClients](./TraderWithClients.md) that sells energy from [ConventionalPlantOperators](./ConventionalPlantOperator.md) at the [DayAheadMarket](./DayAheadMarket.md).
[BidsAtTime](../Comms/BidsAtTime.md) are calculated based on available capacities of its linked power plants, their [MarginalsAtTime](../Comms/MarginalsAtTime.md) and possible mark-ups or mark-downs.
Finally, the bids are forwarded to the exchange and the awarded bids get allocated to the associated power plants.

# Details

The ConventionalTrader collects the [MarginalsAtTime](../Comms/MarginalsAtTime.md) for producing electricity from its associated [ConventionalPlantOperator](./ConventionalPlantOperator.md).
It allows the consideration of mark-ups and / or mark-downs.
For this purpose, a linear interpolation between the lower bound (`minMarkup`) and upper bound (`maxMarkup`) is carried out and added to the marginal costs to formulate the final bids.
Here, the lowest (highest) marginal is tied to the lowest (highest) markup.
These bids are then sent to the `DayAheadMarket`.
After market clearing, awarded bids are received and assigned to the associated plant operator.
If the associated [ConventionalPlantOperator](./ConventionalPlantOperator.md) reported a must-run power, `ConventionalTrader` will submit the associated power at the market with at the minimal market price. 

# Dependencies

* [ConventionalPlantOperator](./ConventionalPlantOperator.md)

see also [TraderWithClients](./TraderWithClients.md)

# Input from file

* `minMarkup` is the lower bound of the interpolation of changes to the marginal costs (default=0)
* `maxMarkup` is the upper bound of the interpolation of changes to the marginal costs (default=0)

# Input from environment

see [TraderWithClients](./TraderWithClients.md)

# Simulation outputs

* `OfferedEnergyInMWH`: Amount of energy offered in MWh
* `AwardedEnergyInMWH`: Amount of energy awarded in MWh

# Contracts

see [TraderWithClients](./TraderWithClients.md)

# Available Products

see

* [TraderWithClients](./TraderWithClients.md)
* [PowerPlantScheduler](../Abilities/PowerPlantScheduler.md)

# Submodules

see [TraderWithClients](./TraderWithClients.md)

# Messages

* [MarginalCost](../Comms/MarginalsAtTime.md): `MarginalCost` and `MarginalCostForecast` received from PlantOperator(s)
* [AmountAtTime](../Comms/AmountAtTime.md): `DispatchAssignment` and `Payout` sent to RenewablePlantOperator

see [TraderWithClients](./TraderWithClients.md)

# See also

* [TraderWithClients](./TraderWithClients.md)
* [PowerPlantScheduler](../Abilities/PowerPlantScheduler.md)