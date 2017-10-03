set JENA_HOME=c:\apache-jena-3.1.0
set CLASSPATH=target/*;c:/apache-jena-3.1.0/lib/*;c:/commons-math3-3.6.1/*

rem mvn clean install

rem java gov.pnnl.goss.cim2glm.CIMImporter ieee13
rem java gov.pnnl.goss.cim2glm.CIMImporter ieee13_assets
java gov.pnnl.goss.cim2glm.CIMImporter -o=glm -l=1.0 -i=1 ieee8500
java gov.pnnl.goss.cim2glm.CIMImporter -o=dss -l=1.0 -i=1 ieee8500


