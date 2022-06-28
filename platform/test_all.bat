call envars.bat
copy cimhubjar.json cimhubconfig.json

call convert_source.bat
rem exit /b

cd /d "%~dp0"
python -m cimhub.MakeLoopScript -b %SRC_PATH%
call convert_xml.bat

cd /d "%~dp0"
python -m cimhub.MakeLoopScript -d %SRC_PATH%
opendsscmd check.dss

cd /d "%~dp0"
python -m cimhub.MakeGlmTestScript %SRC_PATH%
call check_glm.bat

cd /d "%~dp0"
python -m cimhub.Compare_Cases

