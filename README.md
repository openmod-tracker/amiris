<!-- SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>

SPDX-License-Identifier: Apache-2.0 -->
![AMIRIS_Logo](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/uploads/AMIRIS_LogoWTitle.png)

## _Simulate electricity markets emerging from interactions of producers, consumers, and flexibilities_

[![Pipeline status](https://gitlab.com/dlr-ve/esy/amiris/amiris/badges/main/pipeline.svg)](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/commits/main)
[![REUSE status](https://api.reuse.software/badge/gitlab.com/dlr-ve/esy/amiris/amiris/)](https://api.reuse.software/info/gitlab.com/dlr-ve/esy/amiris/amiris/)
[![JOSS paper](https://joss.theoj.org/papers/10.21105/joss.05041/status.svg)](https://joss.theoj.org/papers/10.21105/joss.05041)
[![Zenodo](https://img.shields.io/badge/Research-Zenodo-blue)](https://zenodo.org/communities/amiris)
[![Last Commit](https://img.shields.io/gitlab/last-commit/dlr-ve/esy/amiris/amiris)](https://gitlab.com/fame-framework/fame-io/-/commits/dev)

AMIRIS is an agent-based simulation of electricity markets and their actors. Check out its [full documentation](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/home).

**[Get started right away](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/GetStarted/Getting-started)**!

## What is AMIRIS?

AMIRIS enables you to simulate the interplay between electricity market actors, policy instruments, and scenarios.
Due to its prototypical definitions of agents, you can model decisions on a high level and simulate trans-national electricity systems, e.g., Europe.
At the same time you can also model very detailed decisions, e.g. from households.
Whatever you do: Thanks to the powerful framework [FAME](https://gitlab.com/fame-framework/wiki/-/wikis/home), AMIRIS will deliver results quickly: The typical **runtime is below a minute** for one year and one market zone in hourly resolution.

Below we provide an overview of the agents modelled in AMIRIS. The figure illustrates their associated flow of information, energy, and money.

![AMIRIS Model schema](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/uploads/AMIRIS_ModellSchema_CCBY4.png)

## Who is AMIRIS for?

AMIRIS is intended for informed users in the energy domain, e.g., students, researchers, and companies in that field.
Although not strictly required, [basic knowledge of electricity markets](https://en.wikipedia.org/wiki/Electricity_market#Wholesale_electricity_market) is helpful regarding, e.g., concepts of market clearing.

## Applications

AMIRIS development started 2008 and has seen many different applications since. 
Selected recent applications:

- **Energy Policy Instruments**: In [TradeRES](https://traderes.eu/) AMIRIS was used to analyse cost recovery of renewables in ~100% renewable electricity systems under different support regimes. [Paper](https://doi.org/10.1109/EEM60825.2024.10608886)
- Intertwined dynamics between **Energy Community Markets** and national electricity markets were analysed with AMIRIS. [Paper](https://doi.org/10.1016/j.egyr.2024.06.052)
- Interactions of **Household Flexibility** (heat pumps, electric vehicles, and electricity storages) with the German energy system were modelled with AMIRIS. [Paper](https://elib.dlr.de/207802)
- In project VERMEER **Impacts of Cold Dunkelflaute** events on the European electricity system were investigated using **[Market Coupling](https://zenodo.org/records/10561382)** in AMIRIS. [Report](https://elib.dlr.de/196641/)
- AMIRIS was used to assess the **Economic Potential of Large Flexibility Providers** in future electricity market scenarios. [Paper](https://doi.org/10.1016/j.est.2024.110959)
- **Monetary Saving Potentials of Load Shifting** were analysed using AMIRIS by Johannes Kochems. [Dissertation](https://depositonce.tu-berlin.de/items/4a364bac-9e97-4d35-8eb6-645824cfc02d)
- A wide range of scenarios for **Future Electricity Markets** was explored using an AMIRIS [scenario generator](https://doi.org/10.5281/zenodo.8382789). [Paper](https://doi.org/10.1016/j.egyr.2024.11.013)

## Non-Applications

AMIRIS follows an explorative approach: it is perfectly suited to explore effects emerging from interactions of energy system actors under a given set of assumptions.
While individual actors can optimise their decisions, AMIRIS **does not optimise the energy system** as a whole.
Also, AMIRIS does not enforce system-wide constraints, e.g., carbon emission caps.
Thus, to answers question like "What is the cheapest possible electricity system considering a maximum carbon emission of X?", we recommend using optimisation-based tools, e.g., [REMix](https://gitlab.com/dlr-ve/esy/remix/framework), [PyPSA](https://pypsa.org/), or [oemof](https://oemof.org/).

## Community

AMIRIS is mainly developed by the German Aerospace Center's Institute of Networked Energy Systems.
We provide multi-level support for AMIRIS users: please see our dedicated [Support Page](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Community/Support). **We welcome any contributions**: bug reports, feature request, and, of course, code.
Please follow our [Contribution Guidelines](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Community/Contribute).

## Citing AMIRIS

If you use AMIRIS in an academic context please cite [doi: 10.21105/joss.05041](https://doi.org/10.21105/joss.05041).
In other contexts, please include a link to our [Gitlab repository](https://gitlab.com/dlr-ve/esy/amiris/amiris).

## Acknowledgements

Development of AMIRIS was funded by the German Aerospace Center, the German Federal Ministry for Economic Affairs and Climate Action, the German Federal Ministry of Education and Research, and the German Federal for the Environment, Nature Conservation and Nuclear Safety. 
It also received funding from the European Union’s Horizon 2020 research and innovation programme under grant agreement No 864276.
For an extended list of third-party funded research projects see the [AMIRIS Home Page](https://helmholtz.software/software/amiris).
We express our gratitude to all [contributors](CONTRIBUTING.md#list-of-contributors).

## What next?

* [Install and run AMIRIS](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/GetStarted/Getting-started)
* [Read previous publications & material](https://zenodo.org/communities/amiris)
* [Ask questions](https://forum.openmod.org/tag/amiris)
* [Contribute an issue or code](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/wikis/Community/Contribute)
