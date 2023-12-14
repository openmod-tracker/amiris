<!-- SPDX-FileCopyrightText: 2023 German Aerospace Center <amiris@dlr.de>

SPDX-License-Identifier: Apache-2.0 -->
# Upgrading

## [2.0.0]
This version drops JDK8 support.
If you have a higher JDK already installed, no steps are required.
Check your JDK version with `java --version` (or `java -version` on some systems). 
If your Java version is below 11, please download and install a recent JDK from e.g. [here](https://adoptium.net/).

Update your input scenarios and contracts.
Several input parameters (field names and contract product names) changed or were removed.

Check your scripts that digest AMIRIS outputs.
Several output columns were renamed.