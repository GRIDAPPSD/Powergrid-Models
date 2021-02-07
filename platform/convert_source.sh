source envars.sh

sed 's!_SRCPATH_!/home/tom/src!g' ConvertCIM100.template > ConvertCIM100.dss
opendsscmd ConvertCIM100.dss
./ConvertTransactive.sh

#opendsscmd ConvertR2.dss

