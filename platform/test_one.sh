#!/bin/bash
source envars.sh

./convert_source.sh

# part of ./convert_xml.sh
curl -D- -X POST $DB_URL --data-urlencode "update=drop all"
curl -D- -H "Content-Type: application/xml" --upload-file /home/tom/src/Powergrid-Models/platform/cimxml/IEEE8500.xml -X POST $DB_URL
java -cp $CIMHUB_PATH $CIMHUB_PROG -u=$DB_URL -o=both -l=1.0 -i=1 /home/tom/src/Powergrid-Models/platform/both/IEEE8500

opendsscmd check_one.dss

# part of ./check_glm.sh
cd /home/tom/src/Powergrid-Models/platform/both/
gridlabd -D WANT_VI_DUMP=1 IEEE8500_run.glm >../test/glm/IEEE8500.log
mv IEEE8500*.csv ../test/glm

cd /home/tom/src/Powergrid-Models/platform/
python3 -m cimhub.Compare_Cases

