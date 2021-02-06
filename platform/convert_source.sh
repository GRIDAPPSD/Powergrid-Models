declare -r DB_URL="http://localhost:8889/bigdata/namespace/kb/sparql"
#declare -r DB_URL="http://blazegraph:8080/bigdata/namespace/kb/sparql"
declare -r CIMHUB_PATH="../../CIMHub/target/libs/*:../../CIMHub/target/cimhub-0.0.1-SNAPSHOT.jar"
declare -r CIMHUB_PROG="gov.pnnl.gridappsd.cimhub.CIMImporter"

sed 's!_SRCPATH_!/home/tom/src!g' ConvertCIM100.template > ConvertCIM100.dss
opendsscmd ConvertCIM100.dss
./ConvertTransactive.sh

#opendsscmd ConvertR2.dss

