# 42 words

`GreenHydrogenTraderMonthly` is a type of `ElectrolysisTrader` that operates an `Electrolyzer` unit to produce hydrogen from (green) electricity.
Similar to the [GreenHydrogenTrader](./GreenHydrogenTrader.md), it purchases electricity from an associated [VariableRenewableOperatorPpa](./VariableRenewableOperatorPpa.md) via a Power Purchase Agreement (PPA).
In contrast, it has to fulfil the requirement that the electrolyzer's demand does not exceed the variable renewable generation used on a monthly basis.
As a child of [FuelsTrader](../Abilities/FuelsTrader.md), it can also trade fuels at a [FuelsMarket](./FuelsMarket.md).
Being a [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md) it can request support for hydrogen production. 

# Details

## Strategies

There is one dispatch strategy named `GREEN_HYDROGEN_MONTHLY` formulated in the module [MonthlyEquivalence](../Modules/MonthlyEquivalence.md).
This strategy seeks to maximize profits from green hydrogen production and selling of (excess) electricity purchased via a PPA.

The GreenHydrogenTraderMonthly may be eligible for support payments for the hydrogen produced paid by the [HydrogenSupportPolicy](./HydrogenSupportPolicy.md).

## Contracts

The GreenHydrogenTraderMonthly requires a forecast for

* electricity prices from the [PriceForecaster](./PriceForecaster.md),
* hydrogen prices from the [FuelsMarket](./FuelsMarket.md), and
* [PpaInformation](../Comms/PpaInformation.md), i.e. the production potential and PPA price, from the [VariableRenewableOperator](./VariableRenewableOperator.md).

Once the forecasts are obtained, the ElectrolysisTrader uses its [Strategist](../Modules/ElectrolyzerStrategist.md) to calculate a suitable [BidSchedule](../Modules/BidSchedule.md).
The DispatchSchedule can be of any duration between 1 and the foresight period.
It will be based upon the specifications of the attached [Electrolyzer](../Modules/Electrolyzer.md) unit.
Following the DispatchSchedule, the GreenHydrogenTrader places both, demand and supply bids at the [DayAheadMarket](./DayAheadMarket.md).

# Dependencies

* [PowerPlantScheduler](../Abilities/PowerPlantScheduler.md)
* [FuelsTrader](../Abilities/FuelsTrader.md)
* [GreenHydrogenProducer](../Abilities/GreenHydrogenProducer.md)
* [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)
* [DayAheadMarketTrader](../Abilities/DayAheadMarketTrader.md)
* [FuelsMarket](./FuelsMarket.md)
* [PriceForecaster](./PriceForecaster.md)
* [HydrogenSupportProvider](../Abilities/HydrogenSupportProvider.md)

See also

* [ElectrolysisTrader](./ElectrolysisTrader.md)
* [FlexibilityTrader](./FlexibilityTrader.md)

# Input from file

See

* [ElectrolysisTrader](./ElectrolysisTrader.md)
* [FlexibilityTrader](./FlexibilityTrader.md)

# Input from environment

See

* [ElectrolysisTrader](./ElectrolysisTrader.md)
* [FlexibilityTrader](./FlexibilityTrader.md)

# Simulation outputs

See

* [ElectrolysisTrader](./ElectrolysisTrader.md)
* [FlexibilityTrader](./FlexibilityTrader.md)

# Contracts

* MarketForecaster: Send requests, get electricity price forecasts, send bids forecasts
* FuelsMarket: Send requests, get hydrogen price forecasts, offer produced hydrogen
* DayAheadMarket: Send bids, get awards
* VariableRenewableOperatorPpa: Send requests, get PpaInformation forecasts

See also [ElectrolysisTrader](./ElectrolysisTrader.md)

# Available Products

* `MonthlyReset` triggering a monthly reset of the monthly schedule and green electricity surplus

See also

* [ElectrolysisTrader](./ElectrolysisTrader.md)
* [GreenHydrogenProducer](../Abilities/GreenHydrogenProducer.md)
* [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)

# Submodules

* [MonthlyEquivalence](../Modules/MonthlyEquivalence.md)

see also [ElectrolysisTrader](./ElectrolysisTrader.md)

# Messages

* [PointInTime](../Comms/PointInTime.md): `PriceForecastRequest` sent to MarketForecaster, `PpaInformationForecast` sent to VariableRenewableOperator
* [ClearingTimes](../Comms/ClearingTimes.md): `HydrogenPriceForecastRequest` sent to FuelsMarket
* [AmountAtTime](../Comms/AmountAtTime.md): received `PriceForecast` from PriceForecaster, sent `HydrogenOffer` to FuelsMarket, received `FuelsReward` from FuelsMarket
* [FuelCost](../Comms/FuelCost.md): received `HydrogenPriceForecast` from FuelsMarket
* [PpaInformation](../Comms/PpaInformation.md): `PpaInformationForecast` and `PpaInformation` received from VariableRenewableOperator

See also

* [ElectrolysisTrader](./ElectrolysisTrader.md)
* [FlexibilityTrader](./FlexibilityTrader.md)

# See also

* [Trader](./Trader.md)
* [FuelsTrader](../Abilities/FuelsTrader.md)
* [FlexibilityTrader](./FlexibilityTrader.md)
* [ElectrolysisTrader](./ElectrolysisTrader.md)
* [PowerPlantScheduler](../Abilities/PowerPlantScheduler.md)
* [GreenHydrogenProducer](../Abilities/GreenHydrogenProducer.md)
* [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)
* [FuelsMarket](./FuelsMarket.md)
* [DayAheadMarket](./DayAheadMarket.md)
* [VariableRenewableOperator](./VariableRenewableOperator.md)
* [HydrogenSupportPolicy](./HydrogenSupportPolicy.md)
* [HydrogenSupportClient](../Abilities/HydrogenSupportClient.md)
* [HydrogenSupportProvider](../Abilities/HydrogenSupportProvider.md)