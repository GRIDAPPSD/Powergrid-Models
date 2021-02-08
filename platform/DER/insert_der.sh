source envars.sh

#the base feeder models for acep_psil and test9500new already include DER
#python3 $CIMHUB_UTILS/InsertDER.py acep_psil_der.txt
#python3 $CIMHUB_UTILS/InsertDER.py test9500new_der.txt

#add a sampling of DER to the transactive base feeder model
python3 $CIMHUB_UTILS/InsertDER.py transactive123_der.txt
