# In short

`HydrogenSupportClient` represents the Ability to receive support payments for hydrogen.

# Details

`HydrogenSupportClient` offers basic functionalities concerning registration for and requesting of hydrogen support at a connected `HydrogenSupportProvider`.

# Input from file

* `SupportInstrument`: Enum indicating which support instrument the client applying for
* `HydrogenPolicySet`: StringSet indicating which policy set a client is associated with 

# Available Products

* `SupportInfoRequest`: Request for support information for contracted technology set(s)
* `SupportPayoutRequest`: Request to obtain support payments for contracted technology set(s)

# Outputs

* `ReceivedHydrogenSupportInEUR`: Received support for hydrogen in EUR

# Messages

* [HydrogenPolicyRegistration](../Comms/HydrogenPolicyRegistration.md) sent `SupportInfoRequest` to HydrogenSupportProvider
* [HydrogenSupportData](../Comms/HydrogenSupportData.md) received `SupportInfo` from HydrogenSupportProvider
* [AmountAtTime](../Comms/AmountAtTime.md) sent `SupportPayouRequest` to HydrogenSupportProvider, received `SupportPayout` from HydrogenSupportProvider

# See also

* [HydrogenSupportProvider](./HydrogenSupportProvider.md)