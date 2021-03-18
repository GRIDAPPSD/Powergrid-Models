mkdir /Users/mcde601/src/GRIDAPPSD/Powergrid-Models/blazegraph/dss/
mkdir /Users/mcde601/src/GRIDAPPSD/Powergrid-Models/blazegraph/glm/
rm /Users/mcde601/src/GRIDAPPSD/Powergrid-Models/blazegraph/dss/*.*
rm /Users/mcde601/src/GRIDAPPSD/Powergrid-Models/blazegraph/glm/*.*
./drop_all.sh
curl -D- -H "Content-Type: application/xml" --upload-file /Users/mcde601/src/GRIDAPPSD/Powergrid-Models/blazegraph/test/IEEE8500_3subs.xml -X POST "http://localhost:8889/bigdata/sparql"
java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=dss -l=1.0 -i=1 /Users/mcde601/src/GRIDAPPSD/Powergrid-Models/blazegraph/dss/IEEE8500_3subs
java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=glm -l=1.0 -i=1 /Users/mcde601/src/GRIDAPPSD/Powergrid-Models/blazegraph/glm/IEEE8500_3subs

