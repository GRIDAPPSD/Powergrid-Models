# updated for Java 11; Apache Maven updates are specified in pom.xml; Blazegraph running in Docker
mvn clean install

java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -u=http://localhost:8889/bigdata/namespace/kb/sparql \
    -o=idx directory

# CIM100 testing with 9500-node feeder; OpenDSS, GridLAB-D and GridLAB-D without houses
# java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -u=http://localhost:8889/bigdata/namespace/kb/sparql \
#     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=both -l=1.0 -i=1 -h=0 -x=0 -t=1 eval

