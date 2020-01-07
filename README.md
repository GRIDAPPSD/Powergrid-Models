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

The taxonomy feeders are modified, as described in the _taxonomy_ subdirectory.

## CIM Translations

The _blazegraph_ subdirectory contains a Java program and script files
to manage the feeder model conversions to and from CIM. [Maven](https://maven.apache.org/), [Java](https://java.com/en/download/), [Jena](https://jena.apache.org/) and [Commons Math](https://commons.apache.org/proper/commons-math/) are required.

To set up and test the converter:

1. Download the [Blazegraph jar file](https://www.blazegraph.com/download/)
2. Make sure to run Java 8. There have been reports that Blazegraph isn't compatible with Java 9 yet.
3. On Windows only, patch the configuration:
   * Add to _rwstore.properties_ ```com.bigdata.rwstore.RWStore.readBlobsAsync=false```
   * Invoke ```jar uf blazegraph.jar RWStore.properties```
4. To start Blazegraph, invoke from a terminal ```java -server -Xmx4g -Dfile.encoding=UTF-8 -jar blazegraph.jar```
5. Point a web browser to _http://localhost:9999/blazegraph_ 
   * On-line help on Blazegraph is available from the browser
6. Create the Blazegraph namespace _kb_ and use that for the rest of these examples
   * You can use a different namespace, but you'll have to specify that using the -u option for the CIMImporter, handediting the default _-u=http://localhost:9999/blazegraph/namespace/kb/sparql_
   * You can use a different namespace, but you may have to hand-edit some of the Python files (e.g. under the Meas directory)
   * The GridAPPS-D platform itself may use a different namespace
7. Load some data from a CIM XML file, or any other XML triple-store
8. Run a query in the browser
   * the file _queries.txt_ contains sample SPARQL that can be pasted into the Blazegraph browser window

Helper scripts on Windows:

* _go.bat_ starts Blazegraph, like item 4 above
* _compile.bat_ recompiles the Java CIM Importer; this step can't be included within _import.bat_ on Windows
* _drop\_all.bat_ empties the triple-store
* _import.bat_ will run the Java importer against the triple-store. Within this file:
  * the ```-o=dss``` option creates an OpenDSS model from CIM
  * the ```-o=glm``` option creates a GridLAB-D model from CIM 
  * the ```-o=both``` option creates both OpenDSS and GridLAB-D models from CIM 
  * the ```-o=idx``` option creates a JSON index of all Feeders in the triple-store. Use this to obtain valid mRID values for the -s option

Helper scripts for Linux/Mac OS X:

* _start\_server.sh_ starts Blazegraph, like item 4 above
* _import.sh_ will compile and run the Java importer against the triple-store. Within this file:
  * the ```-o=dss``` option creates an OpenDSS model from CIM
  * the ```-o=glm``` option creates a GridLAB-D model from CIM 
  * the ```-o=both``` option creates both OpenDSS and GridLAB-D models from CIM 
  * the ```-o=idx``` option creates a JSON index of all Feeders in the triple-store. Use this to obtain valid mRID values for the -s option

If you will need both OpenDSS and GridLAB-D files, the ```-o=both``` option is much more efficient than generating them individually, because over 90% of the execution time is taken up with SPARQL queries that are common to both.

Usage and options for ```java gov.pnnl.goss.cim2glm.CIMImporter [options] output_root```

* ```-s={mRID}          // select one feeder by CIM mRID; selects all feeders if not specified```
* ```-o={glm|dss|both|idx|cim}   // output format; defaults to glm; currently cim supports only CIM14```
* ```-l={0..1}          // load scaling factor; defaults to 1```
* ```-f={50|60}         // system frequency; defaults to 60```                                                 
* ```-n={schedule_name} // root filename for scheduled ZIP loads (defaults to none), valid only for -o=glm```      
* ```-z={0..1}          // constant Z portion (defaults to 0 for CIM-defined LoadResponseCharacteristic)```
* ```-i={0..1}          // constant I portion (defaults to 0 for CIM-defined LoadResponseCharacteristic)```
* ```-p={0..1}          // constant P portion (defaults to 0 for CIM-defined LoadResponseCharacteristic)```
* ```-r={0..1}          // determine ZIP load fraction based on given xml file or randomized fractions```
* ```-h={0..1}          // determine if house load objects should be added to supplement EnergyConsumers```
* ```-x={0, 1}          // indicate whether for glm, the model will be called with a fault_check already created```
* ```-t={0, 1}          // request timing of top-level methods and SPARQL queries, requires -o=both for methods```
* ```-u={http://localhost:9999/blazegraph/namespace/kb/sparql} // blazegraph uri (if connecting over HTTP); defaults to http://localhost:9999/blazegraph/namespace/kb/sparql```

## GridAPPS-D Feeder Models

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
2. All models originated with an OpenDSS version, except for Transactive, which originated from a hand-edited GridLAB-D model, then converted to OpenDSS. See code in directory ```blazegraph/test/glm/pnnl``` for details.
3. Model marked ```Yes``` for Houses have been tested with houses, but they don't require houses.
4. ```Load``` is the net OpenDSS source power injection, which is approximately load plus losses, less DER output

The following steps are used to injest these models, and verify that exports from CIM will solve in both GridLAB-D and OpenDSS. (Note: on Linux and Mac OS X, use ```python3``` as shown below. On Windows, it may be that ```python3``` is not defined, in which case use ```python``` to invoke Python 3.)

1. Start the Blazegraph engine; _existing contents will be removed in the steps below_. GridLAB-D and OpenDSSCmd must also have been installed.
2. From blazegraph/test directory, issue ```./go.sh``` or ```go.bat``` to create the CIM XML files and baseline OpenDSS power flow solutions.
   - Results will be in the ```blazegraph/test``` directory
   - ```rootname.xml``` is the CIM XML file
   - ```rootname_s.csv``` contains exported snapshot loadflow summary
   - ```rootname_i.csv``` contains exported branch currents
   - ```rootname_v.csv``` contains exported bus voltages
   - ```rootname_t.csv``` contains exported regulator tap positions
3. From blazegraph directory, issue ```python3 MakeLoopScript.py -b``` to create the platform-dependent script for step 3
4. From blazegraph directory, issue ```./convert_xml.sh``` or ```convert_xml.bat``` to:
   - Empty and create a new ```blazegraph/both``` directory
   - Sequentially ingest the CIM XML files into Blazegraph, and export both OpenDSS and GridLAB-D models
   - This step may take a few minutes. When finished, all of the GridLAB-D and OpenDSS models will be in ```blazegraph/both``` directory
   - When finished, only the last CIM XML will still be in Blazegraph. _This should be deleted before doing any more work in Blazegraph, to ensure compatible namespaces_.
5. From blazegraph directory, issue ```python3 MakeLoopScript.py -d``` and then ```opendsscmd check.dss``` to run OpenDSS power flows on the exported models.
   - Results will be in the ```blazegraph/both``` directory
   - ```rootname_s.csv``` contains exported snapshot loadflow summary
   - ```rootname_i.csv``` contains exported branch currents
   - ```rootname_v.csv``` contains exported bus voltages
   - ```rootname_t.csv``` contains exported regulator tap positions
6. From blazegraph directory, issue ```python3 MakeGlmTestScript.py``` to create the GridLAB-D wrapper files, ```*run.glm``` and a script execution file in ```blazegraph/both```
7. From blazegraph/both diretory, if on Linux or Mac OS X, issue ```chmod +x *.sh``` and then ```./check_glm.sh```.  If on Windows, just issue ```check_glm```. This runs GridLAB-D power flow on the exported models.
   - Results will be in the ```blazegraph/both``` directory
   - ```rootname_volt.csv``` contains the output from a GridLAB-D voltdump, i.e., the node (bus) voltages
   - ```rootname_curr.csv``` contains the output from a GridLAB-D currdump, i.e., the link (branch) currents
8. From blazegraph directory, issue ```python3 Compare_Cases.py``` to compare the power flow solutions from steps 5 and 7 to the baseline solutions from step 2
9. In the blazegraph/both directory, comparison results are in a set of files:
   - ```*Summary.log``` compares the OpenDSS snapshot load flow solutions
   - Other ```*.log``` files capture GridLAB-D warnings and errors. At present, the exported IEEE 37-bus model, which is a delta system, does not solve in GridLAB-D
   - ```*Missing_Nodes_DSS.txt``` identifies nodes (buses) that appear in one OpenDSS model (baseline step 2 or exported step 5), but not the other.
   - ```*Missing_Links_DSS.txt``` identifies links (branches) that appear in one OpenDSS model (baseline step 2 or exported step 5), but not the other.
   - ```*Compare_Voltages_DSS.csv``` compares the bus voltages from steps 2 and 5, sorted by increasing difference
   - ```*Compare_Voltages_GLM.csv``` compares the bus voltages from steps 2 and 7, sorted by increasing difference
   - ```*Compare_Currents_DSS.csv``` compares the branch currents from steps 2 and 5, sorted by increasing difference
   - ```*Compare_Currents_GLM.csv``` compares the branch currents from steps 2 and 7, sorted by increasing difference

## Circuit Validation Scripts

_This is work in progress; essential changes to DPV J1 are not yet under version control._ The goal is to verify round-trip model translation
and solution between the supported model formats. It also forms the basis for validing eleven feeder models including with GridAPPS-D.

There are currently three supporting Python files in the _blazegraph_ subdirectory:

* _MakeLoopScript.py_ loads the CIM XML files one at a time into Blazegraph, and then extracts a feeder model
  * Use this after the CIM XML files have been created  
  * Blazegraph must be set up
  * Invoke ```python MakeLoopScript.py -b``` to make _convert\_xml.bat_ or _convert\_xml.sh_, which converts all CIM XML into DSS and GLM files
  * Invoke ```python MakeLoopScript.py -d``` to make _check.dss_, after which invoke ```opendsscmd check.dss``` to batch-solve all converted DSS files
* _MakeGlmTestScript.py_ creates _check\_glm.bat_ or _check\_glm.sh_ that will solve all supported test circuits in GridLAB-D
* _Compare_Cases.py_ has been described in steps 8 and 9 above

The funtionality of these two scripts has been incorporated above, so they might be removed:

* _MakeTable.py_ gathers OpenDSS solution summary information from CSV files into _table.txt_
* _MakeConversionScript.py_ creates _ConvertCDPSM.dss_ that will batch-load all supported test circuits into OpenDSS, and export CIM XML
	* Assumes the OpenDSS **source tree** has been checked out to _c:\opendss_
	* Assumes the EPRI DPV models have been downloaded to directories like _c:\epri_dpv|J1_ or _~/src/epri_dpv/J1_
	* After ```python MakeConversionScript.py``` invoke ```opendsscmd ConvertCDPSM.dss```





