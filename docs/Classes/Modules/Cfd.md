# In Short
Cfd is a type of [PolicyItem](./PolicyItem) holding all the [TechnologySet](../Comms/TechnologySet)-specific information for a `CfD` scheme, i.e. a two-sided contracts for differences scheme building on a variable market premium scheme.
Support payments equal the amount of generation eligible for support multiplied with the market premium.
The market premium equals the difference between the levelised cost of electricity (LCOE) of the [TechnologySet](../Comms/TechnologySet) and the market value of the related energy carrier.
Market values are evaluated after accounting period.
Market premia may be negative and then correspond to payments by the contracted [AggregatorTrader](../Agents/AggregatorTrader).
Not all hours of generation may be eligible for support, depending on the number of hours with negative prices that occurred during the accounting period.

# Input from file
Included parameters are
* `Lcoe`: The lcoe of the [TechnologySet](../Comms/TechnologySet) which (by assumption) equals to its value to be applied in a variable ex post market premium scheme and given in EUR/MWh of power produced.
* `maxNumberOfNegativeHours`: The maximum number of consecutive hours with negative prices tolerated until support payment is suspended.

# See also
[PolicyItem](./PolicyItem)