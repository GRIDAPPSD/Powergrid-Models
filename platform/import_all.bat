call envars.bat
copy cimhubjar.json cimhubconfig.json

call load_all_cimxml.bat
cd /d "%~dp0"
call list_all_measurements.bat
cd /d "%~dp0"
call insert_all_measurements.bat
cd /d "%~dp0"
call insert_all_houses.bat
cd /d "%~dp0"
call test_all_houses.bat

