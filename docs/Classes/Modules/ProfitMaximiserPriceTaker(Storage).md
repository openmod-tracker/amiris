# In Short
Creates [BidSchedules](./BidSchedule) for an associated [StorageDevice](./Device) and thereby maximises profits of the storage operator ignoring potential impact on electricity prices.
Hence, this strategy can be considered as a "price taker".

# Details
ProfitMaximiserPriceTaker is an [ArbitrageStrategist](./ArbitrageStrategist). 
It uses "merit order sensitivities" of type [PriceNoSensitivity](./PriceNoSensitivity) to find the optimal storage utilisation that maximises its profits ignoring own impact on prices.

## General strategy
All possible dispatches within the forecast period are evaluated using dynamic programming **not** considering the changes of price induced to the merit order when the storage is dispatched.
Economic performance of this strategy highly depends on electricity price forecast accuracy and the robustness of the merit order against storage dispatch.
In principle, multiple (small) storage agents could be simulated simultaneously.
Warning: if storage device has significant impact on electricity market prices, storage revenues may be diminished (or even become negative). 

## Bidding strategy
ProfitMaximiserPriceTaker offers at minimum electricity price and requests at maximum electricity price. 

# See also
[ArbitrageStrategist](./ArbitrageStrategist)
