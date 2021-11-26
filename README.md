# AMIRIS
AMIRIS is the **A**gent-based **M**arket model for the **I**nvestigation of **R**enewable and **I**ntegrated energy **S**ystems.

It is an agent-based model of the German electricity market to analyse and evaluate energy policy instruments and their impact on the actors involved in the simulation context.
Different prototypical agents on the electricity market interact with each other, each employing complex decision strategies. 
AMIRIS allows to calculate the impact of policy instruments on economic performance of power plant operators and marketers.

Please have a look at the [AMIRIS-Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/home) to get further information.
Do not hesitate to ask questions about AMIRIS at the [openMod-Forum](https://forum.openmod.org/).

## Requirements
### JDK
AMIRIS is written in Java (1.8) using the open Framework for distributed Agent-based Modelling of Energy systems [(FAME)](https://gitlab.com/fame-framework).
This, AMIRIS requires a Java-8 JDK, which can be obtained, e.g., [here](https://adoptopenjdk.net/). 

### Maven
AMIRIS has several dependencies, all of which available via the prominent [Apache Maven](https://maven.apache.org/) build tool.
Most development environments for Java support Apache Maven by default, but you can also install Maven directly.

### Eclipse (optional)
We recommend to use Eclipse and provide an Eclipse project for your convenience.

## Installation
Clone the AMIRIS repository:

```
    git clone https://gitlab.com/dlr-ve/esy/amiris/amiris.git
```

Use your maven client to download dependencies and build the project. 
Fetch yourself a cup of coffee and let it download the following software, according to the Amiris pom.xml in the root of the project.

## Run AMIRIS
AMIRIS, as FAME application, can be run in parallel mode on multiple processors, or using a single processor. 
As default, it runs in single core mode.
Most configurations execute quite fast (depending on your machine, of course) even with on a single processor: Running AMIRIS in the example configuration takes about one minute on a standard laptop.

### Single-Core Mode
#### With Eclipse
Create a new Java run configuration.

In tab "Main" specify: 

* Name: e.g. `RunAMIRIS`
* Project: e.g. `amiris`
* Main class: `de.dlr.gitlab.fame.setup.FameRunner`

In tab "Arguments" specify
* Program arguments: `-f <Path/to/your/input/file.pb>`

In tab "JRE" select a Java 8 or Java 11 (higher versions not tested) JDK.

Before you can run the configuration, first select a valid input-file and enter its path after the `-f ` option in tab "Arguments".
Click "Apply" and "run" buttons.

#### In Console
You need to package AMIRIS first. 

##### Package
Using Maven packaging is simple: 
Go to the root folder of AMIRIS, and run 

```
    mvn package
```

This creates a Java ARchive (JAR) file in the "target/" folder that includes AMIRIS and all of its dependencies. 
The file should be named `amiris-jar-with-dependencies.jar` by default.

##### Start AMIRIS
In the AMIRIS base directory run 

```
    java -cp "target/amiris-jar-with-dependencies.jar" de.dlr.gitlab.fame.setup.FameRunner -f <Path/to/your/input/file.pb>"
```

Please replace `<Path/to/your/input/file.pb>` with a path to a valid AMIRIS input file.

### Multi-Core Mode
If you want to use it in parallel mode, you need to install [Open-MPI](https://www.open-mpi.org/) or [MPJ-Express](http://mpj-express.org/) first.
Since those libraries are platform-dependent, a manual compilation of those libraries is required.
The exact procedure depends on the chosen parallelisation library and your operating system. 
Please follow the instructions at the [FAME-Wiki](https://gitlab.com/fame-framework/wiki/-/wikis/home).

## Results
Once AMIRIS is run, the output files are generated in the "result" folder in the format "AMIRIS.YYYY-MMM-DD_HH-MM-SS.pb".
With FAME-Io and its script convertResults.py, this protobuffer file (.pb) can be converted to a number of .csv files, each representing an agent class, e.g. the EnergyExchange.
The results consist of the time series for the output of each agent, in the case of the energy exchange the electricity prices for the simulation duration.