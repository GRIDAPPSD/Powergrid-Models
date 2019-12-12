python ..\..\..\..\taxonomy\converter_gld_dss.py transactive123.glm "4.16,0.48,0.208" ieee123transactive
python patch_xy.py
copy BusCoords.new BusCoords.csv
opendsscmd ConvertTransactive.ds
copy Transactive.xml ..\..
copy transactive_*.csv ..\..

