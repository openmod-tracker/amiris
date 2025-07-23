# In less than 42 words

`DemanBalancer` implements our actual market coupling algorithm.
It aims, like the original Price Coupling of Regions (PCR) algorithm, at maximizing the overall welfare amount of all participating `DayAheadMarket`s.
It does so by minimizing price spreads among the participating `DayAheadMarket`s by transferring demand across these `DayAheadMarket`s while considering restrictions of available transfer capacities.
  
# Details

The market coupling algorithm guarantees correctness and termination within tolerance parameters, utilizing two criteria:
1. shifting at a time only the minimal-effective-demand from an expensive `DayAheadMarket` to a less expensive one and
2. processing the most-effective-pair of all possible combinations first. 

The minimal-effective-demand is the maximal demand that can be shifted from one market to another without effecting prices for both plus a user-defined energy amount in order to achieve minimizing the price delta between both markets.

# Submodules

* [CouplingData](../Comms/CouplingData.md)
* [OrderBook](../Modules/OrderBook.md)
* [MarketClearing](../Modules/MarketClearing.md)
* [MeritOrderKernel](../Modules/MeritOrderKernel.md)
* [MarketClearingResult](../Modules/MarketClearingResult.md)
