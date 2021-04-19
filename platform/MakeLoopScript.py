import sys
import os
import stat

def append_dss_case(casefiles, inpath, outpath, fp):
  for c in casefiles:
    print('//', file=fp)
    print('clear', file=fp)
    print('cd', inpath, file=fp)
    print('redirect', c + '_base.dss', file=fp)
    print('set maxiterations=80', file=fp)
    print('set controlmode=off', file=fp)
    print('solve', file=fp)
    print('cd', outpath, file=fp)
    print('export summary ', c + '_s.csv', file=fp)
    print('export voltages', c + '_v.csv', file=fp)
    print('export currents', c + '_i.csv', file=fp)
    print('export taps    ', c + '_t.csv', file=fp)

def append_xml_case(casefiles, xmlpath, outpath, fp):
  cimhubpath = '../../CIMHub/target/'
  classpath = '{:s}libs/*:{:s}cimhub-0.0.1-SNAPSHOT.jar'.format (cimhubpath, cimhubpath)
  progname = 'gov.pnnl.gridappsd.cimhub.CIMImporter'
  for c in casefiles:
    print('curl -D- -X POST $DB_URL --data-urlencode "update=drop all"', file=fp)
    print('curl -D- -H "Content-Type: application/xml" --upload-file', xmlpath + c + '.xml', '-X POST $DB_URL', file=fp)
    print('java -cp "{:s}" {:s} -o=both -l=1.0 -i=1'.format (classpath, progname), outpath + c, file=fp)

if sys.platform == 'win32':
  srcpath = 'c:\\gridapps-d\\powergrid-models\\platform\\'
  print ('win32 support has been removed')
  quit ()
elif sys.platform == 'linux':
  srcpath = '/home/tom/src/Powergrid-Models/platform/'
else: # darwin
  srcpath = '/Users/mcde601/src/GRIDAPPSD/Powergrid-Models/platform/'

db_url = 'http://localhost:8889/bigdata/namespace/kb/sparql'

casefiles = ['ACEP_PSIL',
             'EPRI_DPV_J1',
             'IEEE123',
             'IEEE123_PV',
             'IEEE13',
             'IEEE13_Assets',
             'IEEE37',
             'IEEE8500',
             'IEEE8500_3subs',
             'R2_12_47_2',
             'Transactive']

#casefiles = ['R2_12_47_2']

arg = sys.argv[1]
if len(sys.argv) > 2:
  srcpath = sys.argv[2]
if len(sys.argv) > 3:
  db_url = sys.argv[3]

xmlpath = srcpath + 'cimxml/'
dsspath = srcpath + 'test/dss/'
glmpath = srcpath + 'test/glm/'
bothpath = srcpath + 'both/'

if arg == '-b':
  fp = open ('convert_xml.sh', 'w')
  print ('source envars.sh', file=fp)
  print ('rm -rf', bothpath, file=fp)
  print ('rm -rf', dsspath, file=fp)
  print ('rm -rf', glmpath, file=fp)
  print ('mkdir', bothpath, file=fp)
  print ('mkdir', dsspath, file=fp)
  print ('mkdir', glmpath, file=fp)
  append_xml_case(casefiles, xmlpath, bothpath, fp)
  print ('', file=fp)
  fp.close()
  st = os.stat ('convert_xml.sh')
  os.chmod ('convert_xml.sh', st.st_mode | stat.S_IEXEC)

if arg == '-d':
  fp = open ('check.dss', 'w')
  append_dss_case(casefiles, bothpath, dsspath, fp)
  fp.close()

