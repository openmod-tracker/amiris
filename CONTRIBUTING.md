<!-- SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>

SPDX-License-Identifier: Apache-2.0 -->
# Welcome

Welcome fellow AMIRIS user.
We are happy that you want to contribute to and improve AMIRIS.

## General information

AMIRIS is open for your contribution.
It is an interdisciplinary, open source project, thus help is always appreciated!
The development happens openly on GitLab and is supplemented by online developer meetings to discuss more complicated topics.

### Release plan

There is no fixed release plan at the moment. 
However, a [Roadmap](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Roadmap) exists with tentative release dates.
Minor bug fixes are released continuously.

## What is where?

Here you can find information on how to contribute.
For an overview of AMIRIS in general please visit the [AMIRIS-Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/home).
There, you can also find relevant background information like a glossary and a description of existing model components.
AMIRIS is based on FAME - an open Framework for distributed Agent-based Modelling of Energy systems.
Please visit [FAME on GitLab](https://gitlab.com/fame-framework) to get more details on that framework.

Installation instructions can be found in the Wiki's [Getting-started](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/GetStarted/Getting-started) section.

Issues and Bugs are tracked on the [Repository](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/issues) at GitLab.

We kindly ask you to discuss the AMIRIS development and configuration at the [openMod Forum](https://forum.openmod.org/tag/amiris).

Please find tag naming conventions, definition of done etc. in the Wiki's [Contribute](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Community/Contribute) section.

## Support

Please see our dedicated [Support Page]() in the AMIRIS Wiki.

# How to Contribute

You are welcome to contribute to AMIRIS via bug reports and feature requests at any time.

## Feature request

1. Please check the open issue list to see whether an issue similar to your feature idea already exists.
2. If no similar issue exists: Open a new issue using the *feature_request* template and fill in the corresponding items.
3. You may contact the main developers via [amiris@dlr.de](mailto:amiris@dlr.de) to discuss the issue with us.

## Bug report

1. Please check the open issue list to see whether a similar bug has already been reported.
2. If no similar bug was reported yet: Open a new issue using the *bug_report* template and fill in the corresponding items.
3. You may contact the main developers via [amiris@dlr.de](mailto:amiris@dlr.de) to discuss the issue with us.

## Code contributions

Thank you for your intention to contribute code.
Please note that code contributions (e.g. via pull requests) require you to fill in and sign the [Contributor License Agreement](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Community/CLA.pdf), before we can merge your code into the project.

### Environment

It is recommended to use a current Eclipse version to compile AMIRIS code.
[Maven](https://maven.apache.org/) is strongly recommended for resolving the dependencies of AMIRIS.
We recommend Java 11 (64 bit) to build and run AMIRIS.

### Coding

#### Coding Conventions

Please follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
The file "CodeStyle.xml" in the "misc" folder of the repository covers most of Google's specifications.

### Before submitting

Please, check the following points before submitting a pull request:
1. Please fill in the [Contributor License Agreement](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Community/CLA.pdf) and send it to [amiris@dlr.de](mailto:amiris@dlr.de).
1. Ensure there is a corresponding issue to your code contribution.
1. Make sure your code is based on the latest version of the *dev* branch and that there are no conflicts. In case of conflicts: fix them first.
1. Make sure that existing unit tests are all successful. Add new unit tests that cover your code contributions and ensure that your code works as intended. In case an error occurred that you don't know how to solve, ask for help from the main developers.
1. Update `CHANGELOG.md` in the main repository reflecting on the code changes made. Follow the changelog [style guide](https://github.com/vweevers/common-changelog)
1. Update the version number of the project in `pom.xml`. Follow conventions of [semantic versioning](https://semver.org/) 2.0.0.
1. If a breaking change occurs: add hints on how to upgrade to `UPGRADING.md`.

### Pull request

1. Submit your request using the provided *pull_request* template & (briefly) describe the changes you made in the pull request.
1. Contact the main developers via [amiris@dlr.de](mailto:amiris@dlr.de) or at [openMod Forum](https://forum.openmod.org/tag/amiris) in case you've got any questions.

# List of Contributors

The following people made significant contributions to AMIRIS (in calendrical order of their contribution):

1. [Kristina Nienhaus](https://orcid.org/0000-0003-4180-6767)
1. [Matthias Reeg](https://orcid.org/0000-0001-8247-6499)
1. Nils Roloff
1. [Marc Deissenroth-Uhrig](https://orcid.org/0000-0002-9103-418X)
1. [Martin Klein](https://orcid.org/0000-0001-7283-4707)
1. [Christoph Schimeczek](https://orcid.org/0000-0002-0791-9365)
1. [Ulrich Frey](https://orcid.org/0000-0002-9803-1336)
1. [Evelyn Sperber](https://orcid.org/0000-0001-9093-5042)
1. [Seyedfarzad Sarfarazi](https://orcid.org/0000-0003-0532-5907)
1. [Felix Nitsch](https://orcid.org/0000-0002-9824-3371)
1. [Johannes Kochems](https://orcid.org/0000-0002-3461-3679)
1. [A. Achraf El Ghazi](https://orcid.org/0000-0001-5064-9148)
1. [Leonard Willeke](https://orcid.org/0009-0004-4859-2452)
