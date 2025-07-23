# 42 words

DayAheadMarketMultiZone (DAMMZ) is a type of [DayAheadMarket](./DayAheadMarket.md) which is connected to a [MarketCoupling](./MarketCoupling.md) agent.
It digests the requested and offered energy from connected [DayAheadMarketTraders](../Abilities/DayAheadMarketTrader.md).
It forwards the bids together with transmission capacities to the market coupling. The award of the clearing is sent back to the DayAheadMarketTraders.

# Details

Based on the received [Bids](../Comms/BidsAtTime.md) from the DayAheadMarketTraders, DAMMZ fills the [Demand & Supply book](../Modules/OrderBook.md).
The books are sent to the market coupling agent where all linked markets are cleared together.
After the coupling results are received, the total amount of awarded supply and demand energy is calculated and sent out as [Award](../Comms/AwardData.md) message for each contracted Agent.
If an agent did not place a bid but is contracted for an award, the awarded energy will be Zero.

# Dependencies

* [DayAheadMarket](./DayAheadMarket.md): Parent class defining sending of GateClosureInfos
* [DayAheadMarketTraders](../Abilities/DayAheadMarketTrader.md): to send their supply and demand bids

# Input from file

Additional fields to the ones in [DayAheadMarket](./DayAheadMarket.md):

* `MarketZone`: Identifier specifying the market zone this DayAheadMarket is representing
* `Transmission`: a list of transmission capacities towards connected market zones, each specifying
  * `MarketZone`: Connected Market zone that can be supplied with additional energy
  * `CapacityInMW`: Net transfer capacity of supply from own to connected market zone

# Input from environment

* Bids from DayAheadMarketTraders

# Simulation outputs

Additional to the outputs as in [DayAheadMarket](./DayAheadMarket.md):

* `PreCouplingElectricityPriceInEURperMWH`: electricity price which would occur without any market coupling in EUR/MWh
* `PreCouplingTotalAwardedPowerInMW`: awarded power without any market coupling in MW
* `PreCouplingDispatchSystemCostInEUR`: system cost without any market coupling in EUR
* `AwardedNetEnergyFromImportInMWH`: net awarded power from imports in MWh
* `AwardedNetEnergyToExportInMWH`: net awarded energy to export in MWh

# Contracts
* DayAheadMarketTraders send their BidsAtTime
* DayAheadMarketMultiZone send TransmissionAndBids
* DayAheadMarketMultiZone receive MarketCouplingResult
* DayAheadMarketTraders receive AwardData

see also [DayAheadMarket](./DayAheadMarket.md)

# Available Products

* `TransmissionAndBids` Transmission capacities and bids from local exchange

see also [DayAheadMarket](./DayAheadMarket.md)

# Submodules
* [MarketClearing](../Modules/MarketClearing.md)
* [MeritOrderKernel](../Modules/MeritOrderKernel.md)
* [OrderBook](../Modules/OrderBook.md)
* [MarketClearingResult](../Modules/MarketClearingResult.md)

# Messages
* [AwardData](../Comms/AwardData.md) sent out
* [BidsAtTime](../Comms/BidsAtTime.md) received

see also [DayAheadMarket](./DayAheadMarket.md)

# See also
* [DayAheadMarket](./DayAheadMarket.md)
* [DayAheadMarketTrader](../Abilities/DayAheadMarketTrader.md)
* [MarketCoupling](./MarketCoupling.md)