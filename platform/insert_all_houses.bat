call envars.bat

rem 8500, 9500, R2, 123pv and Transactive feeders

rem arguments are feeder mRID, region, seed value, file of persistent mRIDs, scaling factor on load before assigning houses

python -m cimhub.InsertHouses cimhubconfig.json 4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 3 0 ./houses/ieee8500_house_uuids.json    0.8
python -m cimhub.InsertHouses cimhubconfig.json EE71F6C9-56F0-4167-A14E-7F4C71F10EAA 3 0 ./houses/ieee9500bal_house_uuids.json 1.0
python -m cimhub.InsertHouses cimhubconfig.json 9CE150A8-8CC5-A0F9-B67E-BBD8C79D3095 2 0 ./houses/r2_12_47_2_house_uuids.json  1.0
python -m cimhub.InsertHouses cimhubconfig.json E407CBB6-8C8D-9BC9-589C-AB83FBF0826D 5 0 ./houses/ieee123pv_house_uuids.json   1.0
python -m cimhub.InsertHouses cimhubconfig.json 503D6E20-F499-4CC7-8051-971E23D0BF79 3 0 ./houses/transactive_house_uuids.json 1.0