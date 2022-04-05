#!/bin/bash
#declare -r DB_URL="http://blazegraph:8080/bigdata/namespace/kb/sparql"
declare -r DB_URL="http://localhost:8889/bigdata/namespace/kb/sparql"
declare -r SRC_PATH="/home/tom/src/Powergrid-Models/platform/"
declare -r CIMHUB_PATH="../../CIMHub/cimhub/target/libs/*:../../CIMHub/cimhub/target/cimhub-1.0.1-SNAPSHOT.jar"
declare -r CIMHUB_PROG="gov.pnnl.gridappsd.cimhub.CIMImporter"

