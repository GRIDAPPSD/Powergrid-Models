from SPARQLWrapper import SPARQLWrapper2#, JSON
import sys
import re
import uuid

# constants.py is used for configuring blazegraph.
import constants

if len(sys.argv) < 2:
	print ('usage: python InsertMeasurements.py fname')
	print (' (Blazegraph server must already be started)')
	exit()

fp = open (sys.argv[1], 'r')

sparql = SPARQLWrapper2 (constants.blazegraph_url)
sparql.method = 'POST'

def InsertMeasurement (meascls, measid, eqname, eqid, trmid, meastype, phases):
	#if not measid starts with _ then prepend it, this is here for consistency. otherwise the mrids are uploaded without the initial _
	if (not str(measid).startswith("_")):
		measid = "_"+str(measid)

	resource = '<' + constants.blazegraph_url + '#' + str(measid) + '>'
	equipment = '<' + constants.blazegraph_url + '#' + str(eqid) + '>'
	terminal = '<' + constants.blazegraph_url + '#' + str(trmid) + '>'
	ln1 = resource + ' a c:' + meascls + '. ' 
	ln2 = resource + ' c:IdentifiedObject.mRID \"' + str(measid) + '\". '
	ln3 = resource + ' c:IdentifiedObject.name \"' + str(eqname) + '\". '
	ln4 = resource + ' c:Measurement.PowerSystemResource ' + equipment + '. '
	ln5 = resource + ' c:Measurement.Terminal ' + terminal + '. '
	ln6 = (resource + ' c:Measurement.phases ' + constants.cim100
		+ 'PhaseCode.' + phases + '>. ')
	ln7 = resource + ' c:Measurement.measurementType \"' + meastype + '\"'
	qstr = (constants.prefix + 'INSERT DATA { ' + ln1 + ln2 + ln3 + ln4 +
		ln5 + ln6 + ln7 + '}')

#	print (qstr)
	sparql.setQuery(qstr)
	ret = sparql.query()
#	print (ret)
	return

lines = fp.readlines()
for ln in lines:
	toks = re.split('[,\s]+', ln)
	if toks[0] == 'LinearShuntCompensator':
		phases = toks[3]
		eqid = toks[4]
		trmid = toks[5]
		id1 = uuid.uuid4()
		id2 = uuid.uuid4()
		id3 = uuid.uuid4()
		InsertMeasurement ('Analog', id1, 'LinearShuntCompensator_' + toks[1], eqid, trmid, 'PNV', phases)
		InsertMeasurement ('Analog', id2, 'LinearShuntCompensator_' + toks[1], eqid, trmid, 'VA', phases)
		InsertMeasurement ('Discrete', id3, 'LinearShuntCompensator_' + toks[1], eqid, trmid, 'Pos', phases)
	if toks[0] == 'PowerTransformer' and toks[1] == 'RatioTapChanger':
		phases = toks[5]
		eqid = toks[6]
		trmid = toks[7]
		id1 = uuid.uuid4()
		InsertMeasurement ('Discrete', id1, 'RatioTapChanger_' + toks[2], eqid, trmid, 'Pos', phases)
	if toks[0] == 'EnergyConsumer':
		phases = toks[3]
		eqid = toks[4]
		trmid = toks[5]
		id1 = uuid.uuid4()
		id2 = uuid.uuid4()
		InsertMeasurement ('Analog', id1, 'EnergyConsumer_' + toks[1], eqid, trmid, 'PNV', phases)
		InsertMeasurement ('Analog', id2, 'EnergyConsumer_' + toks[1], eqid, trmid, 'VA', phases)
	if toks[0] == 'SynchronousMachine':
	  phases = toks[3]
	  eqid = toks[4]
	  trmid = toks[5]
	  id1 = uuid.uuid4()
	  id2 = uuid.uuid4()
	  InsertMeasurement ('Analog', id1, 'SynchronousMachine_' + toks[1], eqid, trmid, 'PNV', phases)
	  InsertMeasurement ('Analog', id2, 'SynchronousMachine_' + toks[1], eqid, trmid, 'VA', phases)
	if toks[0] == 'PowerElectronicsConnection' and toks[1] == 'PhotovoltaicUnit':
		phases = toks[5]
		eqid = toks[6]
		trmid = toks[7]
		id1 = uuid.uuid4()
		id2 = uuid.uuid4()
		InsertMeasurement ('Analog', id1, 'PowerElectronicsConnection_PhotovoltaicUnit_' + toks[3], eqid, trmid, 'PNV', phases)
		InsertMeasurement ('Analog', id2, 'PowerElectronicsConnection_PhotovoltaicUnit_' + toks[3], eqid, trmid, 'VA', phases)
	if toks[0] == 'PowerElectronicsConnection' and toks[1] == 'BatteryUnit':
		phases = toks[5]
		eqid = toks[6]
		trmid = toks[7]
		id1 = uuid.uuid4()
		id2 = uuid.uuid4()
		InsertMeasurement ('Analog', id1, 'PowerElectronicsConnection_BatteryUnit_' + toks[3], eqid, trmid, 'PNV', phases)
		InsertMeasurement ('Analog', id2, 'PowerElectronicsConnection_BatteryUnit_' + toks[3], eqid, trmid, 'VA', phases)
	if toks[0] == 'PowerTransformer' and toks[1] == 'PowerTransformerEnd':
		what = toks[2]
		phases = toks[6]
		eqid = toks[7]
		trmid = toks[8]
		id1 = uuid.uuid4()
		if 'v' in what:
			InsertMeasurement ('Analog', id1, 'PowerTransformer_' + toks[3] + '_Voltage', eqid, trmid, 'PNV', phases)
		elif 's' in what:
			InsertMeasurement ('Analog', id1, 'PowerTransformer_' + toks[3] + '_Power', eqid, trmid, 'VA', phases)
		elif 'i' in what:
			InsertMeasurement ('Analog', id1, 'PowerTransformer_' + toks[3] + '_Current', eqid, trmid, 'A', phases)
	if toks[0] == 'ACLineSegment' or toks[0] == 'LoadBreakSwitch' or toks[0] == 'Breaker' or toks[0] == 'Recloser':
		what = toks[1]
		phases = toks[5]
		eqid = toks[6]
		if '1' in what:
			trmid = toks[7]
			if toks[0] != 'ACLineSegment' and what == 'i1':
				id1 = uuid.uuid4()
				InsertMeasurement ('Discrete', id1, toks[0] + '_' + toks[2] + '_State', eqid, trmid, 'Pos', phases)
		else:
			trmid = toks[8]
		id1 = uuid.uuid4()
		if 'v' in what:
			InsertMeasurement ('Analog', id1, toks[0] + '_' + toks[2] + '_Voltage', eqid, trmid, 'PNV', phases)
		elif 's' in what:
			InsertMeasurement ('Analog', id1, toks[0] + '_' + toks[2] + '_Power', eqid, trmid, 'VA', phases)
		elif 'i' in what:
			InsertMeasurement ('Analog', id1, toks[0] + '_' + toks[2] + '_Current', eqid, trmid, 'A', phases)


fp.close()