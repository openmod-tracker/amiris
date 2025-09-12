# In less than 42 words

`MarketCoupling` couples the markets of multiple [`DayAheadMarket`](./DayAheadMarket.md) agents.
It aims at maximizing the overall welfare by minimizing price spreads among the participating markets by transferring demand bids across these markets while considering restrictions of available transfer capacities.
  
# Details

`MarketCoupling` receives `CouplingData` requests from its associated individual `DayAheadMarket` agents.
These comprise: `SupplyOrderBook` for supply bids, `DemandOrderBook` for demand bids, `TransferOrderBook` for import bids, `TransferOrderBook` for export bids, and `TransmissionBook` for transmission capacities.
Using this data it instantiates a 'DemandBalancer' that implements the actual coupling algorithm.
Basically our coupling algorithm guarantees correctness and termination within tolerance parameters, utilizing two criteria: (1) shifting only the minimal-effective-demand from an expensive `DayAheadMarket` to a less expensive one at a time and (2) processing the most-effective-pair of all possible combinations first.
The minimal-effective-demand is the maximal demand that can be shifted from one market to another without effecting prices for both plus a user-defined energy amount in order to achieve the minimization of the price delta between both markets.
Finally, `MarketCoupling` returns the updated order books and transmission capacities to each respective `DayAheadMarket`.

For further details, you may also consult https://doi.org/10.5281/zenodo.10561382

# Dependencies

* [DayAheadMarket](./DayAheadMarket.md) to send `CouplingData` requests and receiving `CouplingData` results

# Input from file

* `MinimumDemandOffsetInMWH` optional offset added to the demand shift that ensures a price change at the involved markets.

# Input from environment

* One `CouplingData` per associated `DayAheadMarket`

# Simulation outputs

* `AvailableTransferCapacityInMWH`: complex output; the capacity available for transfer between two markets in MWH
* `UsedTransferCapacityInMWH`: complex output; the actual used transfer capacity between two markets in MWH
 
# Contracts

* `DayAheadMarket` sending `CouplingData` requests, receiving `CouplingData` results

# Available Products

* `MarketCouplingResult` Result of the coupled market clearing

# Submodules

* [CouplingData](Classes/Comms/CouplingData.md)
* [TransmissionBook](../Modules/TransmissionBook.md)
* [MarketClearingResult](../Modules/MarketClearingResult.md)
* [DemandBalancer](Classes/Comms/DemandBalancer.md)

# Messages
* [CouplingData](Classes/Comms/CouplingData.md) received
* [CouplingData](Classes/Comms/CouplingData.md) sent
