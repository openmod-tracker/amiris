<!-- SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>

SPDX-License-Identifier: Apache-2.0 -->
# Welcome

Welcome fellow AMIRIS user.
We are pleased that you want to contribute to and improve AMIRIS.

## General information

AMIRIS is open to your contribution.
It is an interdisciplinary open source project, so help is always welcome!
Development takes place openly on GitLab and is supplemented by online developer meetings to discuss more complicated issues.

### Release plan

There is currently no fixed release schedule. 
However, there is a [Roadmap](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Roadmap) with tentative release dates.
Minor bug fixes are released continuously.

## What is where?

Here you can find information on how to contribute.
For a general overview of AMIRIS please visit the [AMIRIS-Wiki](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/home).
There, you will also find relevant background information such as a glossary and a description of existing model components.
AMIRIS is based on FAME - an open Framework for distributed Agent-based Modelling of Energy systems.
Please visit [FAME on GitLab](https://gitlab.com/fame-framework/wiki/-/wikis/home) to get more details about this framework.

Installation instructions can be found in the Wiki's [Getting-started](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Get-Started) section.

Issues and bugs are tracked in the [Repository](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/issues) on GitLab.

We kindly ask you to discuss the development and configuration of AMIRIS on the [openMod Forum](https://forum.openmod.org/tag/amiris).

Tag naming conventions, definitions of done, etc. can be found in the [Contribute](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Community/Contribute) section of the wiki.

## Support

Please see our dedicated [Support Page](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Community/Support) in the AMIRIS Wiki.

# How to Contribute

You are always welcome to contribute to AMIRIS by submitting bug reports and feature requests.

## Feature request

1. Please check the open issues list to see if there is an issue similar to your feature idea.
2. If no similar issue exists: Open a new issue using the *feature_request* template and fill in the corresponding fields.
3. You can contact the main developers via [amiris@dlr.de](mailto:amiris@dlr.de) to discuss the issue with us.

## Bug report

1. Please check the open issues list to see whether a similar bug has already been reported.
2. If no similar bug has been reported yet: Open a new issue using the *bug_report* template and fill in the corresponding fields.
3. You can contact the main developers via [amiris@dlr.de](mailto:amiris@dlr.de) to discuss the issue with us.

## Code contributions

Thank you for your interest in contributing code which is highly regarded.
Please note that code contributions (e.g. via pull requests) require you to complete and sign the [Contributor License Agreement](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Community/CLA.pdf), before we can merge your code into the project.

### Environment

It is recommended to use a current Eclipse version to compile AMIRIS code.
[Maven](https://maven.apache.org/) is strongly recommended for resolving the dependencies of AMIRIS.
We recommend Java 11 (64 bit) to build and run AMIRIS.

### Coding

#### Coding Conventions

Please follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
The file `CodeStyle.xml` in the `/misc` folder of the repository covers most of Google's specifications.

### Before submitting

Please, check the following points before submitting a pull request:
1. Please complete the [Contributor License Agreement](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Community/CLA.pdf) and send it to [amiris@dlr.de](mailto:amiris@dlr.de).
1. Make sure there is a corresponding issue to your code contribution.
1. Make sure your code is based on the latest version of the *dev* branch and that there are no conflicts. If there are any conflicts, fix them first.
1. Make sure all existing unit tests are successful. Add new unit tests that cover your code contributions and ensure that your code works as intended. If there is a bug you don't know how to fix, ask the main developers for help.
1. Update `CHANGELOG.md` in the main repository to reflect the code changes made. Follow the changelog [style guide](https://github.com/vweevers/common-changelog)
1. Update the project version number in the `pom.xml` following the conventions of [semantic versioning](https://semver.org/) 2.0.0.
1. If there is a breaking change introduced: add upgrade instructions to `UPGRADING.md`.

### Pull request

1. Submit your request using the provided *pull_request* template & (briefly) describe the changes you made in the pull request.
1. Contact the main developers via [amiris@dlr.de](mailto:amiris@dlr.de) or on the [openMod Forum](https://forum.openmod.org/tag/amiris) in case you've got any questions.

# List of Contributors

The following people made significant contributions to AMIRIS (in order of their first contribution):

1. [Kristina Nienhaus](https://orcid.org/0000-0003-4180-6767)
1. Thomas Kast
1. [Wolfgang Weimer-Jehle](https://orcid.org/0000-0002-2945-7288)
1. [Rudolf Weeber](https://orcid.org/0000-0003-1128-2093)
1. [Matthias Reeg](https://orcid.org/0000-0001-8247-6499)
1. Nils Roloff
1. [Marc Deissenroth-Uhrig](https://orcid.org/0000-0002-9103-418X)
1. [Martin Klein](https://orcid.org/0000-0001-7283-4707)
1. [Christoph Schimeczek](https://orcid.org/0000-0002-0791-9365)
1. [Ulrich Frey](https://orcid.org/0000-0002-9803-1336)
1. [Seyedfarzad Sarfarazi](https://orcid.org/0000-0003-0532-5907)
1. [Felix Nitsch](https://orcid.org/0000-0002-9824-3371)
1. [Evelyn Sperber](https://orcid.org/0000-0001-9093-5042)
1. [Johannes Kochems](https://orcid.org/0000-0002-3461-3679)
1. [A. Achraf El Ghazi](https://orcid.org/0000-0001-5064-9148)
1. [Leonard Willeke](https://orcid.org/0009-0004-4859-2452)
