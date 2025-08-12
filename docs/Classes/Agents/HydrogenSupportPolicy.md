# 42 words

HydrogenSupportPolicy administers support payments for hydrogen.
HydrogenSupportPolicy is a [HydrogenSupportProvider](../Abilities/HydrogenSupportProvider.md).
Payments are made to a [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md).
So far, only a fixed market premium (`MPFIX`) is available as support policy.

# Details

HydrogenSupportPolicy can hold the information for different support schemes parameterizations.
The [PolicyItem](../Modules/PolicyItem(Hydrogen).md) is set-specific parameterization of one of the above-mentioned support policies.

HydrogenSupportPolicy supplies a [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md) marketing the hydrogen produced with the support information it needs.

The HydrogenSupportPolicy calculates the support payout for the different sets.
It therefore receives the information on how much has been produced per hour from the [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md).
The actual support payment calculation depends on the parameterization of the [PolicyItems](../Modules/PolicyItem(Hydrogen).md).

# Dependencies

* [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)

# Input from file

* `SetSupportData`: A list of groups holding all the set-specific PolicyItem parameters; each subgroup specifies a
    * `HydrogenPolicySet`: Name of the TechnologySet that the PolicyItems apply to
    * One or multiple parameterization groups for applicable PolicyItems with their policy-specific parameterization (see [PolicyItem](../Modules/PolicyItem(Hydrogen).md))

# Input from environment

* Registration messages for support from `HydrogenSupportClient`s
* Payout requests from `HydrogenSupportClient`s

# Simulation outputs

None

# Contracts

* Send `SupportInfo` responding to a `SupportInfoRequest` from [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)
* Send `SupportPayout` responding to a `SupportPayoutRequest` from [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)

# Available Products

See [HydrogenSupportProvider](../Abilities/HydrogenSupportProvider.md)

# Submodules

* [HydrogenSupportData](../Comms/HydrogenSupportData.md)
* [PolicyItem](../Modules/PolicyItem(Hydrogen).md)

# Messages

* [HydrogenSupportData](../Comms/HydrogenSupportData.md): sent `SupportInfo` to HydrogenSupportClient
* [HydrogenPolicyRegistration](../Comms/HydrogenPolicyRegistration.md): received `SupportInfoRequest` from HydrogenSupportClient
* [AmountAtTime](../Comms/AmountAtTime.md): received from HydrogenSupportClient with `SupportPayoutRequest`, sent to HydrogenSupportClient as `SupportPayout`

# See Also

* [HydrogenSupportProvider](../Abilities/HydrogenSupportProvider.md)
* [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)
