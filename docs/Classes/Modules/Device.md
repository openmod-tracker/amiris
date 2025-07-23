# In Short
Device represents an energy storage plant of any kind, e.g. a LiIon battery plant or pumped-hydro storage plant.

# Details
A storage Device is based upon its abstract representation [AbstractDevice](./AbstractDevice) which covers the technical aspects of the  plant. Device adds the actual state of the storage plant, i.e. the currently stored energy and the total of energy flows and storage cycles operated with this device so far.

The storage device is capable to change its available power input and output using the method "setInternalPowerInMW". Since the E2P ratio is fixed, this will also change the storage capacity. 
To change the energy content of the Device, use its method "chargeInMW". Using positive energy values will increase its charging state, while negative values will discharge the Device. This will automatically consider losses.

# Input from file
Device defines a set of input parameters, which can be used to define the required inputs for Agents that control a Device. 
Required input parameters are:
* `InstalledPowerInMW`: power of the associated storage device
* `EnergyToPowerRatio`: E2P of the associated storage device
* `ChargingEfficiency`: efficiency of the charging process (value from 0 to 1)
* `DischargingEfficieny`: efficiency of the discharging process (value from 0 to 1)
* `SelfDischargeRatePerHour`: rate at which the associated storage looses energy due to self discharge (value from 0 to 1) per time relative to current level of charged energy
* `InitialEnergyLevelInMWH`: energy stored in the associated device at the beginning of the simulation

# See also
[AbstractDevice](./AbstractDevice)