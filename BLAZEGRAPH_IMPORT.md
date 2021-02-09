# GridAPPS-D Platform Maintenance

Copyright (c) 2017-2021, Battelle Memorial Institute

Feeder model import into GridAPPS-D is now accomplished using Python scripts from [CIMHub](https://github.com/GRIDAPPSD/CIMHub/tree/develop/utils).
Please clone those Python scripts, and check the ```requirements.txt``` file in their directory. Another precondition is that
the CIM XML files have been tested as described in the [platform](platform) directory.

## Importing to Blazegraph

Import blazegraph data from the ```platform/cimxml``` folder.  From the Blazegraph UI click the Update tab and then upload 
each of the .xml files in the directory.

## Inserting Measurements

1. Open a command shell to the ```platform``` directory.
2. Run ```./list_all_measurements.sh``` to create measurement lists for each circuit. mRIDs are reused if possible.
3. Run ```./insert_all_measurements.sh``` to insert the measurements
 
## Inserting Houses

 1. Open a command shell to the ```platform``` directory.
 2. Run ```./insert_all_houses.sh``` to insert the Houses. mRIDs are reused if possible.
 
## Additional Scripts

There are some helper scripts in the ```platform``` directory:

* ```./drop_all_houses.sh``` removes just the Houses from backbone feeders
* ```./drop_all_measurements.sh``` removes just the Measurements from backbone feeders
* ```./import_all.sh``` will import the CIM XML files, insert the Measurements, and insert the Houses in a single step
* ```./list_feeders.sh``` lists the feeder mRID values by name

## Importing more than one model at a time

 1. Mount the ```platform/cimxml``` directory to /tmp/test
 1. Goto the update tab and insert the following 
    ````
    load <file:///tmp/test/ACEP_PSIL.xml>;
    load <file:///tmp/test/IEEE123_PV.xml>;
    load <file:///tmp/test/IEEE123.xml>;
    load <file:///tmp/test/IEEE13_Assets.xml>;
    load <file:///tmp/test/IEEE37.xml>;
    load <file:///tmp/test/IEEE8500.xml>;
    load <file:///tmp/test/EPRI_DPV_J1.xml>;
    load <file:///tmp/test/IEEE13.xml>;
    load <file:///tmp/test/IEEE8500_3subs.xml>;
    load <file:///tmp/test/R2_12_47_2.xml>;
		load <file:///tmp/test/Transactive.xml>;
    ````
 1. Click update button on the bottom.
 
## Exporting blazgraph data from a container

- docker cp blazegraph_container:/var/lib/jetty/blazegraph.jnl ./blazegraph.jnl
