set JENA_HOME=c:\apache-jena-3.6.0
set CLASSPATH=target/*;c:/apache-jena-3.6.0/lib/*;c:/commons-math3-3.6.1/*

rem mvn clean install

java gov.pnnl.goss.cim2glm.CIMImporter -o=dss -l=1.0 -i=1 test


