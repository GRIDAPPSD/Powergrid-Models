# CYMDIST to OpenDSS Converter

This converter has been tested on a sample of electric utility feeder models in 
versions 7.1, 7.2 and 8.1 of CYMDIST. Inputs are a self-contained study (SXST) file
from CYMDIST, and a JSON configuration file created by the user as described below.
You also have to manually create the substation source model for OpenDSS, in part
because this converter has not been tested with models from the CYMDIST substation
add-on module. For SXST files without this add-on, a commented version of the new
circuit definition will be written to the master OpenDSS model file.

To execute the conversion script on Windows:

>  python Cyme2DSS.py Sample.json

For Mac/Linux:

>  python3 Cyme2DSS.py Sample.json

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

Copyright (c) 2017-2019, Battelle Memorial Institute

License: https://gridappsd.readthedocs.io/en/master/license/license.html
