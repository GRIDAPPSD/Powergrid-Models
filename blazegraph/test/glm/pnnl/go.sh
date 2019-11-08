python3 ../../../../taxonomy/converter_gld_dss.py transactive123.glm "4.16,0.48,0.208"
python3 patch_xy.py
cp BusCoords.new BusCoords.csv
opendsscmd ConvertTransactive.ds
# cp BusCoords.csv ~/Documents/PNNL/ZZZ
# cp *.dss ~/Documents/PNNL/ZZZ

