"""
Created by Ahmad Tbaileh (ahmad.tbaileh@pnnl.gov)
Copyright (c) 2018-2020 Battelle Memorial Institute.  The Government retains a paid-up non-exclusive, irrevocable
worldwide license to reproduce, prepare derivative works, perform publicly and display publicly by or for the
Government, including the right to distribute to other Government contractors.
"""

import math
import numpy
import pypyodbc
import json
import sys
import os

def get_phnum(phstr):
  '''Convert a phase letter to a DSS phase number'''
  if phstr == 'A' or phstr == '1':
    return '1'
  if phstr == 'B' or phstr == '2':
    return '2'
  if phstr == 'C' or phstr == '3':
    return '3'
  return None

def get_phsfx(phstr):
  '''Build the OpenDSS bus suffix from the GridLAB-D phase field'''
  phsfx = ''
  if 'A' in phstr or '1' in phstr:
    phsfx += '.1'
  if 'B' in phstr or '2' in phstr:
    phsfx += '.2'
  if 'C' in phstr or '3' in phstr:
    phsfx += '.3'
  return phsfx

def count_ph(phstr):
  '''Count the number of phases from the GridLAB-D phase field'''
  count = 0
  if 'A' in phstr or '1' in phstr:
    count += 1
  if 'B' in phstr or '2' in phstr:
    count += 1
  if 'C' in phstr or '3' in phstr:
    count += 1
  return count

def dss_name(s):
  s1 = (((str(s).replace(" ","_")).replace("'","")).replace("/","_")).replace(".","_")
  return s1

def WriteLoadPower (fp, p, q, s, AllocateLoads):
  if AllocateLoads > 0:
    fp.write (' xfkva=' + '{:.2f}'.format(s))
    return
  # any two of these fully define the load, and if only one is written the default power factor will be 0.88
  if s != 0.0:
    if p != 0.0:
      fp.write (' xfkva=' + '{:.2f}'.format(s))
    else:
      fp.write (' kva=' + '{:.2f}'.format(s))
  if p != 0.0:
    fp.write (' kw=' + '{:.2f}'.format(p))
  if q != 0.0:
    fp.write (' kvar=' + '{:.2f}'.format(q))

def WriteThreePhaseLoad (fp, name, bus, p, q, s, kv, conn, AllocateLoads):
  fp.write('new load.' + name + ' bus1=' + bus + ' phases=3 conn=' + conn + ' kv=' + '{:.3f}'.format(kv))
  WriteLoadPower (fp, p, q, s, AllocateLoads)
  fp.write('\n')

def WriteSinglePhaseLoad (fp, name, phs, bus, p, q, s, kv, conn, AllocateLoads):
  if conn == 'wye':
    kv /= math.sqrt(3.0)
    phs_str = '.' + str(phs)
  else:
    phs_str = '.' + str(phs) + '.' + str((phs + 1) % 3)
  fp.write('new load.' + name + str(phs) + ' bus1=' + bus + phs_str + ' phases=1 conn=' + conn + ' kv=' + '{:.3f}'.format(kv))
  WriteLoadPower (fp, p, q, s, AllocateLoads)
  fp.write('\n')

################################################################################################
#"": "C:/PNNL/Projects/SETO_Protection/DEV",
#"": "./test_dss",

def ConvertMDB(cfg):
  rootdir = cfg['DefaultDir']
  mdbname = cfg['MDBName']
  outpath = cfg['OutDir'] + '/'
  BaseVoltages = cfg['BaseVoltages']
  AllocateLoads = int(cfg['AllocateLoads'])
  myFlag = int(cfg['SubOrFeeder'])  # 1 for a substation source/swing/slack bus, 0 for a feeder source/swing/slack bus
  SW_Flag = int(cfg['InsertSwitchgear'])  # insert switchgear

  ODBCstr = 'DRIVER={Microsoft Access Driver (*.mdb, *.accdb)};' + \
    'UID=admin;UserCommitSync=Ye;Threads=3;SafeTransactions=0;PageTimeout=5;' + \
    'MaxScanRows=8;MaxBufferSize=2048;FIL={MS Access};DriverId=25;' + \
    'DefaultDir=' + rootdir + ';' + \
    'DBQ=' + rootdir + '/' + mdbname + '.mdb;'

  print ('ODBC Connection String:')
  print (' ', ODBCstr)
  con = pypyodbc.connect(ODBCstr)

  # Choosing a substation or a feeder source
  Substation = myFlag
  Feeder = 1 - Substation

  cursor = con.cursor()

  # keep track of the catalog items

  # -----------------------------------------------------------------------------
  # Breakers
  # -----------------------------------------------------------------------------
  print('Processing breakers.')
  cursor.execute( "SELECT * FROM instbreakers")
  breakerf = open(outpath + 'Breakers.dss', 'w');
  breakerf.write('!Breaker Definitions:\n\n');
  for row in cursor.fetchall():
    name = 'swtcontrol.' + dss_name(row['uniquedeviceid'])
    secID = 'line.' + dss_name(row['sectionid'])
    if int(row['nearfromnode']) == 1:
      swterm = 1
    else:
      swterm = 2
    breakerf.write('new ' + name + ' switchedobj=' + secID + ' switchedterm=' + str(swterm))
    if row['breakerisopen'] == 1:
      breakerf.write (' action=open')
    else:
      breakerf.write (' action=close')
    breakerf.write('\n');
  breakerf.close()

  # -----------------------------------------------------------------------------
  # Reclosers
  # -----------------------------------------------------------------------------
  print('Processing reclosers.')
  cursor.execute( "SELECT * FROM instreclosers")
  recf = open(outpath + 'Reclosers.dss', 'w');
  recf.write('!Recloser Definitions:\n\n');
  for row in cursor.fetchall():
    name = 'recloser.' + dss_name(row['uniquedeviceid'])
    secID = 'line.' + dss_name(row['sectionid'])
    if int(row['nearfromnode']) == 1:
      swterm = 1
    else:
      swterm = 2
    recf.write('new ' + name + ' monitoredobj=' + secID + ' monitoredterm=' + str(swterm))
    recf.write(' phasetrip=' + '{:.2f}'.format(2 * float(row['amprating'])))
    if row['recloserisopen'] == 1:
      recf.write (' action=open')
    else:
      recf.write (' action=close')
    recf.write('\n');
  recf.close()

  # -----------------------------------------------------------------------------
  # Sectionalizers
  # -----------------------------------------------------------------------------
  print('Processing sectionalizers.')
  cursor.execute( "SELECT * FROM instsectionalizers")
  secf = open(outpath + 'Sectionalizers.dss', 'w');
  secf.write('!Sectionalizer Definitions:\n\n');
  for row in cursor.fetchall():
    name = 'swtcontrol.' + dss_name(row['uniquedeviceid'])
    secID = 'line.' + dss_name(row['sectionid'])
    if int(row['nearfromnode']) == 1:
      swterm = 1
    else:
      swterm = 2
    secf.write('new ' + name + ' switchedobj=' + secID + ' switchedterm=' + str(swterm))
    if row['sectionalizerisopen'] == 1:
      secf.write (' action=open')
    else:
      secf.write (' action=close')
    secf.write('\n');
  secf.close()

  # -----------------------------------------------------------------------------
  # Fuses
  # -----------------------------------------------------------------------------
  print('Processing Fuses.')
  cursor.execute( "SELECT * FROM instfuses")
  fusef = open(outpath + 'Fuses.dss', 'w');
  fusef.write('!Fuse Definitions:\n\n');
  for row in cursor.fetchall():
    name = 'fuse.' + dss_name(row['uniquedeviceid'])
    secID = 'line.' + dss_name(row['sectionid'])
    amps = row['amprating']
    if len(amps) < 1:
        amps = '40'
    if int(row['nearfromnode']) == 1:
      swterm = 1
    else:
      swterm = 2
    fusef.write('new ' + name + ' ratedcurrent=' + amps + ' monitoredobj=' + secID + ' monitoredterm=' + str(swterm))
    fusef.write(' switchedobj=' + secID + ' switchedterm=' + str(swterm))
    if row['fuseisopen'] == 'OPEN':
      fusef.write(' action=open')
    fusef.write('\n');
  fusef.close()

  # -----------------------------------------------------------------------------
  # Switches
  # -----------------------------------------------------------------------------
  print('Processing switches.')
  cursor.execute ('SELECT * from instswitches')
  switchf = open(outpath + 'Switches.dss', 'w')
  switchf.write('! Switch Definitions\n')
  for row in cursor.fetchall():
    name = 'swtcontrol.' + dss_name(row['uniquedeviceid'])
    secID = 'line.' + dss_name(row['sectionid'])
    if int(row['nearfromnode']) == 1:
      swterm = 1
    else:
      swterm = 2
    switchf.write('new ' + name + ' switchedobj=' + secID + ' switchedterm=' + str(swterm))
    if row['switchisopen'] == 1:
      switchf.write (' action=open')
    else:
      switchf.write (' action=close')
    switchf.write('\n');
  switchf.close()

  # -----------------------------------------------------------------------------
  # Line Sections
  # -----------------------------------------------------------------------------
  print('Processing Lines:')

  spacings = set()
  wires = set()
  cncables = set()
  tscables = set()
  xfmrcodes = set()
  linecodes = set()
  useDev = False

  try:
      cursor.execute("SELECT top 1 * FROM DevConductors")
      cursor.fetchall()
      useDev = True
      print ('DevConductors table found')
  except:
      print ('DevConjuctors table not found')

  if useDev:
      cursor.execute("SELECT instsection.*, devconductors.* " +\
                    "FROM (instsection INNER JOIN devconductors ON instsection.phaseconductorid = devconductors.conductorname)")
  else:
      cursor.execute ('SELECT * FROM instsection')

  sgf = open(outpath + 'SwitchGears.dss', 'w');
  sgf.write('! SwitchGears Definitions:\n');
  lf = open(outpath + 'Lines.dss', 'w')
  lf.write('! Line Definitions\n')
  for row in cursor.fetchall():
    name = dss_name(row['sectionid'])
    bus1 = dss_name(row['fromnodeid'])
    bus2 = dss_name(row['tonodeid'])
    phsfx = get_phsfx(row['sectionphases'])
    phqty = count_ph(row['sectionphases'])
    if SW_Flag == 1:
      if row['fromelboworbaystatus'] == 'O':
        sgf.write ('open line.' + name + ' 1\n')
      elif row['fromelboworbaystatus'] == 'C':
        sgf.write ('close line.' + name + ' 1\n')
      if row['toelboworbaystatus'] == 'O':
        sgf.write ('open line.' + name + ' 2\n')
      elif row['toelboworbaystatus'] == 'C':
        sgf.write ('close line.' + name + ' 2\n')

    length = float(row['sectionlength_mul']) / 5280.0

    spacing = dss_name(row['configurationid']) + '_' + str(phqty)
    if useDev:
        condType = dss_name(row['conductortype'])
    else:
        if spacing.startswith ('UG'):
            condType = 'Conc'
        else:
            condType = 'Bare'
    wire1 = dss_name(row['phaseconductorid'])
    wire2 = dss_name(row['phaseconductor2id'])
    wire3 = dss_name(row['phaseconductor3id'])
    wireN = dss_name(row['neutralconductorid'])
    wiretype = 'wires'
    if 'ActZ' in condType:
        print ('ActZ not supported in section {:s}'.format(name))
        linecodes.add (spacing)
    elif 'Bare' in condType or 'SepN' in condType:
        wiretype = 'wires'
        wires.add (wire1)
        wires.add (wire2)
        wires.add (wire3)
        wires.add (wireN)
        if not spacing.startswith('OH'):
            spacing = 'OH_' + spacing
    elif 'Conc' in condType:
        wiretype = 'cncables'
        cncables.add (wire1)
        cncables.add (wire2)
        cncables.add (wire3)
        if not spacing.startswith('UG'):
            spacing = 'UG_' + spacing
    elif 'Tape' in condType:
        wiretype = 'tscables'
        tscables.add (wire1)
        tscables.add (wire2)
        tscables.add (wire3)
        if not spacing.startswith('UG'):
            spacing = 'UG_' + spacing
    spacings.add (spacing)
    wirearray = []
    ncond = phqty
    wirearray.append (wire1)
    if phqty > 1:
      wirearray.append (wire2)
    if phqty > 2:
      wirearray.append (wire3)
    if 'N' in row['sectionphases'] and wiretype == 'wires':
      wirearray.append (wireN)
      ncond = phqty + 1

    lf.write('new line.' + name + \
             ' bus1=' + bus1 + phsfx + \
             ' bus2=' + bus2 + phsfx + \
             ' phases=' + str(phqty) + \
             ' spacing=' + spacing + \
             ' ' + wiretype + '=' + str(wirearray) + \
             ' length=' + '{:.4f}'.format(length) + \
             ' units=mi' + \
             '\n')
  sgf.close()
  lf.close()

  # -----------------------------------------------------------------------------
  # Transformers
  # -----------------------------------------------------------------------------
  print('Processing Transformers.')

  # Query the transformers
  cursor.execute("SELECT * " + \
                 "FROM  (instprimarytransformers " + \
                 "INNER JOIN  instsection ON instsection.sectionid = instprimarytransformers.sectionid)")

  # Write Transformers.dss
  # for Dominion Energy Virginia, all step transformers are single-phase, possibly banked
  xff = open(outpath + 'Transformers.dss', 'w')
  xff.write('! Transformer Definitions\n')
  for row in cursor.fetchall():
    name = dss_name(row['sectionid'])
    bus1 = dss_name(row['fromnodeid'])
    bus2 = dss_name(row['tonodeid'])
    xftype = dss_name(row['transformertype'])
    xfmrcodes.add (xftype)
    phsfx = row['sectionphases']
    for phs in phsfx:
      if 'A' == phs or 'B' == phs or 'C' == phs:
        phsidx = '.' + str (ord(phs) - ord('A') + 1)
        busarray = []
        if row['highsidenearfromnode'] == 1:
          busarray.append (bus1 + phsidx)
          busarray.append (bus2 + phsidx)
        else:
          busarray.append (bus2 + phsidx)
          busarray.append (bus1 + phsidx)
        xff.write('new transformer.' + name + phs + ' xfmrcode=' + xftype + ' bank=' + name + ' buses=' + str(busarray) + '\n')
    xff.write('edit line.' + name + ' enabled=no\n\n')
  xff.close()

  # -----------------------------------------------------------------------------
  # Capacitors
  # -----------------------------------------------------------------------------
  print('Processing capacitors.')
  # Capacitor is assumed to be located at recceiving end of a section
  # Query the capacitors
  cursor.execute("SELECT * " +\
                "FROM instsection " +\
                "INNER JOIN instcapacitors " +\
                "ON instcapacitors.sectionid = instsection.sectionid")
  capf = open (outpath + 'Capacitors.dss', 'w')
  capf.write('! Capacitor Definitions \n\n')
  for row in cursor.fetchall():
      name = dss_name(row['uniquedeviceid'])
      bus1 = dss_name(row['tonodeid'])
      phsfx = get_phsfx(row['connectedphases'])
      phqty = count_ph(row['connectedphases'])
      kvLL = float(row['ratedkv'])
      kv = kvLL
      if phqty == 1:
          kv = kvLL / math.sqrt(3)
      # Assuming all capacitors are on ## row['module1on']
      kvar = 0.0
      if '1' in phsfx:
          kvar += float(row['fixedkvarphase1']) + float(row['module1kvarperphase'])
      if '2' in phsfx:
          kvar += float(row['fixedkvarphase2']) + float(row['module1kvarperphase'])
      if '3' in phsfx:
          kvar += float(row['fixedkvarphase3']) + float(row['module1kvarperphase'])
      if kvar!=0.0:
          capf.write('new capacitor.{:s} bus1={:s} phases={:d} kvar={:.2f} kv={:.3f} conn=wye\n'.format (name, bus1+phsfx, phqty, kvar, kv)) 						+\
          capf.write('new capcontrol.' + name + '_ctrl ' + 'capacitor=' + name)
          if row['primarycontrolmode'] == 'VOLTS':
              # used metering phase for PT phase
              # used same time delay for both
              capf.write(' type=voltage element=capacitor.' + name + ' terminal=1'+\
                  ' ptphase=' + get_phnum(row['meteringphase']) + ' ptratio=1'+\
                  ' offsetting=' + str(row['module1capswitchtripvalue'])+\
                  ' onsetting=' + str(row['module1capswitchclosevalue'])+\
                  ' delay=' + str(row['timedelaysec'])+\
                  ' delayoff=' + str(row['timedelaysec'])+\
                  ' deadtime=0\n')
          else:
              # unrecognized control type: disable the controller
              print('Warning: "'+row['control']+'" cap control not implemented')
              capf.write(' enabled=false\n')
  capf.close()

  # -----------------------------------------------------------------------------
  # Loads
  # -----------------------------------------------------------------------------
  print('Processing loads:')

  # Write Loads.dss
  '''
  All loads are assumed to be wye connected
  All loads are assumed to be spot loads - no information about distributed loads for now
  Both can easily be changed if provided otherwise
  '''
  loadf = open(outpath + 'Loads.dss', 'w')
  loadf.write('! Load Definitions \n\n')
  feederP = 0.0
  feederQ = 0.0
  feederS = 0.0
  feederS1 = 0.0
  feederS2 = 0.0
  feederS3 = 0.0

  # Query all load
  cursor.execute( "SELECT * " + \
                  "FROM instsection " + \
                  "INNER JOIN loads " + \
                  "ON instsection.sectionid = loads.sectionid")
  # Non-Split-Phase
  print('\tNon-split-phase loads...')
  for row in cursor.fetchall():

    name = dss_name(row['sectionid'])
    bus1 = dss_name(row['fromnodeid'])
    bus2 = dss_name(row['tonodeid'])
    phqty = count_ph(row['sectionphases'])
    p1 = float(row['phase1kw'])
    q1 = float(row['phase1kvar'])
    s1 = float(row['phase1kva'])
    p2 = float(row['phase2kw'])
    q2 = float(row['phase2kvar'])
    s2 = float(row['phase2kva'])
    p3 = float(row['phase3kw'])
    q3 = float(row['phase3kvar'])
    s3 = float(row['phase3kva'])
    feederP += (p1 + p2 + p3)
    feederQ += (q1 + q2 + q3)
    feederS += (s1 + s2 + s3)
    feederS1 += s1
    feederS2 += s2
    feederS3 += s3
    wire1 = dss_name(row['phaseconductorid'])
    if '15kV' in wire1:
      kvload = 12.47
    else:
      kvload = 34.5
    connload = 'wye'
    bSpotLoad = True
    if row['isspotload'] == 0:
      bSpotLoad = False
      p1 *= 0.5
      p2 *= 0.5
      p3 *= 0.5
      q1 *= 0.5
      q2 *= 0.5
      q3 *= 0.5
      s1 *= 0.5
      s2 *= 0.5
      s3 *= 0.5
    bThreePhaseBalancedLoad = False
    if phqty == 3:
      if s1 == s2 and s2 == s3:
        if p1 == p2 and p2 == p3:
          if q1 == q2 and q2 == q3:
            bThreePhaseBalancedLoad = True
            ptotal = p1 + p2 + p3
            qtotal = q1 + q2 + q3
            stotal = s1 + s2 + s3

    if bThreePhaseBalancedLoad:
      if bSpotLoad:
        WriteThreePhaseLoad (loadf, name + '_spot', bus2, ptotal, qtotal, stotal, kvload, connload, AllocateLoads)
      else:
        WriteThreePhaseLoad (loadf, name + '_from', bus1, ptotal, qtotal, stotal, kvload, connload, AllocateLoads)
        WriteThreePhaseLoad (loadf, name + '_to', bus2, ptotal, qtotal, stotal, kvload, connload, AllocateLoads)
    else:
      if p1 != 0.0 or q1 != 0.0 or s1 != 0.0:
        if bSpotLoad:
          WriteSinglePhaseLoad (loadf, name + '_spot', 1, bus2, p1, q1, s1, kvload, connload, AllocateLoads)
        else:
          WriteSinglePhaseLoad (loadf, name + '_from', 1, bus1, p1, q1, s1, kvload, connload, AllocateLoads)
          WriteSinglePhaseLoad (loadf, name + '_to', 1, bus2, p1, q1, s1, kvload, connload, AllocateLoads)
      if p2 != 0.0 or q2 != 0.0 or s2 != 0.0:
        if bSpotLoad:
          WriteSinglePhaseLoad (loadf, name + '_spot', 2, bus2, p2, q2, s2, kvload, connload, AllocateLoads)
        else:
          WriteSinglePhaseLoad (loadf, name + '_from', 2, bus1, p2, q2, s2, kvload, connload, AllocateLoads)
          WriteSinglePhaseLoad (loadf, name + '_to', 2, bus2, p2, q2, s2, kvload, connload, AllocateLoads)
      if p3 != 0.0 or q3 != 0.0 or s3 != 0.0:
        if bSpotLoad:
          WriteSinglePhaseLoad (loadf, name + '_spot', 3, bus2, p3, q3, s3, kvload, connload, AllocateLoads)
        else:
          WriteSinglePhaseLoad (loadf, name + '_from', 3, bus1, p3, q3, s3, kvload, connload, AllocateLoads)
          WriteSinglePhaseLoad (loadf, name + '_to', 3, bus2, p3, q3, s3, kvload, connload, AllocateLoads)

  print ('Total Feeder P=' + '{:.1f}'.format(feederP) + ' kW, ' + '{:.1f}'.format(feederQ) + ' kVAR, ' + '{:.1f}'.format(feederS) + ' kVA')
  print ('Total Feeder Phase Balance:', '{:.1f}'.format(feederS1), '{:.1f}'.format(feederS2), '{:.1f}'.format(feederS3))
  print ('// Total Feeder P=' + '{:.1f}'.format(feederP) + ' kW, ' + '{:.1f}'.format(feederQ) + ' kVAR, ' + '{:.1f}'.format(feederS) + ' kVA', file=loadf)
  print ('// Total Feeder Phase Balance:', '{:.1f}'.format(feederS1), '{:.1f}'.format(feederS2), '{:.1f}'.format(feederS3), file=loadf)
  loadf.close()

  # -----------------------------------------------------------------------------
  # Buscoords
  # -----------------------------------------------------------------------------
  # Node query
  cursor.execute( "SELECT * " + \
                  "FROM (node " + \
                  "INNER JOIN instsection ON instsection.fromnodeid = node.nodeid OR instsection.tonodeid = node.nodeid) " )

  # Write Buscoords.csv
  coordf = open(outpath + 'Buscoords.csv', 'w')
  #coordf.write('!Coordination Definitions:\n\n');
  for row in cursor.fetchall():
    #phsfx = get_phsfx(row['sectionphases'])
    coordf.write(dss_name(row['nodeid']) + ',' + str(row['x']) + ',' + str(row['y']) + '\n')
  coordf.close()

  # -----------------------------------------------------------------------------
  # Sourcebus, Interconnections
  # -----------------------------------------------------------------------------
  print('Processing Large Customers (i.e. DER)')
  derf = open(outpath + 'LargeCust.dss', 'w')
  cursor.execute( "SELECT * " + \
                  "FROM instsection " + \
                  "INNER JOIN instlargecust " + \
                  "ON instsection.sectionid = instlargecust.sectionid")
  for row in cursor.fetchall():
    name = dss_name(row['uniquedeviceid'])
    bus2 = dss_name(row['tonodeid'])
    phqty = count_ph(row['sectionphases'])
    pmpp = float(row['genphase1kw']) + float(row['genphase2kw']) + float(row['genphase3kw'])
    wire1 = dss_name(row['phaseconductorid'])
    if '15kV' in wire1:
      kvgen = 12.47
    else:
      kvgen = 34.5
    if pmpp > 0.0:
      derf.write ('new pvsystem.' + name + ' phases=' + str(phqty) + ' bus1=' + bus2 + get_phsfx(row['sectionphases']))
      derf.write (' kv=' + '{:.2f}'.format(kvgen))
      derf.write (' pmpp=' + '{:.2f}'.format(pmpp) + ' kva=' + '{:.2f}'.format(pmpp) + ' irradiance=1\n')
  derf.close()

  print('Processing the voltage source.')
  sourcef = open(outpath + 'VSource.dss', 'w')
  if Feeder == 1:
    print('Using feeder as a power source slack bus...')
    cursor.execute( "SELECT * FROM instfeeders")
    for row in cursor.fetchall():
      basekv = row['nominalkvll']
      rootbus = row['feederid']
      r1 = float (row['possequenceresistance'])
      x1 = float (row['possequencereactance'])
      r0 = float (row['zerosequenceresistance'])
      x0 = float (row['zerosequencereactance'])
      vpu = float (row['busvoltagelevel']) / 120.0
      sourcef.write('new circuit.' + mdbname + ' bus1=sourcebus phases=3 basekv=' + str(basekv) + '\n')
      sourcef.write('~ r1=' + '{:.4f}'.format(r1) + ' x1=' + '{:.4f}'.format(x1) +
                    ' r0=' + '{:.4f}'.format(r0) + ' x0=' + '{:.4f}'.format(x0) + ' pu=' + '{:.4f}'.format(vpu) + '\n')

      sourcef.write('New Transformer.Reg1 phases=1 bank=reg1 XHL=0.01 kVAs=[15000 15000]\n')
      sourcef.write('~ Buses=[sourcebus.1 regbus.1] kVs=[19.92  19.92] %LoadLoss=0.01 taps=[1.0 1.05]\n')
      sourcef.write('new regcontrol.Reg1  transformer=Reg1 winding=2  vreg=126  band=2  ptratio=166\n')
      sourcef.write('New Transformer.Reg2 phases=1 bank=reg1 XHL=0.01 kVAs=[15000 15000]\n')
      sourcef.write('~ Buses=[sourcebus.2 regbus.2] kVs=[19.92  19.92] %LoadLoss=0.01 taps=[1.0 1.05]\n')
      sourcef.write('new regcontrol.Reg2  transformer=Reg2 winding=2  vreg=126  band=2  ptratio=166\n')
      sourcef.write('New Transformer.Reg3 phases=1 bank=reg1 XHL=0.01 kVAs=[15000 15000]\n')
      sourcef.write('~ Buses=[sourcebus.3 regbus.3] kVs=[19.92  19.92] %LoadLoss=0.01 taps=[1.0 1.05]\n')
      sourcef.write('new regcontrol.Reg3  transformer=Reg3 winding=2  vreg=126  band=2  ptratio=166\n')

      sourcef.write('new line.trunk bus1=regbus bus2=' + str(rootbus) + ' phases=3 switch=yes\n')
      sourcef.write('new energymeter.feeder element=line.trunk terminal=1\n')
  else:
    print('Using a substation transformer as a power source slack bus...')
    cursor.execute(	"SELECT * " +\
                    "FROM node " +\
                    "INNER JOIN instsubstationtransformers " +\
                    "ON instsubstationtransformers.subtranid = node.nodeid ")

    sourcef.write('new circuit.sourceckt bus1=sourcebus phases=3 basekv=115\n')
    sourcef.write('~ pu=1.00 R1=0 X1=0.0001 R0=0 X0=0.0001 \n')
    sourcef.write('new line.trunk bus1=sourcebus bus2=rootbus phases=3 switch=yes\n')
    sourcef.write('new energymeter.feeder element=line.trunk terminal=1\n')
    for row in cursor.fetchall():
        if 0:#row['x'] is not None and row['y'] is not None:
            # Write coordinates
            coordf.write(str(row['substationid'])+',')
            coordf.write(str(row['x'])+',')
            coordf.write(str(row['y'])+'\n')
        if 1:#if row['bustype'] == 'SWING':
            # Connect gld swing bus to the source bus, 115-kV is arbitrary choice
            subname = str(row['subtranid']).replace(" ","_")
            sourcef.write('new line.source_'+ subname + ' phases=3 bus1=rootbus bus2=' + subname + ' switch=y\n')
  sourcef.close()

  # -----------------------------------------------------------------------------
  # Catalog File
  # -----------------------------------------------------------------------------
  catf = open(outpath + 'CatalogStub.dss', 'w')
  print ('// spacings', file=catf)
  for s in spacings:
    print ('new linespacing.' + s + ' nconds=' + str (ncond) + ' nphases=' + str(phqty) + ' units=ft', file=catf)
  print ('// wires', file=catf)
  for s in wires:
    print ('new wiredata.' + s, file=catf)
  print ('// cncables', file=catf)
  for s in cncables:
    print ('new cndata.' + s, file=catf)
  print ('// tscables', file=catf)
  for s in tscables:
    print ('new tsdata.' + s, file=catf)
  print ('// xfmrcodes', file=catf)
  for s in xfmrcodes:
    print ('new xfmrcode.' + s, file=catf)
  catf.close()

  # -----------------------------------------------------------------------------
  # Master File
  # -----------------------------------------------------------------------------
  print('Creating master file.')
  masterf = open(outpath + 'Master.dss', 'w')
  masterf.write('clear\n')
  masterf.write('redirect VSource.dss\n')
  masterf.write('redirect Catalog.dss\n')
  masterf.write('redirect Lines.dss\n')
  masterf.write('redirect Transformers.dss\n')
  masterf.write('redirect Loads.dss\n')
  masterf.write('redirect Capacitors.dss\n')
  masterf.write('redirect LargeCust.dss\n')
  masterf.write('redirect Breakers.dss\n')
  masterf.write('redirect Reclosers.dss\n')
  masterf.write('redirect Sectionalizers.dss\n')
  masterf.write('redirect Fuses.dss\n')
  masterf.write('redirect Switches.dss\n')
  masterf.write('redirect SwitchGears.dss\n')
  masterf.write('redirect CustomEdits.dss\n')
  masterf.write('Set VoltageBases=' + str(BaseVoltages) + '\n')
  masterf.write('calcv\n')
  masterf.write('setloadandgenkv\n')
  masterf.write('buscoords Buscoords.csv\n')
  masterf.write('redirect InitialConditions.dss\n')
  masterf.close()

  EditFile = outpath + 'CustomEdits.dss'
  if not os.path.exists(EditFile):
    ep = open (EditFile, 'w')
    print ("""
// This is included after the feeder backbone has been created, and before calculating voltage bases in OpenDSS. 
// You can start with an empty file. 
// Typical contents include control and protection settings, parameter adjustments, and creation of DER for study. 
// This file is not over-written if you run the converter again.""", file=ep)
    ep.close()
  ICFile = outpath + 'InitialConditions.dss'
  if not os.path.exists(ICFile):
    icp = open (ICFile, 'w')
    print ("""
// the last file included; may have load allocation, load scaling, switching operations and a solve command.
// You can start with an empty file. 
// This file is not over-written if you run the converter again.""", file=icp)
    icp.close()

def usage():
    print("usage: python mdb2dss.py <full path to JSON configuration>")

if __name__ == '__main__':
    if len(sys.argv) != 2:
        usage()
        sys.exit()
    lp = open (sys.argv[1]).read()
    ConvertMDB (json.loads(lp))