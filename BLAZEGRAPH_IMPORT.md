# Importing to Blazegraph

Import blazegraph data from the blazegraph/tests folder.  From the blazegraph ui click the update tab and then upload 
each of the .xml files in the directory.

## Inserting measurements

### Requirements

Python 3 is required for inserting houses and inserting measurements.  In addition the packages in houses/requirements.txt
are required.

### Instructions

 1. Open a command shell to the Meas directory.
 2. Review the listall.sh file which lists the feeders that are available for ingestion.
 3. Run ./listall.sh to generage temporary files for adding measurements.
 4. Run ./insertall.sh to insert measurements for the feeders.
 
## Inserting houses

Note: Make sure the requirements from the Inserting Measurements section above are satisfied.

### Instructions

 1. Open command shell to the houses directory.
 2. Run ./insert_houses.sh script to insert the houses.
 
## Additional Scripts

There are drophouses.sh as well as dropmeasurement.sh scripts available in the houses and Meas directories as well for
removing those from the blazegraph database.

## Importing more than one model at a time

 1. Mount the blazegraph/test directory to /tmp/test
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
