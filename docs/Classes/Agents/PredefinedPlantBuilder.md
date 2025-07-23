# 42 words

A `PredefinedPlantBuilder` is a [PlantBuildingManager](./PlantBuildingManager.md) that creates [Portfolios](../Modules/Portfolio.md) following given `TimeSeries` of installed total power and efficiencies for a given power plant technology.

# Details

The PredefinedPlantBuilder follows a TimeSeries of installed power.
On every update of its Portfolio, the PredefinedPlantBuilder removes outdated  [PowerPlants](../Modules/PowerPlant.md) from the according to the current simulation time.
Then it creates a new set of power plants in that Portfolio that match the specified total installed power plant capacity in the TimeSeries.
The Portfolio may include both "old" and the "new" set of power plants.
However, the new set of power plants becomes active only after their given starting TimeStamp (which marks the tear-down time of the previously built plants).
Thus, only one set of power plants is active at any time.

# Dependencies

No dependencies.

# Input from file

see also [PlantBuildingManager](./PlantBuildingManager.md)

* `Efficiency` Group
    * `Minimal` The minimum efficiency of the respective power plant technology.
    * `Maximal` The maximum efficiency of the respective power plant technology.
* `InstalledPowerInMW` time series of total installed power for the respective plant technology managed by this `PredefinedPlantBuilder`.
* `BlockSizeInMW` defines the typical power capacity of a PowerPlant within the Portfolio to generate.
* `EfficiencyRoundingPrecision` Optional parameter: if present causes interpolated efficiencies to be rounded to the given number of digits

# Input from environment

None

# Simulation outputs

None

# Contracts

see [PlantBuildingManager](./PlantBuildingManager.md)

# Available Products

see [PlantBuildingManager](./PlantBuildingManager.md)

# Submodules

No submodules.

# Messages

see [PlantBuildingManager](./PlantBuildingManager.md)