# Set up Apache Maven in command line

Download and extract Apache Maven zip file from [here](https://maven.apache.org/download.cgi).

## Linux

Add the extracted folder to your Path: `export PATH=/my/path/to/apache-maven-3.8.6/bin:$PATH`
Adjust the version section of your path according to the version you have downloaded.

## Windows

* Press the Windows key and type `Environment`, this should bring up the menu to edit your user's environment variables.
* Locate the "Path" variable in the top list, select it and press the `edit` button.
* `Add` a new entry and browse to the /bin subdirectory of your extracted Maven folder.
* Save and exit the menu

# Test

Open a **new** shell and type "mvn -version", which should recognize the mvn command and list your installed Maven version.
