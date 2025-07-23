# In Short
`Strategist` is a basic abstract class for strategist operating a flexibility device, e.g., an energy storage or flexible electrolysis unit.

# Details
Strategist covers storing of forecasts for electricity prices or merit orders and handles basic strategy parameters, such as the schedule duration or forecast periods.
It also offers a routine of how to calculate a new [BidSchedule](./BidSchedule).

## Input
* `ForecastPeriodInHours` Must be smaller than or equal to that of the MarketForecaster
* `ScheduleDurationInHours` Number of hours each created schedule is viable; should be less than or equal to the forecast period
* `BidToleranceInEURperMWH` Optional input to define by what amount the offered price shall be lowered(when selling) or increased (when buying) energy based on the dispatch schedule  
* `ForecastUpdateType`: Defines the mode which should be applied to request electricity price forecasts. Either `ALL` for all time steps, discarding previously received electricity price forecasts, or `INCREMENTAL` for missing time steps only.

# Child classes
* [ElectrolyzerStrategist](./ElectrolyzerStrategist)
* [ArbitrageStrategist](./ArbitrageStrategist)

# See also
* [ElectrolyzerStrategist](./ElectrolyzerStrategist)
* [ArbitrageStrategist](./ArbitrageStrategist)