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

If there have been no changes to the CIM XML files in ```platform/cimxml```, you may follow these [import instructions](BLAZEGRAPH_IMPORT.md).

If the source models have changed under ```platform/dss``` or ```platform/glm```, please test them first as described next.

## GridAPPS-D Model Testing

Feeder model translations for GridAPPS-D are now accomplished using [CIMHub](https://github.com/GRIDAPPSD/CIMHub).
Please be able to build CIMHub as described in the previous section, so that you can
perform this model testing process as a developer.

After the models have been tested, check them in to ```platform/cimxml```, from which they can be [imported into GridAPPS-D](BLAZEGRAPH_IMPORT.md).

### Preparations

Verify that the Blazegraph namespace is _kb_ and use that for the rest of these examples
* You can use a different namespace, but you'll have to specify that using the -u option for the CIMImporter, handediting the default _-u=http://localhost:8889/bigdata/namespace/kb/sparql_
* You can use a different namespace, but you may have to hand-edit some of the Python files (e.g. under the Meas directory)
* The GridAPPS-D platform itself may use a different namespace

This process assumes you have the [CIMHub](https://github.com/GRIDAPPSD/CIMHub), Powergrid-Models and OpenDSS 
repositories cloned under ~/src. CIMHub needs to have been built from that repository using ```mvn clean install```. 

If you don't have the OpenDSS repository, the following steps may be used to clone just the examples used in GridAPPS-D.

```
cd ~/src
mkdir OpenDSS
cd OpenDSS
svn checkout --depth immediates https://svn.code.sf.net/p/electricdss/code/trunk/Version7 .
svn update --set-depth infinity Test
svn update --set-depth infinity Distrib/EPRITestCircuits
svn update --set-depth infinity Distrib/IEEETestCases
```

### Batch Testing Process

To test all 11 feeder models at once, before importing into the platform:

1. Change to the ```platform``` directory
2. Edit the two ```declare``` lines at the top of ```go.sh``` so they match your path and Blazegraph URL
3. Start Blazegraph with ```docker restart blazegraph```
4. Issue ```./go.sh```

If any errors occur, you might need the step-by-step process to localize the problem.
(Note: the IEEE 37-bus feeder will not solve in GridLAB-D; this is a known issue.)

### Step-by-step Testing Process

The following steps are used to ingest these models, and verify that exports from CIM will solve in both GridLAB-D and OpenDSS. 
GridLAB-D and OpenDSSCmd must have already been installed.

1. All steps are performed from the ```platform``` directory.
2. Start the Blazegraph engine; _existing contents will be removed in the steps below_.
3. Issue ```./convert_source.sh``` to create the CIM XML files and baseline OpenDSS power flow solutions.
    - Feeder Models will be in the ```cimxml``` subdirectory
        - ```rootname.xml``` is the CIM XML file
        - ```rootname_uuids.dat``` is a file used to persist CIM mRIDs
    - Baseline Results will be in the ```test``` subdirectory
        - ```rootname_s.csv``` contains exported snapshot loadflow summary
        - ```rootname_i.csv``` contains exported branch currents
        - ```rootname_v.csv``` contains exported bus voltages
        - ```rootname_t.csv``` contains exported regulator tap positions
4. Issue ```python3 MakeLoopScript.py -b``` to create the script for step 5
    - optionally specify the non-default source path and Blazegraph URL:
      ```python3 MakeLoopScript.py -b /home/tom/src/Powergrid-Models/platform/ http://localhost:8889/bigdata/namespace/kb/sparql```
5. Issue ```./convert_xml.sh``` to:
    - Empty and create a new ```test``` directory
    - Sequentially ingest the CIM XML files into Blazegraph, and export both OpenDSS and GridLAB-D models
    - This step may take a few minutes. When finished, all of the GridLAB-D and OpenDSS models will be in ```both``` subdirectory
    - When finished, only the last CIM XML will still be in Blazegraph. _This should be deleted before doing any more work in Blazegraph, to ensure compatible namespaces_.
6. Issue ```python3 MakeLoopScript.py -d``` to make a sequential solution script for OpenDSS.
    - optionally specify the non-default source path and Blazegraph URL:
     ```python3 MakeLoopScript.py -d /home/tom/src/Powergrid-Models/platform/```
7. Issue ```opendsscmd check.dss``` to run OpenDSS power flows on the exported models.
    - Results will be in the ```test/dss``` subdirectory
        - ```rootname_s.csv``` contains exported snapshot loadflow summary
        - ```rootname_i.csv``` contains exported branch currents
        - ```rootname_v.csv``` contains exported bus voltages
        - ```rootname_t.csv``` contains exported regulator tap positions
8. Issue ```python3 MakeGlmTestScript.py``` to create the GridLAB-D wrapper files, ```*run.glm``` and a script execution file in ```both``` subdirectory
    - optionally specify the non-default source path and Blazegraph URL:
     ```python3 MakeGlmTestScript.py /home/tom/src/Powergrid-Models/platform/```
9. Issue ```./check_glm.sh```.  This runs GridLAB-D power flow on the exported models.
    - Results will be in the ```tests/glm``` directory
        - ```rootname.log``` contains the GridLAB-D error, warning and information messages
        - ```rootname_volt.csv``` contains the output from a GridLAB-D voltdump, i.e., the node (bus) voltages
        - ```rootname_curr.csv``` contains the output from a GridLAB-D currdump, i.e., the link (branch) currents
10. Issue ```python3 Compare_Cases.py``` to compare the power flow solutions from steps 7 and 9 to the baseline solutions from step 3
11. In the ```test/dss``` directory, OpenDSS comparison results are in a set of files:
    - ```*Summary.log``` compares the OpenDSS snapshot load flow solutions
    - ```*Missing_Nodes_DSS.txt``` identifies nodes (buses) that appear in one OpenDSS model (baseline step 2 or exported step 5), but not the other.
    - ```*Missing_Links_DSS.txt``` identifies links (branches) that appear in one OpenDSS model (baseline step 2 or exported step 5), but not the other.
    - ```*Compare_Voltages_DSS.csv``` compares the bus voltages from steps 3 and 7, sorted by increasing difference
    - ```*Compare_Currents_DSS.csv``` compares the branch currents from steps 3 and 7, sorted by increasing difference
12. In the ```test/glm``` directory, GridLAB-D comparison results are in a set of files. At present, the exported IEEE 37-bus model, which is a delta system, does not solve in GridLAB-D.
    - ```*Compare_Voltages_GLM.csv``` compares the bus voltages from steps 3 and 9, sorted by increasing difference
    - ```*Compare_Currents_GLM.csv``` compares the branch currents from steps 3 and 9, sorted by increasing difference

### Comparing Test Results

After completing step 3 of the batch process or step 10 of the detailed process, you should see
a summary of the model output differences as shown below.

* Nbus  is the number of buses found in [Base OpenDSS, Converted OpenDSS, Converted GridLAB-D]
* Nlink is the number of links found in [Base OpenDSS, Converted OpenDSS, Converted GridLAB-D]
* MAEv  is the mean absolute voltage error between Base OpenDSS and [Converted OpenDSS, Converted GridLAB-D], in per-unit
* MAEi  is the mean absolute link current error between Base OpenDSS and [Converted OpenDSS, Converted GridLAB-D], in Amperes

```
ACEP_PSIL      Nbus=[  24,  24,   39] Nlink=[   39,   39,  21] MAEv=[0.0008,0.0316] MAEi=[ 0.9958,22.6701]
EPRI_DPV_J1    Nbus=[4245,4245, 5674] Nlink=[ 7887, 7831,4186] MAEv=[0.0008,0.1860] MAEi=[ 0.1231,54.4130]
IEEE123        Nbus=[ 274, 274,  433] Nlink=[  470,  470, 257] MAEv=[0.0004,0.0038] MAEi=[ 0.0179, 3.1347]
IEEE123_PV     Nbus=[ 442, 442,  655] Nlink=[  744,  748, 338] MAEv=[0.0001,0.0160] MAEi=[ 0.0573, 2.3041]
Transactive    Nbus=[3036,3036, 5602] Nlink=[ 6888, 6888, 363] MAEv=[0.0002,0.0042] MAEi=[ 0.0220, 0.6304]
IEEE13         Nbus=[  56,  56,   90] Nlink=[  103,  103,  44] MAEv=[0.0197,0.0450] MAEi=[ 9.5887,35.5342]
IEEE13_Assets  Nbus=[  41,  41,   66] Nlink=[   77,   79,  37] MAEv=[0.0099,0.0355] MAEi=[14.1490,29.2863]
IEEE37         Nbus=[ 117, 117,    0] Nlink=[  180,  180,   0] MAEv=[0.2536,0.0000] MAEi=[ 5.4502, 0.0000]
IEEE8500       Nbus=[8531,8531,10915] Nlink=[12086,12086,4958] MAEv=[0.0017,0.0706] MAEi=[ 0.2038, 1.2057]
IEEE8500_3subs Nbus=[9493,9493,12463] Nlink=[13874,13897,5570] MAEv=[0.0036,0.0547] MAEi=[ 0.1925, 0.5880]
R2_12_47_2     Nbus=[1631,1632, 1665] Nlink=[ 2246, 2269, 638] MAEv=[0.0158,0.0134] MAEi=[10.0851, 6.5670]
```

Some notes about these comparisons:

* The IEEE37 example has zero entries for Nbus, Nlink, MAEv and MAEi for GridLAB-D because that model doesn't solve
* GridLAB-D doesn't export load currents and other shunt currents to the CSV file, but Nlink includes them for OpenDSS
* Only voltage errors within 0.8 per-unit are included in MAEv. This means the comparison doesn't try to match voltages in a de-energized part of the network due to wiring, phasing or switching errors. However, such errors would still appear in MAEi.
* Efforts may be undertaken to reduce MAEv and MAEi.

## Model Translations

Feeder model converters from CYMDist and Synergi Electric to OpenDSS are now maintained as part of 
CIMHub [here](https://github.com/GRIDAPPSD/CIMHub/tree/issue/1175/converters). 

## Directory Contents

These directories are actively maintained:

* CIM: Enterprise Architect file of the CIM UML subset used in GridAPPS-D
* DER: scripts to add DER to some of the platform feeders for DERMS interoperability testing
* platform: helper scripts to load 11 feeders into the platform
* platform/cimxml: feeder CIM XML and mRID files
* platform/dss/EPRI: the EPRI J1 distributed PV model, with a full set of XY coordinates
* platform/dss/NREL: a version of the IEEE 123-Bus test feeder with PV added
* platform/dss/UAF: model of the University of Alaska - Fairbanks Power System Integration Lab
* platform/dss/WSU: the IEEE 9500-node test feeder
* platform/glm/pnnl: a transactive energy version of the IEEE 123-bus feeder, with secondary service points for houses
* platform/support: appliance schedules for GridLAB-D
* platform/test: feeder model solutions from OpenDSS before the CIM export, i.e., the baseline solutions
* platform/test/dss: solutions from converted OpenDSS models, commpared to the baseline (created on the fly)
* platform/test/glm: solutions from converted GridLAB-D models, commpared to the baseline (created on the fly)
* taxonomy: PNNL taxonomy feeders as used in GridAPPS-D

These directories are deprecated, moved, or not of general interest:

* archive (unused files)
* blazegraph (moved to CIMHub)
* cnf (platform maintenance)
* converters (moved to CIMHub)
* glmanip_module (platform maintenance)
* houses (moved to CIMHub)
* Meas (moved to CIMHub)

