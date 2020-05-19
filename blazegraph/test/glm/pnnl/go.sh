python3 ../../../../taxonomy/converter_gld_dss.py transactive123.glm "4.16,0.48,0.208" ieee123transactive
python3 patch_xy.py
cp Buscoords.new Buscoords.csv
opendsscmd ConvertTransactive.ds
cp Transactive.xml ../..
cp transactive_*.csv ../..

