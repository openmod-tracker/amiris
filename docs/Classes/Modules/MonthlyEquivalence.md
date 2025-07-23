# In short
`MonthlyEquivalence` is a kind of [ElectrolyzerStrategist](./ElectrolyzerStrategist) for [GreenHydrogenTraderMonthly](../Agents/GreenHydrogenTraderMonthly).
It creates [BidSchedules](./BidSchedule) using forecasts for electricity prices, hydrogen price and the green electricity production potential provided via [Ppa](../Comms/PpaInformation).  
MonthlyEquivalence tries to utilise hours with the highest economic potential within the forecast period to produce hydrogen, thereby considering selling green electricity purchased via the PPA as an opportunity.

# Input
None

# Details
MonthlyEquivalence tries to maximise its profits from both, selling hydrogen or electricity, dependent on the price  forecast and the [PpaInformation](../Comms/PpaInformation) forecast.
Thereby, an important boundary condition is the so-called monthly equivalence, meaning that the total amount of the electrolyzer's demand may not exceed the energy procured from green electricity via a PPA on a monthly basis. 
An important implicit assumption is that the associated [GreenHydrogenTraderMonthly](../Agents/GreenHydrogenTraderMonthly) is contractually obliged to purchase the entire renewable generation from the PPA.

In order to schedule the electrolyzer dispatch, the grey electricity to purchase and the green electricity to sell accordingly, the
following solution strategy is applied:
1. The electrolyzer is scheduled to run for times with a production potential from the PPA and a positive power price not exceeding the hydrogen opportunity cost equivalent. 
   This is the value which marks the maximum willingness to pay for hydrogen, i.a. hydrogen price multiplied by efficiency of electrolyzer.
   In case the electrolyzer capacity is the limiting factor, the surplus generation from renewables is placed at 0 costs since it has to be purchased anyways.
   In case the price exceeds the willingness to pay, an offer with the full production volume from the PPA and the opportunity cost equivalent for this hour is placed. 
2. A surplus position value is calculated by forming the difference between the overall renewable power potential and the overall demand of the electrolyzer to run at full capacity at any time for which the power price does not exceed the hydrogen opportunity cost equivalent. 
   In case this value is positive, there are no restrictions and the electrolyzer can be operated at full load for these hours which is scheduled accordingly. 
   In case the value is negative, it won't be possible to operate the electrolyzer at full load at any time. 
   Thus, a more careful planning approach has to be taken which is described in the following.
3. Schedule grey electricity purchase (in case of a negative surplus position):
   * In order to maximise profits, the hours with the highest economic potential, which is given by the difference between the hydrogen opportunity cost equivalent and the forecasted power price - usually those with the lowest power prices -  are used first. 
     The algorithm selects hours in descending order of economic potential and decrements the surplus until all renewable surpluses are used and the renewable power production potential matches the electrolyzer demand.
   * It may be economically beneficial to trade hours with slightly negative prices and a renewable production potential for other hours when electricity can be procured with a high economic potential. 
     Therefore, in the next step, hours with the lowest costs for selling renewable electricity in times of negative prices are identified and matched to hours with the highest remaining economic potential.
     The electrolyzer schedule is adjusted accordingly, but since it is just an exchange of hours, the total production and surplus position remain unchanged.
4. Reschedule for extremely negative prices: There may be situations in which the price forecast is so low that it is even below the negative value of the PPA price for this hour. 
   Thus, economically, it makes sense to curtail the renewable generation for these hours and buy the respective volumes from the market. 
   In case there is no net surplus, the respective curtailment volumes need to be accounted for by reducing the electrolyzer demand for times with the lowest economic potential.

# See also
* [Strategist](./ElectrolyzerStrategist)
* [ElectrolyzerStrategist](./ElectrolyzerStrategist)
* [Electrolyzer](./Electrolyzer)
* [GreenHydrogenTraderMonthly](../Agents/GreenHydrogenTraderMonthly)