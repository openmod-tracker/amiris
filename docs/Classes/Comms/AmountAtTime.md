# In Short

Message transmitting a TimeStamp and an associated amount.
This message type is quite generic and can be used for simple communication needs.
Several extension are derived from this type.

# Details

Contained information:

* `validAt`: The time the DataItem is valid at
* `amount`: The actual amount to be exchanged between the contract parties

# Derived classes

* [Co2Cost](./Co2Cost.md)
* [FuelCost](./FuelCost.md)
* [YieldPotential](./YieldPotential.md)