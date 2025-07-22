Instructions on how to set up, execute and modify AMIRIS

# Requirements

## Technical

AMIRIS is a *JAVA* application configured via *Python* scripts.

To run AMIRIS **Python 3.9 or higher** and Java Development Kit **JDK 11 or higher** are required.

## Skills

To configure and **run AMIRIS** applications, **no programming skills** are required.
Experience with energy system modelling and **Python is helpful**.
However, if you want to modify or extend the functionality of AMIRIS, you should have at least a basic understanding of Java.
To design new agents and interactions from scratch a basic understanding of [FAME](https://gitlab.com/fame-framework/wiki/-/wikis/home) is also required.

## Tools

These tools are recommended when working with AMIRIS:

| Action                 | Format    | Tools                                      | Level         |
|------------------------|-----------|--------------------------------------------|---------------|
| Run AMIRIS             | command   | command line (e.g. bash, powershell)       | user          |
| Inspect AMIRIS results | CSV       | spreadsheet application (e.g. Excel, Calc) | user          |
| Configure AMIRIS       | YAML, CSV | text editor (e.g. Notepad++, NodepadNext)  | user          |
| New agents / logic     | Java      | Java IDE (e.g. Eclipse, Intellij, VSCode)  | programmer    |

# Install and Run AMIRIS

After you have met the technical requirements as described above, choose one of the two options to run AMIRIS using AMIRIS-Py:

- **Beginner**: In case you are new to Python, follow the [Step-by-Step Guide](./Get-Started/StepByStep).
- **Experienced Python user**: We suggest to use the [Quickstart Guide](./Get-Started/QuickStart) to get your first simulation result in less than 5 minutes using `amirispy`.

You could run AMIRIS also without AMIRIS-Py by using [FAME-Io](Get-Started/FameioSetup) directly.
This is a slightly less convenient way, but might be necessary when you modify and build AMIRIS (see [below](#build-amiris)).

# Results

Congratulations, you successfully ran your first simulation with AMIRIS! Check the [Result Page](./Get-Started/Results) to learn more about the results you just generated.

# Run your first real-world simulation

The [`Simple`](https://gitlab.com/dlr-ve/esy/amiris/examples/-/tree/dev/Simple) scenario example is intended to achieve a fast setup - it is not based on a real-world energy system.
Switch to the [examples](https://gitlab.com/dlr-ve/esy/amiris/examples) folder `Germany2019` to investigate market dynamics closer to a real-world electricity system.
Run another simulation and check the results.

# Experiment

You may ask yourself, "how would a higher carbon price impact market dynamics"? 
To find out, open the `scenario.yaml` configuration file in a text editor and search for the agent `CarbonMarket`. 
Now replace the link under `Co2Prices` with your value, for example

```
Co2Prices: 100
```

Rerun the simulation and observe the impact of your changes on the electricity prices.

Please also refer to the [FAME-Wiki](https://gitlab.com/fame-framework/wiki/-/wikis) when applying more advanced adaptations to your scenario, such as changing the [simulation duration](https://gitlab.com/fame-framework/wiki/-/wikis/GetStarted/core/Contracts).

# Build AMIRIS

So far you only ran the AMIRIS model as it is provided.
If you were to modify code and change agent logic of AMIRIS, you would also need to package the application.
See the [AMIRIS Build Guide](./Get-Started/Build) for instructions.

# Multi-Core Mode

AMIRIS, as any FAME application, can be run using a single process, or in parallel mode with multiple processes.
By default, AMIRIS runs in single process mode.
Most configurations execute quite fast (depending on your machine, of course) even with only a single process.
Running any of the AMIRIS-Examples takes only a few seconds on a standard laptop computer.

If you want to use it in parallel mode, you need to install [Open-MPI](https://www.open-mpi.org/) or [MPJ-Express](http://mpjexpress.org/) first.
Since those libraries are platform-dependent they require a manual compilation.
The exact procedure depends on the chosen parallelization library and your operating system.
Please follow the instructions in the [FAME-Wiki](https://gitlab.com/fame-framework/wiki/-/wikis/GetStarted/parallel/RunParallel).
