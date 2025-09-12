# In Short

ArbitrageStrategists create arbitrage strategies for storage [Devices](./Device.md) based on information about the future merit orders in form of [MeritOrderSensitivities](./MeritOrderSensitivity.md).

# Details

ArbitrageStrategists create [BidSchedules](./BidSchedule.md) for a specific storage [Device](./Device.md) using foresight of [MeritOrderSensitivities](./MeritOrderSensitivity.md).
The [ArbitrageStrategist](./ArbitrageStrategist.md) itself is an abstract upper class, implementing [Strategist(Flexibility)](./Strategist(Flexibility).md) for the case of storage [Devices](./Device.md).
The instantiated child class defines the actual strategy to be applied.

Uses the function "createSchedule" to create schedules for time periods shorter (or equal to) its forecastPeriod.
An Agent (usually a [StorageTrader](../Agents/StorageTrader.md)) needs to host the Strategist and must "feed-in" the MeritOrderSensitivity forecasts using the functions "storeMeritOrderForesight" or "storeElectricityPriceForecast" (depending on the actual child class).
In order to keep a lean memory profile, the agent should also call "clearSensitivitiesBefore" from time to time.

Provides the "calcNumberOfEnergyStates" function to determine the rounded number of energy states - that can be employed by child classes: If the energy to power ratio (E2P) is not an integer number, the number of states to represent the storage capacity might not be an exact integer for ArbitrageStrategists based on dynamic programming.
As a remedy, the number of states is rounded to the next closest integer. 
This, however, might slightly increase or decrease the storage capacity.
If the difference between the actual and correct storage capacity exceeds a predefined margin a warning is triggered. 
This situation may occur whenever the product of EnergyToPowerRatio and ModelledChargingSteps does not resemble an integer number.

# Child classes

* [FileDispatcher](./FileDispatcher(Storage).md)
* [SystemCostMinimiser](./SystemCostMinimiser(Storage).md)
* [ProfitMaximiser](./ProfitMaximiser(Storage).md)
* [ProfitMaximiserPriceTaker](./ProfitMaximiserPriceTaker(Storage).md)
* [MultiAgentMedian](./MultiAgentMedian(Storage).md)

# See also

* [Strategist(Flexibility)](./Strategist(Flexibility).md)
* [Device](./Device.md)
* [StorageTrader](../Agents/StorageTrader.md)
* [MeritOrderSensitivities](./MeritOrderSensitivity.md)