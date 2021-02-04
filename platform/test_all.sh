declare -r DB_URL="http://localhost:8889/bigdata/namespace/kb/sparql"
declare -r SRC_PATH="/home/tom/src/Powergrid-Models/platform/"
#declare -r CIMHUB_PATH="../../CIMHub/target/libs/*:../../CIMHub/target/cimhub-0.0.1-SNAPSHOT.jar"
#declare -r CIMHUB_PROG="gov.pnnl.gridappsd.cimhub.CIMImporter"

./convert_source.sh
python3 MakeLoopScript.py -b $SRC_PATH $DB_URL
./convert_xml.sh
python3 MakeLoopScript.py -d $SRC_PATH
opendsscmd check.dss
python3 MakeGlmTestScript.py $SRC_PATH
./check_glm.sh
python3 Compare_Cases.py

