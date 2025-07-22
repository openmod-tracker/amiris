# 42 words

`FlexibilityTrader` is an intermediary abstract base class derived from `Trader`.
It groups Traders operating a flexibility asset like a storage device, heat pump or electrolysis unit and provides forecasting routines for them.
FlexibilityTrader adds an `AnnualCostCalculator` and associated inputs and outputs to Traders.
It also adds an action to report these costs (write them to the output).

# Details

FlexibilityTrader adds new input and output fields to its children to calculate annual costs (annuities and fixed cost).
It also comprises a new action to report these costs.
This new action should be scheduled using a contract with itself.

# Dependencies

FlexibilityTrader uses [AnnualCostCalculator](../Modules/AnnualCostCalculator.md) to calculate annuities and fixed costs.

# Input from file

Additional inputs used by [AnnualCostCalculator](../Modules/AnnualCostCalculator.md) are added to the inputs using a parameter group name `Refinancing`.
See also [Trader](./Trader.md)

# Input from environment

See [Trader](./Trader)

# Simulation outputs

* `FixedCostsInEUR`: Fixed cost of flexible device in EUR
* `InvestmentAnnuityInEUR`: Investment annuity of flexible device in EUR
* `VariableCostsInEUR`: Variable cost of flexible device in EUR
* `ReceivedMoneyInEUR`: Overall received money in EUR

see also [Trader](./Trader.md)

# Contracts

* FlexibilityTrader (Self): Execute annual cost reports and write to output

# Available Products

* `MeritOrderForecastRequest`: A request for a (perfect foresight) merit order prognosis
* `PriceForecastRequest`: A request for a (perfect foresight) electricity price prognosis
* `AnnualCostReport` Used to trigger annual cost reporting, requires no message to be sent / received 
See also [Trader](./Trader.md)

# Submodules

None, see also [Trader](./Trader.md)

# Messages

None, see also [Trader](./Trader.md)

# See also

* [Trader](./Trader.md)
* [AnnualCostCalculator](../Modules/AnnualCostCalculator.md)
