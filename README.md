# AMIRIS
AMIRIS is the **A**gent-based **M**arket model for the **I**nvestigation of **R**enewable and **I**ntegrated energy **S**ystems.

It is an agent-based simulation of electricity markets and their actors.
AMIRIS enables researches to analyse and evaluate energy policy instruments and their impact on the actors involved in the simulation context.
Different prototypical agents on the electricity market interact with each other, each employing complex decision strategies. 
AMIRIS allows to calculate the impact of policy instruments on economic performance of power plant operators and marketers.

Please have a look at the [AMIRIS-Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/home) to get further information.
Do not hesitate to ask questions about AMIRIS at the [openMod-Forum](https://forum.openmod.org/).

## Usage Prerequisites
### JDK
AMIRIS is written in Java (1.8) and uses [(FAME)](https://gitlab.com/fame-framework), the open Framework for distributed Agent-based Modelling of Energy systems.
Thus, AMIRIS requires a Java-JDK version 8 or higher, which can be obtained, e.g., [here](https://adoptopenjdk.net/). 

### Maven
AMIRIS has several dependencies, all of which are available via the prominent [Apache Maven](https://maven.apache.org/) build tool.
Most development environments for Java offer an integrated version Apache Maven - thus you do not have to install it.
If you do not used an IDE or don't want to use their integrations, you can also install Maven directly to your system.
In that case, please follow the instructions marked with `Console` below.

### Java IDE (optional)
We recommend to use Eclipse and provide an Eclipse project for your convenience.

## Installation
### Download

Clone the AMIRIS repository:

```
    git clone https://gitlab.com/dlr-ve/esy/amiris/amiris.git
```

### Building
Eclipse comes with its own Maven integration.
Thus, steps differ in Eclipse (which has automatic building) and on console.

#### With Eclipse
* Add the cloned repository to your Git-view.
* Import the project using the Eclipse import wizard. An existing Eclipse project is provided.
* Eclipse automatically builds the project
* proceed with step [Run AMIRIS](#Run-AMIRIS)

#### Console
Use your maven client to download dependencies and build the project. 
Go to the root folder of AMIRIS, and run

```
    mvn package
```

Wait for Maven to fetch all dependencies and to build AMIRIS.
This creates a Java ARchive (JAR) file in the "target/" folder that includes AMIRIS and all of its dependencies. 
The file should be named `amiris-jar-with-dependencies.jar` by default.

## Run AMIRIS
AMIRIS, as FAME application, can be run in parallel mode on multiple processors, or using a single processor. 
As default, it runs in single core mode.
Most configurations execute quite fast (depending on your machine, of course) even with on a single processor: Running AMIRIS in the example configuration takes about one minute on a standard laptop.

### Single-Core Mode
#### With Eclipse
Create a new run configuration for "Java application" with name e.g. ``RunAMIRIS``

In tab "Main" specify: 
* Project: e.g. `amiris`
* Main class: `de.dlr.gitlab.fame.setup.FameRunner`

In tab "Arguments" specify
* Program arguments: `-f input/input.pb`

In tab "JRE" select a JDK version 8 or 11 (higher versions not tested).

Before you can run the configuration, first select a valid input-file and enter its path after the `-f ` option in tab "Arguments".
Click "Apply" and "run" buttons.

Learn how to create own simulation configurations in the [AMIRIS-Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Getting-started).

#### In Console
In the AMIRIS base directory run 

```
    java -cp "target/amiris-jar-with-dependencies.jar" de.dlr.gitlab.fame.setup.FameRunner -f input/input.pb
```

Learn how to create own simulation configurations in the [AMIRIS-Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Getting-started).

### Multi-Core Mode
If you want to use it in parallel mode, you need to install [Open-MPI](https://www.open-mpi.org/) or [MPJ-Express](http://mpj-express.org/) first.
Since those libraries are platform-dependent, a manual compilation of those libraries is required.
The exact procedure depends on the chosen parallelisation library and your operating system. 
Please follow the instructions at the [FAME-Wiki](https://gitlab.com/fame-framework/wiki/-/wikis/home).

## Results
Once AMIRIS is run, the output files are generated in the "result" folder in the format "AMIRIS.YYYY-MMM-DD_HH-MM-SS.pb".
With FAME-Io and its script convertResults.py, this protobuffer file (.pb) can be converted to a number of .csv files, each representing an agent class, e.g. the EnergyExchange.
The results consist of the time series for the output of each agent, in the case of the energy exchange the electricity prices for the simulation duration.

## Available Support
This is a purely scientific project by (at the moment) one research group. 
Thus, there is no paid technical support available.

If you experience any trouble with AMIRIS, you may contact the developers at the [openMod-Forum](https://forum.openmod.org/) or via [amiris@dlr.de](mailto:amiris@dlr.de).
Please report bugs and make feature requests by filing issues following the provided templates (see also [CONTRIBUTING](CONTRIBUTING)).
For substantial enhancements, we recommend that you contact us via [amiris@dlr.de](mailto:amiris@dlr.de) for working together on the code in common projects or towards common publications and thus further develop AMIRIS.