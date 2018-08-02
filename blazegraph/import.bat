
mvn clean install

rem java gov.pnnl.goss.cim2glm.CIMImporter -o=idx test
java -jar -jar  target\cim2glm-0.0.1-SNAPSHOT.jar -u=http://localhost:9999/blazegraph/namespace/kb/sparql -s=_4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 -o=glm -l=1.0 -i=1 test


