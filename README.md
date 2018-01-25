# GridAPPS-D Feeder Models

This repository contains distribution feeder model converters and 
validation tools for the GridAPPS-D project. It is also a standalone 
source of models in these formats:

* [Common Information Model (CIM)](http://gridappsd.readthedocs.io/en/latest/developer_resources/index.html#cim-documentation) 
* [GridLAB-D](http://gridlab-d.shoutwiki.com/wiki/Index) 
* [OpenDSS](https://sourceforge.net/projects/electricdss/)

from these sources:

* [IEEE Distribution Test Cases](http://ewh.ieee.org/soc/pes/dsacom/testfeeders/) 
* [PNNL Taxonomy of North American Feeders](https://www.gridlabd.org/models/feeders/taxonomy_of_prototypical_feeders.pdf)
* [EPRI Large OpenDSS Test Feeders](https://sourceforge.net/p/electricdss/code/HEAD/tree/trunk/Distrib/EPRITestCircuits/Readme.pdf)
* [EPRI Distributed Photovoltaic (DPV) Test Feeders](http://dpv.epri.com/)

## Taxonomy Feeders

The original taxonomy feeders have been updated as follows:

* more realistic transformer impedance and core parameters
* more appropriate secondary and load voltages, based on the size and type of load
* alleviate line, cable and transformer overloads
* choose fuse current limits from standard fuse, recloser and breaker sizes 
* add margin to fuse current limits so they don't blow during steady state. _Note: This had to be redone because the new load voltage levels increased many of the component currents._
* assign capacitor nominal voltages based on the nominal primary voltage
* incorporate the [xy coordinates](http://emac.berkeley.edu/gridlabd/taxonomy_graphs/) from Michael A. Cohen _Note: The xy coordinates are used in GridLAB-D, CIM and OpenDSS, but not standalone GridLAB-D_
* remove assertion statements

The solution results change, so GridLAB-D regression tests
will continue using the original taxonomy feeders from the GridLAB-D
repository. The updated taxonomy feeders are recommended for research
projects, as the updates produce more realistic results, especially
for voltage and overload questions.

In order to update the taxonomy feeders:

1. [Python 3.x](https://www.python.org/downloads/) and the [NetworkX](https://networkx.github.io/) package are required.
2. From a command prompt in the ```taxonomy``` subdirectory, invoke ```python FixTransformers.py```
3. Based on ```./base_taxonomy/orig*.glm```, this creates the updated taxonomy feeders in ```./base_taxonomy/new*.glm```
4. From a command prompt in ```taxonomy/base_taxonomy```, invoke
   * ```run_all_new``` (on Windows)
   * ```chmod +x *.sh``` and then ```run_all_new.sh``` (on Linux or Mac OS X)
5. Twenty-four GridLAB-D simulations should run without errors or significant warnings

## GridLAB-D to OpenDSS Conversion

After processing the taxonomy updates, OpenDSS conversion proceeds as follows:

1. From a command prompt in the ```taxonomy``` subdirectory, invoke ```python converter_gld_dss.py```
2. This will create twenty-four directories like _./new_GC_12_47_1_ with an OpenDSS model and bus coordinates in several files
3. From a command prompt in one of those subdirectories, invoke ```opendsscmd Master.dss``` to run the simulation
   * _opendsscmd_ is the cross-platform solver used in GridAPPS-D. 
   * You may also use the Windows GUI version, _OpenDSS.exe_, to open and solve _Master.dss_ 

## CIM Translations

The _blazegraph_ subdirectory contains a Java program and script files
to manage the feeder model conversions to and from CIM. [Maven](https://maven.apache.org/) and [Java](https://java.com/en/download/) are required.

To set up and test the converter:

1. Download the [Blazegraph jar file](https://www.blazegraph.com/download/)
2. On Windows only, patch the configuration:
   * Add to _rwstore.properties_ ```com.bigdata.rwstore.RWStore.readBlobsAsync=false```
   * Invoke ```jar uf blazegraph.jar RWStore.properties```
3. To start Blazegraph, invoke from a terminal ```java -server -Xmx4g -Dfile.encoding=UTF-8 -jar blazegraph.jar```
4. Point browser to _http://localhost:9999/blazegraph_ 
   * On-line help on Blazegraph is available from the browser
5. Load some data from a CIM XML file, or any other XML triple-store
6. Run a query in the browser
   * the file _queries.txt_ contains sample SPARQL that can be pasted into the Blazegraph browser window

Helper scripts on Windows:

* _go.bat_ starts Blazegraph, like item 3 above
* _compile.bat_ recompiles the Java CIM Importer; this step can't be included within _import.bat_ on Windows
* _drop\_all.bat_ empties the triple-store
* _import.bat_ will run the Java importer against the triple-store. Within this file:
  * the ```-o=dss``` option creates an OpenDSS model from CIM
  * the ```-o=glm``` option creates a GridLAB-D model from CIM 

Helper scripts for Linux/Mac OS X:

* _start\_server.sh_ starts Blazegraph, like item 3 above
* _import.sh_ will compile and run the Java importer against the triple-store. Within this file:
  * the ```-o=dss``` option creates an OpenDSS model from CIM
  * the ```-o=glm``` option creates a GridLAB-D model from CIM 

## Circuit Validation

_This is work in progress; Linux/Mac has not been tested._ The goal is to verify round-trip model translation
and solution between the supported model formats. 
There are currently four supporting Python files in the _blazegraph_ subdirectory:

* _MakeConversionScript.py_ creates _ConvertCDPSM.dss_ that will batch-load all supported test circuits into OpenDSS, and export CIM XML
  * Use this first
  * Assumes the OpenDSS **source tree** has been checked out to _c:\opendss_
  * Assumes the EPRI DPV models have been downloaded to directories like _c:\epri_dpv|J1_
  * After ```python MakeConversionScript.py``` invoke ```opendsscmd ConvertCDPSM.dss```
* _MakeLoopScript.py_ loads the CIM XML files one at a time into Blazegraph, and then extracts a feeder model
  * Use this after _MakeConversionScript.py_  
  * Blazegraph must be set up
  * Invoke ```python MakeLoopScript.py -b``` to make _convert\_xml.bat_, which converts all CIM XML into DSS and GLM files
  * Invoke ```python MakeLoopScript.py -d``` to make _check.dss_, after which invoke ```opendsscmd check.dss``` to batch-solve all converted DSS files
* _MakeTable.py_ gathers OpenDSS solution summary information from CSV files into _Table.txt_
* _MakeGlmTestScript.py_ creates _check\_glm.bat_ that will solve all supported test circuits in GridLAB-D





