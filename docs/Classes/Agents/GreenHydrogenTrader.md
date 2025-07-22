# 42 words

`GreenHydrogenTrader` is a [Trader](./Trader.md) that operates an [Electrolyzer](../Modules/Electrolyzer.md) unit to produce green hydrogen from renewable (green) electricity.
It purchases electricity from an associated [VariableRenewableOperatorPpa](./VariableRenewableOperatorPpa.md) via a Power Purchase Agreement (PPA).
`GreenHydrogenTrader` can resell purchased electricity and / or use it to produce hydrogen and sell that instead.

# Details

The `GreenHydrogenTrader` has two ways to make a profit: First, it can use electricity to produce green hydrogen and sell it on the [FuelsMarket](./FuelsMarket.md).
Second, it can sell (surplus) electricity at the [DayAheadMarket](./DayAheadMarket.md).
The decision how to use the electricity is based on the expected profit for selling hydrogen and the capacity limitations of the [Electrolyzer](../Modules/Electrolyzer.md).

The dispatch of the `GreenHydrogenTrader` is influenced by an associated portfolio of renewable power plants.
The electrolyzer is required to use not more electricity than can be generated from the associated renewable power plants in any hour (hourly equivalence).
Furthermore, there optionally can be support payments for the hydrogen produced paid by the [HydrogenSupportPolicy](./HydrogenSupportPolicy.md).

The trader places two bids to the day-ahead market:

## Surplus bid

It is assumed that the `GreenHydrogenTrader` is contractually obliged to take or pay for all the generation from the associated renewable plants that it has contracted under a PPA.
Therefore, it offers to sell any potential surplus generation exceeding the electrolyzer's capacity at a price of 0 EUR/MWh on the [DayAheadMarket](./DayAheadMarket.md) for a non-negative price.

## Displacement bid

The `GreenHydrogenTrader` calculates the expected revenue for the sale of green hydrogen on the fuels market.
Based on the efficiency of the electrolyzer and potential support payments it may receive for the produced hydrogen, it then calculates the value of converting electricity to hydrogen at the current time.
If the electricity is more valuable on the day-ahead market, it should be sold directly, instead of producing hydrogen.
Thus, an appropriate bid is placed on the [DayAheadMarket](./DayAheadMarket.md) and `GreenHydrogenTrader` will curtail the production of the electrolyzer if higher revenues can be achieved by selling electricity on the [DayAheadMarket](./DayAheadMarket.md) than selling hydrogen on the [FuelsMarket](./FuelsMarket.md).

## Contracts

* [VariableRenewableOperatorPpa](./VariableRenewableOperatorPpa.md) sends `PpaInformationForecastRequest`, `PpaInformationRequest`, `DispatchAssigment`, `Payout` and receives `PpaInformationForecast`, `PpaInformation`
* [MarketForecaster](./MarketForecaster.md) sends `BidsForecast` and receives `ForecastRequest`
* [FuelsMarket](FuelsMarket.md) sends `FuelPriceForecastRequest`, `FuelPriceReqeust`, `FuelBid` and receives `FuelPriceForecast`, `FuelPrice`, `FuelBill`
* [DayAheadMarket](./DayAheadMarket.md) sends `Bids` and receives `GateClosureInfo`, `Awards`
* [HydrogenSupportProvider](..Abilities/HydrogenSupportProvider.md) sends `SupportInfoRequest`, `SupportPayoutRequest` and receives `SupportInfo`, `SupportPayout`

# Dependencies

* [PowerPlantScheduler](../Abilities/PowerPlantScheduler.md)
* [FuelsTrader](../Abilities/FuelsTrader.md)
* [GreenHydrogenProducer](../Abilities/GreenHydrogenProducer.md)
* [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)
* [Trader](./Trader.md)
* [FuelsMarket](./FuelsMarket.md)
* [DayAheadMarket](./DayAheadMarket.md)
* [HydrogenSupportProvider](../Abilities/HydrogenSupportProvider.md)

# Input from file

* `FuelType`: string, defined in [FuelsTrader](../Abilities/FuelsTrader.md) fuel type, i.e. hydrogen
* `Device`: Group, defined in [Electrolyzer](../Modules/Electrolyzer.md) covering technical details of the electrolyzing device to administrate
* `Support`: Optional group, defined in [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md) covering hydrogen support details

# Input from environment

* `GateClosureInfo` and `Awards` from [DayAheadMarket](./DayAheadMarket.md)
* `PpaInformationForecast` and `PpaInformation` from [VariableRenewableOperator](./VariableRenewableOperator.md)
* `FuelPriceForecast` and `FuelPrice` from [FuelsMarket](./FuelsMarket.md)
* `ForecastRequest` from [MarketForecaster](./MarketForecaster.md)
* `SupportInfo` and `SupportPayout` from [HydrogenSupportProvider](../Abilities/HydrogenSupportProvider.md)

# Simulation outputs

* `ProducedHydrogenInMWH`: Amount of green hydrogen produced in this period using the electrolysis unit in MWh
* `VariableCostsInEUR`: Variable operation and maintenance costs in EUR
* `FixedCostsInEUR`: Fixed cost of green hydrogen trader in EUR
* `InvestmentAnnuityInEUR`: Investment annuity of green hydrogen trader in EUR
* `ReceivedMoneyForHydrogenInEUR`: Total received money for selling hydrogen in EUR
* `OfferedSurplusEnergyInMWH`: Surplus electricity generation offered to the day-ahead market in MWh

See also [GreenHydrogenProducer](../Abilities/GreenHydrogenProducer.md), and [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)

# Available Products

See [GreenHydrogenProducer](../Abilities/GreenHydrogenProducer.md), and [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)

# Submodules

* [Electrolyzer](../Modules/Electrolyzer.md)

# Messages

* [ClearingTimes](../Comms/ClearingTimes.md): sent to VariableRenewableOperator on `PpaInformationRequest`, received from DayAheadMarket
* [PpaInformation](../Comms/PpaInformation.md): received `PpaInformationForecast` and `PpaInformation` from VariableRenewableOperator
* [AmountAtTime](../Comms/AmountAtTime.md):
    * sent `DispatchAssignment` and `Payout` to VariableRenewableOperator
    * sent `SupportPayoutRequest` to HydrogenSupportProvider, received `SupportPayout` from HydrogenSupportProvider
* [BidsAtTime](../Comms/BidsAtTime.md) sent to DayAheadMarket
* [AwardData](../Comms/AwardData.md) received from DayAheadMarket

See also [FuelsTrader](../Abilities/FuelsTrader.md), and [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)

# See also

* [Trader](./Trader.md)
* [FuelsTrader](../Abilities/FuelsTrader.md)
* [GreenHydrogenProducer](../Abilities/GreenHydrogenProducer.md)
* [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)
* [PowerPlantScheduler](../Abilities/PowerPlantScheduler.md)
* [GreenHydrogenProducer](../Abilities/GreenHydrogenProducer.md)
* [FuelsMarket](./FuelsMarket.md)
* [DayAheadMarket](./DayAheadMarket.md)
* [MarketForecaster](./MarketForecaster.md)
* [VariableRenewableOperator](./VariableRenewableOperator.md)
* [HydrogenSupportPolicy](./HydrogenSupportPolicy.md)
* [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)