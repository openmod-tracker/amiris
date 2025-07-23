# In short

`FileDispatcher` is a kind of [ElectrolyzerStrategist](./ElectrolyzerStrategist.md).
It creates [BidSchedule](./BidSchedule) from a time series file for a corresponding [Electrolyzer](./Electrolyzer.md).
Given time series can be either a hydrogen production, or an electricity consumption and can be either an absolute number or relative to peak conversion capacity.

# Input

* `HourlySchedule` Time series describing the dispatch of the corresponding electrolyzer; its interpretation also depends on `Mode` and `Target` values
* `Mode` one of [ABSOLUTE, RELATIVE]: if `ABSOLUTE` time series is interpreted as absolute conversion energy in MWh, if `RELATIVE`, time series is interpreted as conversion rate relative to peak conversion capacity.  
* `Target` one of [ELECTRICITY, HYDROGEN]: if `ELECTRICITY`, time series is interpreted as electric consumption, if `HYDROGEN`, time series is interpreted as hydrogen production

# See also

* [Strategist](./ElectrolyzerStrategist.md)
* [Electrolyzer](./Electrolyzer.md)