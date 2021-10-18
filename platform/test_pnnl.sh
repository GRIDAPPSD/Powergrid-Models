#!/bin/bash
source envars.sh

curl -D- -X POST $DB_URL --data-urlencode "update=drop all"
curl -D- -H "Content-Type: application/xml" --upload-file /home/tom/src/Powergrid-Models/platform/cimxml/PNNL.xml -X POST $DB_URL
java -cp $CIMHUB_PATH $CIMHUB_PROG -o=both -l=1.0 -i=1 /home/tom/src/Powergrid-Models/platform/both/PNNL

opendsscmd check_pnnl.dss

python3 -c """import cimhub.api as cimhub;cimhub.make_glmrun_script([{'root':'PNNL','glmvsrc':1.0}],'./both/','../test/glm','check_pnnl.sh')"""
./check_pnnl.sh
#python3 Compare_Cases.py

