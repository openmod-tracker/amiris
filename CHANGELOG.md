<!-- SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>

SPDX-License-Identifier: CC0-1.0 -->
# Changelog

## [2.0.0](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v2.0.0) - TBA
_ If you are upgrading: please see [UPGRADING.md](UPGRADING.md)._

### Changed
- **Breaking**: Forecasting products now defined in class `Forecaster` instead of `MeritOrderForecaster` #53 (@dlr-cjs, Evelyn Sperber, Seyerfarzad Sarfarazi, @kriniti)
- Forecaster now re-checks for missing forecasts in every hour #42 (@dlr-cjs)
- CITATION.cff harmonised with related JOSS paper #51 (@dlr-cjs)

### Added
- PriceForecaster: New forecasting agent that provides forecasted electricity prices #53 (@dlr-cjs, Evelyn Sperber, Seyedfarzad Sarfarazi, @kriniti)
- UrlModelService: utility class to support calling external models via POST web-requests #52 (@dlr-cjs)
- JOSS Paper at folder 'paper/' #3 (@dlr-cjs, @dlr_fn, @litotes18, @dlr_jk, @kriniti, @kyleniemeyer)
- UPGRADING.md: help people with upgrading tasks
- CI: Automatic checks for changelog updates #60 (@dlr-cjs)

### Removed
- **Breaking**: Drop support of JDK 8, 9 and 10; new minimum version is JDK 11, associated with #52 (@dlr-cjs)

### Fixed
- Storage: bids could exceed market limits - now they abide by the market limits #47 (@dlr-cjs)
- Storage: discretisation of planning no longer causes over-charging or under-depletion #55 (@dlr-cjs)
- Forecaster: wrote out forecast prices with one hour lag - the time lag is removed #48 (@dlr-cjs)
- CI: Deploy pipeline to run on stage deploy #62 (@dlr-cjs)

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
