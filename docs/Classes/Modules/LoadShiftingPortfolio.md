# In Short
Represents a portfolio of units that are eligible for load shifting and marketed as a portfolio. The units are assumed to be homogeneous in shifting times and (variable) costs associated with the shift.

# Details
A LoadShiftingPortfolio is characterized by its maximum allowed shift time, energy and power bounds as well as the variable costs for shifting energy. It serves to map back the energy states resulting from an optimized schedule to real world energy levels, whereby a positive load shift storage level corresponds to an advancing and a negative one to a delaying of loads compared to the baseline load time series.

The LoadShiftingPortfolio is capable to change the current load shift energy storage level as well as the current shift time using "updateEnergyShiftStorageLevelAndShiftTime". Hereby, it tracks whether a shift is prolonged. This can be achieved by shifting parts of a portfolio in one direction while other parts are shifted in the other direction simultaneously to compensate for a prior shift. This is only possible if it doesn't violate power bounds as well as the overall portfolio's power limitation, see [LoadShiftStateManager](./LoadShiftStateManager) for the details as well as Zerrahn and Schill (2014) for a scientific evaluation of this circumstance.

# Input from file
LoadShiftPortfolio defines a set of input parameters, which can be used to define the required inputs for Agents that control a LoadShiftPortfolio, i.e. the [LoadShiftingTrader](../Agents/LoadShiftingTrader). Required input parameters are:
* `InitialEnergyLevelInMWH`: The (fictitious) initial level of load shifting portfolio in MWh.
* `InitialShiftTimeInHours`: The time which the load shifting portfolio has already been shifted for at the start of the simulation in hours.
* `PowerInMW`: The maximum shiftable power of load shifting portfolio in MW. The actual power available in either direction is provided by combining this scalar value with the respective availability time series.
* `PowerUpAvailability`: Availability time series for power shifts in upwards direction (increased load), relative to `PowerInMW`.
* PowerDownAvailability: Availability time series for power shifts in downwards direction (decreased load), relative to `PowerInMW`.
* `EnergyResolutionInMWH`: The resolution of discrete energy steps for any strategist other than `ShiftConsumerCostMinimiserExternal` in MWh.
* `EnergyLimitUpInMWH`: The absolute energy limit for a shift in upwards direction (increased load) in MWh.
* `EnergyLimitDownInMWH`: The absolute energy limit for a shift in downwards direction (decreased load) in MWh.
* `MaximumShiftTimeInHours`: The maximum allowed time for a load shift in hours, including the initial hour of shifting. &rarr; Note that the current shift time is defined in such a way that a value of 1 means a shift already occuring for one hour. Hence it's maximum allowed value is `MaximumShiftTimeInHours` decreased by 1.
* `VariableShiftCostsInEURPerMWH`: The variable costs of a load shift given in EUR per MWh and attributed onto both, upshifts and downshifts.
* `BaselineLoadTimeSeries`: The originally planned load consumption before any load shifting given as a time series relative to `BaselinePeakLoadInMW`.
* `BaselinePeakLoadInMW`: The maximum peak load occuring before load shifting in MW.
* `Efficiency`: The efficiency of load shifting which must be between 0 and 1 (inclusive).
* `InterferenceTimeInHours`: The maximum allowed time for an upshift resp. a downshift as part of a shifting process, used only for strategist `ShiftConsumerCostMinimiserExternal`.
* `MaximumActivations`: The maximum number of full shift cycles over the course of the planning period (e.g. one year).

# Literature
Zerrahn, Alexander; Schill, Wolf-Peter (2015): On the representation of demand-side management in power system models. In: Energy 84, S. 840–845. DOI: [10.1016/j.energy.2015.03.037](https://doi.org/10.1016/j.energy.2015.03.037).