# In Short

Represents an energy storage plant of any kind, e.g. a LiIon battery plant or pumped-hydro storage plant but without the state of the storage device, e.g. without a specific state of charge. 

# Details

The abstract storage device is defined by
* `internalPowerInMW`: the power at which the device is capable of charging **and** discharging, ignoring losses. *Internal* charging and discharging power is always symmetric.
* `energyStorageCapacityInMWH`: the maximum energy that can be stored in this device internally. The value cannot be set independently, but is based on the internalPower and the energy-to-power ratio
* `energyToPowerRatio`: ratio between internal energy storage capacity and internal (dis-)charging power.
* `selfDischargeRatePerHour`: fraction of the actual energy in storage lost per hour. 
* `dischargingEfficiency`: fraction <=1; the externally available energy from discharging operations is multiplied by the dischargingEfficiency
* `chargingEfficiency`: fraction <=1; charging operations required external energy are increased by "1 / chargingEfficiency"

# See also

Child class: [Device](./Device.md)