# In Short

`ElectrolyzerStrategist` is an abstract base class hosting common tasks for `Electrolyzer` strategists.
It extends the basic [Strategist(Flexibility)](./Strategist(Flexibility).md) class.
Currently, it has two children:
`FileDispatcher` reproduces a previously defined dispatch of an electrolyzer unit.
`SingleAgentSimple` performs a rough estimate of an optimal dispatch strategy with some flexibility regarding the time of the dispatch.

# Details

ElectrolyzerStrategist create [BidSchedules](./BidSchedule.md) for a specific electrolyzer unit using forecasted electricity prices and an estimate of how these could change depending on additional demand.
The ElectrolyzerStrategist hereby accounts for optional support payments for the produced hydrogen from [HydrogenSupportPolicy](../Agents/HydrogenSupportPolicy.md) as these effect opportunity costs of hydrogen production.
The extension of its parent [Strategist(Flexibility)](./Strategist(Flexibility).md) comprises storing hydrogen price forecasts and initialising the specific strategies.
ElectrolyzerStrategist should be hosted and used by an [ElectrolysisTrader](../Agents/ElectrolysisTrader.md) agent.

## Input

* `StrategistType` defines which type of strategist to use
* `FixedDispatch` Group, defined in [FileDispatcher](./FileDispatcher(Electrolysis).md)
* `Simple` Group, defined in [SingleAgentSimple](./SingleAgentSimple(Electrolysis).md)
* parameters defined in [Strategist(Flexibility)](./Strategist(Flexibility).md)
* `PriceLimitOverrideInEURperMWH` optional Time series; if present overrides electricity price bidding limit otherwise inferred from hydrogen price

# Child classes

* [FileDispatcher](./FileDispatcher(Electrolysis).md)
* [SingleAgentSimple](./SingleAgentSimple(Electrolysis).md)

# See also

* [Electrolyzer](Electrolyzer.md)
* [ElectrolysisTrader](../Agents/ElectrolysisTrader.md)
* [Strategist(Flexibility)](./Strategist(Flexibility).md)
* [HydrogenSupportPolicy](../Agents/HydrogenSupportPolicy.md)