# updated process: build a jar file using Apache Maven with pom.xml
# mvn clean install

java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -s=_42A912B6-ED88-34B1-5BCB-FB3DE01AEA6B -l=0.84 -o=glm nantucket
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=idx nantucket

