# 42 words

The NoSupportTrader is an [AggregatorTrader](./AggregatorTrader.md) to market renewable generation that doesn't receive any support payments.
It trades energy of associated power plants at the [DayAheadMarket](./DayAheadMarket.md).

# Details

In contrast to other AggregatorTraders, it does not request support, but only reports yield potentials of associated power plants to the SupportPolicy.
Thus, many functionalities of [AggregatorTrader](./AggregatorTrader.md) are not bypassed. 

It pays out only market revenues to its clients.
Thereby, it may keep a certain share of the market revenues - specified in the input data.

# Dependencies

see [Trader](./Trader.md)

# Input from file

* `ShareOfRevenues`: Share of market revenues the NoSupportTrader keeps to itself (double $`\in [0,1]`$)

see [AggregatorTrader](./AggregatorTrader.md)

# Input from environment

see [AggregatorTrader](./AggregatorTrader.md)

# Simulation outputs

see [AggregatorTrader](./AggregatorTrader.md)

# Contracts

see [AggregatorTrader](./AggregatorTrader.md)

# Available Products

see [AggregatorTrader](./AggregatorTrader.md)

# Submodules

None

# Messages

see [AggregatorTrader](./AggregatorTrader.md)

# See also

* [AggregatorTrader](./AggregatorTrader.md)