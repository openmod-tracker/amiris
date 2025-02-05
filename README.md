<!-- SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>

SPDX-License-Identifier: Apache-2.0 -->
![AMIRIS_Logo](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/uploads/AMIRIS_LogoWTitle.png)

## _Simulate electricity markets emerging from interactions of producers, consumers, and flexibilities_

[![Pipeline status](https://gitlab.com/dlr-ve/esy/amiris/amiris/badges/main/pipeline.svg)](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/commits/main)
[![REUSE status](https://api.reuse.software/badge/gitlab.com/dlr-ve/esy/amiris/amiris/)](https://api.reuse.software/info/gitlab.com/dlr-ve/esy/amiris/amiris/)
[![JOSS paper](https://joss.theoj.org/papers/10.21105/joss.05041/status.svg)](https://joss.theoj.org/papers/10.21105/joss.05041)
[![Zenodo](https://img.shields.io/badge/Research-Zenodo-blue)](https://zenodo.org/communities/amiris)
[![Last Commit](https://img.shields.io/gitlab/last-commit/dlr-ve/esy/amiris/amiris)](https://gitlab.com/fame-framework/fame-io/-/commits/main)

AMIRIS is an agent-based simulation of electricity markets and their actors.

Want to get started right away? Check out our [Quick Start Guide](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/GetStarted/QuickStart).

You can find our full documentation at the [AMIRIS-Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/home).

## What is AMIRIS?

AMIRIS enables you to simulate the interplay between electricity market actors, policy instruments, and scenarios.
Due to its prototypical definitions of agents, you can model decisions on a high level and simulate trans-national electricity systems, e.g., Europe.
At the same time you can also model very detailed decisions, e.g. from households.
Whatever you do: Thanks to the powerful framework [FAME](https://gitlab.com/fame-framework), AMIRIS will deliver results quickly: Its typical **runtime is below a minute** for a market zone in hourly resolution.

## Applications

AMIRIS development started 2008 and has seen many different applications since.

### Evaluate Energy Policy Instruments
- TradeRES: support instruments in 100% renewable electricity systems
- Diss Johannes: load shifting potentials

### Assess Impact of Extreme Events
- VERMEER: Dunkelflaute
- Evelyn: Paper Gas Price

### Model Household Decisions
- Diss Farzad: households
- En4U: heatpump, ev, pvs

Different prototypical agents on the electricity market interact with each other, each employing complex decision strategies. 
AMIRIS allows calculating the impact of policy instruments on economic performance of power plant operators and marketers.

## Non-Applications

AMIRIS follows an explorative approach: its is perfect to explores emerging effects created by energy system actors and their interactions under a given set of assumptions.
While individual actors can optimise their decisions, AMIRIS does not optimise the energy system as a whole.
Also, AMIRIS cannot enforce system-wide constraints like, e.g., a carbon emission cap.
Thus, if you want to answers question like "What is the cheapest possible electricity system considering a maximum carbon emission of X?", we recommend using optimisation-based tools, e.g., [REMix](https://gitlab.com/dlr-ve/esy/remix/framework), [PyPSA](https://pypsa.org/), or [oemof](https://oemof.org/).

## Community
AMIRIS is mainly developed by the German Aerospace Center's Institute of Networked Energy Systems.
We provide multi-level support for AMIRIS users: please see our dedicated [Support Page](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Community/Support).

We welcome any contributions: bug reports, feature request, and, of course code contributions.
Please follow our [Contribution Guidelines](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Community/Contribute).

## Citing AMIRIS

If you use AMIRIS in your scientific work please cite: [doi: 10.21105/joss.05041](https://doi.org/10.21105/joss.05041)

## Acknowledgements

Development of AMIRIS was funded by the German Aerospace Center, the German Federal Ministry for Economic Affairs and Climate Action, the German Federal Ministry of Education and Research, and the German Federal for the Environment, Nature Conservation and Nuclear Safety. 
It received funding from the European Union’s Horizon 2020 research and innovation programme under grant agreement No 864276.
We express our gratitude to all [contributors](CONTRIBUTING.md#list-of-contributors).

# Old Stuff - move to wiki
## Recommended Skills
AMIRIS is a *JAVA* application configured via *Python* scripts.
To configure and run AMIRIS applications, no programming skills are strictly necessary, but experience with energy system modelling and Python is helpful.
Developers, who want to modify the functionality or enhance the capabilities of AMIRIS, however, should have at least basic understanding of Java.
In addition, a basic understanding of [(FAME)](https://gitlab.com/fame-framework) is required in order to design new agents and their interactions.

## Get Started
### System Requirements
AMIRIS is based on [FAME](https://gitlab.com/fame-framework), the open Framework for distributed Agent-based Modelling of Energy systems.
To run AMIRIS, Python 3.9 or higher and Java Development Kit (JDK) 11 or higher are required.
In case you want to modify the AMIRIS code, additional tools might be required.
See our [Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/GetStarted/Getting-started) for additional instructions.

#### Java
AMIRIS requires JDK version 11 or higher and has been tested to work with versions 11, 17, and 21.
You can test if you have a JDK by using the command `java --version` (or `java -version` on some systems).
This should show your Java version if Java was found.
If you get a command not found error, or if Java version is less than 11 please download and install a recent JDK from e.g. [here](https://adoptium.net/).

#### Python
You will need a Python-enabled shell with Python 3.9 or higher and pip.
You can test if you have Python available by using the command `python --version`.
This should show your Python version if the Python command was found.
Note that if you use a Python environment manager you can have several Python versions on your system side by side.
If you do not have Python installed on your system, you may use e.g. [conda](https://docs.conda.io/en/latest/miniconda.html) or [mamba](https://github.com/conda-forge/miniforge#mambaforge) or [Poetry](https://python-poetry.org/).

### Set up Python Environment
In case you do not have any experience with creating a Python environment, we recommend to use [anaconda](https://www.anaconda.com/).
Install anaconda, start the anaconda prompt or powershell and enter:

1. `conda create -n amirisEnv python=3.9`
2. `conda activate amirisEnv`

In case you are using mamba, simple replace "conda" in the first command with "mamba" (but not in the second).

### Install AMIRIS-Py
We recommend to use [AMIRIS-Py](https://gitlab.com/dlr-ve/esy/amiris/amiris-py/-/blob/main/README.md).
AMIRIS-Py provides "one-command" installation and execution scripts, but you may also run AMIRIS using FAME scripts (see [here](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/GetStarted/Getting-started)).
In your AMIRIS Python environment (called "amirisEnv" above), run

```
pip install amirispy
```

### Download AMIRIS
1. Create a new folder on your disk called, e.g., "AMIRIS": `mkdir <AMIRIS>`
2. Open your Python-enabled shell and navigate to this newly created folder: `cd <AMIRIS>` 
3. If not done yet, activate your Python environment with amiris-py: `conda activate <amirisEnv>`
4. To download the latest AMIRIS build use: `amiris install`. This downloads the latest [AMIRIS model](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/jobs/artifacts/main/download?job=deploy:jdk11) and the latest version of [AMIRIS examples](https://gitlab.com/dlr-ve/esy/amiris/examples) into the current folder.

Your "AMIRIS" folder should now look like this:

```
AMIRIS
├─── examples
│    ├─── Austria2019/
│    ├─── Germany2019/
│    ├─── ...
│    ├─── Simple/
│    └─── README.md
├─── amiris-core_X.y.z-with-dependencies.jar
└─── fameSetup.yaml
```

You are now ready to execute AMIRIS.

## Run AMIRIS with AMIRIS-Py
Use amiris-py again to run AMIRIS:

```
amiris run -j ./amiris-core_3.1.0-jar-with-dependencies.jar -s ./examples/Simple/scenario.yaml -o simple
```

This runs the packaged AMIRIS Java archive (Jar) file specified after the `-j` option and simulates the scenario specified after the `-s` option.
The AMIRIS outputs are stored in a folder as designated after the `-o` option. 
Check out the files in the AMIRIS folder - use the version code of the jar file you downloaded.

## Results 
Open the created output folder called e.g. "simple".
Each type of agent has its own output file in CSV format.
Open the files in your favourite CSV editor.
The files take the following general structure:

| AgentId | TimeStep | Col1 | Col2 | Col3 | ... |
|---------|----------|------|------|------|-----|

where:
* `AgentId` refers to the unique ID of that agent - as specified in the input scenario.yaml 
* `TimeStep` refers to the time step at which the output was created; the number refers to the passed seconds since January 1st 2000, 00:00h (ignoring leap years). To convert to a human-readable time stamp best use the python function `fameio.source.time.FameTime.convert_fame_time_step_to_datetime`
* `Col1` refers to the agent-type specific first output column
* `Col2` refers to the agent-type specific second output column
* `...` there can be arbitrarily many output columns - depending on the type of the agent 

Here, `AgentId` and `TimeStep` form a 2-column multiindex.
Thus, each agent can only write one value per column and simulation time.
For example, open the "EnergyExchange.csv". 
The Agent with ID 1 is the only one of type EnergyExchange - so this column is kind of uninteresting in this file.
The fourth column is named "ElectricityPriceInEURperMWH" and contains the market-clearing day-ahead electricity prices.

Although in this file, all columns are filled in every time step, this is not the case for all types of agents.
Some agents write their column entries at slightly different time steps.
This is caused by the simulation, which saves output data at the time step the action is performed.

Some types of agents need to write out more than one output per time step.
E.g., the conventional plant operators writes out the dispatched power for every power plant of each agent and each time step.
Such output will be assigned an extra CSV file named "AgentType_MultiindexColumn".
These files can feature an N-dimensional-multiindex with a single "value" column like so:

| AgentId | TimeStep | 3rd index | 4th index | ... | Value |
|---------|----------|-----------|-----------|-----|-------|

In this example of "ConventionalPlantOperator_DispatchedPowerInMWHperPlant.csv", `AgentId`, `TimeStep` and `ID` of the power plant form a 3D-multiindex.
Each index is assigned a single value for "DispatchedPowerInMWHperPlant".

## Next Steps
Congratulations, you have now successfully run AMIRIS. 
You want to see which inputs led to those results? See the input scenario at "./examples/Simple/scenario.yaml".
Or do you want to create your own simulation configuration or how to modify AMIRIS?
Check out the [AMIRIS-Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/GetStarted/Getting-started).
Please also refer to the [FAME-Wiki](https://gitlab.com/fame-framework/wiki/-/wikis) when applying more advanced adaptations to your scenario, such as changing the [simulation duration](https://gitlab.com/fame-framework/wiki/-/wikis/GetStarted/core/Contracts). 
