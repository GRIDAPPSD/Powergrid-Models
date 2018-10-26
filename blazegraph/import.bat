rem java gov.pnnl.goss.cim2glm.CIMImporter -o=idx test

set CLASSPATH=target/*;c:/apache-jena-3.6.0/lib/*;c:/commons-math3-3.6.1/*
java gov.pnnl.goss.cim2glm.CIMImporter -u=http://localhost:9999/blazegraph/namespace/kb/sparql -s=_E407CBB6-8C8D-9BC9-589C-AB83FBF0826D -o=glm -l=1.0 -i=1 ieee123pv

rem java -jar -jar  target\cim2glm-0.0.1-SNAPSHOT.jar -u=http://localhost:9999/blazegraph/namespace/kb/sparql -s=_E407CBB6-8C8D-9BC9-589C-AB83FBF0826D -o=glm -l=1.0 -i=1 ieee123pv


