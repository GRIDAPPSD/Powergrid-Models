#!/bin/bash
source envars.sh

./convert_source.sh
python3 -m cimhub.MakeLoopScript -b $SRC_PATH
./convert_xml.sh
python3 -m cimhub.MakeLoopScript -d $SRC_PATH
opendsscmd check.dss
python3 -m cimhub.MakeGlmTestScript $SRC_PATH
./check_glm.sh
python3 -m cimhub.Compare_Cases

