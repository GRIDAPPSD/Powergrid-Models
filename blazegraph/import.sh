# updated process: build a jar file using Apache Maven with pom.xml
mvn clean install

java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=idx test

# CIM100 testing with 9500-node feeder; OpenDSS, GridLAB-D and GridLAB-D with houses
java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=both -l=1.0 -i=1 -x=0 eval

# CIM100 testing with Transactive feeder; OpenDSS, GridLAB-D and GridLAB-D with houses
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_503D6E20-F499-4CC7-8051-971E23D0BF79 -o=dss -l=1.0 -i=1 transactive
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_503D6E20-F499-4CC7-8051-971E23D0BF79 -o=glm -l=1.0 -i=1 -x=0 transactive
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_503D6E20-F499-4CC7-8051-971E23D0BF79 -o=glm -l=1.0 -i=1 -h=1 transactiveHouse

# CIM100 testing with 9500-node feeder; OpenDSS, GridLAB-D and GridLAB-D with houses
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=dss -l=1.0 -i=1 eval
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=glm -l=1.0 -i=1 -x=0 eval
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=glm -l=1.0 -i=1 -h=1 evalHouse

#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#  -s=_E407CBB6-8C8D-9BC9-589C-AB83FBF0826D -o=glm -l=1.0 -i=1 ieee123pv
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#  -s=_E407CBB6-8C8D-9BC9-589C-AB83FBF0826D -o=dss -l=1.0 -i=1 ieee123pv

# CIM100 testing with single-phase generator
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=dss -l=1.0 -i=1 ieee8500_3subs
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_AAE94E4A-2465-6F5E-37B1-3E72183A4E44 -o=glm -l=1.0 -i=1 ieee8500_3subs

# CIM100 testing
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#   -s=_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 -o=glm -l=1.0 -i=1 ieee13
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#   -s=_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 -o=dss ieee13
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#   -s=_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 -o=dss -n=ieeezipload ieee13play
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#   -s=_5B816B93-7A5F-B64C-8460-47C17D6E4B0F -o=glm -l=1.0 -i=1 ieee13assets
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#   -s=_5B816B93-7A5F-B64C-8460-47C17D6E4B0F -o=dss ieee13assets

# CIM100 testing with houses
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_9CE150A8-8CC5-A0F9-B67E-BBD8C79D3095 -o=glm -l=1.0 -i=1 -h=1 taxR2_2house
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter \
#     -s=_4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 -o=glm -l=1.0 -i=1 -h=1 ieee8500house

# circuits used in GridAPPS-D

#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_77966920-E1EC-EE8A-23EE-4EFD23B205BD -o=glm -l=1.0 -i=1 acep_psil
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_77966920-E1EC-EE8A-23EE-4EFD23B205BD -o=dss acep_psil
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_E407CBB6-8C8D-9BC9-589C-AB83FBF0826D -o=glm -l=1.0 -i=1 ieee123pv
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_9CE150A8-8CC5-A0F9-B67E-BBD8C79D3095 -o=glm -l=1.0 -i=1 taxR2_2
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_9CE150A8-8CC5-A0F9-B67E-BBD8C79D3095 -o=glm -l=1.0 -i=1 -h=1 taxR2_2house
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_C1C3E687-6FFD-C753-582B-632A27E28507 -o=glm -l=1.0 -i=1 ieee123
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 -o=glm -l=1.0 -i=1 ieee13
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_5B816B93-7A5F-B64C-8460-47C17D6E4B0F -o=glm -l=1.0 -i=1 ieee13assets
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 -o=glm -l=1.0 -i=1 ieee8500
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 -o=glm -l=1.0 -i=1 -h=1 ieee8500house
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_67AB291F-DCCD-31B7-B499-338206B9828F -o=glm -l=1.0 -i=1 epriJ1

# other test cases not used in GridAPPS-D

#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_49003F52-A359-C2EA-10C4-F4ED3FD368CC -o=glm -l=1.0 -i=1 ieee37

