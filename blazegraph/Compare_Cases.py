import csv
import operator
import math
import sys
import os

# Do all of the name-matching in upper case!!

# 208/120 is always used as a candidate base voltage
casefiles = [{'root':'ACEP_PSIL',      'bases':[314.0,480.0]},
             {'root':'EPRI_DPV_J1',    'bases':[416.0,12470.0,69000.0]},
             {'root':'IEEE123',        'bases':[480.0,4160.0]},
             {'root':'IEEE123_PV',     'bases':[4160.0]},
             {'root':'Transactive',    'bases':[4160.0]},
             {'root':'IEEE13',         'bases':[480.0,4160.0,13200.0,115000.0]},
             {'root':'IEEE13_Assets',  'bases':[480.0,4160.0,115000.0]},
             {'root':'IEEE37',         'bases':[480.0,4800.0,230000.0]},
             {'root':'IEEE8500',       'bases':[12470.0,115000.0]},
             {'root':'IEEE8500_3subs', 'bases':[12480.0,69000.0,115000.0]},
             {'root':'R2_12_47_2',     'bases':[480.0,12470.0,100000.0]}]

#casefiles = [{'root':'IEEE8500_3subs', 'bases':[12480.0,69000.0,115000.0]}]

#casefiles = [{'root':'IEEE123_PV',     'bases':[4160.0]}]

#casefiles = [{'root':'Transactive',     'bases':[4160.0]}]

dir1 = './test/'
dir2 = './dss/'
dir3 = './glm/'

def dss_phase(col):
    if col==1:
        return '_A'
    elif col==2:
        return '_B'
    else:
        return '_C'

# heuristically estimate a base voltage from a set of common values, assuming
#  that a normal per-unit voltage should be 0.9 to 1.1, and 120.0 is the default base

def glmVpu(v, bases):
    vpu = v / 120.0
    if vpu < 1.1:
        return vpu
    for vbase in bases:
        vpu = v / vbase
        if vpu < 1.1:
            return vpu
    return 0.0 # indicates a problem

def load_glm_voltages(fname, voltagebases):
    vglm = {}
    buses = []
    if not os.path.isfile (fname):
        return buses, vglm
    fd = open (fname, 'r')
    rd = csv.reader (fd, delimiter=',')
    next (rd)
    next (rd)
    for row in rd:
        bus = row[0].upper()
        buses.append (bus)
        maga = float(row[1])
        if maga > 0.0:
            vglm[bus+'_A'] = glmVpu (maga, voltagebases)
        magb = float(row[3])
        if magb > 0.0:
            vglm[bus+'_B'] = glmVpu (magb, voltagebases)
        magc = float(row[5])
        if magc > 0.0:
            vglm[bus+'_C'] = glmVpu (magc, voltagebases)
    fd.close()
    return buses, vglm
    
def load_glm_currents(fname):
    iglm = {}
    links = []
    if not os.path.isfile (fname):
        return links, iglm
    fd = open (fname, 'r')
    rd = csv.reader (fd, delimiter=',')
    next (rd)
    #link_name,currA_mag,currA_angle,currB_mag,currB_angle,currC_mag,currC_angle
    for row in rd:
        link = row[0].upper()
        if link.startswith ('LINE_') or link.startswith ('REG_') or link.startswith ('SWT_') or link.startswith ('XF_'):
            links.append(link)
            maga = float(row[1])
            if maga > 0.001:
                iglm[link+'_A'] = maga
            magb = float(row[3])
            if magb > 0.001:
                iglm[link+'_B'] = magb
            magc = float(row[5])
            if magc > 0.001:
                iglm[link+'_C'] = magc
    fd.close()
    return links, iglm

def load_currents(fname):
    idss = {}
    if not os.path.isfile (fname):
        return idss
    fd = open (fname, 'r')
    rd = csv.reader (fd, delimiter=',', skipinitialspace=True)
    next (rd)
    itol = 1.0e-8  # if this is too high, the comparison may think a conductive branch is missing
    #Element, I1_1, Ang1_1, I1_2, Ang1_2, I1_3, Ang1_3, I1_4, Ang1_4, Iresid1, AngResid1, I2_1, Ang2_1, I2_2, Ang2_2, I2_3, Ang2_3, I2_4, Ang2_4, Iresid2, AngResid2
    for row in rd:
        link = row[0].strip('\"').upper()
        i1 = float(row[1])
        i2 = float(row[3])
        i3 = float(row[5])
        idx = 1
        if i1 > itol:
            idss[link+'.'+str(idx)] = i1
            idx += 1
        if i2 > itol:
            idss[link+'.'+str(idx)] = i2
            idx += 1
        if i3 > itol:
            idss[link+'.'+str(idx)] = i3
            idx += 1
    fd.close()
    return idss

def load_voltages(fname):
    vdss = {}
    if not os.path.isfile (fname):
        return vdss
    fd = open (fname, 'r')
    rd = csv.reader (fd, delimiter=',', skipinitialspace=True)
    next (rd)
    #Bus, BasekV, Node1, Magnitude1, Angle1, pu1, Node2, Magnitude2, Angle2, pu2, Node3, Magnitude3, Angle3, pu3
    for row in rd:
        bus = row[0].strip('\"').upper()
        if len(bus) > 0:
            vpu1 = float(row[5])
            vpu2 = float(row[9])
            vpu3 = float(row[13])
            if float(vpu1) > 0:
                phs = dss_phase (int(row[2]))
                vdss[bus+phs] = vpu1
            if float(vpu2) > 0:
                phs = dss_phase (int(row[6]))
                vdss[bus+phs] = vpu2
            if float(vpu3) > 0:
                phs = dss_phase (int(row[10]))
                vdss[bus+phs] = vpu3
    fd.close()
    return vdss

def load_taps(fname):
    vtap = {}
    if not os.path.isfile (fname):
        return vtap
    fd = open (fname, 'r')
    rd = csv.reader (fd, delimiter=',', skipinitialspace=True)
    next (rd)
    # Name, Tap, Min, Max, Step, Position
    for row in rd:
        bus = row[0].strip('\"').upper()
        if len(bus) > 0:
            vtap[bus] = int (row[5])
    fd.close()
    return vtap

# Summary information - we want the last row
# DateTime, CaseName, Status, Mode, Number, 
# LoadMult, NumDevices, NumBuses, NumNodes, Iterations, 
# ControlMode, ControlIterations, MostIterationsDone, Year, Hour, 
# MaxPuVoltage, MinPuVoltage, TotalMW, TotalMvar, MWLosses, 
# pctLosses, MvarLosses, Frequency
def load_summary(fname):
    summ = {}
    if not os.path.isfile (fname):
        return summ
    fd = open (fname, 'r')
    rd = csv.reader (fd, delimiter=',', skipinitialspace=True)
    next (rd)
    for row in rd:
        summ['Status'] = row[2]
        summ['Mode'] = row[3]
        summ['Number'] = row[4]
        summ['LoadMult'] = row[5]
        summ['NumDevices'] = row[6]
        summ['NumBuses'] = row[7]
        summ['NumNodes'] = row[8]
        summ['Iterations'] = row[9]
        summ['ControlMode'] = row[10]
        summ['ControlIterations'] = row[11]
        summ['MaxPuVoltage'] = row[15]
        summ['MinPuVoltage'] = row[16]
        summ['TotalMW'] = row[17]
        summ['TotalMvar'] = row[18]
        summ['MWLosses'] = row[19]
        summ['pctLosses'] = row[20]
        summ['MvarLosses'] = row[21]
        summ['Frequency'] = row[22]
    fd.close()
    return summ

def write_comparisons(path1, path2, path3, rootname, voltagebases):
    v1 = load_voltages (path1 + rootname + '_v.csv')
    v2 = load_voltages (path2 + rootname + '_v.csv')
    t1 = load_taps (path1 + rootname + '_t.csv')
    t2 = load_taps (path2 + rootname + '_t.csv')
    i1 = load_currents (path1 + rootname + '_i.csv')
    i2 = load_currents (path2 + rootname + '_i.csv')
    s1 = load_summary (path1 + rootname + '_s.csv')
    s2 = load_summary (path2 + rootname + '_s.csv')

    gldbus, gldv = load_glm_voltages (path3 + rootname + '_volt.csv', voltagebases)
    gldlink, gldi = load_glm_currents (path3 + rootname + '_curr.csv')

#    print (gldbus)
#    print (gldv)
#    print (v1)
#    print (gldlink)
#    print (gldi)
#    print (i1)

    flog = open (path2 + rootname + '_Summary.log', 'w')
    print ('Quantity  Case1   Case2', file=flog)
    for key in ['Status', 'Mode', 'Number', 'LoadMult', 'NumDevices', 'NumBuses', 
            'NumNodes', 'Iterations', 'ControlMode', 'ControlIterations', 'MaxPuVoltage',
            'MinPuVoltage', 'TotalMW', 'TotalMvar', 'MWLosses', 'pctLosses',
            'MvarLosses', 'Frequency']:
        print (key, s1[key], s2[key], file=flog)

    print ('\nRegulator, Case 1 Tap, Case 2 Tap', file=flog)
    for bus in t1:
        if bus in t2:
            print (bus, str(t1[bus]), str(t2[bus]), file=flog)
        else:
            print (bus, str(t1[bus]), '**ABSENT**', file=flog)
    for bus in t2:
        if bus not in t1:
            print (bus, '**ABSENT**', str(t2[bus]), file=flog)
    flog.close()

    # bus naming convention will be "bus name"_A, _B, or _C
    vdiff = {}
    for bus in v1:
        if bus in v2:
            vdiff [bus] = abs(v1[bus] - v2[bus])
    sorted_vdiff = sorted(vdiff.items(), key=operator.itemgetter(1))
    fcsv = open (path2 + rootname + '_Compare_Voltages_DSS.csv', 'w')
    print ('bus_phs,vbase,vdss,vdiff', file=fcsv)
    for row in sorted_vdiff:
        if row[1] < 0.8:
            bus = row[0]
            print (bus, '{:.5f}'.format(v1[bus]), '{:.5f}'.format(v2[bus]), 
                     '{:.5f}'.format(row[1]), sep=',', file=fcsv)
    fcsv.close()
    # bus naming convention will be "bus name"_A, _B, or _C
    vdiff = {}
    for bus in v1:
        if bus in gldv:
            vdiff [bus] = abs(v1[bus] - gldv[bus])
    sorted_vdiff = sorted(vdiff.items(), key=operator.itemgetter(1))
    fcsv = open (path2 + rootname + '_Compare_Voltages_GLM.csv', 'w')
    print ('bus_phs,vbase,vglm,vdiff', file=fcsv)
    for row in sorted_vdiff:
        if row[1] < 0.8:
            bus = row[0]
            print (bus, '{:.5f}'.format(v1[bus]), '{:.5f}'.format(gldv[bus]), 
                     '{:.5f}'.format(row[1]), sep=',', file=fcsv)
    fcsv.close()

    ftxt = open (path2 + rootname + '_Missing_Nodes_DSS.txt', 'w')
    nmissing_1 = 0
    nmissing_2 = 0
    for bus in v1:
        if not bus in v2:
            print (bus, 'not in Case 2', file=ftxt)
            nmissing_2 += 1
    for bus in v2:
        if not bus in v1:
            print (bus, 'not in Case 1', file=ftxt)
            nmissing_1 += 1
    print (len(v1), 'Case 1 nodes,', nmissing_2, 'not in Case 2', file=ftxt)
    print (len(v2), 'Case 2 nodes,', nmissing_1, 'not in Case 1', file=ftxt)
    ftxt.close()

    # branch (link) naming convention will be "class.instance".1, .2 or .3 for OpenDSS
    idiff = {}
    for link in i1:
        if link in i2:
            idiff [link] = abs(i1[link] - i2[link])
    sorted_idiff = sorted(idiff.items(), key=operator.itemgetter(1))
    fcsv = open (path2 + rootname + '_Compare_Currents_DSS.csv', 'w')
    print ('class.name.phs,ibase,idss,idiff', file=fcsv)
    for row in sorted_idiff:
        link = row[0]
        print (link, '{:.3f}'.format(i1[link]), '{:.3f}'.format(i2[link]), 
                '{:.3f}'.format(row[1]), sep=',', file=fcsv)
    fcsv.close()

    # from GridLAB-D the link names will start with line_, swt_, reg_ or xf_
    # if there are non-zero magA, magB or magC values, 
    #   look for the next phase current 1, 2, or 3 from the matching OpenDSS branch name
    # for example, GridLAB-D line_632670_A corresponds to OpenDSS Line.632670.1
    idiff = {}
    for link in gldlink:
        dsslink = ''
        nextdssphase = 1
        if link.startswith('LINE_'):
            dsslink = 'LINE.' + link[len('LINE_'):].upper() + '.'
        elif link.startswith('XF_'):
            dsslink = 'TRANSFORMER.' + link[len('XF_'):].upper() + '.'
        elif link.startswith('SWT_'):
            dsslink = 'LINE.' + link[len('SWT_'):].upper() + '.'
        elif link.startswith('REG_'):
            dsslink = 'TRANSFORMER.' + link[len('REG_'):].upper() + '.'
        for phs in ['_A', '_B', '_C']:
            gldtarget = link + phs
            if gldtarget in gldi:
                dsstarget = dsslink + str(nextdssphase)
                if dsstarget in i1:
                    idiff [gldtarget] = [abs(i1[dsstarget] - gldi[gldtarget]), dsstarget]
                    nextdssphase += 1
    sorted_idiff = sorted(idiff.items(), key=operator.itemgetter(1))
    fcsv = open (path2 + rootname + '_Compare_Currents_GLM.csv', 'w')
    print ('class_name_phs,ibase,iglm,idiff', file=fcsv)
    for row in sorted_idiff:
        gldtarget = row[0]
        phsdiff = row[1][0]
        dsstarget = row[1][1]
        print (gldtarget, '{:.3f}'.format(i1[dsstarget]), '{:.3f}'.format(gldi[gldtarget]), 
                '{:.3f}'.format(phsdiff), sep=',', file=fcsv)
    fcsv.close()

    ftxt = open (path2 + rootname + '_Missing_Links_DSS.txt', 'w')
    nmissing_1 = 0
    nmissing_2 = 0
    for link in i1:
        if not link in i2:
            print (link, 'not in Case 2', file=ftxt)
            nmissing_2 += 1
    for link in i2:
        if not link in i1:
            print (link, 'not in Case 1', file=ftxt)
            nmissing_1 += 1
    print (len(i1), 'Case 1 links,', nmissing_2, 'not in Case 2', file=ftxt)
    print (len(i2), 'Case 2 links,', nmissing_1, 'not in Case 1', file=ftxt)
    ftxt.close()

if __name__ == "__main__":
    for row in casefiles:
        root = row['root']
        bases = row['bases']
        for i in range(len(bases)):
            bases[i] /= math.sqrt(3.0)
        write_comparisons (dir1, dir2, dir3, root, bases)

