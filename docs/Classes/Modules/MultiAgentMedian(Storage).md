# In Short
Creates [BidSchedules](./BidSchedule) for an associated [StorageDevice](./Device) based on a price interval and price median.
This storage Strategist can be used on multiple storage agents in the same simulation and can cope reasonably well with errors of the price forecast.

# Details
## General strategy
MultiAgentMedian is an [ArbitrageStrategist](./ArbitrageStrategist).
It uses [Price forecasts without sensitivity](./PriceNoSensitivity).
To create a dispatch schedule, MultiAgentMedian uses forecasted electricity prices to calculate a price median within the forecast period.
Based on this price median and on the charging and discharging efficiencies of the connected storage device, safety margins for charging and discharging are defined.
Then, for all times when prices are below the charging safety margin, the strategist will try to charge and for all times with prices above the discharging safety margin, the strategist will try to discharge.
For each time interval that is continuously below (above) the safety margins, the strategist will try to fully charge (discharge) the storage device.
It will not consider its own impact on the electricity prices.
The strategist will use an `assessment function` of the prices to determine how much energy to (dis-)charge in each time segment.
The (dis-)charged energy will be proportional to the assessment function's value in the given time - also considering the storage device power limits. 

## Bidding
For bidding, the safety margins will be used to define the price of the offered / requested energy.
The offered / requested energy amount will be depending on the forecasted electricity price at that time and the specified assessment function parameters.

## Charge / Discharge amount assessment
For each block of consecutive times with charging or discharging, MultiAgentMedian will try to fill / deplete the storage device completely (respecting the maximum charge & discharge power, of course).
The energy to charge / discharge for each time will depend on 
* x: the delta between the appropriate safety margin and the forecasted electricity price
* f(x): the value of the polynomial assessment function
* a,b,c,...: polynomial coefficients

For each of a charging or discharging block of time segments, x & f(x) is calculated using the polynomial relation:

```math
f(x) = a + b \cdot x + c \cdot x^2 + d \cdot x^3 + ... 
```

Then, the charging power at each time is chosen proportional to f(x), such that the sum of the charged energy fills the storage completely / the discharging power is chosen such that the sum of the discharged energy equals the current energy in storage.