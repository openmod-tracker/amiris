# AMIRIS
AMIRIS is the **A**gent-based **M**arket model for the **I**nvestigation of **R**enewable and **I**ntegrated energy **S**ystems.

It is an agent-based simulation of electricity markets and their actors.
AMIRIS enables researches to analyse and evaluate energy policy instruments and their impact on the actors involved in the simulation context.
Different prototypical agents on the electricity market interact with each other, each employing complex decision strategies. 
AMIRIS allows to calculate the impact of policy instruments on economic performance of power plant operators and marketers.
It is based on [(FAME)](https://gitlab.com/fame-framework), the open Framework for distributed Agent-based Modelling of Energy systems.

Please have a look at the [AMIRIS-Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/home) to get further information.
Do not hesitate to ask questions about AMIRIS at the [openMod-Forum](https://forum.openmod.org/tag/amiris).

## Recommended Skills
AMIRIS is a *JAVA* application configured via *Python* scripts.
To configure and run AMIRIS applications, no prior skills are strictly necessary.
However, AMIRIS developers should have at least basic experiences with both languages.

## AMIRIS Setup & Execution
Here, we provide quick guides on how to execute the AMIRIS Java application, one for **Java-Rookies** and one for **Java-Experts**.
Feel free to follow whichever guide you feel comfortable with.
To see how AMIRIS is configured via Python, please check the [Getting-Started](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Getting-started) guide on the Wiki.

### Rookies
In case you are **not familiar** with Java development we recommend following the instructions given here.

#### Requirements
Please download and install the latest [Eclipse IDE](https://www.eclipse.org/).

#### Clone & Build
1. Start Eclipse
2. Click `File` &rarr; `Import` &rarr; `Git` &rarr; `Projects from Git` &rarr; `Next`
3. Select `Clone URI` &rarr; `Next`
4. In the URI-Field enter `https://gitlab.com/dlr-ve/esy/amiris/amiris.git`; authentication is not required &rarr; `Next`
5. Select branch `main` &rarr; `Next`
6. Choose a directory &rarr; `Next`
7. `Import existing Eclipse project` &rarr; `Next`
8. Select `amiris` to import &rarr; `Next`

This should import the Eclipse project in the repository to you Eclipse workspace.
Eclipse automatically builds the project.
Thus, you **need not** install FAME separately: Eclipse's Maven plugin will sort out the dependencies in the background.

#### Run
Create a new run configuration for `Java application` with name, e.g., "RunAMIRIS". To do so:
* Go to eclipse's `Run Configurations` menu (`Run` / `Run Configurations`)
* In the eclipse's `Run Configurations` menu select `New launch configuration`
* Change the name of the new run configuration to "RunAMIRIS"


In tab `Main` of your new run configuration "RunAMIRIS" specify: 
* `Project`: amiris
* `Main class`: de.dlr.gitlab.fame.setup.FameRunner

In tab `Arguments` of your new run configuration "RunAMIRIS" specify
* `Program arguments`: -f input/input.pb

In tab `JRE` of your new run configuration "RunAMIRIS" select a JDK version 8 or 11 (this should be the default case).
Click `Apply` and `Run` buttons to run an AMIRIS simulation. This should result in an output similar to:

```
    Starting up 1 processes.
    Warm-up completed after 1 ticks. 
    22.02.2022 16:43:55 :: Ran 157746 ticks in: 22501 ms 
```

Once finished proceed with section [Results](#Results)

### Experts
In case you are **familiar with Java development** or do not want to use Eclipse we recommend following the instructions given here.

#### Requirements
Please check the given requirements below.

##### JDK
AMIRIS is written in Java (1.8) and uses [(FAME)](https://gitlab.com/fame-framework), the open Framework for distributed Agent-based Modelling of Energy systems.
Thus, AMIRIS requires a Java-JDK version 8 or higher, which can be obtained, e.g., [here](https://adoptopenjdk.net/). 

##### Maven
AMIRIS has several dependencies, all of which are available via the prominent [Apache Maven](https://maven.apache.org/) build tool.
Most development environments for Java offer an integrated version Apache Maven - thus you do not have to install it.
If you do not use an IDE or don't want to use their integrations, you can also install Maven directly to your system.

#### Clone & Build
Clone the AMIRIS repository:

```
    git clone https://gitlab.com/dlr-ve/esy/amiris/amiris.git
```

Go to the root folder of the newly cloned amiris folder, and execute

```
    mvn package
```

Wait for Maven to fetch all dependencies and to build AMIRIS.
This creates a Java archive (JAR) file in the `target/` folder that includes AMIRIS and all of its dependencies. 
The file should be named `amiris-core_X.y-jar-with-dependencies.jar` by default, where X.y is the current version of AMIRIS in pom.xml.
You only need to *re-package* AMIRIS once you *change the code-base* of AMIRIS.

##### Optional
You can change the name of the file created during packaging in the "pom.xml": search for `maven-assembly-plugin` and replace the entry at `<finalName>`.

#### Run AMIRIS
In the AMIRIS base directory run 

```
    java -cp "target/amiris-core_1.1-jar-with-dependencies.jar" de.dlr.gitlab.fame.setup.FameRunner -f input/input.pb
```

## Results 
Once AMIRIS is run, the output files are generated in the `result/` folder in the format `AMIRIS.YYYY-MMM-DD_HH-MM-SS.pb`.
These files are in a binary `protobuf` format and need to be converted to a human-readable format first.

Learn how to read results with FAME-Io in the [AMIRIS-Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Getting-started).

## Create own simulations
Learn how to create own simulation configurations with FAME-Io in the [AMIRIS-Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Getting-started).

### Multi-Core Mode
AMIRIS, as FAME application, can be run in parallel mode on multiple processors, or using a single processor. 
As default, it runs in single core mode.
Most configurations execute quite fast (depending on your machine, of course) even with on a single processor.
Running AMIRIS in the sample configuration takes a few seconds on a standard laptop.

If you want to use it in parallel mode, you need to install [Open-MPI](https://www.open-mpi.org/) or [MPJ-Express](http://mpj-express.org/) first.
Since those libraries are platform-dependent, a manual compilation of those libraries is required.
The exact procedure depends on the chosen parallelization library and your operating system. 
Please follow the instructions at the [FAME-Wiki](https://gitlab.com/fame-framework/wiki/-/wikis/home).

## Available Support
This is a purely scientific project by (at the moment) one research group. 
Thus, there is no paid technical support available.

If you experience any trouble with AMIRIS, you may contact the developers at the [openMod-Forum](https://forum.openmod.org/tag/amiris) or via [amiris@dlr.de](mailto:amiris@dlr.de).
Please report bugs and make feature requests by filing issues following the provided templates (see also [CONTRIBUTING](CONTRIBUTING)).
For substantial enhancements, we recommend that you contact us via [amiris@dlr.de](mailto:amiris@dlr.de) for working together on the code in common projects or towards common publications and thus further develop AMIRIS.
