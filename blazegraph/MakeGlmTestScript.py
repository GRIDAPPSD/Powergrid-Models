import sys;

glmpath = 'c:\\gridapps-d\\powergrid-models\\blazegraph\\glm\\'

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

bp = open (glmpath + 'check_glm.bat', 'w')

for c in casefiles:
    print('gridlabd', c[0] + '_run.glm >' + c[0] + '.log 2>&1', file=bp)

    fp = open (glmpath + c[0] + '_run.glm', 'w')

    print('clock {', file=fp)
    print('  timezone EST+5EDT;', file=fp)
    print('  starttime \'2000-01-01 0:00:00\';', file=fp)
    print('  stoptime \'2000-01-01 0:00:00\';', file=fp)
    print('};', file=fp)
    print('#set relax_naming_rules=1', file=fp)
    print('#set profiler=1', file=fp)
    print('module powerflow {', file=fp)
    print('  solver_method NR;', file=fp)
    print('  line_capacitance TRUE;', file=fp)
    print('};', file=fp)
    print('module tape;', file=fp)
    print('#define VSOURCE=' + str (c[1]), file=fp)
    print('#include \"' + c[0] + '_base.glm\";', file=fp)
    print('object voltdump {', file=fp)
    print('  filename ' + c[0] + '_volt.csv;', file=fp)
    print('  mode polar;', file=fp)
    print('};', file=fp)
    print('object currdump {', file=fp)
    print('  filename ' + c[0] + '_curr.csv;', file=fp)
    print('  mode polar;', file=fp)
    print('};', file=fp)
    fp.close()

bp.close()
