from SPARQLWrapper import SPARQLWrapper2, JSON
import sys

if len(sys.argv) < 2:
	print ('usage: python DropMeasurements.py feeder_id')
	print (' (Blazegraph server must already be started)')
	exit()

endpoint = "http://localhost:9999/blazegraph/namespace/kb/sparql"

prefix = """
PREFIX r: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX c: <http://iec.ch/TC57/2012/CIM-schema-cim17#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
"""

sparql = SPARQLWrapper2 (endpoint)
sparql.method = 'POST'

qstr = prefix + """
 DELETE {
  ?m a ?class.
  ?m c:IdentifiedObject.mRID ?uuid.
  ?m c:IdentifiedObject.name ?name.
  ?m c:Measurement.PowerSystemResource ?psr.
  ?m c:Measurement.Terminal ?trm.
  ?m c:Measurement.phases ?phases.
  ?m c:Measurement.measurementType ?type.
 } WHERE {
  VALUES ?fdrid {\"""" + sys.argv[1] + """\"}
  VALUES ?class {c:Analog c:Discrete}
  ?fdr c:IdentifiedObject.mRID ?fdrid. 
  ?eq c:Equipment.EquipmentContainer ?fdr.
  ?trm c:Terminal.ConductingEquipment ?eq.
  ?m a ?class.
  ?m c:IdentifiedObject.mRID ?uuid.
  ?m c:IdentifiedObject.name ?name.
  ?m c:Measurement.PowerSystemResource ?psr.
  ?m c:Measurement.Terminal ?trm.
  ?m c:Measurement.phases ?phases.
  ?m c:Measurement.measurementType ?type.
 }
"""

print (qstr)
sparql.setQuery(qstr)
ret = sparql.query()
print (ret.info)
   
