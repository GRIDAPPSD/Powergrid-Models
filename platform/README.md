# GridAPPS-D Platform Feeder Model Testing and Import

Copyright (c) 2017-2021, Battelle Memorial Institute

If there have been no changes to the CIM XML files in ```platform/cimxml```, you may follow these [import instructions](../BLAZEGRAPH_IMPORT.md).

If the source models have changed under ```platform/dss``` or ```platform/glm```, please test them first as described next.

## GridAPPS-D Model Testing

Feeder model translations for GridAPPS-D are now accomplished using [CIMHub](https://github.com/GRIDAPPSD/CIMHub).
Please be able to build CIMHub as described in the previous section, so that you can
perform this model testing process as a developer.

After the models have been tested, check them in to ```platform/cimxml```, from which they can be [imported into GridAPPS-D](../BLAZEGRAPH_IMPORT.md).

### Preparations

Verify that the Blazegraph namespace is _kb_ and use that for the rest of these examples
* You can use a different namespace, but you'll have to specify that using the -u option for the CIMImporter, handediting the default _-u=http://localhost:8889/bigdata/namespace/kb/sparql_
* You can use a different namespace, but you may have to hand-edit some of the Python files (e.g. under the Meas directory)
* The GridAPPS-D platform itself may use a different namespace
* Please ```pip3 install cimhub --upgrade```. (Earlier versions used a local copy of the Python utilities, with a CIMHUB_UTILS environtment variable point to it. This has been removed.)

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

### Configuration

The CIM namespace, Blazegraph URL and local paths are now configured in two files.
Please check their contents, adjusting as needed, before going further.

Bash scripts include [envars.sh](envars.sh), which defines:

* ```DB_URL``` is the Blazegraph URL, including namespace. Should match ```blazegraph_url``` in [cimhubconfig.json](cimhubconfig.json)
* ```SRC_PATH``` is the fully qualified path to the ```platform``` scripts
* ```CIMHUB_PATH``` is the relative path to CIMHub code (you must have cloned this repository)
* ```CIMHUB_PROG``` is the Java program name for CIMHub

Python scripts configure CIMHub from [cimhubconfig.json](cimhubconfig.json), which defines:

* ```blazegraph_url``` is the Blazegraph URL, including namespace. Should match ```DB_URL``` in [envars.sh](envars.sh)
* ```cim_ns``` is the CIM namespace (not the Blazegraph namespace)

### Batch Testing Process

If there have not been any changes to the CIM XML, you may skip ahead to the [Preparation for Batch Import](#preparation-for-batch-import).

To test all 11 feeder models at once, before importing into the platform:

1. Change to the ```platform``` directory
2. Start Blazegraph with ```docker restart blazegraph```
3. Issue ```./test_all.sh```

If any errors occur, you might need the [Step-by-step Testing Process](#step-by-step-testing-process) to localize the problem.
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
4. Issue ```python3 -m cimhub.MakeLoopScript -b``` to create the script for step 5
    - optionally specify the non-default source path:
      ```python3 -m cimhub.MakeLoopScript -b /home/tom/src/Powergrid-Models/platform/```
5. Issue ```./convert_xml.sh``` to:
    - Empty and create a new ```test``` directory
    - Sequentially ingest the CIM XML files into Blazegraph, and export both OpenDSS and GridLAB-D models
    - This step may take a few minutes. When finished, all of the GridLAB-D and OpenDSS models will be in ```both``` subdirectory
    - When finished, only the last CIM XML will still be in Blazegraph. _This should be deleted before doing any more work in Blazegraph, to ensure compatible namespaces_.
6. Issue ```python3 -m cimhub.MakeLoopScript -d``` to make a sequential solution script for OpenDSS.
    - optionally specify the non-default source path and Blazegraph URL:
     ```python3 -m cimhub.MakeLoopScript -d /home/tom/src/Powergrid-Models/platform/```
7. Issue ```opendsscmd check.dss``` to run OpenDSS power flows on the exported models.
    - Results will be in the ```test/dss``` subdirectory
        - ```rootname_s.csv``` contains exported snapshot loadflow summary
        - ```rootname_i.csv``` contains exported branch currents
        - ```rootname_v.csv``` contains exported bus voltages
        - ```rootname_t.csv``` contains exported regulator tap positions
8. Issue ```python3 -m cimhub.MakeGlmTestScript``` to create the GridLAB-D wrapper files, ```*run.glm``` and a script execution file in ```both``` subdirectory
    - optionally specify the non-default source path and Blazegraph URL:
     ```python3 -m cimhub.MakeGlmTestScript /home/tom/src/Powergrid-Models/platform/```
9. Issue ```./check_glm.sh```.  This runs GridLAB-D power flow on the exported models.
    - Results will be in the ```tests/glm``` directory
        - ```rootname.log``` contains the GridLAB-D error, warning and information messages
        - ```rootname_volt.csv``` contains the output from a GridLAB-D voltdump, i.e., the node (bus) voltages
        - ```rootname_curr.csv``` contains the output from a GridLAB-D currdump, i.e., the link (branch) currents
10. Issue ```python3 -m cimhub.Compare_Cases``` to compare the power flow solutions from steps 7 and 9 to the baseline solutions from step 3
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
  OpenDSS branch flow in LINE.SEG4 from BATT, Base case
  Phs     Volts     rad      Amps     rad         kW          kVAR   PhsPhs     Volts     rad
    A    284.27 -0.0663    346.27 -0.7681     75.172 + j    63.548     AB      492.36  0.4573
    B    284.27 -2.1607    346.27 -2.8625     75.172 + j    63.548     BC      492.36 -1.6371
    C    284.27  2.0281    346.27  1.3263     75.172 + j    63.548     CA      492.36  2.5517
    Total S =   225.515 + j   190.643
  OpenDSS branch flow in LINE.SEG4 from BATT, Converted case
  Phs     Volts     rad      Amps     rad         kW          kVAR   PhsPhs     Volts     rad
    A    284.27 -0.0663    346.27 -0.7681     75.172 + j    63.548     AB      492.36  0.4573
    B    284.27 -2.1607    346.27 -2.8625     75.172 + j    63.548     BC      492.36 -1.6371
    C    284.27  2.0281    346.27  1.3263     75.172 + j    63.548     CA      492.36  2.5517
    Total S =   225.515 + j   190.643
  GridLAB-D branch flow in LINE_SEG4 from BATT
  Phs     Volts     rad      Amps     rad         kW          kVAR   PhsPhs     Volts     rad
    A    272.52 -0.0135    361.42 -0.7163     75.160 + j    63.657     AB      472.02  0.5101
    B    272.52  4.1753    361.42  3.4725     75.160 + j    63.657     BC      472.02 -1.5843
    C    272.52  2.0809    361.42  1.3781     75.160 + j    63.657     CA      472.02  2.6045
    Total S =   225.479 + j   190.972
ACEP_PSIL        Nbus=[    24,    24,    39] Nlink=[    39,    39,    21] MAEv=[ 0.0000, 0.0408] MAEi=[   0.0012,  24.6886]
EPRI_DPV_J1      Nbus=[  4245,  4245,  5674] Nlink=[  5674,  5674, 10341] MAEv=[ 0.0007, 0.1801] MAEi=[   0.1025,  52.5268]
IEEE123          Nbus=[   274,   274,   433] Nlink=[   386,   386,   393] MAEv=[ 0.0000, 0.0025] MAEi=[   0.0216,   0.0860]
IEEE123_PV       Nbus=[   442,   442,   655] Nlink=[   564,   564,   639] MAEv=[ 0.0000, 0.0015] MAEi=[   0.0068,   0.4770]
Transactive      Nbus=[  3036,  3036,  5602] Nlink=[  5507,  5507,   690] MAEv=[ 0.0006, 0.0022] MAEi=[   0.0100,   0.1158]
IEEE13           Nbus=[    56,    56,    90] Nlink=[    87,    87,    60] MAEv=[ 0.0000, 0.0180] MAEi=[   0.0206,   1.9258]
IEEE13_Assets    Nbus=[    41,    41,    66] Nlink=[    64,    64,    45] MAEv=[ 0.0000, 0.0040] MAEi=[   0.0224,   0.8783]
IEEE13_OCHRE     Nbus=[   160,   160,   246] Nlink=[   231,   231,    99] MAEv=[ 0.0000, 0.0005] MAEi=[   0.0010,   0.0438]
  OpenDSS branch flow in LOAD.S728 from 728, Base case
  Phs     Volts     rad      Amps     rad         kW          kVAR   PhsPhs     Volts     rad
    A   2647.75 -0.0820     17.25 -0.5541     40.687 + j    20.776     AB     4684.00  0.4607
    B   2776.82 -2.1660     17.26 -2.6473     42.490 + j    22.195     BC     4835.86 -1.6691
    C   2736.31  1.9775     17.28  1.5404     42.832 + j    20.010     CA     4615.27  2.5086
    Total S =   126.010 + j    62.980
  OpenDSS branch flow in LOAD.S728 from 728, Converted case
  Phs     Volts     rad      Amps     rad         kW          kVAR   PhsPhs     Volts     rad
    A   2647.47 -0.0820     17.25 -0.5541     40.683 + j    20.774     AB     4684.26  0.4608
    B   2777.39 -2.1660     17.26 -2.6473     42.499 + j    22.199     BC     4836.09 -1.6692
    C   2736.00  1.9775     17.28  1.5404     42.827 + j    20.007     CA     4614.76  2.5086
    Total S =   126.008 + j    62.979
IEEE37           Nbus=[   117,   117,     0] Nlink=[   180,   180,     0] MAEv=[ 0.0001,-1.0000] MAEi=[   0.0006,  -1.0000]
IEEE8500         Nbus=[  8531,  8531, 10915] Nlink=[  9720,  9720, 11109] MAEv=[ 0.0014, 0.0077] MAEi=[   0.1094,   0.8632]
IEEE8500_3subs   Nbus=[  9493,  9493, 12463] Nlink=[ 11196, 11196, 12132] MAEv=[ 0.0004, 0.0023] MAEi=[   0.0280,   0.9905]
R2_12_47_2       Nbus=[  1631,  1631,  1665] Nlink=[  1857,  1857,  1404] MAEv=[ 0.0006, 0.0050] MAEi=[   0.3401,   0.6481]
```

Some notes about these comparisons:

* The IEEE37 example has zero entries for Nbus, Nlink, MAEv and MAEi for GridLAB-D because that model doesn't solve. The open-delta regulator bank is not implemented in GridLAB-D, and it was left out of this feeder in GridLAB-D's autotest suite.
* GridLAB-D doesn't export load currents and other shunt currents to the CSV file, but Nlink includes them for OpenDSS
* The EPRI J1 model does not solve properly in GridLAB-D, probably due to staggered single-phase regulators.
* In the ACEP_PSIL model, GridLAB-D must swap the windings on a wye/delta transformer, which spoils the MAEi comparison. The detailed branch output illustrates that the currents agree outside of that transformer.
* In the IEEE37 example, a delta load comparison has been included to show the agreement of line-to-line voltages.
* The IEEE13 and IEEE13_Assets examples include a mixture of constant power, constant impedance and constant current load models. The IEEE8500 example solves in GridLAB-D only with a constant-current load model. The EPRI J1 uses a CVR load model, approximated with constant-current in GridLAB-D. The other examples all use constant-power load models.
* For line constants, OpenDSS defaults to the Deri earth model, while GridLAB-D implements reduced-order Carson. Except for the IEEE123 example, which is distributed with OpenDSS, all of these examples use the reduced-order Carson earth model.
* Only voltage errors within 0.8 per-unit are included in MAEv. This means the comparison doesn't try to match voltages in a de-energized part of the network due to wiring, phasing or switching errors. However, such errors would still appear in MAEi.
* Further efforts may be undertaken to reduce MAEv and MAEi.

### Preparation for Batch Import

This procedure verifies that platform models work with Houses and Measurements. A precondition is that the Batch
Testing Process or Step-by-step Testing Process has been completed. A postcondition is that the CIM XML files
are ready to deploy in GridAPPS-D as described in [BLAZEGRAPH_IMPORT](../BLAZEGRAPH_IMPORT.md).

To import all 11 feeder models at once, including Houses and Measurements:

1. Change to the ```platform``` directory
2. Start Blazegraph with ```docker restart blazegraph```
3. Issue ```./import_all.sh```

The last several lines of console output should indicate that 6 cases have run successfully with Houses.

In order to test the DER scripts on a platform feeder model (assume Blazegraph still running):

1. Change to the ```platform/DER``` directory
2. Issue ```./insert_der.sh```
3. Issue ```./test_der.sh```
4. Issue ```./drop_der.sh```

Console output from step 3 should indicate that a DER case ran on the Transactive model, in both OpenDSS and GridLAB-D.
The test DER is removed from Blazegraph in step 4.

## Directory Contents

The following subdirectories are actively maintained under version control:

* [cimxml](cimxml): feeder CIM XML and mRID files
* [DER](DER): scripts, input files and UUID files that create DER on a platform circuit
* [dss/EPRI](dss/EPRI): the EPRI J1 distributed PV model, with a full set of XY coordinates
* [dss/NREL](dss/NREL): a version of the IEEE 123-Bus test feeder with PV added
* [dss/UAF](dss/UAF): model of the University of Alaska - Fairbanks Power System Integration Lab
* [dss/WSU](dss/WSU): the IEEE 9500-node test feeder
* [glm/pnnl](glm/pnnl): a transactive energy version of the IEEE 123-bus feeder, with secondary service points for houses
* [houses](houses): scripts, input files and UUID files that create Houses on a platform circuit
* [Meas](Meas): scripts, input files and UUID files that create Measurements on a platform circuit
* [support](support): appliance schedules for GridLAB-D
* [test](test): feeder model solutions from OpenDSS before the CIM export, i.e., the baseline solutions

The following directories are created on the fly by scripted testing or import:

* both: converted OpenDSS and GridLAB-D models from ```test_all.sh```
* test/dss: solutions from converted OpenDSS models, commpared to the baseline
* test/glm: solutions from converted GridLAB-D models, commpared to the baseline
 
