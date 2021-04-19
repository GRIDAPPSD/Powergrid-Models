source envars.sh

./convert_source.sh
python3 MakeLoopScript.py -b $SRC_PATH $DB_URL
./convert_xml.sh
python3 MakeLoopScript.py -d $SRC_PATH
opendsscmd check.dss
python3 MakeGlmTestScript.py $SRC_PATH
./check_glm.sh
python3 Compare_Cases.py

