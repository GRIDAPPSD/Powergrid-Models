# GridAPPS-D Feeder Models

Copyright (c) 2017-2021, Battelle Memorial Institute

This repository contains distribution feeder model converters and 
validation tools for the GridAPPS-D project. It is also a standalone 
source of models in these formats:

* [Common Information Model (CIM)](http://gridappsd.readthedocs.io/en/latest/developer_resources/index.html#cim-documentation) 
* [GridLAB-D](http://gridlab-d.shoutwiki.com/wiki/Index) 
* [OpenDSS](https://sourceforge.net/projects/electricdss/)

from these sources:

* [IEEE Distribution Test Cases](https://site.ieee.org/pes-testfeeders/)
* [PNNL Taxonomy of North American Feeders](https://doi.org/10.2172/1040684)
* [EPRI Large OpenDSS Test Feeders](https://sourceforge.net/p/electricdss/code/HEAD/tree/trunk/Distrib/EPRITestCircuits/Readme.pdf)
* [EPRI Distributed Photovoltaic (DPV) Test Feeders](http://dpv.epri.com/)

The taxonomy feeders are modified, as described in the _taxonomy_ subdirectory.

## Feeder Characteristics

Eleven feeder models are tested routinely for use in GridAPPS-D, summarized in the table below:

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

## GridAPPS-D Model Import

Feeder model import into GridAPPS-D is now accomplished using [CIMHub](https://github.com/GRIDAPPSD/CIMHub).
Please be able to build CIMHub as described in the previous section, so that you can
perform this model import process as a developer.

Verify that the Blazegraph namespace is _kb_ and use that for the rest of these examples
   * You can use a different namespace, but you'll have to specify that using the -u option for the CIMImporter, handediting the default _-u=http://localhost:8889/bigdata/namespace/kb/sparql_
   * You can use a different namespace, but you may have to hand-edit some of the Python files (e.g. under the Meas directory)
   * The GridAPPS-D platform itself may use a different namespace

Helper scripts for Linux/Mac OS X:

* _import.sh_ will compile and run the Java importer against the triple-store. Within this file:
  * the ```-o=cim``` option creates a CIM14 model from CIM100
  * the ```-o=csv``` option creates a set of comma-delimited text files from CIM100
  * the ```-o=dss``` option creates an OpenDSS model from CIM100
  * the ```-o=glm``` option creates a GridLAB-D model from CIM 100
  * the ```-o=both``` option creates both OpenDSS and GridLAB-D models from CIM100 
  * the ```-o=idx``` option creates a JSON index of all Feeders in the triple-store. Use this to obtain valid mRID values for the -s option

If you will need both OpenDSS and GridLAB-D files, the ```-o=both``` option is much more efficient than generating them individually, because over 90% of the execution time is taken up with SPARQL queries that are common to both.

The following steps are used to ingest these models, and verify that exports from CIM will solve in both GridLAB-D and OpenDSS. (Note: on Linux and Mac OS X, use ```python3``` as shown below. On Windows, it may be that ```python3``` is not defined, in which case use ```python``` to invoke Python 3.)

1. Start the Blazegraph engine; _existing contents will be removed in the steps below_. GridLAB-D and OpenDSSCmd must also have been installed.
2. From ```platform``` directory, issue ```./go.sh``` to create the CIM XML files and baseline OpenDSS power flow solutions.
   - Results will be in the ```test``` directory
   - ```rootname.xml``` is the CIM XML file
   - ```rootname_s.csv``` contains exported snapshot loadflow summary
   - ```rootname_i.csv``` contains exported branch currents
   - ```rootname_v.csv``` contains exported bus voltages
   - ```rootname_t.csv``` contains exported regulator tap positions
3. From ```platform``` directory, issue ```python3 MakeLoopScript.py -b``` to create the platform-dependent script for step 3
4. From ```platform``` directory, issue ```./convert_xml.sh``` to:
   - Empty and create a new ```test``` directory
   - Sequentially ingest the CIM XML files into Blazegraph, and export both OpenDSS and GridLAB-D models
   - This step may take a few minutes. When finished, all of the GridLAB-D and OpenDSS models will be in ```model_output_tests/both``` directory
   - When finished, only the last CIM XML will still be in Blazegraph. _This should be deleted before doing any more work in Blazegraph, to ensure compatible namespaces_.
5. From ```platform``` directory, issue ```python3 MakeLoopScript.py -d``` and then ```opendsscmd check.dss``` to run OpenDSS power flows on the exported models.
   - Results will be in the ```test``` directory
   - ```rootname_s.csv``` contains exported snapshot loadflow summary
   - ```rootname_i.csv``` contains exported branch currents
   - ```rootname_v.csv``` contains exported bus voltages
   - ```rootname_t.csv``` contains exported regulator tap positions
6. From ```platform``` directory, issue ```python3 MakeGlmTestScript.py``` to create the GridLAB-D wrapper files, ```*run.glm``` and a script execution file in ```blazegraph/both```
7. From ```test``` diretory, if on Linux or Mac OS X, issue ```chmod +x *.sh``` and then ```./check_glm.sh```.  This runs GridLAB-D power flow on the exported models.
   - Results will be in the ```model_output_tests/both``` directory
   - ```rootname_volt.csv``` contains the output from a GridLAB-D voltdump, i.e., the node (bus) voltages
   - ```rootname_curr.csv``` contains the output from a GridLAB-D currdump, i.e., the link (branch) currents
8. From ```platform``` directory, issue ```python3 Compare_Cases.py``` to compare the power flow solutions from steps 5 and 7 to the baseline solutions from step 2
9. In the ```test``` directory, comparison results are in a set of files:
   - ```*Summary.log``` compares the OpenDSS snapshot load flow solutions
   - Other ```*.log``` files capture GridLAB-D warnings and errors. At present, the exported IEEE 37-bus model, which is a delta system, does not solve in GridLAB-D
   - ```*Missing_Nodes_DSS.txt``` identifies nodes (buses) that appear in one OpenDSS model (baseline step 2 or exported step 5), but not the other.
   - ```*Missing_Links_DSS.txt``` identifies links (branches) that appear in one OpenDSS model (baseline step 2 or exported step 5), but not the other.
   - ```*Compare_Voltages_DSS.csv``` compares the bus voltages from steps 2 and 5, sorted by increasing difference
   - ```*Compare_Voltages_GLM.csv``` compares the bus voltages from steps 2 and 7, sorted by increasing difference
   - ```*Compare_Currents_DSS.csv``` compares the branch currents from steps 2 and 5, sorted by increasing difference
   - ```*Compare_Currents_GLM.csv``` compares the branch currents from steps 2 and 7, sorted by increasing difference


## Model Translations

Feeder model converters from CYMDist and Synergi Electric to OpenDSS
are available [here](https://github.com/GRIDAPPSD/CIMHub/tree/issue/1175/converters). 


