Instructions on how to set up AMIRIS with Eclipse

## Requirements

Please download and install the latest [Eclipse IDE](https://www.eclipse.org/).

## Clone & Build

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

## Run

Create a new run configuration for `Java application` with name, e.g., "RunAMIRIS". To do so:

* Go to eclipse's `Run Configurations` menu (`Run` / `Run Configurations`)
* In the eclipse's `Run Configurations` menu select `New launch configuration`
* Change the name of the new run configuration to "RunAMIRIS"

In tab `Main` of your new run configuration "RunAMIRIS" specify:

* `Project`: amiris
* `Main class`: de.dlr.gitlab.fame.setup.FameRunner

In tab `Arguments` of your new run configuration "RunAMIRIS" specify

* `Program arguments`: -f input/input.pb

In tab `JRE` of your new run configuration "RunAMIRIS" select JDK version 11 (this should be the default case).
Click `Apply` and `Run` buttons to run an AMIRIS simulation. This should result in an output similar to:

```
    Starting up 1 processes.
    Warm-up completed after 1 ticks. 
    22.02.2022 16:43:55 :: Ran 157746 ticks in: 22501 ms 
```
