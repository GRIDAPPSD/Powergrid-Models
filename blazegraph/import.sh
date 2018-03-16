# updated process: build a jar file using Apache Maven with pom.xml
mvn clean install

#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 -o=dss test13
java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 -o=dss test8500

java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62 -o=glm test13
java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_C1C3E687-6FFD-C753-582B-632A27E28507 -o=glm test123
java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_5B816B93-7A5F-B64C-8460-47C17D6E4B0F -o=glm test13assets
java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 -o=glm -i=1 test8500
java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_67AB291F-DCCD-31B7-B499-338206B9828F -o=glm -i=1 testJ1
java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_9CE150A8-8CC5-A0F9-B67E-BBD8C79D3095 -o=glm -i=1 testR2

java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_49003F52-A359-C2EA-10C4-F4ED3FD368CC -o=glm test37
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_49003F52-A359-C2EA-10C4-F4ED3FD368CC -o=dss test37
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=idx test

