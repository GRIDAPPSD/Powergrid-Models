## Inserting Distributed Energy Resources (DER)

DER may be added to an existing network model in Blazegraph. Currently, only Photovoltaic
and Battery DER types may be added. For convenience, the DER locations are defined by
a bus name, i.e., the name of CIM ConnectivityNode rather than the mRID of a CIM
ConnectivityNode. This assumes that ConnectivityNode names will be unique, which is not
required in CIM although it's required in most power flow programs.

The first step is to create a comma-separated file to define the DER. See *transactive123_der.txt*
for an example. It contains the following lines, all required:

1. *uuid_file* names a file to save the mRIDs for inserted DER. This file will persist the mRID values if you run the script again to change some DER attributes; any DER that is updated on the same bus will use the same mRID values. If this file doesn't exist, random mRID values will be generated, and then saved for subsequent re-use.

2. *blazegraph_url* defines the Blazegraph triple-store to query against.

3. *cim_namespace* defines the CIM namespace, i.e. the UML version, for attributes

4. *feederID* is the mRID of a feeder to search for the buses with DER

5. One line of text defines each new or updated DER. These lines must follow lines 1-4. The comma-separated fields are:
 
    - name of the DER

    - bus name (ConnectivityNode name) for DER connection. For best results, have lines or transformers in the network models that separate all DER, i.e., no more than one per bus.
 
    - phases; on a three-phase system use combinations of ABC. On a split-phase seconary use "s1s2"

    - type; choose Battery or Photovoltaic

    - RatedkVA of the inverter

    - RatedkV of the inverter. Choose 0.208 for split-phase secondary, line-to-line kV for two-phase or three-phase primary, line-to-neutral kV for single-phase primary

    - active power output in kW. If negative for a battery it will be charging. At present, there is no way of specifying separate inverter and PV panel ratings

    - reactive power output in kVAR. If negative, the inverter absorbs reactive power

    - Battery storage capacity in kWH; may be omitted for Photovoltaic

    - Battery stored energy in kWH, which defines the state of charge. May be omitted for Photovoltaic

As in the example file, any line beginning with // is interpreted to be a comment line

Other files in this directory are:

- *InsertDER.py* is the script to insert DER. Blazegraph must be running and loaded with the base network model. Commmand line argument is the DER text file as described above. See *insert.sh* for a calling example.

- *DropDER.py* removes previously inserted DER from Blazegraph. First argument is a config file (described next) and the second argument is a file of the object mRIDs to remove. See *drop.sh* for a calling example

- *config.txt* contains the *blazegraph_url* and *cim_namespace* for the script that deletes DER


