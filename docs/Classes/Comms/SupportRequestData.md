# In Short

Informs the [SupportPolicy](../Agents/SupportPolicy.md) about the energy infeed a certain [TechnologySet](./TechnologySet.md) marketed within the accounting period.
For a capacity premium, it informs about the installed capacity eligible for a capacity premium payment in an accounting period.

# Details

Contained information:

* `clientId` UUID of the power plant operator connected to this support request
* `setType` the name of the [TechnologySet](./TechnologySet.md) (enum)
* `supportInstrument` the support instrument applied for the TechnologySet
* `accountingPeriod` the accounting period (TimeSegment) for which the support is to be paid
* `infeed` the actual infeed of the given set under the connected support instrument
* `installedCapacityInMW` the installed capacity relevant for capacity-based support

Given a [ClientData](../Modules/ClientData.md) object, SupportRequestData can also calculate the overall feed-in of a client in a given accounting period.
This is used to initialise the `amount` for energy-based support schemes.

# See also

None