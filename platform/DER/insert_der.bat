call envars.bat
copy /q ..\cimhubjar.json ..\cimhubconfig.json

rem the base feeder models for acep_psil and test9500new already include DER
rem python -m cimhub.InsertDER ../cimhubconfig.json acep_psil_der.txt
rem python -m cimhub.InsertDER ../cimhubconfig.json test9500new_der.txt

rem add a sampling of DER to the transactive base feeder model
python -m cimhub.InsertDER ../cimhubconfig.json transactive123_der.txt
