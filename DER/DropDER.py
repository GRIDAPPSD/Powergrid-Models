from SPARQLWrapper import SPARQLWrapper2
import sys
import re
import uuid
import os.path

prefix_template = """PREFIX r: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX c: <{cimURL}>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
"""

drop_loc_template = """DELETE {{
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:Location.CoordinateSystem ?crs.
}} WHERE {{
 VALUES ?uuid {{\"{res}\"}}
 VALUES ?class {{c:Location}}
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:Location.CoordinateSystem ?crs.
}}
"""

drop_trm_template = """DELETE {{
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:Terminal.ConductingEquipment ?eq.
 ?m c:ACDCTerminal.sequenceNumber ?seq.
 ?m c:Terminal.ConnectivityNode ?cn.
}} WHERE {{
 VALUES ?uuid {{\"{res}\"}}
 VALUES ?class {{c:Terminal}}
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:Terminal.ConductingEquipment ?eq.
 ?m c:ACDCTerminal.sequenceNumber ?seq.
 ?m c:Terminal.ConnectivityNode ?cn.
}}
"""

drop_pec_template = """DELETE {{
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:Equipment.EquipmentContainer ?fdr.
 ?m c:PowerElectronicsConnection.PowerElectronicsUnit ?unit.
 ?m c:PowerSystemResource.Location ?loc.
 ?m c:PowerElectronicsConnection.maxIFault ?flt.
 ?m c:PowerElectronicsConnection.p ?p.
 ?m c:PowerElectronicsConnection.q ?q.
 ?m c:PowerElectronicsConnection.ratedS ?S.
 ?m c:PowerElectronicsConnection.ratedU ?U.
}} WHERE {{
 VALUES ?uuid {{\"{res}\"}}
 VALUES ?class {{c:PowerElectronicsConnection}}
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:Equipment.EquipmentContainer ?fdr.
 ?m c:PowerElectronicsConnection.PowerElectronicsUnit ?unit.
 ?m c:PowerSystemResource.Location ?loc.
 ?m c:PowerElectronicsConnection.maxIFault ?flt.
 ?m c:PowerElectronicsConnection.p ?p.
 ?m c:PowerElectronicsConnection.q ?q.
 ?m c:PowerElectronicsConnection.ratedS ?S.
 ?m c:PowerElectronicsConnection.ratedU ?U.
}}
"""

drop_pep_template = """DELETE {{
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:PowerElectronicsConnectionPhase.phase ?phs.
 ?m c:PowerElectronicsConnectionPhase.PowerElectronicsConnection ?pec.
 ?m c:PowerElectronicsConnectionPhase.p ?p.
 ?m c:PowerElectronicsConnectionPhase.q ?q.
 ?m c:PowerSystemResource.Location ?loc.
}} WHERE {{
 VALUES ?uuid {{\"{res}\"}}
 VALUES ?class {{c:PowerElectronicsConnectionPhase}}
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:PowerElectronicsConnectionPhase.phase ?phs.
 ?m c:PowerElectronicsConnectionPhase.PowerElectronicsConnection ?pec.
 ?m c:PowerElectronicsConnectionPhase.p ?p.
 ?m c:PowerElectronicsConnectionPhase.q ?q.
 ?m c:PowerSystemResource.Location ?loc.
}}
"""

drop_pv_template = """DELETE {{
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:PowerSystemResource.Location ?loc.
}} WHERE {{
 VALUES ?uuid {{\"{res}\"}}
 VALUES ?class {{c:PhotovoltaicUnit}}
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:PowerSystemResource.Location ?loc.
}}
"""

drop_bat_template = """DELETE {{
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:BatteryUnit.ratedE ?rated.
 ?m c:BatteryUnit.storedE ?stored.
 ?m c:BatteryUnit.batteryState ?state.
 ?m c:PowerSystemResource.Location ?loc.
}} WHERE {{
 VALUES ?uuid {{\"{res}\"}}
 VALUES ?class {{c:BatteryUnit}}
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:BatteryUnit.ratedE ?rated.
 ?m c:BatteryUnit.storedE ?stored.
 ?m c:BatteryUnit.batteryState ?state.
 ?m c:PowerSystemResource.Location ?loc.
}}
"""

if len(sys.argv) < 3:
  print ('usage: python3 DropDER.py config uuidfname')
  print (' Blazegraph server must already be started')
  exit()

cim_ns = ''
blz_url = ''
sparql = None

fp = open (sys.argv[1], 'r')
for ln in fp.readlines():
  toks = re.split('[,\s]+', ln)
  if toks[0] == 'blazegraph_url':
    blz_url = toks[1]
    sparql = SPARQLWrapper2 (blz_url)
    sparql.method = 'POST'
  elif toks[0] == 'cim_namespace':
    cim_ns = toks[1]
    prefix = prefix_template.format(cimURL=cim_ns)
fp.close()

fp = open (sys.argv[2], 'r')
for ln in fp.readlines():
  toks = re.split('[,\s]+', ln)
  if len(toks) > 2 and not toks[0].startswith('//'):
    cls = toks[0]
    nm = toks[1]
    mRID = toks[2]
  qstr = None
  if cls == 'PowerElectronicsConnection':
    qstr = prefix + drop_pec_template.format(res=mRID)
  elif cls == 'PowerElectronicsConnectionPhase':
    qstr = prefix + drop_pep_template.format(res=mRID)
  elif cls == 'Terminal':
    qstr = prefix + drop_trm_template.format(res=mRID)
  elif cls == 'Location':
    qstr = prefix + drop_loc_template.format(res=mRID)
  elif cls == 'PhotovoltaicUnit':
    qstr = prefix + drop_pv_template.format(res=mRID)
  elif cls == 'BatteryUnit':
    qstr = prefix + drop_bat_template.format(res=mRID)

  if qstr is not None:
#    print (qstr)
    sparql.setQuery(qstr)
    ret = sparql.query()
    print('deleting', cls, nm, ret.response.msg)
fp.close()

