---
title: 'AMIRIS: Agent-based Market model for the Investigation of Renewable and Integrated energy Systems'
tags:
  - Java
  - agent-based
  - energy systems
  - electricity market
authors:
  - name: Christoph Schimeczek^[corresponding author]
    orcid: 0000-0002-0791-9365
    affiliation: 1
  - name: Kristina Nienhaus
    orcid: 0000-0003-4180-6767
    affiliation: 1
  - name: Ulrich Frey
    orcid: 0000-0002-9803-1336
    affiliation: 1
  - name: Evelyn Sperber
    orcid: 0000-0001-9093-5042
    affiliation: 1
  - name: Seyedfarzad Sarfarazi
    orcid: 0000-0003-0532-5907
    affiliation: 1
  - name: Felix Nitsch
    orcid: 0000-0002-9824-3371
    affiliation: 1
  - name: Johannes Kochems
    orcid: 0000-0002-3461-3679
    affiliation: 1
  - name: A. Achraf El Ghazi
    orcid: 0000-0001-5064-9148
    affiliation: 1
affiliations:
 - name: German Aerospace Center (DLR), Institute of Networked Energy Systems, Curiestr. 4, 70563 Stuttgart, Germany
   index: 1
date: 20 September 2022
bibliography: paper.bib
---

# Summary
AMIRIS is an agent-based model (ABM) to simulate electricity markets.
The focus of this bottom-up model is on the business-oriented decisions of actors in the energy system.
These actors are represented as prototypical agents in the model, each with own complex decision-making strategies.
Inter alia, the bidding decisions are based on the assessment of electricity market prices and generation forecasts [@Nitsch2021], and diverse actors deciding on different time scales may be modelled.
In particular, the agents' behavior does not only reflect marginal prices, but can also consider effects of support instruments like market premia, uncertainties and limited information, or market power [@Frey2020].
This allows assessing which policy or market design is best suited to an economic and effective energy system [@TorralbaDiaz2020].
The simulations generate results on the dispatch of power plants and flexibility options, technology-specific market values, development of system costs or CO2 emissions.
One important output of the model are simulated market prices [@Deissenroth2017].

AMIRIS is developed in Java using the FAME-Core framework [@FAME_CORE] and is available on Gitlab^[https://gitlab.com/dlr-ve/esy/amiris/amiris/].
One important design goal was to make assumptions and calculations as transparent as possible in order to improve reproducibility.
AMIRIS was successfully tested on different computer systems, ranging from desktop-PCs to high-performance computing clusters.

# Statement of need
In the field of energy systems analysis, linear optimisation models are the most prevalent type of model [@Ringkjob2018].
They are often used to identify cost-optimal systems.
Many linear optimisation models are highly developed, offer a comprehensive set of technologies, cover multiple sectors, and consider constraints of the electricity grid [@Prina2020].
However, they assume perfect competition and disregard market imperfections and actor inhomogeneity [@TorralbaDiaz2020].

ABMs, in contrast, are clearly a minority in the field of energy systems analysis.
However, they are ideally suited to study the interaction and behaviour of heterogeneous actors and can consider market imperfections.
We know only of a few mature ABMs in use, namely PowerACE [@Weidlich2008; @Genoese2011; @Keles2016; @Sensfuß2008], EMLab-Generation [@Chappin2017; @Richstein2014], EMCAS [@Conzelman2005], BRAIN-Energy [@Barazza2018], ElecSim [@Kell2019], MASCEM [@Vale2011], and RESTrade [@TradeRES2021].
Of the before-mentioned models, only EMLab-Generation is open-sourced yet.
EMLab, however, models long-term investment decisions and does not cover the short-term dispatch of power plants.

AMIRIS fills this gap and provides detailed modelling of short- to mid-term dispatch decisions.
It comprises different types of agents, like power plant operators, traders, marketplaces, forecasters, storage operators, and policy providers.
Due to this comprehensive modelling of actors in energy systems, scientists can utilise AMIRIS to investigate their specific research questions.
Using FAME-Io [@FAME_IO], openly available and tested model configurations^[https://gitlab.com/dlr-ve/esy/amiris/examples] can be easily adapted to this end.

AMIRIS is designed for high computational speed.
Hence, a simulation of the German electricity market for one year in hourly resolution completes in about twenty seconds on a modern desktop PC (Intel Core i7 10510U, 16 GB RAM) in single-core mode.
Preparation of inputs and extraction of results, as automatically performed by the AMIRIS-Py package^[https://pypi.org/project/amirispy/], accounts for about another twenty seconds.
The short runtime and convenient script execution directly translates into scientific value, since being able to run many simulations facilitates sensitivity analyses and robustness checks.

# Use Cases
AMIRIS has been used in several project and its use resulted in several publications.

## Projects
An identification of the efficiency gap between the results from a fundamental electricity market model and AMIRIS has been conducted in [ERAFlex](https://www.enargus.de/detail/?id=397696) as well as in [INTEEVER-II](https://www.enargus.de/detail/?id=916102).
[ERAFlexII](https://www.enargus.de/detail/?id=2001065) aims at closing this efficiency gap by coupling the two models.
AMIRIS is thus used to identify a new cost minimal solution for the energy system with regard to actors’ business-oriented behaviour under uncertainty.
In [TradeRES](https://traderes.eu/) as well as [UNSEEN](https://www.enargus.de/detail/?id=1211199), AMIRIS analyses different support schemes and market design options for a power system with very high shares of renewables.
Contributions to system adequacy of European electricity exchange under extreme weather events are investigated in [VERMEER](https://www.enargus.de/detail/?id=1295220).
In [InnoSEn](https://www.enargus.de/detail/?id=398183), AMIRIS evaluated economic perspectives of battery storage systems bidding on day-ahead and automatic frequency restoration reserves markets.
Work with AMIRIS in [C/Sells](https://www.csells.net/en/) focused on marketing activities of aggregators in energy communities.
Development activities in [En4U](https://www.enargus.de/detail/?id=1449730) are dedicated to analyse aggregated behaviour patterns of individual electricity consumers under uncertainty.

## Publications
AMIRIS has been validated for the Austrian day-ahead electricity market by @Nitsch2021b.
An identification of the efficiency gap between the results from a fundamental electricity market model and an agent-based simulation model has been conducted by @TorralbaDiaz2020.
@Reeg2019 elaborated the efficient dispatch and refinancing conditions of renewables under different support schemes.
@Nitsch2021 evaluated the economic perspectives of battery storage systems bidding on day-ahead and automatic frequency restoration reserves markets.
The market integration of PV-battery systems has been analysed by @Klein2020.
@Frey2020 analysed market effects of the variable market premium on the electricity market.

# Acknowledgements
Development of AMIRIS was funded by the German Aerospace Center, the German Federal Ministry for Economic Affairs and Climate Action, the German Federal Ministry of Education and Research, and the German Federal for the Environment, Nature Conservation and Nuclear Safety.
It received funding from the European Union's Horizon 2020 research and innovation programme under grant agreement No 864276.
We express our gratitude to former contributors of AMIRIS, notably Matthias Reeg, Nils Roloff, Marc Deissenroth-Uhrig and Martin Klein.

# References
