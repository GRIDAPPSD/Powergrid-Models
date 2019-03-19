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
# DateTime, CaseName, Status, Mode, Number, LoadMult, NumDevices, NumBuses, NumNodes, Iterations, ControlMode, ControlIterations, MostIterationsDone, Year, Hour, MaxPuVoltage, MinPuVoltage, TotalMW, TotalMvar, MWLosses, pctLosses, MvarLosses, Frequency

v1 = load_voltages ('Case1v.csv')
v2 = load_voltages ('Case2v.csv')
t1 = load_taps ('Case1t.csv')
t2 = load_taps ('Case2t.csv')
diff = {}

print ('Regulator, Case 1 Tap, Case 2 Tap')
for bus in t1:
    if bus in t2:
        print (bus, str(t1[bus]), str(t2[bus]))
    else:
        print (bus, str(t1[bus]), '**ABSENT**')
for bus in t2:
    if bus not in t1:
        print (bus, '**ABSENT**', str(t2[bus]))

# bus naming convention will be "bus name"_A, _B, or _C
for bus in v1:
    if bus in v2:
        diff [bus] = abs(v1[bus] - v2[bus])

sorted_diff = sorted(diff.items(), key=operator.itemgetter(1))

fcsv = open ('Compare_Voltages.csv', 'w')
print ('bus_phs,v1,v2,diff', file=fcsv)
for row in sorted_diff:
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
fcsv.close()

