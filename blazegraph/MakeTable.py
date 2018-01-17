import sys;
import re;
import os.path;

glmpath = 'c:\\gridapps-d\\powergrid-models\\blazegraph\\glm\\'
dsspath = 'c:\\gridapps-d\\powergrid-models\\blazegraph\\dss\\'
xmlpath = 'c:\\gridapps-d\\powergrid-models\\blazegraph\\test\\'

casefiles = [['IEEE13',66395.3],
             ['IEEE13_Assets',66395.3],
             ['IEEE8500',66395.3],
             ['IEEE34',39837.2],
             ['IEEE37',132790.6],
             ['IEEE123',2401.8],
             ['DY-bal',7200.0],
             ['GYD-bal',7200.0],
             ['OYOD-bal',7200.0],
             ['OYOD-unbal',7200.0],
             ['YD-bal',7200.0],
             ['YY-bal',7200.0],
             ['IEEE8500u',66395.3],
             ['EPRI5',66395.3],
             ['EPRI7',66395.3],
             ['EPRI24',132790.6],
             ['R1_12_47_1',57735.0],
             ['R1_12_47_2',57735.0],
             ['R1_12_47_3',57735.0],
             ['R1_12_47_4',57735.0],
             ['R1_25_00_1',57735.0],
             ['R2_12_47_1',57735.0],
             ['R2_12_47_2',57735.0],
             ['R2_12_47_3',57735.0],
             ['R2_25_00_1',57735.0],
             ['R2_35_00_1',57735.0],
             ['R3_12_47_1',57735.0],
             ['R3_12_47_2',57735.0],
             ['R3_12_47_3',57735.0],
             ['R4_12_47_1',57735.0],
             ['R4_12_47_2',57735.0],
             ['R4_25_00_1',57735.0],
             ['R5_12_47_1',57735.0],
             ['R5_12_47_2',57735.0],
             ['R5_12_47_3',57735.0],
             ['R5_12_47_4',57735.0],
             ['R5_12_47_5',57735.0],
             ['R5_25_00_1',57735.0],
             ['R5_35_00_1',57735.0],
             ['EPRI_DPV_J1',39837.2],
             ['EPRI_DPV_K1',39837.2],
             ['EPRI_DPV_M1',38682.5]]

op = open ('table.txt', 'w')
print ('Case,Solved,Ndev,Nbus,Nnode,Vmax,Vmin,Ptot,Qtot,Ploss,Qloss', file=op)

for c in casefiles:
    fname = xmlpath + c[0] + '.csv'
    if os.path.isfile(fname):
        fp = open (fname, 'r')
        lines = fp.readlines()
        ln = lines[1]
        vals = re.split('[,\s]+', ln)
        print (c[0], vals[3], vals[7], vals[8], vals[9], 
               vals[16], vals[17], vals[18], vals[19], 
               vals[20], vals[22], sep=',', file = op)
        fp.close()
    fname = dsspath + c[0] + '.csv'
    if os.path.isfile(fname):
        fp = open (fname, 'r')
        lines = fp.readlines()
        ln = lines[1]
        vals = re.split('[,\s]+', ln)
        print (c[0], vals[3], vals[7], vals[8], vals[9], 
               vals[16], vals[17], vals[18], vals[19], 
               vals[20], vals[22], sep=',', file = op)
        fp.close()
    print('', file = op)

op.close()
