from SPARQLWrapper import SPARQLWrapper2#, JSON
# constants.py is used for configuring blazegraph.
import sys
sys.path.append("..")
from Meas import constants

if len(sys.argv) < 2:
	print ('usage: python DropHouses.py feeder_id')
	print (' (Blazegraph server must already be started)')
	exit()

sparql = SPARQLWrapper2(constants.blazegraph_url)
sparql.method = 'POST'

qstr = constants.prefix + """DELETE {
  ?h a ?class.
  ?h c:IdentifiedObject.mRID ?uuid.
  ?h c:IdentifiedObject.name ?name.
  ?h c:House.floorArea ?floorArea.
  ?h c:House.numberOfStories ?numberOfStories.
  ?h c:House.coolingSetpoint ?coolingSetpoint.
  ?h c:House.heatingSetpoint ?heatingSetpoint.
  ?h c:House.hvacPowerFactor ?hvacPowerFactor.
  ?h c:House.coolingSystem ?coolingSystemRaw.
  ?h c:House.heatingSystem ?heatingSystemRaw.
  ?h c:House.thermalIntegrity ?thermalIntegrityRaw.
  ?h c:House.EnergyConsumer ?econ.
 } WHERE {
  VALUES ?fdrid {\"""" + sys.argv[1] + """\"}
  VALUES ?class {c:House}
  ?fdr c:IdentifiedObject.mRID ?fdrid. 
  ?econ c:Equipment.EquipmentContainer ?fdr.
  ?h a ?class.
  ?h c:House.EnergyConsumer ?econ.
 }
"""

#print (qstr)
sparql.setQuery(qstr)
ret = sparql.query()
#print (ret.info)
print(ret.response.msg)