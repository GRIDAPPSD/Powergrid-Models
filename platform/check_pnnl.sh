#!/bin/bash
cd ./both/
gridlabd -D WANT_VI_DUMP=1 PNNL_run.glm >PNNL.log
mv P*.csv ../test/glm
