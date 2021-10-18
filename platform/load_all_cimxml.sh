#!/bin/bash
source envars.sh

curl -D- -X POST $DB_URL --data-urlencode "update=drop all"

curl -D- -H "Content-Type: application/xml" --upload-file ./cimxml/ACEP_PSIL.xml -X POST $DB_URL
curl -D- -H "Content-Type: application/xml" --upload-file ./cimxml/EPRI_DPV_J1.xml -X POST $DB_URL
curl -D- -H "Content-Type: application/xml" --upload-file ./cimxml/IEEE123.xml -X POST $DB_URL
curl -D- -H "Content-Type: application/xml" --upload-file ./cimxml/IEEE123_PV.xml -X POST $DB_URL
curl -D- -H "Content-Type: application/xml" --upload-file ./cimxml/IEEE13.xml -X POST $DB_URL
curl -D- -H "Content-Type: application/xml" --upload-file ./cimxml/IEEE13_Assets.xml -X POST $DB_URL
curl -D- -H "Content-Type: application/xml" --upload-file ./cimxml/IEEE37.xml -X POST $DB_URL
curl -D- -H "Content-Type: application/xml" --upload-file ./cimxml/IEEE8500.xml -X POST $DB_URL
curl -D- -H "Content-Type: application/xml" --upload-file ./cimxml/IEEE8500_3subs.xml -X POST $DB_URL
curl -D- -H "Content-Type: application/xml" --upload-file ./cimxml/R2_12_47_2.xml -X POST $DB_URL
curl -D- -H "Content-Type: application/xml" --upload-file ./cimxml/Transactive.xml -X POST $DB_URL
curl -D- -H "Content-Type: application/xml" --upload-file ./cimxml/final9500nodebalanced.xml -X POST $DB_URL

java -cp $CIMHUB_PATH $CIMHUB_PROG -u=$DB_URL -o=idx test
