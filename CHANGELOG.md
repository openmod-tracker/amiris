<!-- SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>

SPDX-License-Identifier: CC0-1.0 -->

# [Aguilera - v1.4 (TBA)](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v1.4)
## Major Changes:
* Removed support for JDK versions 8 to 10, new minimum version is JDK11 

## Changes
* Added: UrlModelService - utility class to support calling external models via POST web-requests

## Other changes
* Added: JOSS Paper at folder paper/
* Updated: CITATION.cff

# [Agua - v1.3 (2023-03-21)](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v1.3):
## Changes
* Added: IndividualPlantBuilder - allows to parameterise individual conventional power plants from a list
* Updated: StorageTrader with Strategist "DISPATCH_FILE" can now also provide forecasts
* Added: new fuel type "HYDROGEN"

## Minor changes
* Added: REUSE license statements for each file in repository
* Added: automatic unit testing infrastructure
* Added: Reference results for an integration test
* Updated: use FAME-Core version 1.4.2
* Updated: improved packaging with executable JAR file
* Updated: README

## Fixes
* PlantBuildingManager: calculation of Portfolio targetTime fixed
* Typos in issue templates
* Missing Marginals: Always send at least one supply Marginal message, even if total available power is Zero
* CfD: Bidding strategy in RenewableTrader was not sensible and thus updated

# [Adams - v1.2 (2022-02-14)](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v1.2):
Harmonised inputs & outputs across agents

# [Acatenango - v1.1 (2022-01-21)](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v1.1): 
Harmonised contracting for Forecaster and Exchange

# [Acamarachi - v1.0 (2021-12-06)](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/releases/v1.0): 
Initial release of AMIRIS
