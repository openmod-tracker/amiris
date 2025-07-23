# 42 words

An abstract class for [TraderWithClients](./TraderWithClients.md) who aggregate supply energy and sell it at the [DayAheadMarket](./DayAheadMarket.md).
The `AggregatorTrader` receives support payments from the [SupportPolicy](./SupportPolicy.md) agent and passes it to its clients, i.e. [RenewablePlantOperators](./RenewablePlantOperator.md), based on a given distribution logic.

# Details

`AggregatorTrader` itself is an abstract class that cannot be instantiated.
It registers clients, i.e. [RenewablePlantOperators](./RenewablePlantOperator.md) with a specific [TechnologySets](../Comms/TechnologySet.md) and markets their power generation using the logic defined in its child classes.
`AggregatorTrader` assigns the dispatch for the [TechnologySets](../Comms/TechnologySet.md) in its portfolio based on the awards received and based on increasing bid prices.
If clients have a similar bid price and are to be partially curtailed (based on the market result), `AggregatorTrader` assigns them an equal share of their power potential.

`AggregatorTrader` communicates with a [SupportPolicy](./SupportPolicy.md) agent in order to receive support revenues for its clients.
To do so, it first requests support information from the `SupportPolicy`.
Then, at the end of each accounting period, it requests a support payout from the same `SupportPolicy`.
Finally, it distributes the payments received, consisting of market revenues from the [DayAheadMarket](./DayAheadMarket.md) and support revenues from the `SupportPolicy` to its clients.
It may keep a certain share of revenues to cover for its costs and its margin (see the child classes).

The data to keep track of for billing its clients is stored in a client map structure, holding [ClientData](../Modules/ClientData.md) objects.

# Dependencies

* [RenewablePlantOperator](./RenewablePlantOperator.md)
* [SupportPolicy](./SupportPolicy.md)

see also [TraderWithClients](./TraderWithClients.md)

# Input from file

* `ForecastError`: Group holding power forecasting error information for the yield potentials
    * `Mean`: The normalised mean value of a normally distributed error (positive value means an overestimation of feed-in potentials)
    * `Variance`: The normalised variance of a normally distributed error

# Input from environment

* `SetRegistration` from RenewablePlantOperators
* `SupportPayout` from SupportPolicy
* `SupportInfo` from SupportPolicy

see [TraderWithClients](./TraderWithClients.md)

# Simulation outputs

* `ReceivedSupportInEUR`: Overall received support payments from policy agent in EUR
* `RefundedSupportInEUR`: Overall support refunded to policy agent (in CFD scheme) in EUR
* `ReceivedMarketRevenues`: Overall received market revenues from marketing power plants in EUR
* `TrueGenerationPotentialInMWH`: Actual electricity generation potential in MWh

see also [TraderWithClients](./TraderWithClients.md)

# Contracts

* [RenewablePlantOperator](./RenewablePlantOperator.md) sends `SetRegistration`, `MarginalCost` and receives `DispatchAssignment`, `Payout`
* [SupportPolicy](./SupportPolicy.md) sends `SupportInfo` and `SupportPayout` and receives `SupportInfoRequest`, `SupportPayoutRequest` and `YieldPotential`

See also [TraderWithClients](./TraderWithClients.md)

# Available Products

* `SupportInfoRequest`: Request for support information for contracted technology set(s)
* `SupportPayoutRequest`: Request to obtain support payments for contracted technology set(s)
* `YieldPotential`: Yield potential of contracted technology set(s)

See also

* [TraderWithClients](./TraderWithClients.md)
* [PowerPlantScheduler](../Abilities/PowerPlantScheduler.md)

# Submodules

* [ClientData](../Modules/ClientData.md)

# Messages

* [TechnologySet](../Comms/TechnologySet.md): received `SetRegistration` from RenewablePlantOperator & sent with `SupportInfoRequest` to SupportPolicy
* [AmountAtTime](../Comms/AmountAtTime.md): also received with `SetRegistration` from RenewablePlantOperator
* [SupportData](../Comms/SupportData.md): received `SupportInfo` from SupportPolicy
* [YieldPotential](../Comms/YieldPotential.md): sent on `YieldPotential` to SupportPolicy
* [AmountAtTime](../Comms/AmountAtTime.md): `DispatchAssignment` and `Payout` sent to RenewablePlantOperator
* [SupportRequestData](../Comms/SupportRequestData.md): sent `SupportPayoutRequest` to SupportPolicy
* [SupportResponseData](../Comms/SupportResponseData.md): received on `SupportPayout` from SupportPolicy
* [MarginalsAtTime](../Comms/MarginalsAtTime.md): `MarginalCost` and `MarginalCostForecast` received from PlantOperator(s)

See also [TraderWithClients](./TraderWithClients.md)

# Child classes

* [SystemOperatorTrader](./SystemOperatorTrader.md)
* [RenewableTrader](./RenewableTrader.md)
* [NoSupportTrader](./NoSupportTrader.md)

# See also

* [TraderWithClients](./TraderWithClients.md)
* [PowerPlantScheduler](../Abilities/PowerPlantScheduler.md)
* [SupportPolicy](./SupportPolicy.md)