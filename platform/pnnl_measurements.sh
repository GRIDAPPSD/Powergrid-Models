#!/bin/bash
source envars.sh
python3 -m cimhub.ListMeasureables cimhubconfig.json pnnl _0EAA27FD-4D5F-4F29-A928-939CF7D0EBA0 Meas

python3 -m cimhub.InsertMeasurements cimhubconfig.json ./Meas/pnnl_lines_pq.txt  ./Meas/pnnl_msid.json
python3 -m cimhub.InsertMeasurements cimhubconfig.json ./Meas/pnnl_loads.txt     ./Meas/pnnl_msid.json
python3 -m cimhub.InsertMeasurements cimhubconfig.json ./Meas/pnnl_node_v.txt    ./Meas/pnnl_msid.json
python3 -m cimhub.InsertMeasurements cimhubconfig.json ./Meas/pnnl_special.txt   ./Meas/pnnl_msid.json
python3 -m cimhub.InsertMeasurements cimhubconfig.json ./Meas/pnnl_switch_i.txt  ./Meas/pnnl_msid.json
python3 -m cimhub.InsertMeasurements cimhubconfig.json ./Meas/pnnl_xfmr_pq.txt   ./Meas/pnnl_msid.json


