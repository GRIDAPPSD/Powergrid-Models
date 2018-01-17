# updated process: build a jar file using Apache Maven with pom.xml
mvn clean install

java -classpath "target/*:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=dss ieee8500
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=dss ieee13assets
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=dss ieee13
#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=glm ieee13

#java -classpath "target/*:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -l=1 -i=1 -n=zipload_schedule ieee8500
# java -classpath "target/*:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=dss ieee8500
#java -classpath ".:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CIMImporter ieee8500
#java -classpath ".:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CIMImporter ieee13
#java -classpath ".:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CIMImporter ieee13_assets

