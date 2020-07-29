# updated for Java 11; Apache Maven updates are specified in pom.xml; Blazegraph running in Docker
#mvn clean install

#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -u=http://localhost:8889/bigdata/namespace/kb/sparql \
#    -o=idx directory

java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
     -s=_503D6E20-F499-4CC7-8051-971E23D0BF79 -o=both -l=1.0 -i=1 -t=1 transactive2

#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#   -s=_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 -o=both -l=1.0 -i=1 ieee13

# CIM100 testing with 9500-node feeder; OpenDSS, GridLAB-D and GridLAB-D with houses
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=both -l=1.0 -i=1 -h=0 -x=0 -t=1 eval

# CIM100 testing with Transactive feeder; OpenDSS, GridLAB-D and GridLAB-D with houses
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_503D6E20-F499-4CC7-8051-971E23D0BF79 -o=dss -l=1.0 -i=1 transactive
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_503D6E20-F499-4CC7-8051-971E23D0BF79 -o=glm -l=1.0 -i=1 -x=0 transactive
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_503D6E20-F499-4CC7-8051-971E23D0BF79 -o=glm -l=1.0 -i=1 -h=1 transactiveHouse

# CIM100 testing with 9500-node feeder; OpenDSS, GridLAB-D and GridLAB-D with houses
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=dss -l=1.0 -i=1 eval
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=glm -l=1.0 -i=1 -x=0 eval
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=glm -l=1.0 -i=1 -h=1 evalHouse

#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#  -s=_E407CBB6-8C8D-9BC9-589C-AB83FBF0826D -o=glm -l=1.0 -i=1 ieee123pv
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#  -s=_E407CBB6-8C8D-9BC9-589C-AB83FBF0826D -o=dss -l=1.0 -i=1 ieee123pv

# CIM100 testing with single-phase generator
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=dss -l=1.0 -i=1 ieee8500_3subs
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=glm -l=1.0 -i=1 ieee8500_3subs

# CIM100 testing
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#   -s=_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 -o=glm -l=1.0 -i=1 ieee13
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#   -s=_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 -o=dss ieee13
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#   -s=_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 -o=dss -n=ieeezipload ieee13play
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#   -s=_5B816B93-7A5F-B64C-8460-47C17D6E4B0F -o=glm -l=1.0 -i=1 ieee13assets
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#   -s=_5B816B93-7A5F-B64C-8460-47C17D6E4B0F -o=dss ieee13assets

# CIM100 testing with houses
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_9CE150A8-8CC5-A0F9-B67E-BBD8C79D3095 -o=glm -l=1.0 -i=1 -h=1 taxR2_2house
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 -o=glm -l=1.0 -i=1 -h=1 ieee8500house

# circuits used in GridAPPS-D

#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -s=_77966920-E1EC-EE8A-23EE-4EFD23B205BD -o=glm -l=1.0 -i=1 acep_psil
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -s=_77966920-E1EC-EE8A-23EE-4EFD23B205BD -o=dss acep_psil
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -s=_E407CBB6-8C8D-9BC9-589C-AB83FBF0826D -o=glm -l=1.0 -i=1 ieee123pv
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -s=_9CE150A8-8CC5-A0F9-B67E-BBD8C79D3095 -o=glm -l=1.0 -i=1 taxR2_2
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -s=_9CE150A8-8CC5-A0F9-B67E-BBD8C79D3095 -o=glm -l=1.0 -i=1 -h=1 taxR2_2house
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -s=_C1C3E687-6FFD-C753-582B-632A27E28507 -o=glm -l=1.0 -i=1 ieee123
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -s=_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 -o=glm -l=1.0 -i=1 ieee13
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -s=_5B816B93-7A5F-B64C-8460-47C17D6E4B0F -o=glm -l=1.0 -i=1 ieee13assets
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -s=_4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 -o=glm -l=1.0 -i=1 ieee8500
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -s=_4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 -o=glm -l=1.0 -i=1 -h=1 ieee8500house
#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -s=_67AB291F-DCCD-31B7-B499-338206B9828F -o=glm -l=1.0 -i=1 epriJ1

# other test cases not used in GridAPPS-D

#java -cp "target/libs/*:target/cim2glm-0.0.1-SNAPSHOT.jar" gov.pnnl.goss.cim2glm.CIMImporter -s=_49003F52-A359-C2EA-10C4-F4ED3FD368CC -o=glm -l=1.0 -i=1 ieee37


