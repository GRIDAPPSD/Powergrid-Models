This directory contains data and scripts that will insert houses into a 
CIM feeder model that has EnergyConsumer objects (aka ZIP loads) already 
defined.  In order to actually use the houses, a -h=1 argument must be 
given to the CIMImporter, and only -o=glm applies.

The feeder model must already be loaded into Blazegraph, and you must also 
know the feeder mRID.  

Example files include:

* _test.sh_ inserts houses on two feeders, first the 8500-node in region 3, and second the taxonomy feeder in region 2
* _drop_all.sh_ removes houses on feeders that were populated by _test.sh_

# insertHouses.py
Main module for this directory. To see inputs, run `python3 insertHouses.py --help`. 

In short, it gathers residential EnergyConsumers from a CIM triplestore 
database, uses a createHouses.createHouses object to generate housing 
units for each EnergyConsumer, and then inserts the housing unit 
definitions into the CIM triplestore.  

TODO: The last step, inserting housing units into the triplestore, is incomplete.

NOTE: insertHouses.py imports the constants.py module from ../Meas. This is used to configure CIM triplestore connection and queries.

# createHouses.py
Module with a class, createHouses. Reads data file in eia_recs directory, and uses class function genHousesForFeeder to generate houses for the given climate region.

# DropHouses.py
Supporting script that formulates the SPARQL statement for removing houses. 

# eia_recs directory
See README within directory. In short, has EIA RECS data, a script to parse it, and resulting data files for use by createHouses.

Note that the data files are stored with Git LFS.
# requirements.txt
Standard Python requirements file detailing all packages needed for modules within this directory (and eia_recs directory)