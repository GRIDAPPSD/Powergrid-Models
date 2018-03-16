from SPARQLWrapper import SPARQLWrapper2, JSON
import sys

def FlatPhases (phases):
	if len(phases) < 1:
		return ['A', 'B', 'C']
	if 'ABC' in phases:
		return ['A', 'B', 'C']
	if 'AB' in phases:
		return ['A', 'B']
	if 'AC' in phases:
		return ['A', 'C']
	if 'BC' in phases:
		return ['B', 'C']
	if 'A' in phases:
		return ['A']
	if 'B' in phases:
		return ['B']
	if 'C' in phases:
		return ['C']
	if 's12' in phases:
		return ['s12']
	if 's1s2' in phases:
		return ['s1', 's2']
	if 's1' in phases:
		return ['s1']
	if 's2' in phases:
		return ['s2']
	return []


if len(sys.argv) < 3:
	print ('usage: python ListMeasureables.py feeder_mRID fname')
	print (' (Blazegraph server must already be started, with feeder_mRID model data loaded)')
	exit()

op = open (sys.argv[2], 'w')
sparql = SPARQLWrapper2("http://localhost:9999/blazegraph/namespace/kb/sparql")

prefix = """
PREFIX r: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX c: <http://iec.ch/TC57/2012/CIM-schema-cim17#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
"""

fidselect = """ VALUES ?fdrid {\"""" + sys.argv[1] + """\"}
 ?s c:Equipment.EquipmentContainer ?fdr.
 ?fdr c:IdentifiedObject.mRID ?fdrid. """

#################### capacitors

qstr = prefix + """SELECT ?name ?bus ?phases ?eqid ?trmid WHERE {""" + fidselect + """
 ?s r:type c:LinearShuntCompensator.
 ?s c:IdentifiedObject.name ?name.
 ?s c:IdentifiedObject.mRID ?eqid. 
 ?t c:Terminal.ConductingEquipment ?s.
 ?t c:IdentifiedObject.mRID ?trmid. 
 ?t c:Terminal.ConnectivityNode ?cn. 
 ?cn c:IdentifiedObject.name ?bus.
 OPTIONAL {?scp c:ShuntCompensatorPhase.ShuntCompensator ?s.
 ?scp c:ShuntCompensatorPhase.phase ?phsraw.
   bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phases) } }
"""
#print (qstr)
sparql.setQuery(qstr)
ret = sparql.query()
#print ('\nLinearShuntCompensator binding keys are:',ret.variables)
for b in ret.bindings:
	if 'phases' in b: # was OPTIONAL in the query
		phases = FlatPhases (b['phases'].value)
	else:
		phases = FlatPhases ('ABC')
	for phs in phases:
		print ('LinearShuntCompensator',b['name'].value,b['bus'].value,phs,b['eqid'].value,b['trmid'].value,file=op)
   
#################### regulators

qstr = prefix + """SELECT ?name ?wnum ?bus (group_concat(distinct ?phs;separator=\"\") as ?phases) ?eqid ?trmid WHERE {
 SELECT ?name ?wnum ?bus ?phs ?eqid ?trmid WHERE { """ + fidselect + """
 ?rtc r:type c:RatioTapChanger.
 ?rtc c:IdentifiedObject.name ?rname.
 ?rtc c:IdentifiedObject.mRID ?rtcid.
 ?rtc c:RatioTapChanger.TransformerEnd ?end.
 ?end c:TransformerEnd.endNumber ?wnum.
 ?end c:TransformerEnd.Terminal ?trm.
 ?trm c:IdentifiedObject.mRID ?trmid. 
 ?trm c:Terminal.ConnectivityNode ?cn. 
 ?cn c:IdentifiedObject.name ?bus.
 OPTIONAL {?end c:TransformerTankEnd.phases ?phsraw.
  bind(strafter(str(?phsraw),"PhaseCode.") as ?phs)}
 ?end c:TransformerTankEnd.TransformerTank ?tank.
 ?tank c:TransformerTank.PowerTransformer ?s.
 ?s c:IdentifiedObject.name ?name.
 ?s c:IdentifiedObject.mRID ?eqid.
 ?tank c:IdentifiedObject.name ?tname.
 } ORDER BY ?name ?phs
}
GROUP BY ?name ?wnum ?bus ?eqid ?trmid
ORDER BY ?name
"""
#print (qstr)
sparql.setQuery(qstr)
ret = sparql.query()
#print ('\nRatioTapChanger binding keys are:',ret.variables)
for b in ret.bindings:
	phases = FlatPhases (b['phases'].value)
	for phs in phases:
		print ('PowerTransformer','RatioTapChanger',b['name'].value,b['wnum'].value,b['bus'].value,phs,b['eqid'].value,b['trmid'].value,file=op)

#################### switches

qstr = prefix + """SELECT ?name ?bus1 ?bus2 (group_concat(distinct ?phs1;separator=\"\") as ?phases1) ?eqid ?trm1id ?trm2id WHERE {
  SELECT ?name ?bus1 ?bus2 ?phs1 ?eqid ?trm1id ?trm2id WHERE {""" + fidselect + """
 ?s r:type c:LoadBreakSwitch.
 ?s c:IdentifiedObject.name ?name.
 ?s c:IdentifiedObject.mRID ?eqid. 
 ?t1 c:Terminal.ConductingEquipment ?s.
 ?t1 c:ACDCTerminal.sequenceNumber "1".
 ?t1 c:IdentifiedObject.mRID ?trm1id. 
 ?t1 c:Terminal.ConnectivityNode ?cn1. 
 ?cn1 c:IdentifiedObject.name ?bus1.
 ?t2 c:Terminal.ConductingEquipment ?s.
 ?t2 c:ACDCTerminal.sequenceNumber "2".
 ?t2 c:IdentifiedObject.mRID ?trm2id. 
 ?t2 c:Terminal.ConnectivityNode ?cn2. 
 ?cn2 c:IdentifiedObject.name ?bus2.
 OPTIONAL {?scp c:SwitchPhase.Switch ?s.
 ?scp c:SwitchPhase.phaseSide1 ?phs1raw.
 	bind(strafter(str(?phs1raw),\"SinglePhaseKind.\") as ?phs1) } } ORDER BY ?name ?phs1
 } GROUP BY ?name ?bus1 ?bus2 ?eqid ?trm1id ?trm2id
 ORDER BY ?name
"""
#print (qstr)
sparql.setQuery(qstr)
ret = sparql.query()
#print ('\nLoadBreakSwitch binding keys are:',ret.variables)
for b in ret.bindings:
	phases1 = FlatPhases (b['phases1'].value)
	for phs1 in phases1:
		print ('LoadBreakSwitch','v1',b['name'].value,b['bus1'].value,b['bus2'].value,phs1,b['eqid'].value,b['trm1id'].value,b['trm2id'].value,file=op)
   
##################### ACLineSegments
   
qstr = prefix + """SELECT ?name ?bus1 ?bus2 (group_concat(distinct ?phs;separator=\"\") as ?phases) ?eqid ?trm1id ?trm2id WHERE {
  SELECT ?name ?bus1 ?bus2 ?phs ?eqid ?trm1id ?trm2id WHERE {""" + fidselect + """
 ?s r:type c:ACLineSegment.
 ?s c:IdentifiedObject.name ?name.
 ?s c:IdentifiedObject.mRID ?eqid. 
 ?t1 c:Terminal.ConductingEquipment ?s.
 ?t1 c:ACDCTerminal.sequenceNumber "1".
 ?t1 c:IdentifiedObject.mRID ?trm1id. 
 ?t1 c:Terminal.ConnectivityNode ?cn1. 
 ?cn1 c:IdentifiedObject.name ?bus1.
 ?t2 c:Terminal.ConductingEquipment ?s.
 ?t2 c:ACDCTerminal.sequenceNumber "2".
 ?t2 c:IdentifiedObject.mRID ?trm2id. 
 ?t2 c:Terminal.ConnectivityNode ?cn2. 
 ?cn2 c:IdentifiedObject.name ?bus2.
 OPTIONAL {?acp c:ACLineSegmentPhase.ACLineSegment ?s.
 ?acp c:ACLineSegmentPhase.phase ?phsraw.
	bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) } } ORDER BY ?name ?phs
 } GROUP BY ?name ?bus1 ?bus2 ?eqid ?trm1id ?trm2id
 ORDER BY ?name
"""
#print (qstr)
sparql.setQuery(qstr)
ret = sparql.query()
#print ('\nACLineSegment binding keys are:',ret.variables)
for b in ret.bindings:
	phases = FlatPhases (b['phases'].value)
	for phs in phases:
		print ('ACLineSegment','v1',b['name'].value,b['bus1'].value,b['bus2'].value,phs,b['eqid'].value,b['trm1id'].value,b['trm2id'].value,file=op)
   
####################### - EnergyConsumer

qstr = prefix + """SELECT ?name ?bus (group_concat(distinct ?phs;separator=\"\") as ?phases) ?eqid ?trmid WHERE {
	SELECT ?name ?bus ?phs ?eqid ?trmid WHERE {""" + fidselect + """
 ?s r:type c:EnergyConsumer.
 ?s c:IdentifiedObject.name ?name.
 ?s c:IdentifiedObject.mRID ?eqid. 
 ?t1 c:Terminal.ConductingEquipment ?s.
 ?t1 c:IdentifiedObject.mRID ?trmid. 
 ?t1 c:ACDCTerminal.sequenceNumber "1".
 ?t1 c:Terminal.ConnectivityNode ?cn1. 
 ?cn1 c:IdentifiedObject.name ?bus.
 OPTIONAL {?acp c:EnergyConsumerPhase.EnergyConsumer ?s.
 ?acp c:EnergyConsumerPhase.phase ?phsraw.
	bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) } } ORDER BY ?name ?phs
 } GROUP BY ?name ?bus ?eqid ?trmid
 ORDER BY ?name
"""
#print (qstr)
sparql.setQuery(qstr)
ret = sparql.query()
#print ('\nEnergyConsumer binding keys are:',ret.variables)
for b in ret.bindings:
	phases = FlatPhases (b['phases'].value)
	for phs in phases:
		print ('EnergyConsumer',b['name'].value,b['bus'].value,phs,b['eqid'].value,b['trmid'].value,file=op)


####################### - Storage

qstr = prefix + """SELECT ?name ?uname ?bus (group_concat(distinct ?phs;separator=\"\") as ?phases) ?eqid ?trmid WHERE {
	SELECT ?name ?uname ?bus ?phs ?eqid ?trmid WHERE {""" + fidselect + """
 ?s r:type c:PowerElectronicsConnection.
 ?s c:IdentifiedObject.name ?name.
 ?s c:IdentifiedObject.mRID ?eqid. 
 ?peu r:type c:BatteryUnit.
 ?peu c:IdentifiedObject.name ?uname.
 ?s c:PowerElectronicsConnection.PowerElectronicsUnit ?peu.
 ?t1 c:Terminal.ConductingEquipment ?s.
 ?t1 c:IdentifiedObject.mRID ?trmid. 
 ?t1 c:ACDCTerminal.sequenceNumber "1".
 ?t1 c:Terminal.ConnectivityNode ?cn1. 
 ?cn1 c:IdentifiedObject.name ?bus.
 OPTIONAL {?pep c:PowerElectronicsConnectionPhase.PowerElectronicsConnection ?s.
 ?pep c:PowerElectronicsConnectionPhase.phase ?phsraw.
	bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) } } ORDER BY ?name ?phs
 } GROUP BY ?name ?uname ?bus ?eqid ?trmid
 ORDER BY ?name
"""
#print (qstr)
sparql.setQuery(qstr)
ret = sparql.query()
#print ('\nPowerElectronicsConnection->BatteryUnit binding keys are:',ret.variables)
for b in ret.bindings:
	phases = FlatPhases (b['phases'].value)
	for phs in phases:
		print ('PowerElectronicsConnection','BatteryUnit',b['name'].value,b['uname'].value,b['bus'].value,phs,b['eqid'].value,b['trmid'].value,file=op)

####################### - Solar

qstr = prefix + """SELECT ?name ?uname ?bus (group_concat(distinct ?phs;separator=\"\") as ?phases) ?eqid ?trmid WHERE {
	SELECT ?name ?uname ?bus ?phs ?eqid ?trmid WHERE {""" + fidselect + """
 ?s r:type c:PowerElectronicsConnection.
 ?s c:IdentifiedObject.name ?name.
 ?s c:IdentifiedObject.mRID ?eqid. 
 ?peu r:type c:PhotovoltaicUnit.
 ?peu c:IdentifiedObject.name ?uname.
 ?s c:PowerElectronicsConnection.PowerElectronicsUnit ?peu.
 ?t1 c:Terminal.ConductingEquipment ?s.
 ?t1 c:IdentifiedObject.mRID ?trmid. 
 ?t1 c:ACDCTerminal.sequenceNumber "1".
 ?t1 c:Terminal.ConnectivityNode ?cn1. 
 ?cn1 c:IdentifiedObject.name ?bus.
 OPTIONAL {?pep c:PowerElectronicsConnectionPhase.PowerElectronicsConnection ?s.
 ?pep c:PowerElectronicsConnectionPhase.phase ?phsraw.
	bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) } } ORDER BY ?name ?phs
 } GROUP BY ?name ?uname ?bus ?eqid ?trmid
 ORDER BY ?name
"""
#print (qstr)
sparql.setQuery(qstr)
ret = sparql.query()
#print ('\nPowerElectronicsConnection->PhotovoltaicUnit binding keys are:',ret.variables)
for b in ret.bindings:
	phases = FlatPhases (b['phases'].value)
	for phs in phases:
		print ('PowerElectronicsConnection','PhotovoltaicUnit',b['name'].value,b['uname'].value,b['bus'].value,phs,b['eqid'].value,b['trmid'].value,file=op)

####################### - PowerTransformer, no tanks

qstr = prefix + """SELECT ?name ?wnum ?bus ?eqid ?trmid WHERE {""" + fidselect + """
 ?s r:type c:PowerTransformer.
 ?s c:IdentifiedObject.name ?name.
 ?s c:IdentifiedObject.mRID ?eqid.
 ?end c:PowerTransformerEnd.PowerTransformer ?s.
 ?end c:TransformerEnd.Terminal ?trm.
 ?end c:TransformerEnd.endNumber ?wnum.
 ?trm c:IdentifiedObject.mRID ?trmid. 
 ?trm c:Terminal.ConnectivityNode ?cn. 
 ?cn c:IdentifiedObject.name ?bus.
}
ORDER BY ?name ?wnum
"""
#print (qstr)
sparql.setQuery(qstr)
ret = sparql.query()
#print ('\nPowerTransformer (no-tank) binding keys are:',ret.variables,'plus phases=ABC')
for b in ret.bindings:
	for phs in 'ABC':
		print ('PowerTransformer','PowerTransformerEnd','v1',b['name'].value,b['wnum'].value,b['bus'].value,phs,b['eqid'].value,b['trmid'].value,file=op)

####################### - PowerTransformer, with tanks

qstr = prefix + """SELECT ?name ?wnum ?bus ?phases ?eqid ?trmid WHERE {""" + fidselect + """
 ?s r:type c:PowerTransformer.
 ?s c:IdentifiedObject.name ?name.
 ?s c:IdentifiedObject.mRID ?eqid.
 ?tank c:TransformerTank.PowerTransformer ?s.
 ?end c:TransformerTankEnd.TransformerTank ?tank.
 ?end c:TransformerEnd.Terminal ?trm.
 ?end c:TransformerEnd.endNumber ?wnum.
 ?trm c:IdentifiedObject.mRID ?trmid. 
 ?trm c:Terminal.ConnectivityNode ?cn. 
 ?cn c:IdentifiedObject.name ?bus.
 OPTIONAL {?end c:TransformerTankEnd.phases ?phsraw.
  bind(strafter(str(?phsraw),"PhaseCode.") as ?phases)}
}
ORDER BY ?name ?wnum ?phs
"""
#print (qstr)
sparql.setQuery(qstr)
ret = sparql.query()
#print ('\nPowerTransformer (with tank) binding keys are:',ret.variables)
for b in ret.bindings:
	phases = FlatPhases (b['phases'].value)
	for phs in phases:
		print ('PowerTransformer','TransformerTankEnd','v1',b['name'].value,b['wnum'].value,b['bus'].value,phs,b['eqid'].value,b['trmid'].value,file=op)


op.close()

   
