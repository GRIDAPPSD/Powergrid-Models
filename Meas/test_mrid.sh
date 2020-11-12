# Find the correct Blazegraph URL; defaults to running inside composed containers
#if (($# > 0)) 
#then
  declare -r DB_URL="http://localhost:8889/bigdata/namespace/kb/sparql"
#else
#  declare -r DB_URL="http://blazegraph:8080/bigdata/namespace/kb/sparql"
#fi

# empty the Blazegraph repository; this is optional unless you are re-uploading the same circuit
curl -D- -X POST $DB_URL --data-urlencode "update=drop all"

# upload the CDPSM combined file to Blazegraph
curl -D- -H "Content-Type: application/xml" --upload-file ../blazegraph/test/IEEE13.xml -X POST $DB_URL

# list feeders now in the Blazegraph repository; will need the feeder mRIDs from this output
java -cp "../blazegraph/target/libs/*:../blazegraph/target/cim2glm-0.0.1-SNAPSHOT.jar" \
  gov.pnnl.goss.cim2glm.CIMImporter -u=$DB_URL -o=idx test

python3 InsertMeasurements.py ieee13nodeckt_special.txt  ieee13nodeckt_measid.json
python3 InsertMeasurements.py ieee13nodeckt_lines_pq.txt ieee13nodeckt_measid.json
python3 InsertMeasurements.py ieee13nodeckt_loads.txt    ieee13nodeckt_measid.json
python3 InsertMeasurements.py ieee13nodeckt_node_v.txt   ieee13nodeckt_measid.json
python3 InsertMeasurements.py ieee13nodeckt_switch_i.txt ieee13nodeckt_measid.json
python3 InsertMeasurements.py ieee13nodeckt_xfmr_pq.txt  ieee13nodeckt_measid.json


