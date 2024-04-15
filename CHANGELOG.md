<!-- SPDX-FileCopyrightText: 2024 German Aerospace Center <amiris@dlr.de>

SPDX-License-Identifier: CC0-1.0 -->
# Changelog
## [2.2.0](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v2.2.0) - TBA
### Added
- Add new agent `DayAheadMarketMultiZone` for market clearing with multiple coupled markets #16 (@dlr_elghazi @dlr_fn)
- Add new agent `MarketCoupling` which allows coupling of multiple `DayAheadMarketMultiZone` #16 (@dlr-cjs @litotes18 @dlr_elghazi @dlr_fn)
- Add new agent `ImportTrader` which can account for "negative" load from net-import hours #16 (@dlr-cjs @dlr_elghazi @dlr_fn)

## [2.1.0](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v2.1.0) - 2024-04-04
### Changed
- Increased FAME-Core version to 1.6 #106 (@dlr-cjs, @dlr_fn)

## [2.0.0](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v2.0.0) - 2024-03-12
_If you are upgrading: please see [`UPGRADING.md`](UPGRADING.md)_

### Changed
- **Breaking**: Rename `EnergyExchange` to `DayAheadMarketSingleZone` #41 (@dlr-cjs)
- **Breaking**: Forecasting products now defined in class `Forecaster` instead of `MeritOrderForecaster` #53 (@dlr-cjs, dlr_es, Seyerfarzad Sarfarazi, @kriniti)
- **Breaking**: FuelsMarket product `FuelsBill` renamed to `FuelBill` #54 (@dlr-cjs, @dlr_jk)
- **Breaking**: ConventionalPlantOperator now based on `FuelsTrader` interface using new `FuelBid` message #54 (@dlr-cjs, @dlr_jk)
- **Breaking**: StorageTrader input field `forecastRequestOffset` renamed to `electricityForecastRequestOffset` #54 (@dlr-cjs, @dlr_jk)
- **Breaking**: input field `DistributionMethod` moved to new group `Clearing` in DayAheadMarkets and MarketForecasters #86 (@dlr-cjs, @dlr_es, @dlr_elghazi, @litotes18)
- **Breaking**: Output field `CostsInEUR` in PowerPlantOperator & StorageTrader renamed to `VariableCostsInEUR` #54 (@dlr-cjs, @dlr_jk)
- **Breaking**: Renamed output fields to harmonise them across agent types #76 (@dlr-cjs)
  - `ConventionalPlantOperator`: rename column `DispatchedPowerInMWHperPlant` to `DispatchedEnergyInMWHperPlant`
  - `ConventionalTrader`: rename column `OfferedPowerInMW` to `OfferedEnergyInMWH`
  - `ConventionalTrader`: rename column `AwardedPower` to `AwardedEnergyInMWH`
  - `DayAheadMarket`: rename column `TotalAwardedPowerInMW` to `AwardedEnergyInMWH`
  - `MarketForecaster`: rename column `AwardedPowerForecast` to `AwardedEnergyForecastInMWH`
  - `MarketForecaster`: rename column `ElectricityPriceForecast` to `ElectricityPriceForecastInEURperMWH`
  - `PowerPlantOperator`: rename column `AwardedPowerInMWH` to `AwardedEnergyInMWH`
  - `PowerPlantOperator`: rename column `OfferedPowerInMW` to `OfferedEnergyInMWH`
  - `StorageTrader`: rename column `OfferedPowerInMW` to `OfferedEnergyInMWH`
  - `StorageTrader`: rename column `AwardedChargePowerInMWH` to `AwardedChargeEnergyInMWH`
  - `StorageTrader`: rename column `AwardedDischargePowerInMWH` to `AwardedDischargeEnergyInMWH`
  - `StorageTrader`: rename column `AwardedPowerInMWH` to `AwardedEnergyInMWH`
  - `AggregatorTrader`: rename column `TruePowerPotentialInMWH` to `TrueGenerationPotentialInMWH`
  - `SupportPolicy`: rename column `MarketValue` to `MarketValueInEURperMWH`
- **Breaking**: `AggregatorTrader`'s optional input field `Variance` renamed to `StandardDeviation` #91 (@dlr-cjs)
- **Breaking**: `DemandTrader` changed type of input for `ValueOfLostLoad` from `double` to `time_series` allowing time-dependent value of lost load #100 (@dlr-cjs, @dlr_jk, @dlr_es)
- OrderBookItems with negative power are not allowed #83 (@dlr-cjs, @dlr_elghazi)
- MeritOrderKernel throws exception if clearing fails #83 (@dlr-cjs, @dlr_elghazi)
- Introduced a new TraderWithClients class to remove errors related to not used actions #81 (@dlr-cjs, @dlr_elghazi)
- Changed default log level from FATAL to ERROR  #81 (@dlr-cjs, @dlr_elghazi)
- Forecaster now re-checks for missing forecasts in every hour #42 (@dlr-cjs)
- ArbitrageStrategist now extends `flexibility.Strategist` #54 (@dlr-cjs, @dlr_jk)
- Include accounting for storage self-discharge in storage strategies #21 (@dlr-cjs, @dlr_jk, @dlr_es, @dlr_elghazi)
- FileDispatcher(Storage) modified due to changes in `ArbitrageStrategist` #54 (@dlr-cjs, @dlr_jk)
- SystemCostMinimizer modified due to changes in `ArbitrageStrategist` #54 (@dlr-cjs, @dlr_jk)
- ArbitrageStrategist performs rounding of energy states and throws warning if not within tolerance #78 (@dlr_fn, @dlr-cjs, @dlr_elghazi)
- Newly raised error if in ConventionalPlantOperator no money foreseen for `Payout` is assigned to plants #89 (@litotes18, @dlr_jk, @dlr-cjs)
- StorageTrader now extends FlexibilityTrader #54 (@dlr-cjs, @dlr_jk)
- Refactoring of SupportPolicy, PolicyInfo, RenewableTrader and bidding strategies #66 (@dlr-cjs, @dlr_jk)
- Refactoring of StepPower #96 (@dlr-cjs)
- CITATION.cff harmonised with related JOSS paper #51 (@dlr-cjs)
- Update Reference examples to changes in outputs #41, #54, #75, #76 (@dlr-cjs)
- Move flexibility agents' forecasting to FlexibilityTrader #79 (@litotes18, @dlr_jk)
- Refactorings of `PowerPlant` to improve code clarity #94 (@dlr-cjs)
- CI: Any `javadoc` warning will now fail the pipeline #101 (@dlr-cjs)

### Added
- Package `electrolysis`: including `Electrolyzer` and related dispatch strategies #54 (@dlr-cjs, @dlr_jk)
- `ElectrolysisTrader`: new Trader demanding electricity and producing hydrogen from it via electrolysis #54 (@dlr-cjs, @dlr_jk)
- `FlexibilityTrader`: new abstract Trader operating a type of flexibility asset #54 (@dlr-cjs, @dlr_jk)
- `PriceForecaster`: new forecasting agent that provides forecasted electricity prices #53 (@dlr-cjs, Evelyn Sperber, Seyedfarzad Sarfarazi, @kriniti)
- Package `storage.arbitrageStrategists`: added new strategist `ProfitMaximiser` #72 (@dlr-cjs)
- Package `storage.arbitrageStrategists`: added new strategist `MultiAgentSimple` #73 (@dlr-cjs)
- UrlModelService: utility class to support calling external models via POST web-requests #52 (@dlr-cjs)
- Package `flexibility`: Basic classes for flexibility's dispatch planning #54 (@dlr-cjs, @dlr_jk)
- Added new support instrument `FINANCIAL_CFD` in SupportPolicy and RenewableTrader #68 (@dlr-cjs, @litotes18, @dlr_jk)
- Package `accounting`: including class AnnualCostCalculator #54 (@dlr-cjs, @dlr_jk)
- New inputs and actions for `PowerPlantOperator`s to use AnnualCostCalculator #99 (@dlr-cjs)
- Added new option `ShortagePrice` to MarketForecaster and DayAheadMarket to dynamically adjust scarcity prices #86 (@dlr-cjs, @dlr_es, @dlr_elghazi, @litotes18)
- `FuelsMarket`: new Fuels `BIOMASS` and `OTHER` #54 (@dlr-cjs, @dlr_jk)
- `FuelsTrader`: interface to trade with FuelsMarket #54 (@dlr-cjs, @dlr_jk)
- `FuelBid` message to send bids for fuels to FuelsMarket #54 (@dlr-cjs, @dlr_jk)
- `Trader`: has new product `AnnualCostReport` #54 (@dlr-cjs, @dlr_jk)
- `ElectrolysisTrader`: add optional bidding price limit override #57 (@dlr_jk, @dlr-cjs)
- `ConventionalOperator`: add output `ReceivedMoneyInEUR` for received money per plant #74 (@dlr_cjs)
- Package `forecast`: added `PriceForecasterFile` to provide price forecasts from file #95 (@dlr-cjs)
- FileDispatcher(Storage): added optional input parameter `DispatchTolerance` #50 (@dlr_jk, @litotes18, @dlr_fn)
- Added missing javadocs #101 (@dlr-cjs, @dlr_jk)
- JOSS Paper at folder 'paper/' #3 (@dlr-cjs, @dlr_fn, @litotes18, @dlr_jk, @kriniti, @kyleniemeyer)
- Tests in packages `accounting` and `util` #54 (@dlr-cjs, @dlr_jk)
- UPGRADING.md: help people with upgrading tasks #58 (@dlr-cjs)
- MarketClearing: Checks for Bid message integrity #54 (@dlr-cjs, @dlr_jk)
- CI: Automatic checks for changelog updates #60 (@dlr-cjs)
- README.md: added `Acknowledgements` section #70 (@dlr-cjs)
- README.md: added link to REMix !106 (@dlr-cjs)

### Removed
- **Breaking**: Drop support of JDK 8, 9 and 10; new minimum version is JDK 11, associated with #52 (@dlr-cjs)
- **Breaking**: Remove unused input parameter `PurchaseLeviesAndTaxesInEURperMWH` from `StorageTrader`

### Fixed
- PredefinedPlantBuilder: delivering portfolio for second year #65 (@dlr-cjs, @dlr_jk, @dlr_fn)
- Storage: bids could exceed market limits - now they abide by the market limits #47 (@dlr-cjs)
- AggregatorTrader: corrected association of support payments in case of multiple clients with similar set #104 (@dlr-cjs)
- Storage: discretisation of planning no longer causes over-charging or under-depletion #55 (@dlr-cjs)
- Forecaster: wrote out forecast prices with one hour lag - the time lag is removed #48 (@dlr-cjs)
- ConventionalPlantOperator: did not sort power plants for dispatch correctly - sorting now according to marginal cost #97 (@dlr-cjs)
- CI: Pipeline to publish javadocs #62 #63 (@dlr-cjs)
- ConventionalPlantOperator: do not store outputs for inactive plants #75 (@dlr_fn, @dlr-cjs)
- Paper: corrected year in reference Weidlich et al. (2008) #88 (@dlr_fn)
- Error messages in example `input.pb` file #87 (@dlr_jk, @dlr-cjs, @litotes18)

## [1.3](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v1.3) - 2023-03-21
### Changed
- StorageTrader with Strategist "DISPATCH_FILE" can now also provide forecasts
- REUSE license statements for each file in repository
- README improved
- Use FAME-Core version 1.4.2
- Improved packaging with executable JAR file

### Added
- IndividualPlantBuilder: allows to parameterise individual conventional power plants from a list
- FuelsMarket: new fuel type "HYDROGEN"
- Automatic unit testing infrastructure
- Reference results for an integration test

### Fixed
- PlantBuildingManager: calculation of Portfolio targetTime fixed
- CfD: Bidding strategy in RenewableTrader was not sensible and thus updated
- Missing Marginals: Always send at least one supply Marginal message, even if total available power is Zero
- Typos in issue templates

## [1.2](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v1.2) - 2022-02-14
### Changed
- Harmonised inputs & outputs across agents

## [1.1](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v1.1) - 2022-01-21
### Changed
- Harmonised contracting for Forecaster and Exchange

## [1.0](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v1.0) - 2021-12-06
_Initial Release._
