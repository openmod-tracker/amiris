# In Short

Creates [BidSchedules](./BidSchedule.md) for an associated [LoadShiftingPortfolio](./LoadShiftingPortfolio.md) and minimising the total power payment obligations of the consumers, thus also accounting for their tariff structure.

# Details

ShiftConsumerCostMinimiserExternal is a [LoadShiftingStrategist](./LoadShiftingStrategist.md).
It uses "merit order sensitivities" of type [PriceNoSensitivity](./PriceNoSensitivity.md) to find the optimal dispatch path that maximises the load shifting portfolio's profits.
Hereby, end user tariff components are considered and included in the dispatch optimization.
The price components included may be static or dynamic placing different incentives.
The ShiftProfitMaximiserTariffs uses price information from an [EndUserTariff](./EndUserTariff.md) for the dispatch optimization.
The underlying portfolio is assumed to be homogeneous in terms of pricing.

## General strategy

The dispatch strategy is decided by calling an external optimisation model making use of the [UrlModelService](../Util/UrlModelService.md).
The model formulation itself relates to modelling a storage with additional inter-temporal constraints (i.e. balancing requirements).
The full formulation is laid down in Kochems (2024), pp. 101-105 and 135-136 and based on Gils (2015), pp. 67-70.
In order to account for own price repercussion, a time series containing sensitivity values, i.e. expected price change rates due to flexible load reactions, can be used in the optimization model.
In order to apply the strategy, the external optimisation model must be available and additionally, one of the supported solvers (gurobi, CPLEX, GLPK, CBC) has to be installed.

## Bidding

The Bids are made to resemble the expected price within the merit order after applying the changed demand or supply by ShiftConsumerCostMinimiserExternal.
A small price tolerance is included to force the scheduled dispatch.

# Literature

* Gils, Hans Christian (2015): Balancing of Intermittent Renewable Power Generation by Demand Response and Thermal Energy Storage. Dissertation. University of Stuttgart, Stuttgart. DOI: [10.18419/opus-6888](http://dx.doi.org/10.18419/opus-6888).
* Kochems, Johannes (2024): Lastmanagementpotenziale im deutschen Stromsystem. Einzelwirtschaftliche Bewertung gesamtwirtschaftlicher Potenzialschätzungen. Dissertation. TU Berlin, Berlin. DOI: [10.14279/depositonce-22008](https://doi.org/10.14279/depositonce-22008)