#!/bin/bash
source envars.sh
#cp cimhubjar.json cimhubconfig.json

./load_all_cimxml.sh
./list_all_measurements.sh
./insert_all_measurements.sh
./insert_all_houses.sh
./test_all_houses.sh

