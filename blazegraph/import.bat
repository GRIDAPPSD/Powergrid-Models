set CLASSPATH=target/*;c:/apache-jena-3.6.0/lib/*;c:/commons-math3-3.6.1/*
java gov.pnnl.goss.cim2glm.CIMImporter -o=idx directory
java gov.pnnl.goss.cim2glm.CIMImporter ^
 -u=http://localhost:9999/blazegraph/namespace/kb/sparql ^
 -o=glm -l=1.0 CandleSt
rem java gov.pnnl.goss.cim2glm.CIMImporter ^
rem  -u=http://localhost:9999/blazegraph/namespace/kb/sparql ^
rem  -s=_E407CBB6-8C8D-9BC9-589C-AB83FBF0826D -o=glm -l=1.0 -i=1 ieee123pv
rem java gov.pnnl.goss.cim2glm.CIMImporter ^
rem  -u=http://localhost:9999/blazegraph/namespace/kb/sparql ^
rem  -s=_E407CBB6-8C8D-9BC9-589C-AB83FBF0826D -o=dss -l=1.0 ieee123pv
rem java gov.pnnl.goss.cim2glm.CIMImporter ^
rem  -u=http://localhost:9999/blazegraph/namespace/kb/sparql ^
rem  -s=_77966920-E1EC-EE8A-23EE-4EFD23B205BD -o=dss -l=1.0 ieee3sub
rem java gov.pnnl.goss.cim2glm.CIMImporter ^
rem  -u=http://localhost:9999/blazegraph/namespace/kb/sparql ^
rem  -s=_77966920-E1EC-EE8A-23EE-4EFD23B205BD -o=glm -l=1.0 -i=1 ieee3sub

