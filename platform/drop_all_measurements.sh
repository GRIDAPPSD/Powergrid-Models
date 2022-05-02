#!/bin/bash
source envars.sh

python3 -m cimhub.DropMeasurements cimhubconfig.json _77966920-E1EC-EE8A-23EE-4EFD23B205BD # ACEP PSIL
python3 -m cimhub.DropMeasurements cimhubconfig.json _67AB291F-DCCD-31B7-B499-338206B9828F # J1
python3 -m cimhub.DropMeasurements cimhubconfig.json _5B816B93-7A5F-B64C-8460-47C17D6E4B0F # IEEE 13 Assets
python3 -m cimhub.DropMeasurements cimhubconfig.json _13AD8E07-3BF9-A4E2-CB8F-C3722F837B62 # IEEE 13 Ochre
python3 -m cimhub.DropMeasurements cimhubconfig.json _49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 # IEEE 13
python3 -m cimhub.DropMeasurements cimhubconfig.json _49003F52-A359-C2EA-10C4-F4ED3FD368CC # IEEE 37
python3 -m cimhub.DropMeasurements cimhubconfig.json _C1C3E687-6FFD-C753-582B-632A27E28507 # IEEE 123
python3 -m cimhub.DropMeasurements cimhubconfig.json _E407CBB6-8C8D-9BC9-589C-AB83FBF0826D # IEEE 123 PV
python3 -m cimhub.DropMeasurements cimhubconfig.json _4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 # IEEE 8500
#python3 -m cimhub.DropMeasurements cimhubconfig.json _AAE94E4A-2465-6F5E-37B1-3E72183A4E44 # WSU 9500-node
python3 -m cimhub.DropMeasurements cimhubconfig.json _9CE150A8-8CC5-A0F9-B67E-BBD8C79D3095 # Taxonomy
python3 -m cimhub.DropMeasurements cimhubconfig.json _503D6E20-F499-4CC7-8051-971E23D0BF79 # Transactive
python3 -m cimhub.DropMeasurements cimhubconfig.json _EE71F6C9-56F0-4167-A14E-7F4C71F10EAA # IEEE 9500

