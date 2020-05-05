# Copyright (C) 2017-2020 Battelle Memorial Institute
# file: Cyme2DSS.py
"""Converts a CYMDIST self-contained study file, with SXST extension, to OpenDSS.

TBD.

References:
    `CEC/PG&E converter <https://github.com/gridlab-d/Taxonomy_Feeders/tree/master/PGE_Models>`_

Public Functions:
    :populate_feeder: processes one GridLAB-D input file

Todo:
    * rationalize NormalStatus, ConnectionStatus, ClosedPhase

"""

import csv 
import math
import xml.etree.ElementTree as ET
import json
import sys
import os

# to make DSS names
intab =  "[]{}()|,.=~/!?\'$ "
outtab = "-----------------"
dsstab = str.maketrans(intab, outtab)
# then use key.translate(dsstab)

# the catalog tables need to be global; some are written only if used
OHConductorTable = {}
LineSpacingTable = {}
LineConfigTable = {}
CableConfigTable = {}
XfmrConfigTable = {}
FuseConfigTable = {}
RegConfigTable = {}
ECGTable = {}
PhotovoltaicTable = {}
BESSTable = {}
MatrixTable = {}

# coordinate table is also global, because feeder segments may insert midpoints
NodeXYTable = {}

# source table is global so it can be written near the top of the master DSS file
SourceTable = {}

def Connected (child):
    s = child.find('ConnectionStatus').text 
    if s == 'Connected':
        return 'yes'
    elif s == 'Bypassed':
        return 'bypassed'
    return 'no'

def CollectLoadPQ (child):
    PA = 0
    PB = 0
    PC = 0
    QA = 0
    QB = 0
    QC = 0
    xstr = 'CustomerLoads/CustomerLoad/CustomerLoadModels/CustomerLoadModel/CustomerLoadValues/CustomerLoadValue'
    for CustomerLoadValue in child.findall(xstr):
        phs = CustomerLoadValue.find('Phase').text
        for LoadValue in CustomerLoadValue.findall('LoadValue'):
            if LoadValue.attrib['Type'] == 'LoadValueKW_KVAR':
                P = float(LoadValue.find('KW').text)
                Q = float(LoadValue.find('KVAR').text)
            elif LoadValue.attrib['Type'] == 'LoadValueKW_PF':
                P = float(LoadValue.find('KW').text)
                PF = float(LoadValue.find('PF').text)/100
                Q = P * math.tan(math.acos(PF))
            elif LoadValue.attrib['Type'] == 'LoadValueKVA_PF':
                KVA = float(LoadValue.find('KVA').text)
                PF = float(LoadValue.find('PF').text)/100
                P = KVA * PF
                Q = P * math.tan(math.acos(PF))
            nphs = len(phs)
            P /= nphs
            Q /= nphs
            if phs.find ('A') >= 0:
                PA += P
                QA += Q
            if phs.find ('B') >= 0:
                PB += P
                QB += Q
            if phs.find ('C') >= 0:
                PC += P
                QC += Q
    return [PA*LoadScale,QA*LoadScale,PB*LoadScale,QB*LoadScale,PC*LoadScale,QC*LoadScale]

def CreateWireData(f,Name,row):
    if row[0] <= 0.00001 or row[2] <= 0.00001:
        f.write('// ')
    f.write('new wiredata.' + Name)
    f.write(' rac=' + '{:.5f}'.format(row[0]))
    f.write(' gmrac=' + '{:.5f}'.format(row[1]))
    f.write(' diam=' + '{:.5f}'.format(row[2]))
    f.write(' normamps=' + str(row[3]))
    f.write(' runits=km radunits=cm gmrunits=cm\n')
    return

def CreateLineSpacing(f,Name,row):
    f.write('new linespacing.' + Name)
    f.write(' nconds=' + str(row[0]))
    f.write(' nphases=' + str(row[1]))
    f.write(' x=' + row[2])
    f.write(' h=' + row[3])
    f.write(' units=m\n')
    return

# MatrixTable[EquipmentID] = [nphases,Raa,Rbb,Rcc,Xaa,Xbb,Xcc,Baa,Bbb,Bcc,
#                             Rab,Rbc,Rca,Xab,Xbc,Xca,Bab,Bbc,Bca,NominalRating]
def CreateMatrix(f,Name,row):
    if int(row[20]) < 1:
        return
    n = row[0]
    Raa = row[1]
    Rbb = row[2]
    Rcc = row[3]
    Xaa = row[4]
    Xbb = row[5]
    Xcc = row[6]
    Caa = row[7] / 0.377 # uS to nF
    Cbb = row[8] / 0.377
    Ccc = row[9] / 0.377
    Rab = row[10]
    Rbc = row[11]
    Rca = row[12]
    Xab = row[13]
    Xbc = row[14]
    Xca = row[15]
    Cab = row[16] / 0.377
    Cbc = row[17] / 0.377
    Cca = row[18] / 0.377
    f.write('new linecode.' + Name)
    f.write(' nphases=' + str(n))
    # TODO - determine how they define line codes for A vs. B vs. C, or AB vs. BC vs. CA
    if n == 1:
        f.write (' rmatrix=[{:.4f}]'.format(Raa))
        f.write (' xmatrix=[{:.4f}]'.format(Xaa))
        f.write (' cmatrix=[{:.4f}]'.format(Caa))
    elif n == 2:
        f.write (' rmatrix=[{:.4f}|{:.4f} {:.4f}]'.format(Raa, Rab, Rbb))
        f.write (' xmatrix=[{:.4f}|{:.4f} {:.4f}]'.format(Xaa, Xab, Xbb))
        f.write (' cmatrix=[{:.4f}|{:.4f} {:.4f}]'.format(Caa, Cab, Cbb))
    else:
        f.write (' rmatrix=[{:.4f}|{:.4f} {:.4f}|{:.4f} {:.4f} {:.4f}]'.format(Raa, Rab, Rbb, Rca, Rbc, Rcc))
        f.write (' xmatrix=[{:.4f}|{:.4f} {:.4f}|{:.4f} {:.4f} {:.4f}]'.format(Xaa, Xab, Xbb, Xca, Xbc, Xcc))
        f.write (' cmatrix=[{:.4f}|{:.4f} {:.4f}|{:.4f} {:.4f} {:.4f}]'.format(Caa, Cab, Cbb, Cca, Cbc, Ccc))
    f.write(' normamps=' + str(row[19]))
    f.write(' units=' + CYMELineCodeUnit + '\n')
    return

# ConfigTable[EquipmentID] = [nphases,r1,x1,r0,x0,b1,b0,NominalRating,use]
# Note: CYMDIST appears to use Zs=(Z0+2Z1)/3 and Zm=(Z0-Z1)/3 for 1-phase and 2-phase lines
def CreateLineCode(f,Name,row):
    if int(row[8]) < 1:
        return
    n = row[0]
    f.write('new linecode.' + Name)
    f.write(' nphases=' + str(row[0]))
    if n == 1:
        rs = (row[3] + 2.0 * row[1]) / 3.0
        xs = (row[4] + 2.0 * row[2]) / 3.0
        cs = (row[6] + 2.0 * row[5]) / 3.0 / 0.377  # uS/length ==> nF/length
#        cs = 0.0
        f.write(' rmatrix=[' + '{:.4f}'.format(rs) + '] xmatrix=[' + '{:.4f}'.format(xs) + ']')
        f.write(' cmatrix=[' + '{:.4f}'.format(cs) + ']')
    elif n == 2:
        rs = (row[3] + 2.0 * row[1]) / 3.0
        xs = (row[4] + 2.0 * row[2]) / 3.0
        cs = (row[6] + 2.0 * row[5]) / 3.0 / 0.377  # uS/length ==> nF/length
        rm = (row[3] - row[1]) / 3.0
        xm = (row[4] - row[2]) / 3.0
        cm = (row[6] - row[5]) / 3.0 / 0.377  # uS/length ==> nF/length
#        cs = 0.0
#        cm = 0.0
        f.write(' rmatrix=[' + '{:.4f}'.format(rs) + '|' + '{:.4f}'.format(rm) + ' ' + '{:.4f}'.format(rs) + ']')
        f.write(' xmatrix=[' + '{:.4f}'.format(xs) + '|' + '{:.4f}'.format(xm) + ' ' + '{:.4f}'.format(xs) + ']')
        f.write(' cmatrix=[' + '{:.4f}'.format(cs) + '|' + '{:.4f}'.format(cm) + ' ' + '{:.4f}'.format(cs) + ']')
    else:
        f.write(' r1=' + '{:.4f}'.format(row[1]))
        f.write(' x1=' + '{:.4f}'.format(row[2]))
        f.write(' r0=' + '{:.4f}'.format(row[3]))
        f.write(' x0=' + '{:.4f}'.format(row[4]))
        f.write(' b1=' + '{:.4f}'.format(row[5]))
        f.write(' b0=' + '{:.4f}'.format(row[6]))
#        f.write(' b1=0')
#        f.write(' b0=0')
    f.write(' normamps=' + str(row[7]))
    f.write(' units=' + CYMELineCodeUnit + '\n')
    return

# don't reference these from OpenDSS because CYME allows three-phase transformers to reference single-phase transformerDBs
def CreateXfmrCode(f,Name,row):
    f.write('// new xfmrcode.' + Name)
    f.write(' phases=' + str(row[0]) + ' windings=2')
    f.write(' kva=' + str(row[1]))
    f.write(' kvs=[' + str(row[2]) + ' ' + str(row[3])+']')
    f.write(' conns=[' + str(row[4]) + ' ' + str(row[5])+']')
    f.write(' xhl=' + '{:.5f}'.format(row[6]))
    f.write(' %loadloss=' + '{:.5f}'.format(row[7]))
    f.write(' %imag=' + '{:.5f}'.format(row[8]))
    f.write(' %noloadloss=' + '{:.5f}'.format(row[9]) + '\n')
    return

def CreateNode(f,Name,NomV,BusType,phase):
    f.write('object node {\n')
    f.write('    name '+Name+';\n')
    f.write('    nominal_voltage '+str(NomV)+';\n')
    f.write('    phases '+phase+';\n')
    if BusType == 'S':
        f.write('    bustype SWING;\n')
    f.write('}\n\n')
    return

def ParseNPhases (phs):
    return len(phs)

def ParseTerminals (phs):
    retval = ''
    if len(phs) < 3:
        p1 = ord(phs[0]) - ord('A') + 1
        retval += '.' + str(p1)
        if len(phs) > 1:
            p2 = ord(phs[1]) - ord('A') + 1
            retval += '.' + str(p2)
    return retval

def ParseLoadTerminals (phs,conn):
    if (conn == 'wye') or (len(phs) == 3):
        retval = ParseTerminals(phs)
    else:
        if len(phs) == 1:
            if phs[0] == 'A':
                retval = '.1.2'
            elif phs[0] == 'B':
                retval = '.2.3'
            else:
                retval = '.3.1'
        if len(phs) == 2:
            if phs == 'AB':
                retval = '.1.2.3'
            elif phs == 'BC':
                retval = '.2.3.1'
            else: # AC
                retval = '.1.3.2'
    return retval

# OHPhaseTable[SectionID] = [LineFromNode,LineToNode,Phase,DeviceLength,LineSpacing,WireA,WireB,WireC,WireN]
def CreateByPhase(f,Name,row):
    nphs = ParseNPhases(row[2])
    phs = ParseTerminals(row[2])
    nwires = 0
    for wire in row[5:9]: # wireA through wireN
        if wire!= 'OH_NONE':
            nwires += 1
    f.write('new line.' + Name)
    f.write(' bus1=' + row[0] + phs)
    f.write(' bus2=' + row[1] + phs)
    f.write(' phases=' + str(nphs))
    if nphs < 3 and (row[4] == 'OH_DEFAULT' or row[4] == 'OH_UNKNOWN'):
        f.write(' spacing=' + row[4] + str(nwires) + str(nphs))
    else:
        f.write(' spacing=' + row[4])
    f.write(' wires=[')
    for wire in row[5:9]: # wireA through wireN
        if wire!= 'OH_NONE':
            f.write(wire + ' ')
    f.write(']')
    f.write(' length=' + '{:.6f}'.format(CYMEtoDSSSection * row[3]))
    f.write(' units=' + DSSSectionUnit + '\n')

# LineConfigTable[EquipmentID] = [nphases,r1,x1,r0,x0,b1,b0,NominalRating,use]
# OHLineTable[SectionID] = [LineFromNode,LineToNode,Phase,DeviceLength,LineConfig]
# CableConfigTable[EquipmentID] = [nphases,r1,x1,r0,x0,b1,b0,NominalRating,use]
# UGLineTable[SectionID] = [LineFromNode,LineToNode,Phase,DeviceLength,LineConfig]

def CreateMatrixLine(f,Name,row,cfg):
    nphs = ParseNPhases(row[2])
    phs = ParseTerminals(row[2])
    f.write('new line.' + Name)
    f.write(' bus1=' + row[0] + phs)
    f.write(' bus2=' + row[1] + phs)
    f.write(' phases=' + str(nphs))
    len = row[3] * CYMEtoDSSSection # miles
    if nphs == cfg[0]:
        f.write(' linecode=' + row[4])
        f.write(' normamps=' + str(cfg[19]))
        f.write(' length=' + '{:.6f}'.format(len))
        f.write(' units=' + DSSSectionUnit + '\n')
    else:
        rs = (cfg[1] + cfg[2] + cfg[3]) * CYMEtoDSSLineCode / 3.0  # creating [ohms/unit] compatible with length
        xs = (cfg[4] + cfg[5] + cfg[6]) * CYMEtoDSSLineCode / 3.0
        bs = (cfg[7] + cfg[8] + cfg[9]) * CYMEtoDSSLineCode / 3.0
        rm = (cfg[10] + cfg[11] + cfg[12]) * CYMEtoDSSLineCode / 3.0
        xm = (cfg[13] + cfg[14] + cfg[15]) * CYMEtoDSSLineCode / 3.0
        bm = (cfg[16] + cfg[17] + cfg[18]) * CYMEtoDSSLineCode / 3.0
        if nphs == 1:
            f.write(' rmatrix=[' + '{:.6f}'.format(rs) + '] xmatrix=[' + '{:.6f}'.format(xs) + ']')
            f.write(' cmatrix=[' + '{:.4f}'.format(cs) + ']')
        elif nphs == 2:
            f.write(' rmatrix=[' + '{:.6f}'.format(rs) + '|' + '{:.6f}'.format(rm) + ' ' + '{:.6f}'.format(rs) + ']')
            f.write(' xmatrix=[' + '{:.6f}'.format(xs) + '|' + '{:.6f}'.format(xm) + ' ' + '{:.6f}'.format(xs) + ']')
            f.write(' cmatrix=[' + '{:.4f}'.format(cs) + '|' + '{:.4f}'.format(cm) + ' ' + '{:.4f}'.format(cs) + ']')
        else:
            print('WARNING: phases > 2 for a line that does not match its linecode:' + Name)
        f.write(' normamps=' + str(cfg[19]))
        f.write(' length=' + '{:.6f}'.format(len))
        f.write(' units=' + DSSSectionUnit)
        f.write(' // linecode=' + row[4] + '\n')
    return

def CreateLine(f,Name,row,cfg):
    nphs = ParseNPhases(row[2])
    phs = ParseTerminals(row[2])
    f.write('new line.' + Name)
    f.write(' bus1=' + row[0] + phs)
    f.write(' bus2=' + row[1] + phs)
    f.write(' phases=' + str(nphs))
    len = row[3] * CYMEtoDSSSection # miles
    if nphs == cfg[0]:
        f.write(' linecode=' + row[4])
        f.write(' normamps=' + str(cfg[7]))
        f.write(' length=' + '{:.6f}'.format(len))
        f.write(' units=' + DSSSectionUnit + '\n')
    else:
        r1 = cfg[1] * CYMEtoDSSLineCode  # creating [ohms/unit] compatible with length
        x1 = cfg[2] * CYMEtoDSSLineCode
        r0 = cfg[3] * CYMEtoDSSLineCode
        x0 = cfg[4] * CYMEtoDSSLineCode
        b1 = cfg[5] * CYMEtoDSSLineCode
        b0 = cfg[6] * CYMEtoDSSLineCode
        if nphs == 1:
            rs = (r0 + r1 + r1) / 3.0
            xs = (x0 + x1 + x1) / 3.0
            cs = (b0 + b1 + b1) / 3.0 / 0.377 # uS ==> nF
#            cs = 0.0
            f.write(' rmatrix=[' + '{:.6f}'.format(rs) + '] xmatrix=[' + '{:.6f}'.format(xs) + ']')
            f.write(' cmatrix=[' + '{:.4f}'.format(cs) + ']')
        elif nphs == 2:
            rs = (r0 + r1 + r1) / 3.0
            xs = (x0 + x1 + x1) / 3.0
            cs = (b0 + b1 + b1) / 3.0 / 0.377 # uS ==> nF
            rm = (r0 - r1) / 3.0
            xm = (x0 - x1) / 3.0
            cm = (b0 - b1) / 3.0 / 0.377
#            cs = 0.0
#            cm = 0.0
            f.write(' rmatrix=[' + '{:.6f}'.format(rs) + '|' + '{:.6f}'.format(rm) + ' ' + '{:.6f}'.format(rs) + ']')
            f.write(' xmatrix=[' + '{:.6f}'.format(xs) + '|' + '{:.6f}'.format(xm) + ' ' + '{:.6f}'.format(xs) + ']')
            f.write(' cmatrix=[' + '{:.4f}'.format(cs) + '|' + '{:.4f}'.format(cm) + ' ' + '{:.4f}'.format(cs) + ']')
        else:
            print('WARNING: phases > 2 for a line that does not match its linecode:' + Name)
        f.write(' normamps=' + str(cfg[7]))
        f.write(' length=' + '{:.6f}'.format(len))
        f.write(' units=' + DSSSectionUnit)
        f.write(' // linecode=' + row[4] + '\n')
    return

def CreateSwitch(f,Name,row):
    nphs = ParseNPhases(row[2])
    phs = ParseTerminals(row[2])
    if Name == '146': # PATCH for 101L8 naming conflict
        Name = 'Sw_146'
    f.write('new line.' + Name)
    f.write(' bus1=' + row[0] + phs)
    f.write(' bus2=' + row[1] + phs)
    f.write(' phases=' + str(nphs))
    f.write(' switch=yes')
    f.write(' // ' + row[4] + '\n')
    if row[3] == 'Closed':
        f.write('  close line.' + Name + ' 1\n')
    else:
        f.write('  open line.' + Name + ' 1\n')
    return

def CreateRecloser(f,Name,row):
    nphs = ParseNPhases(row[2])
    phs = ParseTerminals(row[2])
    f.write('new line.' + Name)
    f.write(' bus1=' + row[0] + phs)
    f.write(' bus2=' + row[1] + phs)
    f.write(' phases=' + str(nphs))
    f.write(' switch=yes')
    f.write(' // ' + row[4] + '\n')
    f.write('new recloser.' + Name)
    f.write(' monitoredobj=line.' + Name)
    f.write(' monitoredterm=' + str(row[5]))
    f.write(' phasetrip=1000\n')
    return

def CreateCapacitor(f,Name,row):
    nphs = ParseNPhases(row[1])
    phs = ParseLoadTerminals(row[1],row[2])
    f.write('new capacitor.' + Name)
    f.write(' phases=' + str(nphs))
    if (row[2] == 'delta') and (nphs == 1): # workaround for OpenDSS bug?
        if row[1] == 'A':
            phs1 = '.1'
            phs2 = '.2'
        elif row[1] == 'B':
            phs1 = '.2'
            phs2 = '.3'
        else:
            phs1 = '.1'
            phs2 = '.3'
        f.write(' bus1=' + row[0] + phs1)
        f.write(' bus2=' + row[0] + phs2)
    else:
        f.write(' bus1=' + row[0] + phs)
        f.write(' conn=' + row[2])
    f.write(' kv=' + '{:.3f}'.format(row[3]))
    f.write(' kvar=' + str(row[4]))
    f.write(' enabled=' + row[5] + '\n')
#    if row[6] == 'Closed':
#        f.write('  close capacitor.' + Name + ' 1\n')
#    else:
#        f.write('  open capacitor.' + Name + ' 1\n')
    return

def WyePhaseToDeltaPhase (phs):
    if phs == '.1':
        return '.1.2'
    elif phs == '.2':
        return '.2.3'
    return '.1.3'

def DeltaPhaseToWyePhase (phs):
    if phs == '.1.2':
        return '.1'
    elif phs == '.2.3':
        return '.2'
    return '.3'

# TransformerPhaseTable[DeviceNumber] = [XfmrFromNode,XfmrToNode,Phase,XfmrCode1,XfmrCode2,XfmrCode3,enabled]
# XfmrConfigTable[EquipmentID] = [phases,kva,kv1,kv2,conn1,conn2,xhl,pctll,pctimag,pctnll]
def WriteOneTransformerPhase(f,Name,bus1,bus2,cfg,enabled):
    kva = cfg[1]
    kv1 = cfg[2]
    kv2 = cfg[3]
    f.write('new transformer.' + Name)
    f.write(' buses=(' + bus1 + ',' + bus2 + ')')
    f.write(' phases=1 windings=2')
    f.write(' kvas=[{:.2f} {:.2f}]'.format(kva,kva))
    f.write(' kvs=[' + "{:.3f}".format(kv1) + ' ' + "{:.3f}".format(kv2)+']\n')
    f.write('~ conns=[wye wye]')
    f.write(' xhl=' + "{:.3f}".format(cfg[6]))
    f.write(' %loadloss=' + "{:.4f}".format(cfg[7]))
    f.write(' %imag=' + "{:.4f}".format(cfg[8]))
    f.write(' %noloadloss=' + "{:.4f}".format(cfg[9]) + '\n')
    f.write(' enabled=' + enabled + '\n')
    return

def CreateTransformerPhase(f,Name,row,cfgtable):
    bus1 = row[0]
    bus2 = row[1]
    XfmrCode1 = row[3]
    XfmrCode2 = row[4]
    XfmrCode3 = row[5]
    enabled = row[6]
    if XfmrCode1 is not None:
        WriteOneTransformerPhase (f, Name+'A', bus1+'.1', bus2+'.1', cfgtable[XfmrCode1], enabled)
    if XfmrCode2 is not None:
        WriteOneTransformerPhase (f, Name+'B', bus1+'.2', bus2+'.2', cfgtable[XfmrCode2], enabled)
    if XfmrCode3 is not None:
        WriteOneTransformerPhase (f, Name+'C', bus1+'.3', bus2+'.3', cfgtable[XfmrCode3], enabled)
    return

# TransformerTable[DeviceNumber] = [XfmrFromNode,XfmrToNode,Phase,XfmrCode,tap1,tap2,enabled]
# XfmrConfigTable[EquipmentID] = [phases,kva,kv1,kv2,conn1,conn2,xhl,pctll,pctimag,pctnll]
# for single-phase Dy transformers, pattern-match phases AC->C, BC->B, AB->A
def CreateTransformer(f,Name,row,cfg):
    nphs = ParseNPhases(row[2])
    phs = ParseTerminals(row[2])
    kva = cfg[1]
    kv1 = cfg[2]
    kv2 = cfg[3]
    con1 = cfg[4]
    con2 = cfg[5]
    if row[6] == 'bypassed':
        f.write ('new line.Transformer_' + Name + ' phases=' + str(nphs) + ' bus1=' + row[0] + phs + ' bus2=' + row[1] + phs)
        f.write (' switch=yes // bypassing ' + Name + '\n')
        return
    if (nphs == 3) and (cfg[0] == 1):
        kva *= 3.0
    elif (cfg[0] == 1):
        if con1 == 'wye':
            kv1 /= math.sqrt(3.0)
        if con2 == 'wye':
            kv2 /= math.sqrt(3.0)
    if (nphs == 1): # line-to-neutral phasing for wye winding
        if con1 == 'wye':
            phs1 = phs
        else:
            phs1 = WyePhaseToDeltaPhase(phs)
        if con2 == 'wye':
            phs2 = phs
        else:
            phs2 = WyePhaseToDeltaPhase(phs)
    elif (nphs == 2): # line-to-line phasing for delta winding
        nphs = 1
        if con1 == 'wye':
            phs1 = DeltaPhaseToWyePhase(phs)
        else:
            phs1 = phs
        if con2 == 'wye':
            phs2 = DeltaPhaseToWyePhase(phs)
        else:
            phs2 = phs
    else:
        phs1 = phs
        phs2 = phs
    f.write('new transformer.' + Name)
    f.write(' buses=(' + row[0] + phs1 + ',' + row[1] + phs2 + ')')
    f.write(' phases=' + str(nphs) + ' windings=2')
    f.write(' kvas=[{:.2f} {:.2f}]'.format(kva,kva))
    f.write(' kvs=[' + "{:.3f}".format(kv1) + ' ' + "{:.3f}".format(kv2)+']\n')
    f.write('~ conns=[' + con1 + ' ' + con2 + ']')
    f.write(' xhl=' + "{:.3f}".format(cfg[6]))
    f.write(' %loadloss=' + "{:.4f}".format(cfg[7]))
    f.write(' %imag=' + "{:.4f}".format(cfg[8]))
    f.write(' %noloadloss=' + "{:.4f}".format(cfg[9]) + '\n')
    f.write('~ taps=(' + "{:.5f}".format(row[4]) + ',' + "{:.5f}".format(row[5]) + ')')
    f.write(' enabled=' + row[6] + ' // xfmrcode=' + row[3] + '\n')
    return

# RegulatorTable[DeviceNumber] = [XfmrFromNode,XfmrToNode,phs,monPhs,conn,kva,kv,vreg,bw,pt,ct,enabled]
def CreateRegulator(f,Name,row):
    nPhs = ParseNPhases(row[2])  # transformer phases
    nMon = ParseNPhases(row[3])  # monitored phases
    if nPhs == 3:
        if row[4] == 'delta':
            phases = ['.1.2', '.2.3', '.3.1']
        else:
            phases = ['.1', '.2', '.3']
    elif nPhs == 2:
        if row[4] == 'delta':  # TODO - only handling single-phase delta
            phases = ['.1.2']
            if 'A' not in row[2]:
                phases = ['.2.3']
            elif 'B' not in row[2]:
                phases = ['.1.3']
        else:
            phases = ['.1', '.2']
            if 'A' not in row[2]:
                phases = ['.2', '.3']
            elif 'B' not in row[2]:
                phases = ['.1', '.3']
    else:
        p1 = ord(row[2][0]) - ord('A') + 1
        if row[3] == 'delta':
            p2 = ord(row[2][1]) - ord('A') + 1
            phases = ['.' + str(p1) + '.' + str(p2)]
        else:
            phases = ['.' + str(p1)]
#    phases = ['.1', '.2', '.3'] # workaround for Nantucket - TODO - single-phase regulator embedded in three-phase line
    if nMon < nPhs: # ganged
        kvLL = math.sqrt(3) * row[6]
        allPhs = ParseTerminals (row[2])
        kva3 = 3*row[5]
        f.write('new transformer.{:s} phases={:d} windings=2 buses=({:s},{:s})'.format(Name, nPhs, row[0]+allPhs, row[1]+allPhs))
        f.write(' kvas=({:.2f},{:.2f}) kvs=({:.3f},{:.3f}) conns=({:s},{:s}) xhl=1 enabled={:s}\n'.format(kva3,kva3,kvLL,kvLL,row[4],row[4],row[11]))
        f.write('new regcontrol.{:s} transformer={:s} winding=2 vreg={:.2f}'.format(Name,Name,row[7]))
        f.write(' band={:.2f} ptratio={:.1f} ctprim={:.1f} enabled={:s}\n'.format(row[8],row[9],row[10],row[11]))
        f.write('\n')
    else: # indepdendent
        for phs in phases:
            ltr = chr(ord('a') + int(phs[1]) - 1)
            f.write('new transformer.' + Name + ltr)
            f.write(' phases=1 windings=2')
            f.write(' buses=(' + row[0] + phs + ',' + row[1] + phs + ')')
            f.write(' kvas=(' + str(row[5]) + ',' + str(row[5]) + ')')
            f.write(' kvs=(' + str(row[6]) + ',' + str(row[6]) + ')')
            f.write(' conns=(' + row[4] + ',' + row[4] + ')')
            f.write(' xhl=1')
            f.write(' enabled=' + row[11] + '\n')
            f.write('new regcontrol.' + Name + ltr)
            f.write(' transformer=' + Name + ltr)
            f.write(' winding=2')
            f.write(' vreg=' + str(row[7]))
            f.write(' band=' + str(row[8]))
            f.write(' ptratio=' + str(row[9]))
            f.write(' ctprim=' + str(row[10]))
            f.write(' enabled=' + row[11] + '\n')
        f.write('\n')
    return

#[DeviceNumber,End,DeviceID,switch,enabled,amps,curve,SectionID,SwtFromNode,SwtToNode,Phase]
def CreateFuse(f,Name,row):
    nphs = ParseNPhases(row[10])
    phs = ParseTerminals(row[10])
    f.write('new line.' + Name)
    f.write(' bus1=' + row[8] + phs)
    f.write(' bus2=' + row[9] + phs)
    f.write(' phases=' + str(nphs))
    f.write(' switch=yes')
    f.write(' // ' + row[4] + '\n')
    f.write('new fuse.' + Name)
    f.write(" monitoredobj=line." + Name) # row[7])
    f.write(" monitoredterm=" + str(row[1]))
    f.write(" ratedcurrent=" + str(row[5]))
    f.write(" fusecurve=" + row[6])
    f.write(" // " + row[2] + "\n")
    return
# this is for a fuse within a line section
#    f.write("new fuse." + Name)
##    f.write(" monitoredobj=line." + row[0])  # worked for SCE
#    f.write(" monitoredobj=line." + row[7])
#    f.write(" monitoredterm=" + str(row[1]))
#    f.write(" ratedcurrent=" + str(row[5]))
#    f.write(" fusecurve=" + row[6])
#    f.write(" // " + row[2] + "\n")
#    return

def CreateSwtControl(f,Name,row):
    f.write("// new swtcontrol." + Name)
    f.write(" switchedobj=line." + row[0])
    f.write(" switchedterm=" + str(row[1]))
    f.write(" action=(" + row[3] + ")")
    f.write(" enabled=" + row[4])
    f.write(" // " + row[2] + "\n")
    return

# DGTable[DeviceNumber] = [GenBus,PhasesConnected,ActiveGen,pf,kva,pufault]
def CreateGenerator(f,Name,row):
    nphs = ParseNPhases(row[1])
    phs = ParseTerminals(row[1])
    kv = DefaultBaseVoltage
    if nphs < 3:
        kv /= math.sqrt(3.0)
    f.write('new pvsystem.' + Name)
#    f.write('new generator.' + Name)
    f.write(' phases=' + str(nphs))
    f.write(' bus1=' + row[0] + phs)
    f.write(' conn=wye kv=' + '{:.3f}'.format(kv))
    f.write(' pmpp=' + '{:.2f}'.format(row[2]))
    f.write(' kva=' + '{:.2f}'.format(row[4]))
#    f.write(' kw=' + '{:.2f}'.format(row[2]))
    f.write(' pf=' + '{:.4f}'.format(row[3]) + '\n')
    return

def WriteOneLoad (f, Name, Bus, P, Q, kV, cls, conn):
    global TotalP
    global TotalQ
    TotalP += P
    TotalQ += Q
    f.write('new load.' + Name)
    f.write(' bus1=' + Bus)
    f.write(' phases=1 model={:1} conn={:s}'.format (LoadModel, conn)) # 1 = PQ, 2 = Z
    f.write(' kv=' + "{:.3f}".format(kV))
    f.write(' kw=' + "{:.3f}".format(P))
    f.write(' kvar=' + "{:.3f}".format(Q))
    f.write(' class=' + str(cls) + '\n')
    return

def WriteBalancedLoad (f, Name, Bus, P, Q, kV, cls, conn):
    global TotalP
    global TotalQ
    if P > 0.0:
        TotalP += P
        TotalQ += Q
        f.write('new load.' + Name)
        f.write(' bus1=' + Bus)
        f.write(' phases=3 model={:1} conn={:s}'.format (LoadModel, conn)) # 1 = PQ, 2 = Z
        f.write(' kv=' + "{:.3f}".format(kV))
        f.write(' kw=' + "{:.3f}".format(P))
        f.write(' kvar=' + "{:.3f}".format(Q))
        f.write(' class=' + str(cls) + '\n')
    return

# LoadTable[DeviceNumber] = [LoadNodeID,Phase,DefaultBaseVoltage,Sload,class]
# Sload = [pa,qa,pb,qb,pc,qc]
# write them all as single-phase line-to-neutral
def CreateLoad(f,Name,row):
    bus = row[0]
    phs = row[1]
    kv = row[2]
    Pa = row[3][0]
    Qa = row[3][1]
    Pb = row[3][2]
    Qb = row[3][3]
    Pc = row[3][4]
    Qc = row[3][5]
    cls = row[4]
    if (len(phs) == 2 and 1 == 2): # single-phase delta connection, bypass for SCE (TODO)
        Pa += Pb + Pc
        Qa += Qb + Qc
        kv = DefaultBaseVoltage
        if phs == 'AB':
            WriteOneLoad (f, Name + '_ab', bus + '.1.2', Pa, Qa, kv, cls, 'delta')
        elif phs == 'BC':
            WriteOneLoad (f, Name + '_bc', bus + '.2.3', Pa, Qa, kv, cls, 'delta')
        else:
            WriteOneLoad (f, Name + '_ac', bus + '.1.3', Pa, Qa, kv, cls, 'delta')
    elif (len(phs) == 3): # for SCE, we "know" the load is balanced, but wye or delta?
        WriteBalancedLoad (f, Name, bus, Pa+Pb+Pc, Qa+Qb+Qc, DefaultBaseVoltage, cls, 'wye')
    else:  # wye connection, written independently
        kv /= math.sqrt(3.0)
        if ((Pa != 0) or (Qa != 0)) and (phs.find('A') >= 0):
            WriteOneLoad (f, Name + '_a', bus + '.1', Pa, Qa, kv, cls, 'wye')
        if ((Pb != 0) or (Qb != 0)) and (phs.find('B') >= 0):
            WriteOneLoad (f, Name + '_b', bus + '.2', Pb, Qb, kv, cls, 'wye')
        if ((Pc != 0) or (Qc != 0)) and (phs.find('C') >= 0):
            WriteOneLoad (f, Name + '_c', bus + '.3', Pc, Qc, kv, cls, 'wye')
    return

def BuildCatalog(root):
    # Extract info for OH Conductor, create a dictionary
    print ('Parsing the feeder model...')
    for ConductorDB in root.findall('./Equipments/Equipments/EquipmentDBs/ConductorDB'):
        EquipmentID = 'OH_'+ConductorDB.find('EquipmentID').text.translate(dsstab)
        if CYMEVersion <= 7.1:
            FirstResistance = float(ConductorDB.find('FirstResistance').text)    # ohm/km
            SecondResistance = float(ConductorDB.find('SecondResistance').text)  # ohm/km
        else:
            FirstResistance = float(ConductorDB.find('ResistanceAC1').text)  
            SecondResistance = float(ConductorDB.find('ResistanceAC2').text) 
        GMR = float(ConductorDB.find('GMR').text)                            # cm
        OutsideDiameter = float(ConductorDB.find('OutsideDiameter').text)    # cm
        NominalRating = float(ConductorDB.find('NominalRating').text)
        OHConductorTable[EquipmentID] = [SecondResistance,GMR,OutsideDiameter,NominalRating] 

    # Extract info for line spacing objects, create a dictionary
    for SpacingDB in root.findall('./Equipments/Equipments/EquipmentDBs/OverheadSpacingOfConductorDB'):
        EquipmentID = 'OH_'+SpacingDB.find('EquipmentID').text.translate(dsstab)
        for GeometricalArrangement in SpacingDB.findall('GeometricalArrangement'):
            # all length units are m
            nphases = int(GeometricalArrangement.find('NbPhasesPerCircuit').text)
            nneutrals = int(GeometricalArrangement.find('NbNeutrals').text)
            nconds = nphases + nneutrals
            x = float(GeometricalArrangement.find('PositionOfConductor1_C1').find('X').text)
            h = float(GeometricalArrangement.find('PositionOfConductor1_C1').find('Y').text)
            xstr = '[' + str(x)
            hstr = '[' + str(h)
            if nphases>1:
                x = float(GeometricalArrangement.find('PositionOfConductor2_C1').find('X').text)
                h = float(GeometricalArrangement.find('PositionOfConductor2_C1').find('Y').text)
                xstr += ' ' + str(x)
                hstr += ' ' + str(h)
            if nphases>2:
                x = float(GeometricalArrangement.find('PositionOfConductor3_C1').find('X').text)
                h = float(GeometricalArrangement.find('PositionOfConductor3_C1').find('Y').text)
                xstr += ' ' + str(x)
                hstr += ' ' + str(h)
            if nneutrals>0:
                x = float(GeometricalArrangement.find('PositionOfConductorN1').find('X').text)
                h = float(GeometricalArrangement.find('PositionOfConductorN1').find('Y').text)
                xstr += ' ' + str(x)
                hstr += ' ' + str(h)
            xstr += ']'
            hstr += ']'
            LineSpacingTable[EquipmentID] = [nconds,nphases,xstr,hstr]

        for AverageGeometricalArrangement in SpacingDB.findall('AverageGeometricalArrangement'):
            gmd = float(AverageGeometricalArrangement.find('GMDPhaseToPhase').text)
            hp = AverageGeometricalArrangement.find('AveragePhaseConductorHeight').text
            hn = AverageGeometricalArrangement.find('AverageNeutralConductorHeight').text
            if EquipmentID.find('-1PH-') >= 0:
                nphases = 1
                xstr = '[0'
                hstr = '[' + hp
            elif EquipmentID.find('-2PH-') >= 0:
                nphases = 2
                gmd *= 0.5
                xstr = '[' + '{:.5f}'.format(-gmd) + ' ' + '{:.5f}'.format(gmd)
                hstr = '[' + hp + ' ' + hp
            else:
                nphases = 3
                gmd /= math.pow(2.0,1/3.0)
                xstr = '[' + '{:.5f}'.format(-gmd) + ' 0 ' + '{:.5f}'.format(gmd)
                hstr = '[' + hp + ' ' + hp + ' ' + hp
            nconds = nphases + 1
            xstr += ' 0]'
            hstr += ' ' + hn + ']'

            LineSpacingTable[EquipmentID] = [nconds,nphases,xstr,hstr]

    # Extract info for OH line config objects, create a dictionary
    for OverheadLineDB in root.findall('./Equipments/Equipments/EquipmentDBs/OverheadLineDB'):
        EquipmentID = 'OH_'+OverheadLineDB.find('EquipmentID').text.translate(dsstab)
        SpacingID = OverheadLineDB.find('ConductorSpacingID').text
        if SpacingID.find('-1PH-') >= 0:
            nphases = 1
        elif SpacingID.find('-2PH-') >= 0:
            nphases = 2
        else:
            nphases = 3
        r1 = float(OverheadLineDB.find('PositiveSequenceResistance').text)
        x1 = float(OverheadLineDB.find('PositiveSequenceReactance').text)
        r0 = float(OverheadLineDB.find('ZeroSequenceResistance').text)
        x0 = float(OverheadLineDB.find('ZeroSequenceReactance').text)
        b1 = float(OverheadLineDB.find('PositiveSequenceShuntSusceptance').text)
        b0 = float(OverheadLineDB.find('ZeroSequenceShuntSusceptance').text)
        NominalRating = float(OverheadLineDB.find('NominalRating').text)
        if r1 <= 0 and x1 <= 0:
            r1 = 0.0001
            x1 = 0.0001
        if r0 <= 0 and x0 <= 0:
            r0 = r1
            x0 = x1
        if b0 <= 0:
            b0 = b1
        LineConfigTable[EquipmentID] = [nphases,r1,x1,r0,x0,b1,b0,NominalRating,0]

    for MatrixDB in root.findall('./Equipments/Equipments/EquipmentDBs/OverheadLineUnbalancedDB'):
        EquipmentID = "ZM_" + MatrixDB.find('EquipmentID').text.translate(dsstab)
        nphases = 3 # TODO - how to find 1-phase and 2-phase matrices; could be the existence of phase conductors
        NominalRating = float(MatrixDB.find('NominalRatingA').text)
        Raa = float(MatrixDB.find('SelfResistanceA').text)
        Rbb = float(MatrixDB.find('SelfResistanceB').text)
        Rcc = float(MatrixDB.find('SelfResistanceC').text)
        Xaa = float(MatrixDB.find('SelfReactanceA').text)
        Xbb = float(MatrixDB.find('SelfReactanceB').text)
        Xcc = float(MatrixDB.find('SelfReactanceC').text)
        Baa = float(MatrixDB.find('ShuntSusceptanceA').text)
        Bbb = float(MatrixDB.find('ShuntSusceptanceB').text)
        Bcc = float(MatrixDB.find('ShuntSusceptanceC').text)
        Rab = float(MatrixDB.find('MutualResistanceAB').text)
        Rbc = float(MatrixDB.find('MutualResistanceBC').text)
        Rca = float(MatrixDB.find('MutualResistanceCA').text)
        Xab = float(MatrixDB.find('MutualReactanceAB').text)
        Xbc = float(MatrixDB.find('MutualReactanceBC').text)
        Xca = float(MatrixDB.find('MutualReactanceCA').text)
        Bab = float(MatrixDB.find('MutualShuntSusceptanceAB').text)
        Bbc = float(MatrixDB.find('MutualShuntSusceptanceBC').text)
        Bca = float(MatrixDB.find('MutualShuntSusceptanceCA').text)
        MatrixTable[EquipmentID] = [nphases,Raa,Rbb,Rcc,Xaa,Xbb,Xcc,Baa,Bbb,Bcc,
                                    Rab,Rbc,Rca,Xab,Xbc,Xca,Bab,Bbc,Bca,NominalRating,0] #position 20 == 1 if used

    for CableDB in root.findall('./Equipments/Equipments/EquipmentDBs/CableDB'):
        EquipmentID = "UG_" + CableDB.find('EquipmentID').text.translate(dsstab)
        nphases = 3 # TODO - how to find 1-phase cables?
        r1 = float(CableDB.find('PositiveSequenceResistance').text)
        x1 = float(CableDB.find('PositiveSequenceReactance').text)
        r0 = float(CableDB.find('ZeroSequenceResistance').text)
        x0 = float(CableDB.find('ZeroSequenceReactance').text)
        b1 = float(CableDB.find('PositiveSequenceShuntSusceptance').text)
        b0 = float(CableDB.find('ZeroSequenceShuntSusceptance').text)
        NominalRating = float(CableDB.find('NominalRating').text)
        if r1 <= 0 and x1 <= 0:
            r1 = 0.0001
            x1 = 0.0001
        if r0 <= 0 and x0 <= 0:
            r0 = r1
            x0 = x1
        if b0 <= 0:
            b0 = b1
        CableConfigTable[EquipmentID] = [nphases,r1,x1,r0,x0,b1,b0,NominalRating,0]

    # Extract Transformer Codes and create a dictionary
    for XfDB in root.findall('./Equipments/Equipments/EquipmentDBs/TransformerDB'):
        EquipmentID = XfDB.find('EquipmentID').text.translate(dsstab)
        kva = float(XfDB.find('NominalRatingKVA').text)
        kv1 = float(XfDB.find('PrimaryVoltage').text) # new TransfoVoltageUnit
        kv2 = float(XfDB.find('SecondaryVoltage').text)
        zhl = float(XfDB.find('PositiveSequenceImpedancePercent').text)
        xr = float(XfDB.find('XRRatio').text)
        if zhl <= 0:
            zhl = 1.0
        if xr <= 0:
            xr = 1.0
        pctnll = float(XfDB.find('NoLoadLossesKW').text)
        pctll = zhl / math.sqrt(xr * xr + 1.0)
        xhl = pctll * xr
        pctnll *= (100.0 / kva)
        pctimag = pctnll * 2 # assumption that magnetizing current can't be zero if there are core losses
        XfType = XfDB.find('TransfoType').text
        XfConn = XfDB.find('TransformerConnection').text
        if XfType.find('SinglePhase') >= 0:
            phases = 1
        else:
            phases = 3
        if XfConn.find('D_') >= 0:
            conn1 = 'delta'
        else:
            conn1 = 'wye'
        if XfConn.find('_D') >= 0:
            conn2 = 'delta'
        else:
            conn2 = 'wye'
    #    if phases == 1:
    #        if conn1 == 'wye':
    #            kv1 /= math.sqrt(3.0)
    #        if conn2 == 'wye':
    #            kv2 /= math.sqrt(3.0)
        XfmrConfigTable[EquipmentID] = [phases,kva,kv1,kv2,conn1,conn2,xhl,pctll,pctimag,pctnll]

    # Extract FusedB and make a dictionary for internal reference; not written to catalog
    for FuseDB in root.findall('./Equipments/Equipments/EquipmentDBs/FuseDB'):
        ID = FuseDB.find('EquipmentID').text.translate(dsstab)
        amps = float(FuseDB.find('FirstRatedCurrent').text)
        if ID.find('KLINK') >= 0:
            curve = 'Klink'
        else:
            curve = 'Tlink'
        FuseConfigTable[ID]= [amps,curve]

    # Extract RegulatorDB and make a dictionary for internal reference; not written to catalog
    for RegDB in root.findall('./Equipments/Equipments/EquipmentDBs/RegulatorDB'):
        ID = RegDB.find('EquipmentID').text.translate(dsstab)
        typ = RegDB.find('Type').text
        kva = float(RegDB.find('RatedKVA').text)
        kvln = float(RegDB.find('RatedKVLN').text)
        boost = float(RegDB.find('MaximumBoost').text)
        buck = float(RegDB.find('MaximumBuck').text)
        if CYMEVersion < 8.0:
            bw = float(RegDB.find('BandWidth').text)
        else:
            bw = float(RegDB.find('ForwardBandwidth').text)
        ct = float(RegDB.find('CTPrimaryRating').text)
        pt = float(RegDB.find('PTRatio').text)
        taps = float(RegDB.find('NumberOfTaps').text)
        reversible = float(RegDB.find('Reversible').text)
        RegConfigTable[ID]= [typ,kva,kvln,boost,buck,bw,ct,pt,taps,reversible]

    for ECGDB in root.findall('./Equipments/Equipments/EquipmentDBs/ElectronicConverterGeneratorDB'):
        EquipmentID = ECGDB.find('EquipmentID').text.translate(dsstab)
        RatedKVA = float(ECGDB.find('RatedKVA').text)
        RatedKVLL = float(ECGDB.find('RatedKVLL').text)
        FaultPU = float(ECGDB.find('FaultContribution').text) / 100
        ECGTable[EquipmentID] = [RatedKVA,RatedKVLL,FaultPU] 

    for PhotovoltaicDB in root.findall('./Equipments/Equipments/EquipmentDBs/PhotovoltaicDB'):
        EquipmentID = PhotovoltaicDB.find('EquipmentID').text.translate(dsstab)
        CellKVA = 0.001 * float(PhotovoltaicDB.find('MPPCurrent').text) * float(PhotovoltaicDB.find('MPPVoltage').text)
        CellFaultPU = float(PhotovoltaicDB.find('SCCurrent').text) / float(PhotovoltaicDB.find('MPPCurrent').text)
        PhotovoltaicTable[EquipmentID] = [CellKVA,CellFaultPU] 

    for BESSDB in root.findall('./Equipments/Equipments/EquipmentDBs/BESSDB'):
        EquipmentID = BESSDB.find('EquipmentID').text.translate(dsstab)
        RatedStorage = float(BESSDB.find('RatedStorageEnergy').text)
        BESSTable[EquipmentID] = [RatedStorage] # max powers, efficiencies, losses also available

def WriteCatalog(filename):
    fcatalog=open(filename,'w')
    #create the line and transformer catalog
    for key in OHConductorTable:
        CreateWireData(fcatalog,key,OHConductorTable[key])
    fcatalog.write('\n')

    # TODO - generalize this, 1st suffix is # of conductors, 2nd suffix is # of phases
    LineSpacingTable['OH_DEFAULT21'] = [2,1,'[0.0 0.2]', '[11.4 8.7]']
    LineSpacingTable['OH_UNKNOWN21'] = [2,1,'[0.0 0.2]', '[11.4 8.7]']
    LineSpacingTable['OH_DEFAULT32'] = [3,2,'[-1.1 1.1 0.2]', '[11.0 11.0 8.7]']
    LineSpacingTable['OH_UNKNOWN32'] = [3,2,'[-1.1 1.1 0.2]', '[11.0 11.0 8.7]']
    LineSpacingTable['OH_DEFAULT11'] = [1,1,'[0.0]', '[11.4]']
    LineSpacingTable['OH_UNKNOWN11'] = [1,1,'[0.0]', '[11.4]']
    LineSpacingTable['OH_DEFAULT22'] = [2,2,'[-1.1 1.1]', '[11.0 11.0]']
    LineSpacingTable['OH_UNKNOWN22'] = [2,2,'[-1.1 1.1]', '[11.0 11.0]']
    for key in LineSpacingTable:
        CreateLineSpacing(fcatalog,key,LineSpacingTable[key])
    fcatalog.write('\n')

    for key in LineConfigTable:
        CreateLineCode(fcatalog,key,LineConfigTable[key])
    fcatalog.write('\n')

    for key in CableConfigTable:
        CreateLineCode(fcatalog,key,CableConfigTable[key])
    fcatalog.write('\n')

    for key in MatrixTable:
        CreateMatrix(fcatalog,key,MatrixTable[key])
    fcatalog.write('\n')

    for key in XfmrConfigTable:
        CreateXfmrCode(fcatalog,key,XfmrConfigTable[key])
    fcatalog.write('\n')

    fcatalog.close()

def BuildInitialCoordinates(root):
    # create a dictionary of node coordinates
    Xmin = sys.float_info.max
    Xmax = -sys.float_info.max
    Ymin = Xmin
    Ymax = Xmax
    for Node in root.findall('./Networks/Network/Nodes/Node'):
        ID = Node.find('NodeID').text.translate(dsstab)
        X = float(Node.find('Connectors').find('Point').find('X').text)
        Y = float(Node.find('Connectors').find('Point').find('Y').text)
        if X < Xmin:
            Xmin = X
        if Y < Ymin:
            Ymin = Y
        if X > Xmax:
            Xmax = X
        if Y > Ymax:
            Ymax = Y
        NodeXYTable[ID] = [X,Y]
    print ('NodeXY X range=[{:f}, {:f}] and Y range =[{:f}, {:f}]'.format(Xmin, Xmax, Ymin, Ymax))

def WriteCoordinates(filename):
    fxy=open(filename,'w')
    for key,row in NodeXYTable.items():
        x=float(row[0])
        y=float(row[1])
        if x <= CoordXmax and x >= CoordXmin and y <= CoordYmax and y >= CoordYmin:
            fxy.write(key + ',' + '{:.6f}'.format(x) + ',' + '{:.6f}'.format(y) + '\n')
    fxy.close()

def WriteFeeder(root, OwnerID, networkfilename, loadfilename):
    # Extract info for sections and lines. create a dictionary
    OHLineTable = {}
    ZMLineTable = {}
    OHPhaseTable = {}
    LoadTable = {}
    UGLineTable = {}
    SwitchTable = {}
    ShuntCapTable = {}
    TransformerTable = {}
    TransformerPhaseTable = {}
    RegulatorTable = {}
    RecloserTable = {}
    FuseTable = {}
    SwtControlTable = {}
    DGTable = {}
    AllDeviceTypes = set()

    # Extract info for sources and create a dictionary - TODO, handling substations vs. Feeders
    #xstr = './Networks/Network/Topos/Topo/Sources/Source'
    xstr = ".//*[NetworkID='" + OwnerID + "']/Sources/Source"
    print(xstr)
    for Source in root.findall(xstr):
        for SourceSetting in Source.findall('SourceSettings'):
            SourceID = SourceSetting.find('SourceID').text.translate(dsstab)
            SourceNodeID = Source.find('SourceNodeID').text.translate(dsstab)
            print ('source ' + SourceID + ' ' + SourceNodeID)
            for ESModels in Source.findall('EquivalentSourceModels'):
                for ESModel in ESModels.findall('EquivalentSourceModel'):
                    for EquivalentSource in ESModel.findall('EquivalentSource'):
                        SourceBaseVoltage = float(EquivalentSource.find('KVLL').text)
                        ActualVoltage = float(EquivalentSource.find('OperatingVoltage1').text)*math.sqrt(3.0)
                        pu = round (ActualVoltage / DefaultBaseVoltage, 4)
                        ang = float(EquivalentSource.find('OperatingAngle1').text)
                        r1 = float(EquivalentSource.find('PositiveSequenceResistance').text)
                        x1 = float(EquivalentSource.find('PositiveSequenceReactance').text)
                        r0 = float(EquivalentSource.find('ZeroSequenceResistance').text)
                        x0 = float(EquivalentSource.find('ZeroSequenceReactance').text)
                        if EquivalentSource.find('ImpedanceUnit').text == 'PU': # 'Ohms':
                            r1 *= Zbase
                            x1 *= Zbase
                            r0 *= Zbase
                            x0 *= Zbase
                        SourceTable[SourceID] = [SourceBaseVoltage,pu,ang,r1,x1,r0,x0,SourceNodeID]

    #xstr = './Networks/Network/Sections/Section'
    #xstr = ".//*[NetworkID='" + OwnerID + "']/Sources/Source"
    xstr = "./Networks/Network/Sections/Section[OwnerID='" + OwnerID + "']"
    print (xstr)
    for Section in root.findall(xstr):
        FromNodeID = Section.find('FromNodeID').text.translate(dsstab)
        ToNodeID = Section.find('ToNodeID').text.translate(dsstab)
        LineFromNode = FromNodeID
        LineToNode = ToNodeID
        SwtFromNode = FromNodeID
        SwtToNode = ToNodeID
        XfmrFromNode = FromNodeID
        XfmrToNode = ToNodeID
        Phase = Section.find('Phase').text
        SectionID = Section.find('SectionID').text.translate(dsstab)
        for Devices in Section.findall('Devices'):
            SectionStatus = 'Closed' # initialize to closed

            # First check for multiple series devices on this section
            SectionLine = 'No'
            SectionSwitch = 'No'
            SectionXfmr = 'No' 
            for child in Devices:
                DeviceType = child.tag
                AllDeviceTypes.add (DeviceType)
                if DeviceType in ('OverheadLine', 'OverheadByPhase', 'Underground'):
                    SectionLine = 'Yes'
                if DeviceType in ('Transformer', 'Regulator'):
                    SectionXfmr = 'Yes'
                if DeviceType in ('Switch', 'Fuse', 'Breaker', 'Recloser'):
                    SectionSwitch = 'Yes'
                    NormalStatus = child.find('NormalStatus').text
                    ClosedPhase = child.find('ClosedPhase').text
                    if ClosedPhase == 'None':
                        SectionStatus = 'Open'

            # if we have two series devices in a section, insert a midpoint node and its coordinates
            for child in Devices:
                DeviceType = child.tag
                AllDeviceTypes.add (DeviceType)
                DeviceNumber = child.find('DeviceNumber').text.translate(dsstab)
                if DeviceType in ('Transformer', 'Regulator') and (SectionLine == 'Yes'):
                    MidNodeID = SectionID + '-xf'
                    print('Xfmr/Reg and Line on', SectionID, 'inserting', MidNodeID)
                    if MidNodeID in NodeXYTable:
                        print ('WARNING: inserting duplicate mid-section node: ' + MidNodeID)
                    x1 = float(NodeXYTable[FromNodeID][0])
                    y1 = float(NodeXYTable[FromNodeID][1])
                    x2 = float(NodeXYTable[ToNodeID][0])
                    y2 = float(NodeXYTable[ToNodeID][1])
                    if child.find('Location').text == 'From': # 'From':
                        x1 = x1 + 0.25 * (x2 - x1)
                        y1 = y1 + 0.25 * (y2 - y1)
                        LineFromNode = MidNodeID
                        XfmrFromNode = FromNodeID
                        XfmrToNode = MidNodeID
                    else:
                        x1 = x1 + 0.75 * (x2 - x1)
                        y1 = y1 + 0.75 * (y2 - y1)
                        LineToNode = MidNodeID
                        XfmrFromNode = MidNodeID
                        XfmrToNode = ToNodeID
                    NodeXYTable[MidNodeID] = [str(x1),str(y1)]
                if DeviceType in ('Switch', 'Breaker', 'Recloser') and (SectionLine == 'Yes'): # not inserting node for 'Fuse'
                    MidNodeID = SectionID + '-swt'
#                    print('Switch and Line on', SectionID, 'inserting', MidNodeID)
                    if MidNodeID in NodeXYTable:
                        print ('WARNING: inserting duplicate mid-section node: ' + MidNodeID)
                    x1 = float(NodeXYTable[FromNodeID][0])
                    y1 = float(NodeXYTable[FromNodeID][1])
                    x2 = float(NodeXYTable[ToNodeID][0])
                    y2 = float(NodeXYTable[ToNodeID][1])
                    if child.find('Location').text == 'From': # 'From':
                        x1 = x1 + 0.25 * (x2 - x1)
                        y1 = y1 + 0.25 * (y2 - y1)
                        LineFromNode = MidNodeID
                        SwtFromNode = FromNodeID
                        SwtToNode = MidNodeID
                    else:
                        x1 = x1 + 0.75 * (x2 - x1)
                        y1 = y1 + 0.75 * (y2 - y1)
                        LineToNode = MidNodeID
                        SwtFromNode = MidNodeID
                        SwtToNode = ToNodeID
                    NodeXYTable[MidNodeID] = [str(x1),str(y1)]

            # the SCE circuit seems to have one device per section, so use DeviceNumber instead of SectionID as key
            # Now go through all the OH and UG lines on this section
            # if Section status is Closed --> account for all OH or UG lines on this section if length>0, ignore the Closed switch
            for child in Devices:
                DeviceType = child.tag
                AllDeviceTypes.add (DeviceType)
                DeviceNumber = child.find('DeviceNumber').text.translate(dsstab)
                if SectionStatus == 'Closed':
                    if DeviceType == 'OverheadLine':
                        DeviceLength = float(child.find('Length').text)
                        if DeviceLength > 0.0:
                            LineConfig = 'OH_' + child.find('LineID').text.translate(dsstab)
                            OHLineTable[DeviceNumber] = [LineFromNode,LineToNode,Phase,DeviceLength,LineConfig]
                            LineConfigTable[LineConfig][8] = 1 # flag to write only the ones we use
                        else:
                            # add a closed switch
                            SwitchTable[DeviceNumber] = [LineFromNode,LineToNode,Phase,'Closed',DeviceType]
                    elif DeviceType == 'OverheadLineUnbalanced':
                        DeviceLength = float(child.find('Length').text)
                        if DeviceLength > 0.0:
                            LineConfig = 'ZM_' + child.find('LineID').text.translate(dsstab)
                            ZMLineTable[DeviceNumber] = [LineFromNode,LineToNode,Phase,DeviceLength,LineConfig]
                            MatrixTable[LineConfig][20] = 1 # flag to write only the ones we use
                        else:
                            # add a closed switch
                            SwitchTable[DeviceNumber] = [LineFromNode,LineToNode,Phase,'Closed',DeviceType]
                    elif DeviceType == 'OverheadByPhase':
                        DeviceLength = float(child.find('Length').text)
                        if DeviceLength > 0.0:
                            LineSpacing = 'OH_' + child.find('ConductorSpacingID').text.translate(dsstab)
                            WireA = 'OH_' + child.find('PhaseConductorIDA').text.translate(dsstab)
                            WireB = 'OH_' + child.find('PhaseConductorIDB').text.translate(dsstab)
                            WireC = 'OH_' + child.find('PhaseConductorIDC').text.translate(dsstab)
                            Obj = child.find('NeutralConductorID1')
                            if Obj is None:
                                Obj = child.find('NeutralConductorID')
                            WireN = 'OH_' + Obj.text.translate(dsstab)
                            row = LineSpacingTable[LineSpacing]
                            if (row[0] > row[1]) and (WireN == 'OH_NONE'):
                                WireN = WireA
                            OHPhaseTable[DeviceNumber] = [LineFromNode,LineToNode,Phase,DeviceLength,LineSpacing,WireA,WireB,WireC,WireN]
                        else:
                            # add a closed switch
                            SwitchTable[DeviceNumber] = [LineFromNode,LineToNode,Phase,"Closed",DeviceType]
                    elif DeviceType == 'Underground':
                        DeviceLength = float(child.find('Length').text)
                        if DeviceLength > 0.0:
                            LineConfig = "UG_" + child.find('CableID').text.translate(dsstab)
                            UGLineTable[DeviceNumber] = [LineFromNode,LineToNode,Phase,DeviceLength,LineConfig]
                            CableConfigTable[LineConfig][8] = 1 # flag to write only the ones we use
                        else:
                            # add a closed switch
                            SwitchTable[DeviceNumber] = [LineFromNode,LineToNode,Phase,"Closed",DeviceType]
                    elif DeviceType == 'Recloser':
                        if child.find('Location').text == 'To':
                            End = 2
                        else:
                            End = 1
                        enabled = Connected(child)
                        switch = child.find('NormalStatus').text
                        DeviceID = child.find('DeviceID').text.translate(dsstab)
                        if SectionID == DeviceNumber:
                            DeviceNumber = 'Recloser_' + SectionID
    #                    RecloserTable[DeviceNumber] = [SectionID,End,DeviceID,switch,enabled]
    #                    print('WARNING: recloser in ' + SectionID + ' will be a closed switch')
#                        SwitchTable[DeviceNumber] = [SwtFromNode,SwtToNode,Phase,"Closed",DeviceID]
                        RecloserTable[DeviceNumber] = [SwtFromNode,SwtToNode,Phase,"Closed",DeviceID,End,enabled]
                    elif DeviceType == 'Switch':
                        if child.find('Location').text == 'To':
                            End = 2
                        else:
                            End = 1
                        switch = child.find('NormalStatus').text
                        closedPhases = child.find('ClosedPhase').text
                        DeviceID = child.find('DeviceID').text.translate(dsstab)
                        if child.find('ConnectionStatus').text == 'Connected': 
                            enabled = 'yes'
                            if closedPhases == 'None':
#                                print(SwtFromNode,SwtToNode,Phase,DeviceID,closedPhases)
                                switch = 'Open'
                            else:
                                switch = 'Closed'
                        else:
                            enabled = 'no'
                            switch = 'Open'
                        if SectionID == DeviceNumber:
                            DeviceNumber = 'Switch_' + SectionID
                        SwitchTable[DeviceNumber] = [SwtFromNode,SwtToNode,Phase,switch,DeviceID]
    #                    SwtControlTable[DeviceNumber] = [SectionID,End,DeviceID,switch,enabled]
                    elif DeviceType == 'Fuse':
                        if child.find('Location').text == 'To':
                            End = 2
                        else:
                            End = 1
                        enabled = Connected(child)
                        switch = child.find('NormalStatus').text
                        DeviceID = child.find('DeviceID').text.translate(dsstab)
                        amps = FuseConfigTable[DeviceID][0]
                        curve = FuseConfigTable[DeviceID][1]
                        if SectionID == DeviceNumber:
                            DeviceNumber = 'Fuse_' + SectionID
#                        FuseTable[DeviceNumber] = [DeviceNumber,End,DeviceID,switch,enabled,amps,curve,SectionID]
                        # this version is for a section with only a fuse; need to write a line impedance
                        FuseTable[DeviceNumber] = [DeviceNumber,End,DeviceID,switch,enabled,amps,curve,SectionID,
                                                   SwtFromNode,SwtToNode,Phase]
#                        print ('Fuse', FuseTable[DeviceNumber])
                    elif DeviceType == 'Breaker': # in SCE circuit, the breaker is in-line with a line segment
                        if child.find('Location').text == 'To':
                            End = 2
                        else:
                            End = 1
                        switch = child.find('NormalStatus').text
                        DeviceID = child.find('DeviceID').text.translate(dsstab)
                        if child.find('ConnectionStatus').text == 'Connected': 
                            enabled = 'yes'
                            switch = 'Closed'
                        else:
                            enabled = 'no'
                            switch = 'Open'
                        #  print('WARNING: breaker in ' + SectionID + ' will be a closed switch')
                        if SectionID == DeviceNumber:
                            DeviceNumber = 'Breaker_' + SectionID
                        SwitchTable[DeviceNumber] = [SwtFromNode,SwtToNode,Phase,'Closed',DeviceID]
                        # SwtControlTable[DeviceNumber] = [SectionID,End,DeviceID,switch,enabled]
                    elif DeviceType == 'Transformer':
                        XfmrCode = child.find('DeviceID').text.translate(dsstab)
                        tap1 = 0.01 * float(child.find('PrimaryTapSettingPercent').text)
                        tap2 = 0.01 * float(child.find('SecondaryTapSettingPercent').text)
                        enabled = Connected(child)
                        TransformerTable[DeviceNumber] = [XfmrFromNode,XfmrToNode,Phase,XfmrCode,tap1,tap2,enabled]
                    elif DeviceType == 'TransformerByPhase':
                        XfmrCode1 = child.find('PhaseTransformerID1').text
                        if XfmrCode1 is not None:
                            XfmrCode1 = XfmrCode1.translate(dsstab)
                        XfmrCode2 = child.find('PhaseTransformerID2').text
                        if XfmrCode2 is not None:
                            XfmrCode2 = XfmrCode2.translate(dsstab)
                        XfmrCode3 = child.find('PhaseTransformerID3').text
                        if XfmrCode3 is not None:
                            XfmrCode3 = XfmrCode3.translate(dsstab)
                        enabled = Connected(child)
                        TransformerPhaseTable[DeviceNumber] = [XfmrFromNode,XfmrToNode,Phase,XfmrCode1,XfmrCode2,XfmrCode3,enabled]
                    elif DeviceType == 'Regulator':
                        RegCode = child.find('DeviceID').text.translate(dsstab)
                        monPhs = child.find('ControlStatus').text
                        if child.find('ConnectionConfiguration').text.find('Delta') > 0:
                            conn = 'delta'
                        else:
                            conn = 'wye'
                        enabled = Connected(child)
                        if CYMEVersion < 8.0:
                            bw = float(child.find('BandWidth').text)
                        else:
                            bw = float(child.find('ForwardSettings').find('BandwidthA').text)
                        vreg = float(child.find('ForwardSettings').find('SetVoltageA').text)
                        vregb = float(child.find('ForwardSettings').find('SetVoltageB').text)
                        vregc = float(child.find('ForwardSettings').find('SetVoltageC').text)
                        if vregb > vreg:
                            vreg = vregb
                        if vregc > vreg:
                            vreg = vregc
                        ct = float(child.find('CTPrimaryRating').text)
                        pt = float(child.find('PTRatio').text)
                        kva = RegConfigTable[RegCode][1] * 10.0
                        kv = RegConfigTable[RegCode][2]
                        if child.find('ReverseSensingMode').text != 'NoReverse':
                            print('WARNING: unhandled ReverseSensingMode for regulator ' + DeviceNumber + ': ' + child.find('ReverseSensingMode').text)
                        if child.find('SettingOption').text != 'LoadCenter':
                            print('WARNING: unhandled SettingOption for regulator ' + DeviceNumber + ': ' + child.find('SettingOption').text)
                        RegulatorTable[DeviceNumber] = [XfmrFromNode,XfmrToNode,Phase,monPhs,conn,kva,kv,vreg,bw,pt,ct,enabled]
                    elif DeviceType == 'ShuntCapacitor':
                        if child.find('Location').text == 'To':
                            CapBus = ToNodeID
                        else:
                            CapBus = FromNodeID
                        PhasesConnected = Phase # child.find('Phase').text
                        kvar = 0 # float(child.find('KVARABC').text)
                        kvar += float(child.find('FixedKVARA').text)
                        kvar += float(child.find('FixedKVARB').text)
                        kvar += float(child.find('FixedKVARC').text)
                        kvar += float(child.find('SwitchedKVARA').text)
                        kvar += float(child.find('SwitchedKVARB').text)
                        kvar += float(child.find('SwitchedKVARC').text)
                        switch = 'Open' # see InitiallyClosedPhase
                        enabled = Connected(child)
                        if child.find('ConnectionConfiguration').text == 'D':
                            conn = 'delta'
                        else:
                            conn = 'wye'
                        nomkv = float(child.find('KVLN').text)
                        if len(PhasesConnected) == 3:
                            nomkv *= math.sqrt(3.0)
                        elif conn == 'delta':
                            nomkv *= math.sqrt(3.0)
                        ShuntCapTable[DeviceNumber] = [CapBus,PhasesConnected,conn,nomkv,kvar,enabled,switch]
                    elif DeviceType == 'Photovoltaic':
                        if child.find('Location').text == 'To':
                            GenBus = ToNodeID
                        else:
                            GenBus = FromNodeID
                        PhasesConnected = Phase
                        enabled = Connected(child)
                        ActiveGen = float(child.find('GenerationModels/DGGenerationModel/ActiveGeneration').text)
                        pf = 0.01 * float(child.find('GenerationModels/DGGenerationModel/PowerFactor').text)
                        PVCode = child.find('DeviceID').text.translate(dsstab)
                        kva = PhotovoltaicTable[PVCode][0] * float(child.find('Ns').text) * float(child.find('Np').text)
                        pufault = PhotovoltaicTable[PVCode][1]
                        if child.find('FaultContributionUnit').text == 'Percent':
                            pufault = 0.01 * float(child.find('FaultContribution').text)
                        DGTable[DeviceNumber] = [GenBus,PhasesConnected,ActiveGen,pf,kva,pufault]
                    elif DeviceType == 'BESS':
                        if child.find('Location').text == 'To':
                            GenBus = ToNodeID
                        else:
                            GenBus = FromNodeID
                        PhasesConnected = Phase
                        enabled = Connected(child)
                        ActiveGen = float(child.find('Converter/ActivePowerRating').text)
                        ReactiveGen = float(child.find('Converter/ReactivePowerRating').text)
                        TotalGen = float(child.find('Converter/ConverterRating').text)
                        pf = 1.0
                        BESSCode = child.find('DeviceID').text.translate(dsstab)
                        kwh = BESSTable[BESSCode][0]
                        pufault = 1.1
                        if child.find('FaultContributionUnit').text == 'Percent':
                            pufault = 0.01 * float(child.find('FaultContribution').text)
                        DGTable[DeviceNumber] = [GenBus,PhasesConnected,ActiveGen,pf,TotalGen,pufault]
                    elif DeviceType == 'ElectronicConverterGenerator':
                        if child.find('Location').text == 'To':
                            GenBus = ToNodeID
                        else:
                            GenBus = FromNodeID
                        PhasesConnected = Phase
                        enabled = Connected(child)
                        ActiveGen = float(child.find('GenerationModels/DGGenerationModel/ActiveGeneration').text)
                        pf = 0.01 * float(child.find('GenerationModels/DGGenerationModel/PowerFactor').text)
                        ECGCode = child.find('DeviceID').text.translate(dsstab)
                        kva = ECGTable[ECGCode][0]
                        pufault = ECGTable[ECGCode][2]
                        DGTable[DeviceNumber] = [GenBus,PhasesConnected,ActiveGen,pf,kva,pufault]
                    elif DeviceType == 'SynchronousGenerator':
                        print ('SynchronousGenerator', FromNodeID, ToNodeID)
                    elif DeviceType not in ['DistributedLoad', 'SpotLoad']:
                        print ('unsupported',DeviceType,'in section',SectionID)

            # Now go through all the loads on this section
            for child in Devices:
                DeviceType = child.tag
                AllDeviceTypes.add (DeviceType)
                DeviceNumber = child.find('DeviceNumber').text
                if DeviceType == 'SpotLoad':
                    if child.find('Location').text == 'To':
                        LoadNodeID = ToNodeID
                    else:
                        LoadNodeID = FromNodeID
                    Sload = CollectLoadPQ(child)
                    LoadTable[DeviceNumber] = [LoadNodeID,Phase,DefaultBaseVoltage,Sload,1]
                elif DeviceType == 'DistributedLoad':
                    LoadNodeID = ToNodeID #TODO - split the load?
                    Sload = CollectLoadPQ(child)
                    LoadTable[DeviceNumber] = [LoadNodeID,Phase,DefaultBaseVoltage,Sload,2]

    print (AllDeviceTypes)
    #add Loads to Buses
    floads=open(loadfilename,'w')
    PLoadTotal = 0
    QLoadTotal = 0
    for key in LoadTable:
        CreateLoad(floads,key,LoadTable[key])
        LoadVector = LoadTable[key][3]
        PLoadTotal = PLoadTotal + LoadVector[0] + LoadVector[2] + LoadVector[4]
        QLoadTotal = QLoadTotal + LoadVector[1] + LoadVector[3] + LoadVector[5]
    floads.write('\n')
    floads.write('// Total Load = ' + '{:.2f}'.format(PLoadTotal) + ' +j ' + '{:.2f}'.format(QLoadTotal) + '\n')
    floads.close()

    #create the network of lines, transformers, regulators, capacitor banks and switches
    fnetwork=open(networkfilename,"w")
    for key in OHLineTable:
        cfg = LineConfigTable[OHLineTable[key][4]]
        CreateLine(fnetwork,key,OHLineTable[key],cfg)
    fnetwork.write('\n')

    for key in ZMLineTable:
        cfg = MatrixTable[ZMLineTable[key][4]]
        CreateMatrixLine(fnetwork,key,ZMLineTable[key],cfg)
    fnetwork.write('\n')

    for key in OHPhaseTable:
        CreateByPhase(fnetwork,key,OHPhaseTable[key])
    fnetwork.write('\n')

    for key in UGLineTable:
        cfg = CableConfigTable[UGLineTable[key][4]]
        CreateLine(fnetwork,key,UGLineTable[key],cfg)
    fnetwork.write('\n')

    for key in SwitchTable:
        CreateSwitch(fnetwork,key,SwitchTable[key])
    fnetwork.write('\n')

    for key in TransformerTable:
        cfg = XfmrConfigTable[TransformerTable[key][3]]
        CreateTransformer(fnetwork,key,TransformerTable[key],cfg)
    fnetwork.write('\n')

    for key in TransformerPhaseTable:
        CreateTransformerPhase(fnetwork,key,TransformerPhaseTable[key],XfmrConfigTable)
    fnetwork.write('\n')

    for key in SwtControlTable:
        CreateSwtControl(fnetwork,key,SwtControlTable[key])
    fnetwork.write('\n')

    for key in FuseTable:
        CreateFuse(fnetwork,key,FuseTable[key])
    fnetwork.write('\n')

    for key in RecloserTable:
        CreateRecloser(fnetwork,key,RecloserTable[key])
    fnetwork.write('\n')

    for key in ShuntCapTable:
        CreateCapacitor(fnetwork,key,ShuntCapTable[key])
    fnetwork.write('\n')

    for key in RegulatorTable:
        CreateRegulator(fnetwork,key,RegulatorTable[key])
    fnetwork.write('\n')

    for key in DGTable:
        CreateGenerator(fnetwork,key,DGTable[key])
    fnetwork.write('\n')
    fnetwork.close()

def ConvertSXST(cfg):
    global xmlfilename, RootName, SubName, LoadScale, LoadModel, DefaultBaseVoltage
    global BaseVoltages, CoordXmin, CoordXmax, CoordYmin, CoordYmax
    global CYMELineCodeUnit, DSSSectionUnit, OwnerIDs, CYMEtoDSSSection
    global CYMEtoDSSLineCode, Zbase, TotalP, TotalQ, CYMESectionUnit, CYMEVersion

    xmlfilename = cfg['xmlfilename']
    RootName = cfg['RootName']
    SubName = cfg['SubName']
    LoadScale = cfg['LoadScale']
    LoadModel = cfg['LoadModel']
    DefaultBaseVoltage = cfg['DefaultBaseVoltage']
    BaseVoltages = cfg['BaseVoltages']
    CoordXmin = cfg['CoordXmin']
    CoordXmax = cfg['CoordXmax']
    CoordYmin = cfg['CoordYmin']
    CoordYmax = cfg['CoordYmax']
    CYMESectionUnit = cfg['CYMESectionUnit']
    CYMELineCodeUnit = cfg['CYMELineCodeUnit']
    DSSSectionUnit = cfg['DSSSectionUnit']
    OwnerIDs = cfg['OwnerIDs']

    if CYMESectionUnit != 'm' or CYMELineCodeUnit != 'km':
        print ('WARNING: CYMDIST line section lengths should be m, line code lengths should be km')
    # 3280.84 ft/km, 1000.0 m/km, 0.621371192 mi/km
    if DSSSectionUnit == 'mi':
        CYMEtoDSSSection = 1.0 / 1609.344      # miles per meter
        CYMEtoDSSLineCode = 1.0 / 0.621371192  # km per mile
    elif DSSSectionUnit == 'kft':
        CYMEtoDSSSection = 0.00328084          # kft per meter
        CYMEtoDSSLineCode = 1.0 / 3.28084      # km per kft
    elif DSSSectionUnit == 'ft':
        CYMEtoDSSSection = 3.2809              # ft per meter
        CYMEtoDSSLineCode = 1.0 / 3280.84      # km per ft
    elif DSSSectionUnit == 'm':
        CYMEtoDSSSection = 1.0                 # m per meter
        CYMEtoDSSLineCode = 1.0 / 1000.0       # km per m
    else:
        print ('WARNING: the DSSSection unit should not be "{:s}"'.format(DSSSectionUnit))
        CYMEtoDSSSection = 1.0
        CYMEtoDSSLineCode = 1.0
    Zbase = DefaultBaseVoltage * DefaultBaseVoltage / 100.0
    TotalP = 0.0
    TotalQ = 0.0

    # Read in cyme xml file data
    print ('Reading the network data from CYME xml...')
    tree = ET.parse(xmlfilename + '.sxst')
    root = tree.getroot()
    CYMEVersion = float (root.find('Version').text)
    print ('Version {:.2f}'.format(CYMEVersion))

    print ('Writing to DSS files...')
    masterfilename = RootName + '_master.dss'
    catalogfilename = RootName + '_catalog.dss'
    xyfilename = RootName + '_xy.dat'
    fmaster=open(masterfilename,'w')
    fmaster.write('clear\n')
    SubFile = SubName + '.sub'
    if not os.path.exists(SubFile):
        sp = open (SubFile, 'w')
        print ("""
// At minimum, this file needs to create the new circuit for OpenDSS.
// You may also add transmission lines, substation switchgear, substation regulator, 
//  energy meter, or other components before the feeder backbone is included.
// This file is not over-written if you run the converter again.""", file=sp)
        if len(SourceTable) < 1:
            print ('// Placeholder source:', file=sp)
            print ('new circuit.{:s} bus1={:s} basekv={:.3f} pu=1 ang=0 r1=0 x1=0.001 r0=0 x0=0.001'.format (RootName, '???', DefaultBaseVoltage), file=sp)
        for key,row in SourceTable.items():
            sp.write('// Use this source impedance as the starting point for {:s}\n'.format(SubName))
            sp.write('// new circuit.' + RootName)
            sp.write(' bus1=' + row[7])
            sp.write(' basekv=' + str(row[0]))
            sp.write(' pu=' + str(row[1]))
            sp.write(' ang=' + str(row[2]))
            sp.write(' r1=' + str(row[3]))
            sp.write(' x1=' + str(row[4]))
            sp.write(' r0=' + str(row[5]))
            sp.write(' x0=' + str(row[6]))
            sp.write(' // ' + key + '\n')
        sp.close()
    fmaster.write('redirect ' + SubFile + '\n')
    fmaster.write('redirect ' + catalogfilename + '\n')
    BuildCatalog (root)
    BuildInitialCoordinates (root)
    for OwnerID in OwnerIDs:
        networkfilename = OwnerID + '_network.dss'
        loadfilename = OwnerID + '_loads.dss'
        fmaster.write('redirect ' + networkfilename + '\n')
        fmaster.write('redirect ' + loadfilename + '\n')
        WriteFeeder (root, OwnerID, networkfilename, loadfilename)
    EditFile = RootName + '.edits'
    if not os.path.exists(EditFile):
        ep = open (EditFile, 'w')
        print ("""
// This is included after the feeder backbone has been created, and before calculating voltage bases in OpenDSS. 
// You can start with an empty file. 
// Typical contents include control and protection settings, parameter adjustments, and creation of DER for study. 
// This file is not over-written if you run the converter again.""", file=ep)
        ep.close()
    fmaster.write('redirect ' + EditFile + '\n')
    fmaster.write('Set VoltageBases = ' + str(BaseVoltages) + '\n')
    fmaster.write('CalcVoltageBases\n')
    fmaster.write('SetLoadAndGenKv\n')
    fmaster.write('buscoords ' + xyfilename + '\n')
    WriteCoordinates (xyfilename)
    WriteCatalog (catalogfilename)
    fmaster.write('solve mode=snap\n')
    fmaster.write('batchedit energymeter..* action=take\n')
    fmaster.close()
    #print('wrote total load = ' + "{:3}".format(TotalP) + ' + j' + "{:3}".format(TotalQ) + ' [kVA]')

def usage():
    print("usage: python Cyme2DSS.py <full path to JSON configuration>")

if __name__ == '__main__':
    if len(sys.argv) != 2:
        usage()
        sys.exit()
    lp = open (sys.argv[1]).read()
    ConvertSXST (json.loads(lp))
