#!/bin/bash
source envars.sh

#python3 -m cimhub.DropDER ../cimhubconfig.json acep_psil_der_uuid.txt
#python3 -m cimhub.DropDER ../cimhubconfig.json test9500new_der_uuid.txt
python3 -m cimhub.DropDER ../cimhubconfig.json transactive123_der_uuid.txt

