set JENA_HOME=c:\apache-jena-3.1.0
set CLASSPATH=target/*;c:/apache-jena-3.1.0/lib/*;c:/commons-math3-3.6.1/*

mvn clean install

rem java gov.pnnl.goss.cim2glm.CIMImporter -o=dss ieee13
rem java gov.pnnl.goss.cim2glm.CIMImporter -o=dss ieee13_assets
rem java gov.pnnl.goss.cim2glm.CIMImporter -o=glm -l=1.0 -i=1 ieee8500
rem java gov.pnnl.goss.cim2glm.CIMImporter -o=dss -l=1.0 -i=1 ieee8500


