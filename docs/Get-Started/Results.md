# AMIRIS Result Files

There can be two different types of AMIRIS output files: Standard and Complex Outputs.

## Standard Output Files

Open the output folder created using AMIRIS-Py or FAME-Io.
If you have followed either the Quickstart Guide, the AMIRIS-Py Step-by-Step Guide or the FAME-Io Step-by-Step Guide, you should have a folder called "simple" with several CSV files in it.

Each agent type has its own output file.
Open the files with your favourite CSV editor or spreadsheet application.
The files will have the following general structure:

| AgentId | TimeStep | Col1 | Col2 | Col3 | ... |
|---------|----------|------|------|------|-----|

where:

* `AgentId` refers to the unique ID of that agent - as specified in the input scenario.yaml
* `TimeStep` refers to the time step at which the output was created
* `Col1`, `Col2`, `Col3`, ... refer to the agent-type specific output columns

There can be any number of output columns, depending on the type of the agent.
Here, `AgentId` and `TimeStep` form a 2-column multi-index.
Thus, each agent can only write one value per column and simulation time.
For example, open the "DayAheadMarketSingleZone.csv".
The Agent with ID 1 is the only one of type DayAheadMarketSingleZone - so this column is of no interest in this file.
The fourth column is called "ElectricityPriceInEURperMWH" and contains the market-clearing day-ahead electricity prices.

Although in this file, all columns are filled in every time step, this is not the case for all types of agents.
Some agents write their column entries at slightly different time steps.
This is due to the fact that the simulation saves the output data at the time when the action is performed.
If you use AMIRIS-Py, the time stamps are adjusted to reduce the number of rows with empty columns.

## Complex Output Files

Some types of agents need to write more than one output per time step.
For example, the conventional plant operators can write out the dispatched power for each power plant of each agent and each time step.
This option is disabled by default to reduce the amount of data.

### Activate Complex Outputs

To activate complex outputs:

1. Create a file named `fameSetup.yaml` and insert the line `output-complex: True`.
2. Place the file in the same folder with the file `amiris-core_x.y.z-jar-with-dependencies.jar`.
3. Rerun the simulation.

The output file should now be larger and the output folder should contain more CSV files.

### Complex Output Files

Each complex output will be assigned an extra CSV file named "AgentType_MultiindexColumn".
These files can contain an N-dimensional-multi-index with a single "value" column like

| AgentId | TimeStep | 3rd index | 4th index | ... | Value |
|---------|----------|-----------|-----------|-----|-------|

In this example of "ConventionalPlantOperator_DispatchedEnergyInMWHperPlant.csv", `AgentId`, `TimeStep` and `ID` of the power plant form a 3-dimensional multiindex.
Each index is assigned a single value for "DispatchedEnergyInMWHperPlant".
To disable complex column output, simply set `output-complex: False` in `fameSetup.yaml` (or delete the file, since the default value for complex indices is False)
