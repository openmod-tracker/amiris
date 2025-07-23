# 42 words

SupportPolicy administers support payments for renewable generators.
Payments are made to an [AggregatorTrader](./AggregatorTrader.md) who passes them to a [RenewablePlantOperator](./RenewablePlantOperator.md).
Available support policies are

* feed-in tariff (`FIT`), 
* variable market premium (`MPVAR`), 
* fixed market premium (`MPFIX`), 
* contracts for differences (`CFD`), 
* financial contracts for differences (`FINANCIAL_CFD`),
* and capacity premium (`CP`).

# Details

SupportPolicy holds all the information for the parameterization of different support schemes.
The [PolicyItem](../Modules/PolicyItem.md) is [TechnologySet](../Comms/TechnologySet.md)-specific parameterization of one of the above-mentioned support policies.

SupportPolicy supplies an [AggregatorTraders](./AggregatorTrader.md) marketing the RES generation with the support information it needs.
The agent keeps track of the yield potentials by RES energy carrier and calculates monthly market values from it with the power price information from the [DayAheadMarket](./DayAheadMarket.md).

The support policy calculates the support payout for the different [TechnologySets](../Comms/TechnologySet.md). 
It therefore receives the information on how much has been produced and what the accounting period has been from the [AggregatorTraders](./AggregatorTrader.md).
For the case of a capacity premium, it uses the information on the installed capacity instead.
The actual support payment calculation depends on the parameterization of the [PolicyItems](../Modules/PolicyItem.md).

SupportPolicy also defines the type of EnergyCarriers that are eligible for policy support.

# Dependencies

* [DayAheadMarket](./DayAheadMarket.md)
* [AggregatorTrader](./AggregatorTrader.md)

# Input from file

* `SetSupportData`: A list of groups holding all the [TechnologySet](../Comms/TechnologySet.md)-specific PolicyItem parameters; each subgroup specifies a
  * `Set`: Name of the TechnologySet that the PolicyItems apply to  - **renamed** to `PolicySet` in AMIRIS v3.0
  * One or multiple parameterization groups for applicable PolicyItems named either `FIT`, `MPFIX` ,`MPVAR`, `CFD`, `FINANCIAL_CFD` or `CP` with their policy-specific parameterization (see [PolicyItem](../Modules/PolicyItem.md))

# Input from environment

* Power prices from [DayAheadMarket](./DayAheadMarket.md)
* Yield potentials from [AggregatorTrader](./AggregatorTrader.md)

# Simulation outputs

* `MarketValueInEURperMWH` complex output; market values per energy carrier and evaluation interval

# Contracts

* Send `SupportInfo` responding to a `SupportInfoRequest` from [AggregatorTrader](./AggregatorTrader.md)
* Send `SupportPayout` responding to a `SupportPayoutRequest` from [AggregatorTrader](./AggregatorTrader.md)
* Perform `MarketValueCalculation` - based on contact with any Agent triggering that
* Receive `Awards` from [DayAheadMarket](./DayAheadMarket.md)
* Receive `YieldPotential` from [AggregatorTrader](./AggregatorTrader.md) - required for calculation of market values

# Available Products

* `SupportInfo`: Info on the support scheme to be applied to a set of plants
* `SupportPayout`: Actual pay-out of the support
* `MarketValueCalculation`: Trigger for market value calculation - not send to anyone

# Submodules

* [SupportData](../Comms/SupportData.md)
* [PolicyItem](../Modules/PolicyItem.md)
* [SetPolicies](../Modules/SetPolicies.md)
* [MarketData](../Modules/MarketData.md)

# Messages

* [SupportData](../Comms/SupportData.md): sent `SupportInfo` to AggregatorTrader
* [TechnologySet](../Comms/TechnologySet.md): received `SupportInfoRequest` from AggregatorTrader
* [YieldPotential](../Comms/YieldPotential.md): received `YieldPotential` from AggregatorTrader
* [AwardData](../Comms/AwardData.md): received `Awards` from DayAheadMarket
* [SupportRequestData](../Comms/SupportRequestData.md): received from AggregatorTrader with `SupportPayoutRequest`
* [SupportDataResponse](../Comms/SupportResponseData.md): sent to AggregatorTrader as `SupportPayout`

# See Also

* [AggregatorTrader](./AggregatorTrader.md)
