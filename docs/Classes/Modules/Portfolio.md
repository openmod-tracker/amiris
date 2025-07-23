# In short
Comprises a set of [PowerPlants](./PowerPlant) of one kind of [Prototype](./PowerPlantPrototype), sorted according to their respective efficiency.

# Details
The `Portfolio` represents a list of `PowerPlant`s, sorted from lowest to highest efficiency. `Portfolio` can also set up this list of power plants, based on
* `installedCapacityInMW` the total capacity to be installed
* `minEfficiency` the lowest energy conversion efficiency within the Portfolio
* `maxEfficiency` the highest energy conversion efficiency within the Portfolio
* `blockSizeInMW` the capacity per PowerPlant within the Portfolio
* `constructionTimeStep` the time step at which the list of power plants become active.
* `tearDownTimeStep` time step at which all power plant are deactivated
* `roundingPrecision` number of decimal places to round interpolated precision to

During setup of the PowerPlants, `Portfolio` calculates the number of blocks (i.e. number of power plants) based on the block size and capacity to install. Keep in mind that the last block may not have full power. For each block, the efficiency is determined as follows: the first block gets the minimal efficiency, the last block the maximal efficiency and all blocks in between a linear interpolated efficiency. Optionally, interpolated efficiencies can be rounded according to a number of digits defined by `roundingPrecision`. Newly setup plants become active only once their `constructionTimeStep` has been reached. They become inactive again once the `tearDownTimeStep` is reached.

`Portfolio` may contain currently active power plants, inactive "past" power plants and inactive "future" power plants in its list at the same time. In this way `Portfolio` is agnostic of time and can list PowerPlants from past and future.

`Portfolio` implements the Portable interface from FAME and can thus be transported in a message.

# Dependencies
- [PowerPlantPrototype](./PowerPlantPrototype)
- [PowerPlant](./PowerPlant)
