# Model Converters

This directory contains Python scripts and configuration examples that convert
some commercial distribution feeder models to OpenDSS. After that, the tools on this
site can produce Common Information Model (CIM) files, and then GridLAB-D files.

These converters have been tested on a relatively small number of real feeders, but the
data cannot be provided to others by PNNL. Instead, users will need to already have 
their own feeder model files, and then modify one of the example JSON converter configuration
files in order to use it.

## CYMDIST to OpenDSS Converter

This converter has been tested on a sample of electric utility feeder models in 
versions 7.1, 7.2 and 8.1 of CYMDIST. Inputs are a self-contained study (SXST) file
from CYMDIST, and a JSON configuration file created by the user as described below.
You also have to manually create the substation source model for OpenDSS, in part
because this converter has not been tested with models from the CYMDIST substation
add-on module. For SXST files without this add-on, a commented version of the new
circuit definition will be written to the master OpenDSS model file.

To execute the conversion script on Windows:

>  python Cyme2DSS.py SampleCyme1.json

For Mac/Linux:

>  python3 Cyme2DSS.py SampleCyme1.json

The JSON configuration file attributes are:

- *xmlfilename*; base file name of the SXST file, should be in current directory
- *RootName*; root file name for the OpenDSS output files
- *SubName*; root file name for the substation model to be included in OpenDSS
- *LoadScale*; scaling factor from CYMDIST to OpenDSS loads, usually 1.0
- *LoadModel*; load model to use in OpenDSS, usually 1 for constant power or 2 for constant impedance
- *DefaultBaseVoltage*; should be the feeder primary line-to-line kV
- *BaseVoltages*; array of one or more voltages, in kV, to use for OpenDSS voltage bases. Should include the DefaultBaseVoltage, plus any transmission, secondary or other voltage bases expected in the model. OpenDSS uses these for per-unit voltage output, and also to set nominal voltages for load and generation through its "SetLoadAndGenkV" command.
- *CoordXmin*; minimum X coordinate value for circuit plots in OpenDSS, used to exclude outliers
- *CoordXmax*; maximum X coordinate value for circuit plots in OpenDSS, used to exclude outliers
- *CoordYmin*; minimum Y coordinate value for circuit plots in OpenDSS, used to exclude outliers
- *CoordYmax*; maximum Y coordinate value for circuit plots in OpenDSS, used to exclude outliers
- *CYMESectionUnit*; unit for section lengths in CYMDIST, only "m" has been tested
- *CYMELineCodeUnit*; unit for section lengths in CYMDIST, only "km" has been tested
- *DSSSectionUnit*;  unit for line section lengths in OpenDSS; only "ft", "kft", "mi" and "m" have been tested
- *OwnerIDs*; array of one or more OwnerID values, i.e., feeders, to convert from the SXST file

The output model files for OpenDSS are:

- *RootName*_catalog.dss; the line and transformer codes used 
- *RootName*_master.dss; the top-level OpenDSS model file to run; it includes the others in this list, plus *SubName.sub* and *RootName.edits* 
- *RootName*_xy.dat; the XY coordinates for circuit plots
- *OwnerID*_loads.dss; the nominal loads for each converted OwnerID, i.e., feeder
- *OwnerID*_network.dss; the lines, transformers, capacitors, switches and other devices for each converted OwnerID, i.e., feeder

The two model files you must manually create for OpenDSS are:

- *RootName.edits*; this is included after the feeder backbone has been created, and before calculating voltage bases in OpenDSS. You can start with an empty file. Typical contents include control and protection settings, parameter adjustments, and creation of DER for study. This file is not over-written if you run the converter again.
- *SubName.sub*; at minimum, this file needs to create the new circuit for OpenDSS. You may also add transmission lines, substation switchgear, substation regulator, energy meter, or other components before the feeder backbone is included. This file is not over-written if you run the converter again.

## Synergi Electric to OpenDSS Converter

This converter has been tested on feeder models from two different utilities. Inputs are the 
Microsoft Access database (MDB) file from Synergi, and a JSON configuration file created by 
the user as described below. First, install a light-weight driver to read MDB files:

>  pip install pypyodbc

Then, to run the conversion

>  python mdb2dss.py SampleSynergi1.json

If this fails, you may need to either install Microsoft Access, or download a Microsoft driver
by searching the internet for *"Microsoft Access Database Engine 2010 Redistributable"*. However,
there are some important constraints. If your Python is 32-bit the MDB driver must also be 32-bit. 
If your Python is 64-bit the MDB driver must also be 64-bit. If you already have Microsoft Office 
installed, you may not be allowed to install a MDB driver that matches your Python installation. 
In that case, you would have to run this converter on a different platform.

The JSON configuration file attributes are:

- *DefaultDir*; path to the MDB file
- *MDBName*; name of the Synergi model file, should exist in DefaultDir
- *RelativeOutDir*; path to the generated OpenDSS files, relative to DefaultDir
- *SubOrFeeder*; set 1 if the source is a substation, or 0 if the source is a feeder (see Synergi documentation)
- *InsertSwitchgear*; set 1 to insert switchgear in padmounts (see Synergi documentation)

The output model files for OpenDSS are:

- *RootName*_catalog.dss; the line and transformer codes used 

Copyright (c) 2017-2020, Battelle Memorial Institute

License: https://gridappsd.readthedocs.io/en/master/license/license.html
