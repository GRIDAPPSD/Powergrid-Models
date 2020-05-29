from SPARQLWrapper import SPARQLWrapper2
import sys
import re
import uuid

prefix_template = """PREFIX r: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX c: {cimURL}
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
"""

qbus_template = """# list the bus name, cn id, terminal id, sequence number, eq id and loc id
SELECT ?bus ?cnid ?tid ?seq ?eqid ?locid WHERE {{
VALUES ?fdrid {{"{:s}"}}
 ?fdr c:IdentifiedObject.mRID ?fdrid.
 ?cn c:ConnectivityNode.ConnectivityNodeContainer ?fdr.
 ?trm c:Terminal.ConnectivityNode ?cn.
 ?trm c:ACDCTerminal.sequenceNumber ?seq.
 ?trm c:Terminal.ConductingEquipment ?eq.
 ?eq c:PowerSystemResource.Location ?loc.
 ?trm c:IdentifiedObject.mRID ?tid.
 ?cn c:IdentifiedObject.mRID ?cnid.
 ?cn c:IdentifiedObject.name ?bus.
 ?eq c:IdentifiedObject.mRID ?eqid.
 ?loc c:IdentifiedObject.mRID ?locid.
}}
ORDER BY ?bus ?tid
"""

qloc_template = """# list the location id, with xy coordinates of each sequence number
SELECT DISTINCT ?locid ?seq ?x ?y WHERE {{
VALUES ?fdrid {{"{:s}"}}
 ?fdr c:IdentifiedObject.mRID ?fdrid.
 ?eq c:PowerSystemResource.Location ?loc.
 ?pt c:PositionPoint.Location ?loc.
 ?pt c:PositionPoint.xPosition ?x.
 ?pt c:PositionPoint.yPosition ?y.
 ?pt c:PositionPoint.sequenceNumber ?seq.
 ?loc c:IdentifiedObject.mRID ?locid.
}}
ORDER BY ?locid ?seq
"""

if len(sys.argv) < 2:
  print ('usage: python3 InsertDER.py fname')
  print (' Blazegraph server must already be started')
  print (' fname must define blazegraph_url, cim_namespace and feederID')
  print ('   before creating any DG')
  exit()

cim_ns = ''
blz_url = ''
fdr_id = ''
prefix = None
qbus = None
qloc = None
sparql = None
buses = {}
locs = {}

fp = open (sys.argv[1], 'r')
lines = fp.readlines()
for ln in lines:
  toks = re.split('[,\s]+', ln)
  if toks[0] == 'blazegraph_url':
    blz_url = toks[1]
  elif toks[0] == 'cim_namespace':
    cim_ns = toks[1]
  elif toks[0] == 'feederID':
    fdr_id = toks[1]
  if sparql is not None:
    if not toks[0].startswith('//') and len(toks[0]) > 0:
      print ('create DG for', toks)
  else:
    if len(blz_url) > 0 and len(cim_ns) > 0 and len(fdr_id) > 0:
      prefix = prefix_template.format(cimURL=cim_ns)
      qbus = prefix + qbus_template.format(fdr_id)
      qloc = prefix + qloc_template.format(fdr_id)
      sparql = SPARQLWrapper2 (blz_url)
      sparql.method = 'POST'

      sparql.setQuery(qbus)
      ret = sparql.query()
      for b in ret.bindings:
        key = b['bus'].value
        cnid = b['cnid'].value
        tid = b['tid'].value
        eqid = b['eqid'].value
        locid = b['locid'].value
        seq = b['seq'].value
        buses[key] = {'cn':cnid, 'trm':tid, 'eq':eqid, 'seq': seq, 'loc': locid}
      print (len(buses))

      sparql.setQuery(qloc)
      ret = sparql.query()
      for b in ret.bindings:
        key = b['locid'].value + ':' + b['seq'].value
        x = b['x'].value
        y = b['y'].value
        locs[key] = {'x': x, 'y': y}
      print (len(locs))

fp.close()

#def InsertMeasurement (meascls, measid, eqname, eqid, trmid, meastype, phases):
#  #if not measid starts with _ then prepend it, this is here for consistency. otherwise the mrids are uploaded without the initial _
#  if (not str(measid).startswith("_")):
#    measid = "_"+str(measid)
#
#  resource = '<' + constants.blazegraph_url + '#' + str(measid) + '>'
#  equipment = '<' + constants.blazegraph_url + '#' + str(eqid) + '>'
#  terminal = '<' + constants.blazegraph_url + '#' + str(trmid) + '>'
#  ln1 = resource + ' a c:' + meascls + '. '
#  ln2 = resource + ' c:IdentifiedObject.mRID \"' + str(measid) + '\". '
#  ln3 = resource + ' c:IdentifiedObject.name \"' + str(eqname) + '\". '
#  ln4 = resource + ' c:Measurement.PowerSystemResource ' + equipment + '. '
#  ln5 = resource + ' c:Measurement.Terminal ' + terminal + '. '
#  ln6 = (resource + ' c:Measurement.phases ' + constants.cim100
#    + 'PhaseCode.' + phases + '>. ')
#  ln7 = resource + ' c:Measurement.measurementType \"' + meastype + '\"'
#  qstr = (constants.prefix + 'INSERT DATA { ' + ln1 + ln2 + ln3 + ln4 +
#    ln5 + ln6 + ln7 + '}')
#
##  print (qstr)
#  sparql.setQuery(qstr)
#  ret = sparql.query()
##  print (ret)
#  return
#
#lines = fp.readlines()
#for ln in lines:
#  toks = re.split('[,\s]+', ln)
#  if toks[0] == 'LinearShuntCompensator':
#    phases = toks[3]
#    eqid = toks[4]
#    trmid = toks[5]
#    id1 = uuid.uuid4()
#    id2 = uuid.uuid4()
#    id3 = uuid.uuid4()
#    InsertMeasurement ('Analog', id1, 'LinearShuntCompensator_' + toks[1], eqid, trmid, 'PNV', phases)
#    InsertMeasurement ('Analog', id2, 'LinearShuntCompensator_' + toks[1], eqid, trmid, 'VA', phases)
#    InsertMeasurement ('Discrete', id3, 'LinearShuntCompensator_' + toks[1], eqid, trmid, 'Pos', phases)
#  if toks[0] == 'PowerTransformer' and toks[1] == 'RatioTapChanger':
#    phases = toks[5]
#    eqid = toks[6]
#    trmid = toks[7]
#    id1 = uuid.uuid4()
#    InsertMeasurement ('Discrete', id1, 'RatioTapChanger_' + toks[2], eqid, trmid, 'Pos', phases)
#  if toks[0] == 'EnergyConsumer':
#    phases = toks[3]
#    eqid = toks[4]
#    trmid = toks[5]
#    id1 = uuid.uuid4()
#    id2 = uuid.uuid4()
#    InsertMeasurement ('Analog', id1, 'EnergyConsumer_' + toks[1], eqid, trmid, 'PNV', phases)
#    InsertMeasurement ('Analog', id2, 'EnergyConsumer_' + toks[1], eqid, trmid, 'VA', phases)
#  if toks[0] == 'SynchronousMachine':
#    phases = toks[3]
#    eqid = toks[4]
#    trmid = toks[5]
#    id1 = uuid.uuid4()
#    id2 = uuid.uuid4()
#    InsertMeasurement ('Analog', id1, 'SynchronousMachine_' + toks[1], eqid, trmid, 'PNV', phases)
#    InsertMeasurement ('Analog', id2, 'SynchronousMachine_' + toks[1], eqid, trmid, 'VA', phases)
#  if toks[0] == 'PowerElectronicsConnection' and toks[1] == 'PhotovoltaicUnit':
#    phases = toks[5]
#    eqid = toks[6]
#    trmid = toks[7]
#    id1 = uuid.uuid4()
#    id2 = uuid.uuid4()
#    InsertMeasurement ('Analog', id1, 'PowerElectronicsConnection_PhotovoltaicUnit_' + toks[3], eqid, trmid, 'PNV', phases)
#    InsertMeasurement ('Analog', id2, 'PowerElectronicsConnection_PhotovoltaicUnit_' + toks[3], eqid, trmid, 'VA', phases)
#  if toks[0] == 'PowerElectronicsConnection' and toks[1] == 'BatteryUnit':
#    phases = toks[5]
#    eqid = toks[6]
#    trmid = toks[7]
#    id1 = uuid.uuid4()
#    id2 = uuid.uuid4()
#    InsertMeasurement ('Analog', id1, 'PowerElectronicsConnection_BatteryUnit_' + toks[3], eqid, trmid, 'PNV', phases)
#    InsertMeasurement ('Analog', id2, 'PowerElectronicsConnection_BatteryUnit_' + toks[3], eqid, trmid, 'VA', phases)
#  if toks[0] == 'PowerTransformer' and toks[1] == 'PowerTransformerEnd':
#    what = toks[2]
#    phases = toks[6]
#    eqid = toks[7]
#    trmid = toks[8]
#    id1 = uuid.uuid4()
#    if 'v' in what:
#      InsertMeasurement ('Analog', id1, 'PowerTransformer_' + toks[3] + '_Voltage', eqid, trmid, 'PNV', phases)
#    elif 's' in what:
#      InsertMeasurement ('Analog', id1, 'PowerTransformer_' + toks[3] + '_Power', eqid, trmid, 'VA', phases)
#    elif 'i' in what:
#      InsertMeasurement ('Analog', id1, 'PowerTransformer_' + toks[3] + '_Current', eqid, trmid, 'A', phases)
#  if toks[0] == 'ACLineSegment' or toks[0] == 'LoadBreakSwitch' or toks[0] == 'Breaker' or toks[0] == 'Recloser':
#    what = toks[1]
#    phases = toks[5]
#    eqid = toks[6]
#    if '1' in what:
#      trmid = toks[7]
#      if toks[0] != 'ACLineSegment' and what == 'i1':
#        id1 = uuid.uuid4()
#        InsertMeasurement ('Discrete', id1, toks[0] + '_' + toks[2] + '_State', eqid, trmid, 'Pos', phases)
#    else:
#      trmid = toks[8]
#    id1 = uuid.uuid4()
#    if 'v' in what:
#      InsertMeasurement ('Analog', id1, toks[0] + '_' + toks[2] + '_Voltage', eqid, trmid, 'PNV', phases)
#    elif 's' in what:
#      InsertMeasurement ('Analog', id1, toks[0] + '_' + toks[2] + '_Power', eqid, trmid, 'VA', phases)
#    elif 'i' in what:
#      InsertMeasurement ('Analog', id1, toks[0] + '_' + toks[2] + '_Current', eqid, trmid, 'A', phases)

