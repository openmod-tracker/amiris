# In short

The `PowerplantPrototype` comprises basic data of conventional power plants and is used in [PowerPlants](./PowerPlant) and [Portfolios](./Portfolio).

# In detail

Covered parameters are:

- cycling costs in Euro per MW,
- specific CO2-emissions for consumed thermal energy in tons per thermal MW,
- fuel type (e.g. hardcoal, lignite),
- planned and unplanned availability,
- variable costs in Euro per MWh, and
- must-run capacities

# Input

`PowerplantPrototype` also defines a ParameterTree of input parameters.
This set can be used in associated Agents to assemble their set of input parameters. Required inputs are:

- `FuelType` The different fuel types like coal, oil, etc.
- `OpexVarInEURperMWH` The variable cost of the respective power plant technology.
- `CyclingCostInEURperMW` Additional cycling costs for conventional power plants in Euro per Megawatt, i.e. costs due to plant start up
- `SpecificCo2EmissionsInTperMWH` specific CO2-emissions in tons per thermal MWh
- `PlannedAvailability` The planned availability of the respective power plant technology, e.g. scheduled downtime of plants.
- `UnplannedAvailabilityFactor` The unplanned availability of the respective power plant technology, due to, e.g., technological problems
- `MustRunFactor` share of the total installed capacity that must run at a given time due to, e.g., heat contracts, or reserve power obligations; the must-run factor will be capped at the product of planned and unplanned availability factors.  
