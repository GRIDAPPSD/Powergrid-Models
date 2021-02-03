import sys
import os
import stat

if sys.platform == 'win32':
  print ('win32 support was removed')
  quit()

casefiles = [['R2_12_47_2',57735.0],
             ['EPRI_DPV_J1',39837.2],
             ['IEEE13',66395.3],
             ['IEEE13_Assets',66395.3],
             ['IEEE8500',66395.3],
             ['IEEE8500_3subs',66395.3],
             ['IEEE37',132790.6],
             ['IEEE123',2401.8],
             ['IEEE123_PV',2401.8],
             ['ACEP_PSIL',277.13],
             ['Transactive',2401.8]]

#casefiles = [['ACEP_PSIL',277.13]]

if sys.platform == 'linux':
  srcpath = '/home/tom/src/Powergrid-Models/platform/'
else: # darwin
  srcpath = '/Users/mcde601/src/GRIDAPPSD/Powergrid-Models/platform/'

if len(sys.argv) > 1:
  srcpath = sys.argv[1]

inpath = srcpath + 'both/'
outpath = srcpath + 'test/glm'
bpname = 'check_glm.sh'
bp = open (bpname, 'w')

for c in casefiles:
  print('cd', inpath, file=bp)
  print('gridlabd -D WANT_VI_DUMP=1', c[0] + '_run.glm >../test/glm/' + c[0] + '.log', file=bp)
  print('mv {:s}*.csv ../test/glm'.format (c[0]), file=bp)
    
  fp = open (inpath + c[0] + '_run.glm', 'w')

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
  print('module generators;', file=fp)
  print('module tape;', file=fp)
  print('module reliability {', file=fp)
  print('  report_event_log false;', file=fp)
  print('};', file=fp)
  print('#define VSOURCE=' + str (c[1]), file=fp)
  print('#include \"' + c[0] + '_base.glm\";', file=fp)
  print('#ifdef WANT_VI_DUMP', file=fp)
  print('object voltdump {', file=fp)
  print('  filename ' + c[0] + '_volt.csv;', file=fp)
  print('  mode POLAR;', file=fp)
  print('};', file=fp)
  print('object currdump {', file=fp)
  print('  filename ' + c[0] + '_curr.csv;', file=fp)
  print('  mode POLAR;', file=fp)
  print('};', file=fp)
  print('#endif', file=fp)
  fp.close()

bp.close()
st = os.stat (bpname)
os.chmod (bpname, st.st_mode | stat.S_IEXEC)

