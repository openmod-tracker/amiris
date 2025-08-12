# AMIRIS terms
* `Award`: document that signifies that a supplier for a particular commodity or service specified in a bid has been accepted
* `AwardedPower`: power (production or demand) awarded to a specific agent (.e.g. trader, plant operator), or traded across the whole system 
* `Bid`: document describing an offer made by an agent in an effort to exchange commodities or services via terms relating to price and quantity
* `Biogas`: type of renewable plant operator
* `BlockSize`: typical installed net electricity production power for generation units within a conventional `PowerPlantPortfolio`; actual `InstalledPower` may deviate
* `Capacity`: installed net electricity production power for an agent; typically used with `RenewablePlant`s
* `CapacityPremium`: premium that guarantees remuneration for reserving capacity of energy transformation units to be available for unexpected events
* `CarbonMarket`: market place emitting and trading CO<sub>2</sub> emission rights
* `ChargingEfficiency`: relative efficiency at charging an electricity `Storage` device; takes values between 0 and 1
* `Co2Emissions`: emitted CO<sub>2</sub> during electricity production
* `CO2Price`: market price for CO<sub>2</sub> emission allowances
* `ContractForDifference`: symmetric market premium model: premium that guarantees a reference value for electricity fed-in compared to an average market performance; if average market performance is above reference value, the difference must be paid back   
* `ConventionalPlant`: power plant for electricity production fuelled by non-renewable fuels (including nuclear power plants)
* `ConventionalTrader`: trades the power of non-renewable power plants
* `CyclingCost`: cost ramping a power plant from 100% to 0% back to 100% in EUR per MW
* `Demand`: a demand for energy
* `DemandTrader`: trader that purchases energy
* `DischargingEfficiency`: see `ChargingEfficiency`
* `DispatchAssignment`: tells a plant operator how much energy is to be produced
* `Efficiency`: ratio between output and input of an energy conversion process, e.g., in power plants or storage systems
* `ElectricityPrice`: given in EUR per MegaWattHour
* `EnergyCarrier`: type of renewable energy electricity generation; may depend on primary energy sources, energy conversion techniques, or position of the conversion device
* `DayAheadMarket`: day-ahead spot market for electricity  
* `EnergyToPowerRatio`: ratio between (dis-)charging power and maximum energy content of an electricity storage system
* `Feed-inTariff`: policy instrument designed to stimulate investment in renewable energy technologies by offering long-term contracts to agents who are producers of renewable energy
* `Fee`: an economic value that represents an amount of money paid for a particular piece of work or for a particular right or service 
* `ForecastError`: error associated with a forecasted value, e.g. electricity price or feed-in potential
* `ForecastedPrice`: forecast of an (electricity) price at a given time
* `ForecastPeriod`: time delta between current time and time for which a forecast is obtained
* `FuelPrice`: market price of a specific type of fuel, e.g. hard-coal
* `FuelPriceRequest`: request for the price of a specific fuel at a given time
* `FuelsMarket`: marketplace for fuel
* `FuelType`: type of fuel, e.g. HARD_COAL or OIL
* `FullLoadHours`: (actual or assumed) electricity generation per year measured in hours with full utilisation of installed `Capacity`  
* `GasCC`: combined-cycle conventional gas power plant
* `Gasturbine`: open-cycle conventional gas power plant
* `Hardcoal`: Anthracite; a hard coal with a high caloric value due to its high carbon content (about 90 % fixed carbon)
* `HydroReservoir`: hydro-electric power plant with capability to retain a certain amount of the inflow water within a reservoir 
* `InitialEnergyLevel`: energy stored within a `Storage` device at the beginning of the simulation
* `InstalledPower`: net electricity production power of an agent or `PowerPlant`; see also `Capacity`
* `LCOE`: Levelised cost of electricity; net cost of converting energy to electricity over the lifetime of an energy transformation unit; They are calculated considering all relevant costs for, e.g., investment, operation, maintenance, fuels, etc.
* `Lignite`: brown coal with a lower caloric value that `Hardcoal` and higher specific CO<sub>2</sub> emissions per released thermal energy
* `Load`: actual energy demand of an agent or energy system; typically represented as TimeSeries
* `MarginalCost`: economic value that corresponds to the cost of producing one more unit of a good (typically electric energy)
* `Markdown`: economic value that indicates a virtual amount deducted from an assumed or historical price to predict a price in a different context
* `MarketRevenues`: revenues generated at a market for selling goods
* `Markup`:  economic value that indicates a virtual amount added to an assumed or historical price to predict a price in a different context
* `MeritOrder`: a plan specification that describes the dispatch ranking of power plants (electrical generation) based on ascending order of price
* `NaturalGas`:  mixture of combustible gases, typically with lower specific CO<sub>2</sub> emissions per released thermal energy compared to `Hardcoal` or `Lignite`
* `Nuclear`: fuel used in nuclear power plants with negligible specific CO<sub>2</sub> emissions per released thermal energy
* `OfferedPower`: power (production or demand) offered / requested by a specific agent (.e.g. trader, plant operator), or traded across the whole system 
* `Oil`: petroleum; a combustible liquid  
* `Opex`: operating expenses for e.g. electricity production 
* `OpexFix`: fixed `Opex`, i.e. expenses that are independent of the actual amount of goods produced
* `OpexVar`: variable `Opex`, i.e. expenses that are linearly increasing with the actual amount of goods produced
* `PV`: photovoltaics; type of `EnergyCarrier`; electricity generation from light
* `PlannedAvailability`: factor of available power at a given time relative to installed power; typically a TimeSeries reflecting scheduled downtimes of power plants due to, e.g., maintenance 
* `Policy`: system of principles, rules and guidelines, adopted by an organisation to guide decision making with respect to particular situations and implemented to achieve stated goals
* `PolicyInstrument`: governmental action intended to promote the adoption of a (transformative) measure
* `Portfolio`: union of units (controlled by an agent) related to electricity production, demand or storage
* `PowerPlant`: electricity production device
* `PowerPlantOperator`: agent that operates an electricity production device
* `PowerPlantPortfolio`: particular mix of power plants of one agent
* `PowerPurchaseAgreement`: also known as `PPA`, an agreement between two parties where one party provides power and the other party grants money; electricity between parties is usually transferred via the grid  
* `PPA`: see `PowerPurchaseAgreement`
* `Premium`: `Remuneration` paid to an agent for implementing actions in line with a `PolicyInstrument`; typically granted for providing power or feed-in of energy 
* `PumpedHydro`: pumped hydroelectric energy storage plant, see also `Storage`
* `Remuneration`: financial compensation provided in exchange for services performed
* `RenewablePlant`: type of `PowerPlant` based on a renewable primary energy source; typically represents a union of many individual physical power generation units  
* `RunOfRiver`: run-of-river hydroelectric power plant; has negligible capacity to retain the inflow of water compared to `HydroReservoir` plants
* `SelfDischargeRate`: rate at which a `Storage` looses energy due to self discharge per time relative to current level of charged energy; (value from 0 to 1), typically relative loss per hour
* `Storage`:  an electricity storage device, i.e. a unit that can be charged with electricity and returns electricity on discharge (ignoring details of actual energy conversion, e.g., to chemical energy)
* `StorageStrategy`: logic applied to determine a dispatch using provided information about associated future values (e.g. expected electricity prices or system costs)
* `StoredEnergy`: (gross) energy currently stored within a `Storage` device
* `SystemCost`: total costs of a system - in AMIRIS this covers only costs for dispatching of power plants and flexibility options
* `UnplannedAvailability`: factor of available power at a given time relative to installed power of a power plant; reflecting unexpected down-times due to, e.g., malfunctions; see also `PlannedAvailability`
* `ValueOfLostLoad`: customers' willingness-to-pay for electricity, i.e. at higher prices customers accept not to be served    
* `VariableMarketPremium`: asymmetric market premium model: premium that guarantees a reference value for electricity fed-in compared to an average market performance; if average market performance is above reference value, the difference is not paid back 
* `VariableCosts`: costs that depend on an amount of goods or services, see `OpexVar`
* `Waste`: solid waste, low-caloric fuel 
* `WindOffshore`: `EnergyCarrier` for wind farms built on the sea 
* `WindOnshore`: `EnergyCarrier` for wind farms built on land
* `YieldProfile`: time-dependent profile factor of the feed-in potential of variable renewable power plants; values between 0 and 1    

# FAME terms
* `Agent`: Basic unit of each FAME-based application; agents act and interact with other agents via `Messages` to create the simulation experience.  
* `DataItem`: Abstract base class for `Message` content with low complexity (i.e. a flat data structure). See `Portable` for more complex Message content.
* `Message`: The only way of `Agent`s to interact; Messages can contain content of type `Portable` or `DataItem`.
* `Portable`: An interface for `Message` content with higher complexity. Supports hierarchical data structures. Inner elements must implement Portable as well. See `DataItem` for simpler Message content. 
* `Protobuf`: [Google Protocol Buffers](https://developers.google.com/protocol-buffers) is a flexible way to define binary data storage format. FAME employs this to define own binary data types for, e.g. input and output data of simulations as well as for inter-process communication during the simulations.
* `TimeStamp`: Representation of simulated time in FAME. *Beware* This does not exactly match real-world time stamps. See this [article](https://gitlab.com/fame-framework/wiki/-/wikis/architecture/decisions/Time).

Please also refer to the [FAME-Glossary](https://gitlab.com/fame-framework/wiki/-/wikis/Glossary) for additional explanations.