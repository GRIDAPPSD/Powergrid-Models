# updated process: build a jar file using Apache Maven with pom.xml
mvn clean install

# java -classpath "target/*:/home/mcde601/src/apache-jena-3.6.0/lib/*:/home/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=dss test

java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=idx test

