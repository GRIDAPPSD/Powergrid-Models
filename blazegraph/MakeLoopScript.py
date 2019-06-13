import sys;

def append_dss_case(casefiles, dsspath, fp):
    for c in casefiles:
        print('//', file=fp)
        print('clear', file=fp)
        print('cd', dsspath, file=fp)
        print('redirect', c + '_base.dss', file=fp)
        print('set maxiterations=20', file=fp)
        print('set controlmode=off', file=fp)
        print('solve', file=fp)
        print('export summary', c + '.csv', file=fp)

def append_xml_case(casefiles, xmlpath, dsspath, glmpath, fp):
    for c in casefiles:
        if sys.platform == 'win32':
            print('call drop_all.bat', file=fp)
            print('curl -D- -H "Content-Type: application/xml" --upload-file', 
                  xmlpath + c + '.xml',
                  '-X POST "http://localhost:9999/blazegraph/sparql"', file=fp)
            print('java gov.pnnl.goss.cim2glm.CIMImporter -o=dss -l=1.0 -i=1', 
                  dsspath + c, file=fp)
            print('java gov.pnnl.goss.cim2glm.CIMImporter -o=glm -l=1.0 -i=1', 
                  glmpath + c, file=fp)
        elif sys.platform == 'linux':
            print('./drop_all.sh', file=fp)
            print('curl -D- -H "Content-Type: application/xml" --upload-file', 
                  xmlpath + c + '.xml',
                  '-X POST "http://localhost:9999/blazegraph/sparql"', file=fp)
            print('java -classpath "target/*:/home/mcde601/src/apache-jena-3.6.0/lib/*:/home/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=dss -l=1.0 -i=1', 
                  dsspath + c, file=fp)
            print('java -classpath "target/*:/home/mcde601/src/apache-jena-3.6.0/lib/*:/home/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=glm -l=1.0 -i=1', 
                  glmpath + c, file=fp)
        else:
            print('./drop_all.sh', file=fp)
            print('curl -D- -H "Content-Type: application/xml" --upload-file', 
                  xmlpath + c + '.xml',
                  '-X POST "http://localhost:9999/blazegraph/sparql"', file=fp)
            print('java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=dss -l=1.0 -i=1', 
                  dsspath + c, file=fp)
            print('java -classpath "target/*:/Users/mcde601/src/apache-jena-3.6.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" gov.pnnl.goss.cim2glm.CIMImporter -o=glm -l=1.0 -i=1', 
                  glmpath + c, file=fp)

if sys.platform == 'win32':
    xmlpath = 'c:\\gridapps-d\\powergrid-models\\blazegraph\\test\\'
    dsspath = 'c:\\gridapps-d\\powergrid-models\\blazegraph\\dss\\'
    glmpath = 'c:\\gridapps-d\\powergrid-models\\blazegraph\\glm\\'
elif sys.platform == 'linux':
    srcpath = '/home/mcde601/src/Powergrid-Models/blazegraph/'
    xmlpath = srcpath + 'test/'
    dsspath = srcpath + 'dss/'
    glmpath = srcpath + 'glm/'
else:
    srcpath = '/Users/mcde601/src/GRIDAPPSD/Powergrid-Models/blazegraph/'
    xmlpath = srcpath + 'test/'
    dsspath = srcpath + 'dss/'
    glmpath = srcpath + 'glm/'

#casefiles = ['IEEE13',
#             'IEEE13_Assets',
#             'IEEE8500',
#             'IEEE34',
#             'IEEE37',
#             'IEEE123',
#             'DY-bal',
#             'GYD-bal',
#             'OYOD-bal',
#             'OYOD-unbal',
#             'YD-bal',
#             'YY-bal',
#             'IEEE8500u',
#             'EPRI5',
#             'EPRI7',
#             'EPRI24',
#             'GC_12_47_1',
#             'R1_12_47_1',
#             'R1_12_47_2',
#             'R1_12_47_3',
#             'R1_12_47_4',
#             'R1_25_00_1',
#             'R2_12_47_1',
#             'R2_12_47_2',
#             'R2_12_47_3',
#             'R2_25_00_1',
#             'R2_35_00_1',
#             'R3_12_47_1',
#             'R3_12_47_2',
#             'R3_12_47_3',
#             'R4_12_47_1',
#             'R4_12_47_2',
#             'R4_25_00_1',
#             'R5_12_47_1',
#             'R5_12_47_2',
#             'R5_12_47_3',
#             'R5_12_47_4',
#             'R5_12_47_5',
#             'R5_25_00_1',
#             'R5_35_00_1',
#             'EPRI_DPV_J1',
#             'EPRI_DPV_K1',
#             'EPRI_DPV_M1']

#casefiles = ['IEEE13',
#             'IEEE13_Assets',
#             'IEEE8500',
#             'IEEE123',
#             'R2_12_47_2',
#             'EPRI_DPV_J1']

casefiles = ['R2_12_47_2']

#casefiles = ['IEEE8500_3subs']

arg = sys.argv[1]

if arg == '-b':
    if sys.platform == 'win32':
        fp = open ("convert_xml.bat", "w")
        print ('mkdir', dsspath, file=fp)
        print ('mkdir', glmpath, file=fp)
        print ('del /q /y', dsspath + '*.*', file=fp)
        print ('del /q /y', glmpath + '*.*', file=fp)
        print ('set JENA_HOME=c:\\apache-jena-3.6.0', file=fp)
        print ('set CLASSPATH=target/*;c:/apache-jena-3.6.0/lib/*;c:/commons-math3-3.6.1/*', file=fp)
    else:
        fp = open ("convert_xml.sh", "w")
        print ('mkdir', dsspath, file=fp)
        print ('mkdir', glmpath, file=fp)
        print ('rm', dsspath + '*.*', file=fp)
        print ('rm', glmpath + '*.*', file=fp)
    append_xml_case(casefiles, xmlpath, dsspath, glmpath, fp)
    print ('', file=fp)

if arg == '-d':
    fp = open ("check.dss", "w")
    append_dss_case(casefiles, dsspath, fp)

fp.close()
