set JENA_HOME=c:\apache-jena-3.6.0
set CLASSPATH=target/*;c:/apache-jena-3.6.0/lib/*;c:/commons-math3-3.6.1/*

rem mvn clean install

rem java gov.pnnl.goss.cim2glm.CIMImporter -o=idx test
java gov.pnnl.goss.cim2glm.CIMImporter -s=_C1C3E687-6FFD-C753-582B-632A27E28507 -o=dss -l=1.0 -i=1 test


