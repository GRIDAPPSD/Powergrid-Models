#!/bin/bash
source envars.sh

#java -cp $CIMHUB_PATH $CIMHUB_PROG -u=$DB_URL -o=idx test

java -cp $CIMHUB_PATH $CIMHUB_PROG -u=$DB_URL \
   -s=_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 -o=both -l=1.0 -i=1 der/ieee13_der

cd der

gridlabd -D WANT_VI_DUMP=1 ieee13_der_run.glm >ieee13_der.log
opendsscmd check.dss

tail *.log
tail *_s.csv

