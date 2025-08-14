# In short

`YieldPotential` defines how much energy of a certain EnergyCarrier could be fed-in if no (market- or network-based) curtailment is applied.
It extends [AmountAtTime](./AmountAtTime.md).

# Details

Contained information:

* `timeStamp` the TimeStamp for which the yield potential is given
* `amount` the actual yield potential for the time stamp, i.e. the energy that could be fed-in
* `energyCarrier` the energyCarrier of the feed-in potential

# See also

* [AmountAtTime](./AmountAtTime.md)