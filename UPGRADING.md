<!-- SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>

SPDX-License-Identifier: Apache-2.0 -->
# Upgrading

## [3.0.0]
### String Sets
This version requires new feature `string_set` provided by `fameio` > v2.3.
Thus, update `fameio` accordingly.

### Fixed typo
A typo in the often used input parameter `InvestmentExpensesesInEURperMW` was fixed to `InvestmentExpensesInEURperMW`.
Update your schema files and scenarios, and if necessary, adjust you scripts if these refer to this parameter name explicitly.


## [2.0.0]
### Minimum JDK 11
This version drops support for JDK 8, 9, and 10..
If you have a higher JDK already installed, no steps are required.
Check your JDK version with `java --version` (or `java -version` on some systems). 
If your Java version is below 11, please download and install a recent JDK from e.g. [here](https://adoptium.net/).

Update your input scenarios and contracts.
Several input parameters (field names and contract product names) changed or were removed.

Check your scripts that digest AMIRIS outputs.
Several output columns were renamed.