declare -r DB_URL="http://localhost:8889/bigdata/namespace/kb/sparql"
#declare -r DB_URL="http://blazegraph:8080/bigdata/namespace/kb/sparql"
declare -r CIMHUB_PATH="../../CIMHub/target/libs/*:../../CIMHub/target/cimhub-0.0.1-SNAPSHOT.jar"
declare -r CIMHUB_PROG="gov.pnnl.gridappsd.cimhub.CIMImporter"

# list feeders now in the Blazegraph repository; will need the feeder mRIDs from this output
#java -cp $CIMHUB_PATH $CIMHUB_PROG -u=$DB_URL -o=idx test

#java -cp $CIMHUB_PATH $CIMHUB_PROG -u=$DB_URL \
#   -s=_F9A70D1F-8F8D-49A5-8DBF-D73BF6DA7B29 -o=both -l=1.0 -i=1 -t=1 ieee13
#java -cp $CIMHUB_PATH $CIMHUB_PROG -u=$DB_URL \
#   -s=_DFBF372D-4291-49EF-ACCA-53DAFDE0338F -o=both -l=1.0 -i=1 -t=1 ieee13assets

# empty the Blazegraph repository; this is optional unless you are re-uploading the same circuit
#curl -D- -X POST $DB_URL --data-urlencode "update=drop all"

#rm *.csv
sed 's!_SRCPATH_!/home/tom/src!g' ConvertCIM100.template > ConvertCIM100.dss
opendsscmd ConvertCIM100.dss
./ConvertTransactive.sh
