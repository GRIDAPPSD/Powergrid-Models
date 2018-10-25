# insertHouses.py
Main module for this directory. To see inputs, run `python3 insertHouses.py --help`. 

In short, it gathers residential EnergyConsumers from a CIM triplestore database, uses a createHouses.createHouses object to generate housing units for each EnergyConsumer, and then inserts the housing unit definitions into the CIM triplestore.

TODO: The last step, inserting housing units into the triplestore, is incomplete.

NOTE: insertHouses.py imports the constants.py module from ../Meas. This is used to configure CIM triplestore connection and queries.

# createHouses.py
Module with a class, createHouses. Reads data file in eia_recs directory, and uses class function genHousesForFeeder to generate houses for the given climate region.

# eia_recs directory
See README within directory. In short, has EIA RECS data, a script to parse it, and resulting data files for use by createHouses.

Note that the data files are stored with Git LFS.
# requirements.txt
Standard Python requirements file detailing all packages needed for modules within this directory (and eia_recs directory)