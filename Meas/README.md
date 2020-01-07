Measurements in GridAPPS-D are added to the triple-store in Blazegraph according to a sensor scheme. Physically, electrical sensors are always connected to a piece of conducting equipment. Therefore, they must be referenced to the correct CIM terminal ID. To get started, the feeder backbone model should already be loaded into the triple-store, and you should have a feeder map or other information required to implement your sensor strategy. It is expected that sensor strategies will be changed as part of platform configuration, either to change an application's test environment, or to evaluate the cost-benefit of different sensor strategies. Therefore, the Measurements are associated to, but not really part of, the feeder model. When GridAPPS-D starts a simulation, it will use both.

Under powergrid-models/Meas, there are four sample Python files, one constants file (constants.py), and three shell scripts that help manage the Measurements. First, these functions extract the candidate Measurements with valid CIM IDs. Second, you have a chance to customize them. Third, you batch-insert the Measurements into Blazegraph's triple-store.

Inspect constants.py to ensure the blazegraph url and sparql prefix is correct:

 - Port 9999 for standalone use of Blazegraph with terminal scripts and a web browser
 - Port 8080 for using Blazegraph within the GridAPPS-D docker container
 - Port 8889 for using Blazegraph from outside the GridAPPS-D docker container

Sample commands below should be executed in the powergrid-models/Meas directory. 

1. "python ListFeeders.py" produces an index of feeders in the triple-store.  Find the mRID of the feeder you want to instrument.  

2. "python ListMeasureables.py rootname _4F76A5F9-271D-9EB8-5E31-AA362D86F2C3" produces a list of ConductingEquipment instances that can have sensors attached, written to separate files as listed below.  The example mRID corresponds to the IEEE 8500-node feeder, circa March 2018.
 
    - rootname_special.txt contains capacitor (state, S, V), tap changer (state), solar photovoltaic (S, V), and storage (S, V) measurements. 
 
    - rootname_node_v.txt contains node voltage measurements for every switch, line segment and transformer, not already included in rootname_special.txt 

    - rootname_loads.txt contains load S and V measurements, which may overlap with rootname_node_v.txt 

		- rootname_machines.txt contains rotating machine S and V measurements, which may overlap with rootname_node_v.txt

    - rootname_switch_i.txt contains LoadBreakSwitch current measurements into the first bus (ConnectivityNode) 

    - rootname_lines_pq.txt contains ACLineSegment S measurements into the first bus (ConnectivityNode)
 
    - rootname_xfmr_pq.txt contains TransformerEnd (i.e. winding) S measurements into winding 1, i.e., only the high-voltage side

3. "python InsertMeasurements.py filename" will insert a measurement file from step 2 into the triple-store.  This Python code makes some assumptions (e.g.  each capacitor, aka LinearShuntCompensator, in the file will get a PNV, VA and POS measurement).  The LoadBreakSwitch POS measurements are not implemented.  You can modify this Python file to implement your own measurement strategies.  
 
4. "python DropMeasurements.py mRID" will remove all measurements associated with the feeder mRID. After this, you can re-start step 3 with a clean slate.

5. listall.sh executes step 2 for each of the eleven feeders currently included in GridAPPS-D

6. insertall.sh inserts a selection of measurement files created in step 5

    - by default, all non-empty measurement files are inserted, implying a more-than-realistic number of sensors

		- a suggested realistic set of measurements would include special, machines, switch_i, plus a subset of PNV and VA measurements from the other files

7. dropall.sh removes all measurements for each of the eleven feeders currently included in GridAPPS-D

Whenever you create a new feeder model from CIMImporter, it will also create a JSON file that describes the sensors that were added to the triple-store.

==================== 

The file ieee8500_rc1.bak includes a small set of measurements used by the VVO app circa RC1. To use these invoke "python InsertMeasurements.py ieee8500_rc1.bak". The file used to be named ieee8500_measurements.txt. However, the state estimator service and other applications need more measurements than this small set.

====================

You can edit any of the files created in step 2 for customizations according to the following rules.  

    - For voltage measurements, the type is PNV and you need to find an ACLineSegment, LoadBreakSwitch, LinearShuntCompensator, EnergyConsumer, PowerTransformer+PowerTransformerEnd or PowerElectronicsConnection that's connected to the ConnectivityNode (aka bus) of interest.  

    - For current and power measurements, the type is A or VA, respectively, and the sign convention is positive into the ConnectivityNode (aka bus) of interest.  Similar the case of PNV, you have to find the ConductingEquipment instance that carries the current or power you wish to measure.  

    - For tap measurements, the type is POS and you find the PowerTransformer+RatioTapChanger of interest.  For switch state measurements, the type is POS and you'll find the LinearShuntCompensator or LoadBreakSwitch of interest.  

Through this process, you would mostly remove lines from rawlist.txt. The other valid changes are:
     - In ACLineSegment or LoadBreakSwitch, the second token may be changed to:
         - v1 to measure PNV at bus1
         - v2 to measure PNV at bus2
         - s1 to measure VA into bus1
         - s2 to measure VA into bus2
         - i1 to measure A into bus1
         - i2 to measure A into bus2
     - In PowerTransformer+PowerTransformerEnd, the third token may be changed to:
         - v to measure PNV on that winding
         - s to measure VA into that winding
         - i to measure A into that winding
     - Note that each line specifies a separate phase measurement; three lines are needed to measure a three-phase quantity.
     - The only valid use case for adding lines to rawlist.txt is to copy-and-paste ACLineSegment, LoadBreakSwitch or PowerTransformer lines to request additional quantities, eg., v2 or s 

