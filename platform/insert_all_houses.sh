declare -r DB_URL="http://localhost:8889/bigdata/namespace/kb/sparql"
declare -r CIMHUB_PATH="../../CIMHub/target/libs/*:../../CIMHub/target/cimhub-0.0.1-SNAPSHOT.jar"
declare -r CIMHUB_PROG="gov.pnnl.gridappsd.cimhub.CIMImporter"
declare -r CIMHUB_UTILS="../../CIMHub/utils"

# 8500, 9500, R2, 123pv and Transactive feeders

#python3 $CIMHUB_UTILS/InsertHouses.py _4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 3
#python3 $CIMHUB_UTILS/InsertHouses.py _AAE94E4A-2465-6F5E-37B1-3E72183A4E44 3
python3 $CIMHUB_UTILS/InsertHouses.py _9CE150A8-8CC5-A0F9-B67E-BBD8C79D3095 2
#python3 $CIMHUB_UTILS/InsertHouses.py _E407CBB6-8C8D-9BC9-589C-AB83FBF0826D 5
#python3 $CIMHUB_UTILS/InsertHouses.py _503D6E20-F499-4CC7-8051-971E23D0BF79 3
