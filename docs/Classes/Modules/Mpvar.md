# In Short
Mpvar is a type of [PolicyItem](./PolicyItem) holding all the [TechnologySet](../Comms/TechnologySet)-specific information for a `MPVAR` scheme, i.e. a variable market premium scheme.
Support payments equal the amount of generation eligible for support multiplied with the market premium. 
The market premium equals the difference between the levelised cost of electricity (LCOE) of the [TechnologySet](../Comms/TechnologySet) and the market value of the related energy carrier.
Market values are evaluated after accounting period.
Market premia are not negative.
Not all hours of generation may be eligible for support, depending on the number of hours with negative prices that occurred during the accounting period.

# Details
Pieces of information included are
* `Lcoe`: The lcoe of the [TechnologySet](../Comms/TechnologySet) which (by assumption) equals to its value to be applied in a variable ex post market premium scheme and given in EUR/MWh of power produced.
* `maxNumberOfNegativeHours`: The maximum number of consecutive hours with negative prices tolerated until suspending support payment

# See also
[PolicyItem](./PolicyItem)