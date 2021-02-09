# GridAPPS-D Platform Feeder Model Testing and Import

Copyright (c) 2018-2021, Battelle Memorial Institute

## Inserting Distributed Energy Resources (DER)

DER may be added to an existing network model in Blazegraph. For convenience, the DER locations are defined by
a bus name, i.e., the name of CIM ConnectivityNode rather than the mRID of a CIM
ConnectivityNode. This assumes that ConnectivityNode names will be unique, which is not
required in CIM although it's required in most power flow programs.

The first step is to create a comma-separated file to define the DER. See *transactive123_der.txt*
for an example. It contains the following lines, all required:

1. *uuid_file* names a file to save the mRIDs for inserted DER. This file will persist the mRID values if you run the script again to change some DER attributes; any DER that is updated on the same bus will use the same mRID values. If this file doesn't exist, random mRID values will be generated, and then saved for subsequent re-use.

2. *feederID* is the mRID of a feeder to search for the buses with DER

3. One line of text defines each new or updated DER. These lines must follow lines 1-4. The comma-separated fields are:
 
    - name of the DER

    - bus name (ConnectivityNode name) for DER connection. For best results, have lines or transformers in the network models that separate all DER, i.e., no more than one per bus.
 
    - phases; on a three-phase system use combinations of ABC. On a split-phase seconary use "s1s2"

    - type; choose Battery, Photovoltaic or SynchronousMachine

    - RatedkVA of the inverter or machine

    - RatedkV of the inverter. Choose 0.208 for split-phase secondary, line-to-line kV for two-phase or three-phase primary, line-to-neutral kV for single-phase primary. Or, rated line-to-line kV for a SynchronousMachine (only three-phase machines are supported).

    - active power output in kW. If negative for a battery it will be charging. At present, there is no way of specifying separate inverter and PV panel ratings

    - reactive power output in kVAR. If negative, the inverter or machine absorbs reactive power

    - Battery storage capacity in kWH; may be omitted for Photovoltaic and SynchronousMachine

    - Battery stored energy in kWH, which defines the state of charge. May be omitted for Photovoltaic and SynchronousMachine

As in the example file, any line beginning with // is interpreted to be a comment line

Other files in this directory are:

- *insert_der.sh* is the script to insert DER, by calling a CIMHub Python utility program. Blazegraph must be running and loaded with the base network model. First Python argument is a config file and the second argument is the DER text file as described above.

- *drop_der.sh* removes previously inserted DER from Blazegraph, by calling CIMHub Python utility program. First Python argument is a config file and the second argument is a file of the object mRIDs to remove.

- *cimhubconfig.json* contains the *blazegraph_url* and *cim_ns* for the scripts (this information used to be in a ```config.txt``)


