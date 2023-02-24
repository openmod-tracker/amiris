<!-- SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>

SPDX-License-Identifier: Apache-2.0 -->
# AMIRIS
AMIRIS is the **A**gent-based **M**arket model for the **I**nvestigation of **R**enewable and **I**ntegrated energy **S**ystems.

It is an agent-based simulation of electricity markets and their actors.
AMIRIS enables researches to analyse and evaluate energy policy instruments and their impact on the actors involved in the simulation context.
Different prototypical agents on the electricity market interact with each other, each employing complex decision strategies. 
AMIRIS allows calculating the impact of policy instruments on economic performance of power plant operators and marketers.
It is based on [FAME](https://gitlab.com/fame-framework), the open Framework for distributed Agent-based Modelling of Energy systems.

Please have a look at the [AMIRIS-Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/home) to get further information.
Do not hesitate to ask questions about AMIRIS at the [openMod-Forum](https://forum.openmod.org/tag/amiris).

## Recommended Skills
AMIRIS is a *JAVA* application configured via *Python* scripts.
No programming skills are necessary to configure and run AMIRIS simulations.
However, AMIRIS developers should have at least basic experiences with both languages.

## System Requirements
To run AMIRIS, Python 3.8 or higher and Java Development Kit (JDK) version 8 or higher are required.
In case you want to modify the AMIRIS code, additional tools might be required.
See our [Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/GetStarted/Getting-started) for additional instructions.

### JDK
AMIRIS is based on the Java tool [FAME](https://gitlab.com/fame-framework), the open Framework for distributed Agent-based Modelling of Energy systems.
It requires a JDK version 8 or higher and has been tested to work with JDK 8, 11 and 17.
You can test if you have a JDK by using the command `java --version` (or `java -version` on some systems).
This should show your Java version if Java was found.
If you get a command not found error, or if Java version is less than 8 please download and install a recent JDK from e.g. [here](https://adoptium.net/).

### Python
You will need a Python-enabled shell with Python 3.8 or higher and pip.
You can test if you have Python available by using the command `python --version`.
This should show your Python version if the Python command was found.
If you do not have Python installed on your system, you may use e.g. [conda](https://docs.conda.io/en/latest/miniconda.html) or [mamba](https://github.com/conda-forge/miniforge#mambaforge).

#### Set up Python Environment
In case you do not have any experience with creating a Python environment, we recommend to use [anaconda ](https://www.anaconda.com/).
Install anaconda, start the anaconda prompt or powershell and enter:

1. `conda create -n amirisEnv python=3.8`
2. `conda activate amirisEnv`

In case you are using mamba, simple replace "conda" in the first command with "mamba" (but not in the second).

### Get AMIRIS-Py
We recommend to use [AMIRIS-Py](https://gitlab.com/dlr-ve/esy/amiris/amiris-py/-/blob/main/README.md).
AMIRIS-Py provides "one-command" installation and execution scripts, but you may also run AMIRIS using FAME scripts (see [here](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/GetStarted/Getting-started)).
In your AMIRIS Python environment (called "amirisEnv" above), run

```
pip install amirispy
```

## Setup with AMIRIS-Py
1. Create a new folder on your disk called, e.g., "AMIRIS": `mkdir <AMIRIS>`
2. Open your Python-enabled shell and navigate to this newly created folder: `cd <AMIRIS>` 
3. If not done yet, activate your Python environment with amiris-py: `conda activate <amirisEnv>`
4. To download the latest AMIRIS build use: `amiris install`. This downloads the latest [AMIRIS model](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/jobs/artifacts/main/download?job=deploy:jdk8) and the latest version of [AMIRIS examples](https://gitlab.com/dlr-ve/esy/amiris/examples) into the current folder.

Your "AMIRIS" folder should now look like this:

```
AMIRIS
├─── examples
│    ├─── Austria2019/
│    ├─── Germany2019/
│    ├─── Simple/
│    └─── README.md
├─── amiris-core_X.y.z-with-dependencies.jar
└─── fameSetup.yaml
```

You are now ready to execute AMIRIS.

## Run AMIRIS with AMIRIS-Py
Use amirispy again to run AMIRIS:

```
amiris run -j ./amiris-core_1.2.3.4-jar-with-dependencies.jar -s ./examples/Simple/scenario.yaml -o simple
```

This runs the packaged AMIRIS Java archive (Jar) file specified after the `-j` option and simulates the scenario specified after the `-s` option.
The AMIRIS outputs are stored in a folder as designated after the `-o` option. 
Check out the files in the AMIRIS folder - if a newer version of AMIRIS was installed, use the version code of the jar file you downloaded.

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

## Available Support
This is a purely scientific project by (at the moment) one research group. 
Thus, there is no paid technical support available.
However, we will give our best to answer your questions and provide support.

If you experience any trouble with AMIRIS, you may contact the developers at the [openMod-Forum](https://forum.openmod.org/tag/amiris) or via [amiris@dlr.de](mailto:amiris@dlr.de).
Please report bugs and make feature requests by filing issues following the provided templates (see also [CONTRIBUTING](CONTRIBUTING)).
For substantial enhancements, we recommend that you contact us via [amiris@dlr.de](mailto:amiris@dlr.de) for working together on the code in common projects or towards common publications and thus further develop AMIRIS.
