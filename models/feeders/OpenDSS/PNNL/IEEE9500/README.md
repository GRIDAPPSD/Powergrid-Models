# IEEE 9500 Node Model

This repository contains OpenDSS files for the 9500 node test feeder

## Directory of Files

* _BusCoords.dss_ gives XY coordinates for all 12.47kV nodes
* _LatLongCoords.dss_ gives latitude/longitude coordinates for all primary and secondary nodes

* _WireData.dss_ specifies GMR for common aluminum and copper wire configurations
* _CableData.dss_ specifies geometry of underground cables used in the model
* _LineGeometry.dss_ specifies geometry and spacing of overhead lines used in the model
* _LineCodes.dss_ provides unbalanced impedance matrices for overhead lines defined in _LineGeometry.dss_

* _LinesSwitchesGeometry_ defines all line and switch objects used in the model using geometry defined in _LineGeometry.dss_
* _LinesSwitchesLineCodes_ defines all line and switch objects used in the model using linecodes defined in _LineCodes.dss_

* _Capacitors.dss_ defines capacitor banks in the model
* _CapControls.dss_ specifies single phase capacitor control setpoints

* _Transformers.dss_ defines stepdown transformers and LTC controllers in substations S1, S2, S3
* _Regulators.dss_ defines poletop regulators and controllers in the model

* _TriplexLineCodes.dss_ provides impedance matrices for customer secondary lines
* _TriplexLines.dss_ defines all customer secondary lines
* _LoadXfmrCodes.dss_ specifies customer transformer parameters and defines all customer transformers in the model

* _BalancedLoads.dss_ defines all customer loads as balanced two-phase 208V loads
* _UnbalancedLoads.dss_ defines all customer loads as unbalanced single-phase 120V loads 

* _Generators.dss_ defines synchronous and inverter-connected three-phase distributed generators
* _EnergyStorage.dss_ defines centralized energy storage units

* _PV_10PEN_DSSPV.dss_ defines rooftop PV using the PVsystem class, 10% penetration
* _PV_NN_100_DSSPV.dss_ defines rooftop PV in New Neighborhood using PVsystem class, 100% penetration

* _master-bal-initial-config.dss_ master file for balanced load basecase with all N/O switch & DER setpoints
* _master-unbal-initial-config.dss_ master file for unbalanced load basecase with all N/O switch & DER setpoints
