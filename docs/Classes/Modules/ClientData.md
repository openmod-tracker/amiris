# In short
ClientData holds data attributed to a single client of an [AggregatorTrader](../Agents/AggregatorTrader), i.e. a [RenewablePlantOperator](../Agents/RenewablePlantOperator), in order to keep track of the feed-in and revenues and to administer the contractual payout of that client. 
It is not Portable, but only used locally in [AggregatorTraders](../Agents/AggregatorTrader).

# Details
ClientData comprises the following attributes:
* `technologySet`: [TechnologySet](../Comms/TechnologySet) associated with the client
* `installedCapacityInMW`: the installed capacity of the client at registration
* `yieldPotential`: TreeMap keyed by TimeStamps with the (hourly) yield potential
* `dispatch`: TreeMap keyed by TimeStamps with the assigned dispatch
* `marketRevenue`: TreeMap keyed by TimeStamps with the (hourly) market revenues based on the awards received and the assigned dispatch
* `supportRevenueInEUR`: TreeMap keyed by TimePeriods with the support revenues for the given accounting period determined by the [SupportPolicy](../Agents/SupportPolicy).
* `marketPremiaInEURPerMWH`: TreeMap keyed by TimePeriods with the market premia for the given accounting period determined by the [SupportPolicy](../Agents/SupportPolicy) and only applicable for the `MPVAR`, `CFD` or `MPFIX` support scheme.

The TreeMaps are regularly cleared in the course of the simulation once a payout by the [AggregatorTrader](../Agents/AggregatorTrader) to the respective client has been done.

# See also
* [AggregatorTrader](../Agents/AggregatorTrader)
* [SupportPolicy](../Agents/SupportPolicy)
* [RenewablePlantOperator](../Agents/RenewablePlantOperator)
* [TechnologySet](../Comms/TechnologySet)