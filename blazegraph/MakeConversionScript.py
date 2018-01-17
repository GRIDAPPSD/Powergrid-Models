import sys;

def append_cases(casefiles, dsspath, outpath, fp):
    for c in casefiles:
        print('//', file=fp)
        print('cd', dsspath, file=fp)
        print('redirect', c[0] + '.dss', file=fp)
        print('set maxiterations=20', file=fp)
        print('solve', file=fp)
        print('cd', outpath, file=fp)
        print('export summary', c[1] + '.csv', file=fp)
        print('export cdpsmcombined', c[1] + '.xml', file=fp)

fp = open ("ConvertCDPSM.dss", "w")
outpath = 'c:/gridapps-d/powergrid-models/blazegraph/test/'

dsspath = 'c:/OpenDSS/Test/'
casefiles = [['IEEE13_CDPSM', 'IEEE13'],
             ['IEEE13_Assets', 'IEEE13_Assets']]
append_cases(casefiles, dsspath, outpath, fp)

dsspath = 'c:/OpenDSS/Distrib/IEEETestCases/'
casefiles = [['./8500-Node/Master', 'IEEE8500'],
             ['./34Bus/ieee34Mod2', 'IEEE34'],
             ['./37Bus/ieee37', 'IEEE37'],
             ['./123Bus/IEEE123Master', 'IEEE123'],
             ['./4Bus-DY-Bal/4Bus-DY-Bal', 'DY-bal'],
             ['./4Bus-GrdYD-Bal/4Bus-GrdYD-Bal', 'GYD-bal'],
             ['./4Bus-OYOD-Bal/4Bus-OYOD-Bal', 'OYOD-bal'],
             ['./4Bus-OYOD-UnBal/4Bus-OYOD-UnBal', 'OYOD-unbal'],
             ['./4Bus-YD-Bal/4Bus-YD-Bal', 'YD-bal'],
             ['./4Bus-YY-Bal/4Bus-YY-Bal', 'YY-bal'],
             ['./8500-Node/Master-unbal', 'IEEE8500u']]
append_cases(casefiles, dsspath, outpath, fp)

dsspath = 'c:/OpenDSS/Distrib/EPRITestCircuits/'
casefiles = [['./ckt5/Master_ckt5', 'EPRI5'],
             ['./ckt7/Master_ckt7', 'EPRI7'],
             ['./ckt24/master_ckt24', 'EPRI24']]
append_cases(casefiles, dsspath, outpath, fp)

dsspath = 'c:/gridapps-d/powergrid-models/taxonomy/'
casefiles = [['./test_R1_12_47_1/Master', 'R1_12_47_1'],
             ['./test_R1_12_47_2/Master', 'R1_12_47_2'],
             ['./test_R1_12_47_3/Master', 'R1_12_47_3'],
             ['./test_R1_12_47_4/Master', 'R1_12_47_4'],
             ['./test_R1_25_00_1/Master', 'R1_25_00_1'],
             ['./test_R2_12_47_1/Master', 'R2_12_47_1'],
             ['./test_R2_12_47_2/Master', 'R2_12_47_2'],
             ['./test_R2_12_47_3/Master', 'R2_12_47_3'],
             ['./test_R2_25_00_1/Master', 'R2_25_00_1'],
             ['./test_R2_35_00_1/Master', 'R2_35_00_1'],
             ['./test_R3_12_47_1/Master', 'R3_12_47_1'],
             ['./test_R3_12_47_2/Master', 'R3_12_47_2'],
             ['./test_R3_12_47_3/Master', 'R3_12_47_3'],
             ['./test_R4_12_47_1/Master', 'R4_12_47_1'],
             ['./test_R4_12_47_2/Master', 'R4_12_47_2'],
             ['./test_R4_25_00_1/Master', 'R4_25_00_1'],
             ['./test_R5_12_47_1/Master', 'R5_12_47_1'],
             ['./test_R5_12_47_2/Master', 'R5_12_47_2'],
             ['./test_R5_12_47_3/Master', 'R5_12_47_3'],
             ['./test_R5_12_47_4/Master', 'R5_12_47_4'],
             ['./test_R5_12_47_5/Master', 'R5_12_47_5'],
             ['./test_R5_25_00_1/Master', 'R5_25_00_1'],
             ['./test_R5_35_00_1/Master', 'R5_35_00_1']]
append_cases(casefiles, dsspath, outpath, fp)

dsspath = 'c:/epri_dpv/'
casefiles = [['./J1/Master_noPV', 'EPRI_DPV_J1'],
             ['./K1/Master_NoPV', 'EPRI_DPV_K1'],
             ['./M1/master_NoPV', 'EPRI_DPV_M1']]
append_cases(casefiles, dsspath, outpath, fp)

fp.close()
