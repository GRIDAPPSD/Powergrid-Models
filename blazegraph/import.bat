set CLASSPATH=target/*;c:/apache-jena-3.6.0/lib/*;c:/commons-math3-3.6.1/*
java gov.pnnl.goss.cim2glm.CIMImporter -o=idx directory
java gov.pnnl.goss.cim2glm.CIMImporter ^
 -u=http://localhost:9999/blazegraph/namespace/kb/sparql ^
-s=_E407CBB6-8C8D-9BC9-589C-AB83FBF0826D -o=glm -l=1.0 -i=1 ieee123pv
java gov.pnnl.goss.cim2glm.CIMImporter ^
 -u=http://localhost:9999/blazegraph/namespace/kb/sparql ^
 -s=_E407CBB6-8C8D-9BC9-589C-AB83FBF0826D -o=dss -l=1.0 ieee123pv
java gov.pnnl.goss.cim2glm.CIMImporter ^
 -u=http://localhost:9999/blazegraph/namespace/kb/sparql ^
 -s=_58DB8DEC-E10E-484E-A2CC-CD87CE670DF2 -o=dss -l=1.0 ieee3sub
java gov.pnnl.goss.cim2glm.CIMImporter ^
 -u=http://localhost:9999/blazegraph/namespace/kb/sparql ^
 -s=_58DB8DEC-E10E-484E-A2CC-CD87CE670DF2 -o=glm -l=1.0 -i=1 ieee3sub

