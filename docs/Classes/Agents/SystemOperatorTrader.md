# 42 words

The SystemOperatorTrader is an [AggregatorTrader](./AggregatorTrader.md) who trades renewable energy eligible for a `FIT` support scheme.
It trades energy of associated power plants at the [DayAheadMarket](./DayAheadMarket.md).

# Details

The SystemOperatorTrader creates [Bids](../Comms/BidsAtTime.md) based on the power production potential contained in [MarginalsAtTime](../Comms/MarginalsAtTime.md) data from linked PowerPlantOperators.
Actual costs from this data set, however, are ignored during Bid creation as the SystemOperatorTrader uses the DayAheadMarket's minimal price.
This resembles the priority feed-in of FIT-marketed renewable energy.

The SystemOperatorTrader passes the received FIT support to its customers while the received market revenues are ignored.
In reality, these are used to cover for the costs of marketing and to reduce the EEG difference costs as a basis for determining the EEG levy.

# Dependencies

see [AggregatorTrader](./AggregatorTrader.md)

# Input from file

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

see [AggregatorTrader](./AggregatorTrader.md)

# Messages

see [AggregatorTrader](./AggregatorTrader.md)