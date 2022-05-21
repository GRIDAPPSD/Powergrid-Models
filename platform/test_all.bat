call envars.bat

rem call convert_source.bat
rem exit /b

python -m cimhub.MakeLoopScript -b %SRC_PATH%
rem call convert_xml.bat

python -m cimhub.MakeLoopScript -d %SRC_PATH%
rem opendsscmd check.dss

python -m cimhub.MakeGlmTestScript %SRC_PATH%
rem call check_glm.bat

python -m cimhub.Compare_Cases

