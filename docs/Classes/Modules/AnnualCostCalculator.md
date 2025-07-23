# In Short
Calculates annual costs for investment annuity and fixed operating expenses based on external inputs.
All inputs are set to zero by default if no external inputs are specified.
Can be used as module by all agents that track annuities and operating costs.
AnnualCostCalculator provides a `Tree` for its used input parameters that can be added to an agent's input Tree.

# Input
* `InvestmentExpensensesInEURperMW` Investment cost per installed production capacity in EUR/MW
* `AnnuityFactor` Dimensionless factor that represents the share of the total cost of investment in the current time period (typically a year). Actual annuity is derived from multiplying the investment cost and the annuity factor.
* `FixedCostsInEURperYearMW` Annual fixed cost per installed capacity in EUR/(a*MW); assumed constant 