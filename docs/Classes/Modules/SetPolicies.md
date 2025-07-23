# In Short
SetPolicies is a component of [SupportPolicy](../Agents/SupportPolicy) and holds all its [PolicyItem](./PolicyItem)s.
SetPolicies links each PolicyItem to the [TechnologySet](../Comms/TechnologySet) it applies to.
It also logs, which type of EnergyCarrier is associated with each TechnologySet.

# Details
Information is stored in EnumMaps mapping the type of support instrument to its respective PolicyItems and each TechnologySet to its respective EnergyCarrier.

# See also
* [SupportPolicy](../Agents/SupportPolicy)
* [TechnologySet](../Comms/TechnologySet)
