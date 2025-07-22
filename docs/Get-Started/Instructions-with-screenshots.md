Step-by-step guide on how to set up AMIRIS with Eclipse

# Install Eclipse

Eclipse is a free and powerful IDE for Java development, thus use it in this tutorial.

1. Open [Eclipse](https://www.eclipse.org/downloads) download page
2. Select your OS and architecture (e.g. Windows and x64) and download <details><summary>Show Screenshots</summary>![](uploads/screenshots/Eclipse_Install_1.png) ![](uploads/screenshots/Eclipse_Install_2.png)</details>
3. Start the setup and choose "for Java developers" <details><summary>Show Screenshots</summary>![](uploads/screenshots/Eclipse_Install_3.png)</details>

You are now ready for Java development!

# Import AMIRIS

We import AMIRIS to Eclipse in the upcoming steps:

1. Once installed, start Eclipse and import AMIRIS as project <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Import_1.png)</details>
2. In the import wizard select *Git* &rarr; *Projects from Git* <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Import_2.png)</details>
3. Select *Clone URI* <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Import_3.png)</details>
4. Open the AMIRIS repository [in your browser](https://gitlab.com/dlr-ve/esy/amiris/amiris/); then click *Clone* and select *Clone with HTTPS* <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Import_4.png)</details>
5. Switch back to Eclipse and insert repository location into import wizard field *URI*; Leave *Authentication* fields empty <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Import_5.png)</details>
6. (optional) Select the *main* branch and deselect any other branches <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Import_6.png)</details>
7. (optional) Change location for project on local hard drive <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Import_7.png)</details>
8. Import existing project into Eclipse <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Import_8.png) ![](uploads/screenshots/AMIRIS_Import_9.png)</details>

You have successfully imported AMIRIS as a new Eclipse project!

# Install JDK

The next steps are likely not necessary.
Skip this section for now and come back in case you experience troubles with your installed JDK version.

1. Open [JDK download](https://adoptium.net/?variant=openjdk11) page in your browser; select *Temurin11* and click on *Other platforms* **even if you are using Windows**.<details><summary>Show Screenshots</summary>![](uploads/screenshots/Eclipse_JDK_1.png)</details>
2. Choose your OS and architecture (e.g. Windows and x64) then download and extract the *zip* file <details><summary>Show Screenshots</summary>![](uploads/screenshots/Eclipse_JDK_2.png)</details>
3. In Eclipse, open menu *Window* and select *Preferences* <details><summary>Show Screenshots</summary>![](uploads/screenshots/Eclipse_JDK_3.png)</details>
4. Add a new JRE *Standard VM* <details><summary>Show Screenshots</summary>![](uploads/screenshots/Eclipse_JDK_4.png)</details>
5. Select *JRE home* directory to match the base folder of your extracted JDK <details><summary>Show Screenshots</summary>![](uploads/screenshots/Eclipse_JDK_5.png)</details>

You have now installed a new Java Development Kit into Eclipse.

# Create Configuration

To execute AMIRIS in Eclipse, do the following:

1. If not yet opened, open imported project AMIRIS by double-clicking it <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Run_0.png)</details>
2. Open Run Configurations <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Run_1.png)</details>
3. Create new run configuration <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Run_2.png)</details>
4. In tab *Main* search for "FameRunner" as main class <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Run_3.png)</details>
5. This is what it should look like <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Run_4.png)</details>
6. Change to tab *Arguments* and enter the string `-f ./input/input.pb` into the section *Program arguments* <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Run_5.png)</details>
7. Change to tab *JRE* and check that *Runtime JRE* points to a valid JDK (any string like "jdk-X.a.b") is ok as long a "X" is equal to or higher than 8. <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Run_6.png)</details>
8. If there is no suitable JDK shown, or nor no name at all
  1. Please execute steps in section [Install JDK](#install-jdk), and then return to creating the run configuration
  2. On tab *JRE* select *Alternate JRE* and apply the newly installed JDK <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Run_7.png)</details>
9. Start AMIRIS by clicking *Apply* and then *Run* <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Run_8.png)</details>
10. AMIRIS has finished its execution once you see an output like this in the Eclipse *Console*: <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Run_9.png)</details>

Congratulations! AMIRIS is now running and should have produced some results.
In the explorer, navigate to the *result* folder within your AMIRIS project - it should contain a file named *AMIRIS.<Date>.pb*.
Note that in Eclipse folders are not automatically updated. Refresh the folder content to see the result file in Eclipse.

# Prepare Environment

So far, we have only executed a predefined scenario (the *input.pb* used above).
To be able to modify a scenario or to create new ones, the Python-based tool *FAME-Io* is required.
We use Anaconda to manage Python environments.
To get it up and running, do as follows:

1. Download and install [Miniconda](https://docs.conda.io/en/latest/miniconda.html) <details><summary>Show Screenshots</summary>![](uploads/screenshots/Conda_Setup_1.png)</details>
2. Start *Anaconda Powershell* <details><summary>Show Screenshots</summary>![](uploads/screenshots/Conda_Setup_2.png)</details>
3. Create an environment with `conda create -n amirisEnv python=3.8` <details><summary>Show Screenshots</summary>![](uploads/screenshots/Conda_Setup_3.png)</details>
4. Activate the environment with `conda activate amirisEnv` <details><summary>Show Screenshots</summary>![](uploads/screenshots/Conda_Setup_4.png)</details>
5. Install FAME-Io via pip with `pip install fameio` <details><summary>Show Screenshots</summary>![](uploads/screenshots/Conda_Setup_5.png)</details>

In this Python environment you can read and write FAME protobuf files, like the *input.pb* used before, or the *AMIRIS.<Date>.pb* created when executing AMIRIS.
Proceed to see that works exactly.

# Get Scenario Examples

Configuration of AMIRIS is not trivial.
We provide some working examples that you can use as starting point.

1. Simply download and extract the [content](https://gitlab.com/dlr-ve/esy/amiris/examples/-/archive/main/examples-main.zip) from the example repository <details><summary>Show Screenshots</summary>![](uploads/screenshots/Examples_Download_1.png)</details>
2. Copy the extracted files to the *input* folder of your amiris project.
3. Note the path of the input folder <details><summary>Show Screenshots</summary>![](uploads/screenshots/Examples_Download_2.png)</details>
4. Open the Anaconda Powershell, and change to the input directory your AMIRIS installation <details><summary>Show Screenshots</summary>![](uploads/screenshots/Examples_Download_3.png)</details>
5. Change to the Germany scenario below the examples directory <details><summary>Show Screenshots</summary>![](uploads/screenshots/Examples_Download_4.png)</details>

You are now ready to prepare a new scenario to run with AMIRIS.

# Prepare Input

AMIRIS uses binary protobuf files as input.
To create such a file from the provided scenario and data, do as follows:

1. In your Anaconda Powershell, type `makeFameRunConfig.exe -f scenario.yaml -o Germany_scenario_input.pb` <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Input_1.png)</details>
2. The new name of the configuration is displayed <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Input_2.png)</details>
3. Switch to Eclipse, and edit the previously created *Run configuration* <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Run_4.png)</details>
4. Change to tab *Arguments* and enter the path to your created input file <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Input_3.png)</details>
5. Run the new config - its completed once you see output similar to <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Input_4.png)</details>

Congratulations! You have successfully run your first AMIRIS simulation with a different configuration file.
Let's have a look at the results of the previous simulation runs.

# Convert Results

Each run of AMIRIS creates a single binary protobuf file.
To inspect these results, we convert them to CSV files:

1. In your Anaconda Powershell, change directory to the *result* folder in the base folder of your AMIRIS project <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Result_1.png)</details>
2. Execute `convertFameResults -f AMIRIS.<dateTime>.pb -o <outputFolderName>` <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Result_2.png)</details>
3. Inspect the newly created folder <details><summary>Show Screenshots</summary>![](uploads/screenshots/AMIRIS_Result_3.png)</details>

Nice! You can now analyse the results with your favourite tool, e.g. Excel, R, or ...