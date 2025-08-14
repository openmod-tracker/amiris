# In short

`HydrogenSupportProvider` represents the Ability to provide support payments for hydrogen.

# Details

`HydrogenSupportProvider` offers Products concerning hydrogen support administration.
It delivers payouts to registered `HydrogenSupportClient`s. 

# Available Products

* `SupportInfo`: Info on the support scheme to be applied to a set of clients
* `SupportPayout`: Actual support payout

# Messages

* [HydrogenPolicyRegistration](../Comms/HydrogenPolicyRegistration.md) received `SupportInfoRequest` from HydrogenSupportClient
* [HydrogenSupportData](../Comms/HydrogenSupportData.md) sent `SupportInfo` to HydrogenSupportClient
* [AmountAtTime](../Comms/AmountAtTime.md) received `SupportPayouRequest` from HydrogenSupportClient, sent `SupportPayout` to HydrogenSupportClient

# See also

* [HydrogenSupportClient](./HydrogenSupportClient.md)