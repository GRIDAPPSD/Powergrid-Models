import csv
import operator
import math

def dss_phase(col):
    if col==1:
        return '_A'
    elif col==2:
        return '_B'
    else:
        return '_C'

def load_currents(fname):
    fd = open (fname, 'r')
    rd = csv.reader (fd, delimiter=',', skipinitialspace=True)
    next (rd)
    idss = {}
    itol = 1.0e-8  # if this is too high, the comparison may think a conductive branch is missing
    #Element, I1_1, Ang1_1, I1_2, Ang1_2, I1_3, Ang1_3, I1_4, Ang1_4, Iresid1, AngResid1, I2_1, Ang2_1, I2_2, Ang2_2, I2_3, Ang2_3, I2_4, Ang2_4, Iresid2, AngResid2
    for row in rd:
        link = row[0].strip('\"')
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
    fd = open (fname, 'r')
    rd = csv.reader (fd, delimiter=',', skipinitialspace=True)
    next (rd)
    vdss = {}
    #Bus, BasekV, Node1, Magnitude1, Angle1, pu1, Node2, Magnitude2, Angle2, pu2, Node3, Magnitude3, Angle3, pu3
    for row in rd:
        bus = row[0].strip('\"')
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
    fd = open (fname, 'r')
    rd = csv.reader (fd, delimiter=',', skipinitialspace=True)
    next (rd)
    vtap = {}
    # Name, Tap, Min, Max, Step, Position
    for row in rd:
        bus = row[0].strip('\"')
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
    fd = open (fname, 'r')
    rd = csv.reader (fd, delimiter=',', skipinitialspace=True)
    next (rd)
    summ = {}
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

v1 = load_voltages ('Case1v.csv')
v2 = load_voltages ('Case2v.csv')
t1 = load_taps ('Case1t.csv')
t2 = load_taps ('Case2t.csv')
s1 = load_summary ('Case1s.csv')
s2 = load_summary ('Case2s.csv')
i1 = load_currents ('Case1i.csv')
i2 = load_currents ('Case2i.csv')

print ('Quantity  Case1   Case2')
for key in ['Status', 'Mode', 'Number', 'LoadMult', 'NumDevices', 'NumBuses', 
            'NumNodes', 'Iterations', 'ControlMode', 'ControlIterations', 'MaxPuVoltage',
            'MinPuVoltage', 'TotalMW', 'TotalMvar', 'MWLosses', 'pctLosses',
            'MvarLosses', 'Frequency']:
    print (key, s1[key], s2[key])

print ('\nRegulator, Case 1 Tap, Case 2 Tap')
for bus in t1:
    if bus in t2:
        print (bus, str(t1[bus]), str(t2[bus]))
    else:
        print (bus, str(t1[bus]), '**ABSENT**')
for bus in t2:
    if bus not in t1:
        print (bus, '**ABSENT**', str(t2[bus]))

# bus naming convention will be "bus name"_A, _B, or _C
vdiff = {}
for bus in v1:
    if bus in v2:
        vdiff [bus] = abs(v1[bus] - v2[bus])
sorted_vdiff = sorted(vdiff.items(), key=operator.itemgetter(1))
fcsv = open ('Compare_Voltages.csv', 'w')
print ('bus_phs,v1,v2,vdiff', file=fcsv)
for row in sorted_vdiff:
    if row[1] < 0.8:
        bus = row[0]
        print (bus, '{:.5f}'.format(v1[bus]), '{:.5f}'.format(v2[bus]), 
                     '{:.5f}'.format(row[1]), sep=',', file=fcsv)
fcsv.close()

ftxt = open ('Missing_Nodes.txt', 'w')
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

# branch (link) naming convention will be "class.instance".1, .2 or .3
idiff = {}
for link in i1:
    if link in i2:
        idiff [link] = abs(i1[link] - i2[link])
sorted_idiff = sorted(idiff.items(), key=operator.itemgetter(1))
fcsv = open ('Compare_Currents.csv', 'w')
print ('link.phs,i1,i2,idiff', file=fcsv)
for row in sorted_idiff:
    link = row[0]
    print (link, '{:.3f}'.format(i1[link]), '{:.3f}'.format(i2[link]), 
                '{:.3f}'.format(row[1]), sep=',', file=fcsv)
fcsv.close()
ftxt = open ('Missing_Links.txt', 'w')
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

