# GridAPPS-D Feeder Models

Copyright (c) 2017-2021, Battelle Memorial Institute

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

The taxonomy feeders are modified, as described in the [taxonomy](taxonomy) subdirectory.

The feeder model conversion code has been moved to [CIMHub](https://github.com/GRIDAPPSD/CIMHub). This repository now contains mostly data.

For platform maintenance, updated feeder models that have been tested can be imported using [these instructions](BLAZEGRAPH_IMPORT.md).

## IEEE 9500-Node Test Case (Proposed)

See [platform/dss/WSU](platform/dss/WSU) for the original OpenDSS version of this test case.

See [CIMHub/ieee9500](https://github.com/GRIDAPPSD/CIMHub/ieee9500) for the latest published CIM, CSV, GridLAB-D and OpenDSS versions.

## Platform Feeder Characteristics

Eleven feeder models are [tested routinely](platform) for use in GridAPPS-D, summarized in the table below:

|Name|Features|Houses|Buses|Nodes|Branches|Load|Origin|
|----|--------|------|-----|-----|--------|----|------|
|ACEP_PSIL|480-volt microgrid with PV, wind and diesel|No|8|24|13|0.28|UAF|
|EPRI_DPV_J1|1800 kW PV in 11 locations|No|3434|4245|4901|9.69|EPRI DPV|
|IEEE13|Added CIM sampler|No|22|57|51|3.44|IEEE (mod)|
|IEEE13_Assets|Uses line spacings and wires|No|16|41|40|3.58|IEEE (mod)|
|IEEE37|Delta system|No|39|117|73|2.59|IEEE|
|IEEE123|Includes switches for reconfiguration|No|130|274|237|3.62|IEEE|
|IEEE123_PV|Added 3320 kW PV in 14 locations|Yes|214|442|334|0.27|IEEE/NREL|
|IEEE8500|Large model, balanced secondary loads|Yes|4876|8531|6103|11.98|IEEE|
|IEEE8500_3subs|Added 2 grid sources and DER|Yes|5294|9499|6823|9.14|GridAPPS-D|
|R2_12_47_2|Supports approximately 4000 houses|Yes|853|1631|1086|6.26|PNNL|
|Transactive|Added 1281 secondary loads to IEEE123|Yes|1516|3051|2812|3.92|GridAPPS-D|

Notes:

1. The "CIM Sampler" version of the IEEE 13-bus model added a single breaker, recloser, fuse, center-tap transformer, split-phase secondary load, PV and battery for the purpose of CIM conversion testing
2. All models originated with an OpenDSS version, except for Transactive, which originated from a hand-edited GridLAB-D model, then converted to OpenDSS. See code in directory ```platform/glm/pnnl``` for details.
3. Model marked ```Yes``` for Houses have been tested with houses, but they don't require houses.
4. ```Load``` is the net OpenDSS source power injection, which is approximately load plus losses, less DER output

## Directory Contents

These directories are actively maintained under version control:

* [CIM](CIM): Enterprise Architect file of the CIM UML subset used in GridAPPS-D
* [cnf](cnf): for platform maintenance
* [glmanip_module](glmanip_module): Utility code that loads a GridLAB-D input file into a Python dictionary.
* [platform](platform): helper scripts and files to load 11 feeders into the platform
* [taxonomy](taxonomy): PNNL taxonomy feeders as used in GridAPPS-D

The [archive](archive) directories are deprecated, moved, or not of general interest.

