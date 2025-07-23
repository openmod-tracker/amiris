# In Short

`ElectrolyzerStrategist` is an abstract base class hosting common tasks for `Electrolyzer` strategists.
It extends the basic [Strategist(Flexibility)](./Strategist(Flexibility)) class.
Currently, it has two children:
`FileDispatcher` reproduces a previously defined dispatch of an electrolyzer unit.
`SingleAgentSimple` performs a rough estimate of an optimal dispatch strategy with some flexibility regarding the time of the dispatch.

# Details

ElectrolyzerStrategist create [BidSchedules](./BidSchedule) for a specific electrolyzer unit using forecasted electricity prices and an estimate of how these could change depending on additional demand.
The ElectrolyzerStrategist hereby accounts for optional support payments for the produced hydrogen from [HydrogenSupportPolicy](../Agents/HydrogenSupportPolicy) as these effect opportunity costs of hydrogen production.
The extension of its parent [Strategist(Flexibility)](./Strategist(Flexibility)) comprises storing hydrogen price forecasts and initialising the specific strategies.
ElectrolyzerStrategist should be hosted and used by an [ElectrolysisTrader](../Agents/ElectrolysisTrader) agent.

## Input

* `StrategistType` defines which type of strategist to use
* `FixedDispatch` Group, defined in [FileDispatcher](./FileDispatcher(Electrolysis))
* `Simple` Group, defined in [SingleAgentSimple](./SingleAgentSimple(Electrolysis))
* parameters defined in [Strategist(Flexibility)](./Strategist(Flexibility))
* `PriceLimitOverrideInEURperMWH` optional Time series; if present overrides electricity price bidding limit otherwise inferred from hydrogen price

# Child classes

* [FileDispatcher](./FileDispatcher(Electrolysis))
* [SingleAgentSimple](./SingleAgentSimple(Electrolysis))

# See also

* [Electrolyzer](Electrolyzer)
* [ElectrolysisTrader](../Agents/ElectrolysisTrader)
* [Strategist(Flexibility)](./Strategist(Flexibility))
* [HydrogenSupportPolicy](../Agents/HydrogenSupportPolicy)