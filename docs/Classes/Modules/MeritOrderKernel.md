# Short Description
Performs merit-order market clearing based on given [SupplyOrderBook](./SupplyOrderBook) and [DemandOrderBook](./DemandOrderBook). Those should contain all Bids and Asks for the same time period in question. 

# Details
The function takes two sorted OrderBooks for demand (descending by offerPrice) and supply (ascending by offerPrice).
The OrderBooks are also sorted ascending by cumulatedPower.
It is assumed that the price of the first element from demand exceeds that of the first supply element.
Additionally, the OrderBooks need to contain a final bid reaching towards positive / negative infinity for supply / demand to ensure the cut of the curves.
In addition, both demand and supply totals must have a positive energy value.
Sorting and bid structure is enforced in the OrderBook class.
The algorithm begins with the lowermost element from demand and supply.
It compares the demand and supply price from these elements.
In case the demand price is lower than the supply price, the condition for a cut of the discrete functions is met.
If no cut is found, the next element from demand and/or supply is selected, whichever has the lower cumulatedPower.
Then the cut condition is evaluated again.

# See also
* [MarketClearingResult](./MarketClearingResult)
* [DemandOrderBook](./DemandOrderBook) 
* [SupplyOrderBook](./SupplyOrderBook)