from SPARQLWrapper import SPARQLWrapper2
import sys
import re
import uuid
import os.path

prefix_template = """PREFIX r: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX c: <{cimURL}>
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

crs_query = """SELECT DISTINCT ?name ?id WHERE {{
 ?crs r:type c:CoordinateSystem.
 ?crs c:IdentifiedObject.mRID ?id.
 ?crs c:IdentifiedObject.name ?name.
}}
ORDER by ?name
"""

ins_pt_template = """INSERT DATA {{
 <{url}#{res}> a c:PositionPoint.
 <{url}#{res}> c:PositionPoint.Location <{url}#{resLoc}>.
 <{url}#{res}> c:PositionPoint.sequenceNumber \"{seq}\".
 <{url}#{res}> c:PositionPoint.xPosition \"{x}\".
 <{url}#{res}> c:PositionPoint.yPosition \"{y}\".
}}
"""

ins_loc_template = """INSERT DATA {{
 <{url}#{res}> a c:Location.
 <{url}#{res}> c:IdentifiedObject.mRID \"{res}\".
 <{url}#{res}> c:IdentifiedObject.name \"{nm}\".
 <{url}#{res}> c:Location.CoordinateSystem <{url}#{resCrs}>.
}}
"""

ins_trm_template = """INSERT DATA {{
 <{url}#{res}> a c:Terminal.
 <{url}#{res}> c:IdentifiedObject.mRID \"{res}\".
 <{url}#{res}> c:IdentifiedObject.name \"{nm}\".
 <{url}#{res}> c:Terminal.ConductingEquipment <{url}#{resEQ}>.
 <{url}#{res}> c:ACDCTerminal.sequenceNumber \"1\".
 <{url}#{res}> c:Terminal.ConnectivityNode <{url}#{resCN}>.
}}
"""

ins_pec_template = """INSERT DATA {{
 <{url}#{res}> a c:PowerElectronicsConnection.
 <{url}#{res}> c:IdentifiedObject.mRID \"{res}\".
 <{url}#{res}> c:IdentifiedObject.name \"{nm}\".
 <{url}#{res}> c:Equipment.EquipmentContainer  <{url}#{resFdr}>.
 <{url}#{res}> c:PowerElectronicsConnection.PowerElectronicsUnit <{url}#{resUnit}>.
 <{url}#{res}> c:PowerSystemResource.Location <{url}#{resLoc}>.
 <{url}#{res}> c:PowerElectronicsConnection.maxIFault \"1.111\".
 <{url}#{res}> c:PowerElectronicsConnection.p \"{p}\".
 <{url}#{res}> c:PowerElectronicsConnection.q \"{q}\".
 <{url}#{res}> c:PowerElectronicsConnection.ratedS \"{ratedS}\".
 <{url}#{res}> c:PowerElectronicsConnection.ratedU \"{ratedU}\".
}}
"""

ins_pep_template = """INSERT DATA {{
 <{url}#{res}> a c:PowerElectronicsConnectionPhase.
 <{url}#{res}> c:IdentifiedObject.mRID \"{res}\".
 <{url}#{res}> c:IdentifiedObject.name \"{nm}\".
 <{url}#{res}> c:PowerElectronicsConnectionPhase.phase <{ns}SinglePhaseKind.{phs}>.
 <{url}#{res}> c:PowerElectronicsConnectionPhase.PowerElectronicsConnection <{url}#{resPEC}>.
 <{url}#{res}> c:PowerElectronicsConnectionPhase.p \"{p}\".
 <{url}#{res}> c:PowerElectronicsConnectionPhase.q \"{q}\".
 <{url}#{res}> c:PowerSystemResource.Location <{url}#{resLoc}>.
}}
"""

ins_pv_template = """INSERT DATA {{
 <{url}#{res}> a c:PhotovoltaicUnit.
 <{url}#{res}> c:IdentifiedObject.mRID \"{res}\".
 <{url}#{res}> c:IdentifiedObject.name \"{nm}\".
 <{url}#{res}> c:PowerSystemResource.Location <{url}#{resLoc}>.
}}
"""

ins_bat_template = """INSERT DATA {{
 <{url}#{res}> a c:BatteryUnit.
 <{url}#{res}> c:IdentifiedObject.mRID \"{res}\".
 <{url}#{res}> c:IdentifiedObject.name \"{nm}\".
 <{url}#{res}> c:BatteryUnit.ratedE \"{ratedE}\".
 <{url}#{res}> c:BatteryUnit.storedE \"{storedE}\".
 <{url}#{res}> c:BatteryUnit.batteryState <{ns}BatteryState.{state}>.
 <{url}#{res}> c:PowerSystemResource.Location <{url}#{resLoc}>.
}}
"""

def GetCIMID (cls, nm, uuids):
  if nm is not None:
    key = cls + ':' + nm
    if key not in uuids:
      uuids[key] = '_' + str(uuid.uuid4()).upper()
    return uuids[key]
  return '_' + str(uuid.uuid4()).upper() # for unidentified CIM instances

def ParsePhases (sphs):
  lst = []
  for code in ['A', 'B', 'C', 's1', 's2']:
    if code in sphs:
      lst.append(code)
  return lst

if len(sys.argv) < 2:
  print ('usage: python3 InsertDER.py fname')
  print (' Blazegraph server must already be started')
  print (' fname must define blazegraph_url, cim_namespace and feederID')
  print ('   before creating any DG')
  exit()

cim_ns = ''
blz_url = ''
fdr_id = ''
crs_id = ''
prefix = None
qbus = None
qloc = None
sparql = None
buses = {}
locs = {}
fuidname = None
uuids = {}

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
  elif toks[0] == 'uuid_file':
    fuidname = toks[1]
    if os.path.exists(fuidname):
      print ('reading identifiable instance mRIDs from', fuidname)
      fuid = open (fuidname, 'r')
      for uuid_ln in fuid.readlines():
        uuid_toks = re.split('[,\s]+', uuid_ln)
        if len(uuid_toks) > 2 and not uuid_toks[0].startswith('//'):
          cls = uuid_toks[0]
          nm = uuid_toks[1]
          key = cls + ':' + nm
          val = uuid_toks[2]
          uuids[key] = val
      fuid.close()

  if sparql is not None:
    if not toks[0].startswith('//') and len(toks[0]) > 0:
      nmPEC = toks[0]
      nmCN = toks[1]
      phases = toks[2]
      unit = toks[3]
      kVA = float(toks[4])
      kV = float(toks[5])
      kW = float(toks[6])
      kVAR = float(toks[7])
      if unit == 'Battery':
        ratedkwh = float(toks[8])
        storedkwh = float(toks[9])
      else:
        ratedkwh = 0.0
        storedkwh = 0.0
      nmUnit = nmPEC + '_' + unit
      nmTrm = nmPEC + '_T1'
      nmLoc = nmPEC + '_Loc'
      idPEC = GetCIMID('PowerElectronicsConnection', nmPEC, uuids)
      idUnit = GetCIMID(unit + 'Unit', nmUnit, uuids)
      idLoc = GetCIMID('Location', nmLoc, uuids)
      idPt = GetCIMID('PositionPoint', None, uuids)
      idTrm = GetCIMID('Terminal', nmTrm, uuids)
      row = buses[nmCN]
      idCN = row['cn']
      keyXY = row['loc'] + ':' + str(row['seq'])
      row = locs[keyXY]
      x = float(row['x'])
      y = float(row['y'])
      print ('create {:s} at {:s} CN {:s} location {:.4f},{:.4f}'.format (nmPEC, nmCN, idCN, x, y))

      inspec = prefix + ins_pec_template.format(url=blz_url, res=idPEC, nm=nmPEC, resLoc=idLoc, resFdr=fdr_id, resUnit=idUnit,
                                                p=kW*1000.0, q=kVAR*1000.0, ratedS=kVA*1000.0, ratedU=kV*1000.0)
      sparql.setQuery(inspec)
      ret = sparql.query()

      if len(phases) > 0 and phases != 'ABC':
        phase_list = ParsePhases (phases)
        nphs = len(phase_list)
        p=kW*1000.0/nphs
        q=kVAR*1000.0/nphs
        for phs in phase_list:
          nmPhs = '{:s}_{:s}'.format (nmPEC, phs)
          idPhs = GetCIMID('PowerElectronicsConnectionPhase', nmPhs, uuids)
          inspep = prefix + ins_pep_template.format (url=blz_url, res=idPhs, nm=nmPhs, resPEC=idPEC, resLoc=idLoc, ns=cim_ns, phs=phs, p=p, q=q)
          sparql.setQuery(inspep)
          ret = sparql.query()

      if unit == 'Battery':
        state = 'Waiting'
        if kW > 0.0:
          state = 'Discharging'
        elif kW < 0.0:
          state = 'Charging'
        insunit = prefix + ins_bat_template.format(url=blz_url, res=idUnit, nm=nmUnit, resLoc=idLoc, ns=cim_ns,
                                                   ratedE=ratedkwh*1000.0, storedE=storedkwh*1000.0, state=state)
      elif unit == 'Photovoltaic':
        insunit = prefix + ins_pv_template.format(url=blz_url, res=idUnit, nm=nmUnit, resLoc=idLoc)
      else:
        insunit = '** Unsupported Unit ' + unit
      sparql.setQuery(insunit)
      ret = sparql.query()

      instrm = prefix + ins_trm_template.format(url=blz_url, res=idTrm, nm=nmTrm, resCN=idCN, resEQ=idPEC)
      sparql.setQuery(instrm)
      ret = sparql.query()

      insloc = prefix + ins_loc_template.format(url=blz_url, res=idLoc, nm=nmLoc, resCrs=crs_id)
      sparql.setQuery(insloc)
      ret = sparql.query()

      inspt = prefix + ins_pt_template.format(url=blz_url, res=idPt, resLoc=idLoc, seq=1, x=x, y=y)
      sparql.setQuery(inspt)
      ret = sparql.query()

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
      print (len(buses), 'buses')

      sparql.setQuery(qloc)
      ret = sparql.query()
      for b in ret.bindings:
        key = b['locid'].value + ':' + b['seq'].value
        x = b['x'].value
        y = b['y'].value
        locs[key] = {'x': x, 'y': y}
      print (len(locs), 'locations')

      sparql.setQuery (prefix + crs_query)
      ret = sparql.query()
      for b in ret.bindings:
        crs_id = b['id'].value
        print ('Coordinate System', b['name'].value, crs_id, 'Feeder', fdr_id)
        break

fp.close()

if fuidname is not None:
  print ('saving identifiable instance mRIDs to', fuidname)
  fuid = open (fuidname, 'w')
  for key, val in uuids.items():
    print ('{:s},{:s}'.format (key.replace(':', ',', 1), val), file=fuid)
  fuid.close()

