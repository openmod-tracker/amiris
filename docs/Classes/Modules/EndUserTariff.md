# In Short

Holds information on the design of end user, i.e. consumer tariffs. This comprises the information of what tariff
components are considered, what value they take and whether they are static and/or partly dynamic, i.e. varying with the
model-endogenous day-ahead power price.

# Inputs

An EndUserTariff is defined by policy-related parameters

* `EEGSurchargeInEURPerMWH`: The EEG levy (levy from German renewable energy act - EEG) in EUR per MWh
* `VolumetricNetworkChargeInEURPerMWH`: Volumetric network charges, i.e. charges dependent on consumed energy in EUR
  per MWh
* `ElectricityTaxInEURPerMWH`: The Electricity tax in EUR per MWh
* `OtherSurchargesInEURPerMWH`: Other surcharges, such as KWKG levy (for CHP), § 19 StromNEV levy, AbLaV levy,
  offshore network levy, concession fee, metering fee in EUR per MWh
* `DynamicTariffComponents`: a list of input groups, each comprising
    * `ComponentName`: The name of the TariffComponent; allowed values
      are `EEG_SURCHARGE`, `POWER_PRICE`, `VOLUMETRIC_NETWORK_CHARGE`, `OTHER_COMPONENTS`.
    * `Multiplier`: The multiplier used for calculating dynamic tariff component from model-endogenous day-ahead
      price.
    * `LowerBound`: A potential lower bound for the tariff component to protect the retailer from negative prices.
    * `UpperBound`: A potential upper bound for the tariff component to protect the consumer from price spikes.

and parameters related to business models:

* `ProfitMarginInEURPerMWH`: the profit to be made by the retailer in EUR per MWh 
* `AverageMarketPriceInEURPerMWH`: the static average market price in EUR per MWh
