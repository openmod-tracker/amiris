<!-- SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>

SPDX-License-Identifier: Apache-2.0 -->
# Upgrading

## [3.0.0]
### String Sets
This version requires new feature `string_set` provided by `fameio` > v2.3.
Thus, update `fameio` accordingly.
If not yet present, add a new `StringSets` section to your scenario.

#### FuelType
Add a new StringSet `FuelType` to your scenario listing all fuel names used by your agents.
Example:

```yaml
StringSets:
  FuelType:
    Values: ['Oil', 'Hydrogen', 'MySpecialFuel']
```

#### Set -> PolicySet
Input parameter `Set` was renamed to `PolicySet` for agent types `RenewablePlantOperator` and its children, as well as for `Policy`.
Therefore, rename occurrences accordingly.
In addition, add a new StringSet `PolicySet` to your scenario listing all policy sets available to your agents.
Example:

```yaml
StringSets:
  PolicySet:
    Values: ['WindOn', 'Biogas', 'MyPolicySet']
```

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