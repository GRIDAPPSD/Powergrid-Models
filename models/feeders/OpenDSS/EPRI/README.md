# EPRI Distributed Photovoltaic (DPV) Feeder Models

This repository contains a modified version of the J1 feeder from:

* [EPRI Distributed Photovoltaic (DPV) Test Feeders](http://dpv.epri.com/)

The local modifications to J1 are:

* Corrected some line code phasing numbers
* Set the base case to include PV
* Set the loadshapes to constant values
* Added some XY coordinates

The purpose is to create a simplified J1 snapshot model that can be automatically exported to CIM or other formats. Time series variations and controls may be downloaded from EPRI's site, or created anew by the user.

The K1 and M1 baseline (unmodified) DPV models have also been archived here, but are not yet used in GridAPPS-D. Those two feeder models do not appear to have PV components on the EPRI site, so the user would have to create those manually.
