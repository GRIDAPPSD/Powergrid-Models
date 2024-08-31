# GridAPPS-D Feeder Models

Copyright (c) 2017-2024, Battelle Memorial Institute

This repository contains distribution feeder models for the GridAPPS-D project
and other use cases in these formats:

* [Common Information Model (CIM)](http://gridappsd.readthedocs.io/en/latest/developer_resources/index.html#cim-documentation) 
* [GridLAB-D](http://gridlab-d.shoutwiki.com/wiki/Index) 
* [OpenDSS](https://sourceforge.net/projects/electricdss/)

from these sources:

* [IEEE Distribution Test Cases](https://site.ieee.org/pes-testfeeders/)
* [PNNL Taxonomy of North American Feeders](https://doi.org/10.2172/1040684)
* [EPRI Large OpenDSS Test Feeders](https://sourceforge.net/p/electricdss/code/HEAD/tree/trunk/Distrib/EPRITestCircuits/Readme.pdf)
* [EPRI Distributed Photovoltaic (DPV) Test Feeders](http://dpv.epri.com/)



## Platform Feeder Characteristics

Eleven feeder models are [tested routinely](platform) for use in GridAPPS-D, summarized in the table below:

|Name|Features|Houses|Buses|Nodes|Branches|Load|Origin|
|----|--------|------|-----|-----|--------|----|------|
|ACEP_PSIL|480-volt microgrid with PV, wind and diesel|No|8|24|13|0.28|UAF|
|EPRI_DPV_J1|1800 kW PV in 11 locations|No|3434|4245|4901|9.69|EPRI DPV|
|IEEE13|Added CIM sampler|No|22|57|51|3.44|IEEE (mod)|
|IEEE13_Assets|Uses line spacings and wires|No|16|41|40|3.58|IEEE (mod)|
|IEEE13_OCHRE|Added 40 service transformers and triplex loads|Yes|74|160|75|0.24|IEEE (mod)|
|IEEE37|Delta system|No|39|117|73|2.59|IEEE|
|IEEE123|Includes switches for reconfiguration|No|130|274|237|3.62|IEEE|
|IEEE123_PV|Added 3320 kW PV in 14 locations|Yes|214|442|334|0.27|IEEE/NREL|
|IEEE8500|Large model, balanced secondary loads|Yes|4876|8531|6103|11.98|IEEE|
|IEEE8500_3subs|Added 2 grid sources and DER|Yes|5294|9499|6823|9.14|GridAPPS-D|
|R2_12_47_2|Supports approximately 4000 houses|Yes|853|1631|1086|6.26|PNNL|
|Transactive|Added 1281 secondary loads to IEEE123|Yes|1516|3051|2812|3.92|GridAPPS-D|

## Revised Build Instructions

This version has been rebased on the CIM-Graph, CIM-Builder, and CIM-Loader libraries instead of the CIMHub Java+Python package

TODO: New Build Instructions


## Directory Contents

This repository contains:
* Archive: Leftover fragments to be removed
* Models: Transmission and distribution models in CIM format with source files used to create those models
* Ontology: Enterprise Architect and CIMTool artifacts for the CIM profiles used by GridAPPS-D
* Platform: Models populated with houses and measurements for simulation in the GridAPPS-D platform

## Attribute and Disclaimer:
This software was created under a project sponsored by the U.S. Department of Energy’s Office of Electricity, an agency of the United States Government.  Neither the United States Government nor the United States Department of Energy, nor Battelle, nor any of their employees, nor any jurisdiction or organization that has cooperated in the development of these materials, makes any warranty, express or implied, or assumes any legal liability or responsibility for the accuracy, completeness, or usefulness or any information, apparatus, product, software, or process disclosed, or represents that its use would not infringe privately owned rights.

Reference herein to any specific commercial product, process, or service by trade name, trademark, manufacturer, or otherwise does not necessarily constitute or imply its endorsement, recommendation, or favoring by the United States Government or any agency thereof, or Battelle Memorial Institute. The views and opinions of authors expressed herein do not necessarily state or reflect those of the United States Government or any agency thereof.

PACIFIC NORTHWEST NATIONAL LABORATORY

operated by BATTELLE for the

UNITED STATES DEPARTMENT OF ENERGY

under Contract DE-AC05-76RL01830

