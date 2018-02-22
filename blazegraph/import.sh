# updated process: build a jar file using Apache Maven with pom.xml
mvn clean install

java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_C1C3E687-6FFD-C753-582B-632A27E28507 -o=dss test

# java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=idx test

