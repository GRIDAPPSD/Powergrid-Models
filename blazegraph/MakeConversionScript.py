import sys;

def append_cases(casefiles, dsspath, outpath, region, subregion, fp):
    for c in casefiles:
        print('//', file=fp)
        print('cd', dsspath, file=fp)
        print('redirect', c[0] + '.dss', file=fp)
        print('set maxiterations=20', file=fp)
        print('solve', file=fp)
        print('cd', outpath, file=fp)
        print('export summary', c[1] + '.csv', file=fp)
        print('export cdpsmcombined', 'file=' + c[1] + '.xml', 'geo=' + region, 'subgeo=' + subregion, file=fp)

fp = open ("ConvertCDPSM.dss", "w")

if sys.platform == 'win32':
    outpath = 'c:/gridapps-d/powergrid-models/blazegraph/test/'
    cimpath = 'c:/OpenDSS/Test/'
    ieeepath = 'c:/OpenDSS/Distrib/IEEETestCases/'
    epripath = 'c:/OpenDSS/Distrib/EPRITestCircuits/'
    taxpath = 'c:/gridapps-d/powergrid-models/taxonomy/'
    dpvpath = 'c:/epri_dpv/'
elif sys.platform == 'linux':
    srcpath = '/home/mcde601/src/'
    outpath = srcpath + 'Powergrid-Models/blazegraph/test/'
    cimpath = srcpath + 'OpenDSS/Test/'
    ieeepath = srcpath + 'OpenDSS/Distrib/IEEETestCases/'
    epripath = srcpath + 'OpenDSS/Distrib/EPRITestCircuits/'
    taxpath = srcpath + 'Powergrid-Models/taxonomy/'
    dpvpath = srcpath + 'epri_dpv/'
else:
    srcpath = '/Users/mcde601/src/'
    outpath = srcpath + 'GRIDAPPSD/Powergrid-Models/blazegraph/test/'
    cimpath = srcpath + 'opendss/Test/'
    ieeepath = srcpath + 'opendss/Distrib/IEEETestCases/'
    epripath = srcpath + 'opendss/Distrib/EPRITestCircuits/'
    taxpath = srcpath + 'GRIDAPPSD/Powergrid-Models/taxonomy/'
    dpvpath = srcpath + 'epri_dpv/'

casefiles = [['IEEE13_CDPSM', 'IEEE13'],
             ['IEEE13_Assets', 'IEEE13_Assets']]
append_cases(casefiles, cimpath, outpath, 'ieee', 'test', fp)

#casefiles = [['./8500-Node/Master', 'IEEE8500'],
#            ['./34Bus/ieee34Mod2', 'IEEE34'],
#            ['./37Bus/ieee37', 'IEEE37'],
#            ['./123Bus/IEEE123Master', 'IEEE123'],
#            ['./4Bus-DY-Bal/4Bus-DY-Bal', 'DY-bal'],
#            ['./4Bus-GrdYD-Bal/4Bus-GrdYD-Bal', 'GYD-bal'],
#            ['./4Bus-OYOD-Bal/4Bus-OYOD-Bal', 'OYOD-bal'],
#            ['./4Bus-OYOD-UnBal/4Bus-OYOD-UnBal', 'OYOD-unbal'],
#            ['./4Bus-YD-Bal/4Bus-YD-Bal', 'YD-bal'],
#            ['./4Bus-YY-Bal/4Bus-YY-Bal', 'YY-bal'],
#            ['./8500-Node/Master-unbal', 'IEEE8500u']]
casefiles = [['./8500-Node/Master', 'IEEE8500'],
            ['./123Bus/IEEE123Master', 'IEEE123']]
append_cases(casefiles, ieeepath, outpath, 'ieee', 'large',  fp)

#casefiles = [['./ckt5/Master_ckt5', 'EPRI5'],
#             ['./ckt7/Master_ckt7', 'EPRI7'],
#             ['./ckt24/master_ckt24', 'EPRI24']]
#append_cases(casefiles, epripath, outpath, 'epri', 'large', fp)
#
#casefiles = [['./new_GC_12_47_1/Master', 'GC_12_47_1'],
#             ['./new_R1_12_47_1/Master', 'R1_12_47_1'],
#             ['./new_R1_12_47_2/Master', 'R1_12_47_2'],
#             ['./new_R1_12_47_3/Master', 'R1_12_47_3'],
#             ['./new_R1_12_47_4/Master', 'R1_12_47_4'],
#             ['./new_R1_25_00_1/Master', 'R1_25_00_1'],
#             ['./new_R2_12_47_1/Master', 'R2_12_47_1'],
#             ['./new_R2_12_47_2/Master', 'R2_12_47_2'],
#             ['./new_R2_12_47_3/Master', 'R2_12_47_3'],
#             ['./new_R2_25_00_1/Master', 'R2_25_00_1'],
#             ['./new_R2_35_00_1/Master', 'R2_35_00_1'],
#             ['./new_R3_12_47_1/Master', 'R3_12_47_1'],
#             ['./new_R3_12_47_2/Master', 'R3_12_47_2'],
#             ['./new_R3_12_47_3/Master', 'R3_12_47_3'],
#             ['./new_R4_12_47_1/Master', 'R4_12_47_1'],
#             ['./new_R4_12_47_2/Master', 'R4_12_47_2'],
#             ['./new_R4_25_00_1/Master', 'R4_25_00_1'],
#             ['./new_R5_12_47_1/Master', 'R5_12_47_1'],
#             ['./new_R5_12_47_2/Master', 'R5_12_47_2'],
#             ['./new_R5_12_47_3/Master', 'R5_12_47_3'],
#             ['./new_R5_12_47_4/Master', 'R5_12_47_4'],
#             ['./new_R5_12_47_5/Master', 'R5_12_47_5'],
#             ['./new_R5_25_00_1/Master', 'R5_25_00_1'],
#             ['./new_R5_35_00_1/Master', 'R5_35_00_1']]
casefiles = [['./new_R2_12_47_2/Master', 'R2_12_47_2']]
append_cases(casefiles, taxpath, outpath, 'pnnl', 'taxonomy', fp)
#
casefiles = [['./J1/Master_noPV', 'EPRI_DPV_J1']]
#             ['./K1/Master_NoPV', 'EPRI_DPV_K1'],
#             ['./M1/master_NoPV', 'EPRI_DPV_M1']]
append_cases(casefiles, dpvpath, outpath, 'epri', 'dpv', fp)

fp.close()
