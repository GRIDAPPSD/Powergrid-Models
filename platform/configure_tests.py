# Copyright (C) 2021-2022 Battelle Memorial Institute
# file: configure_tests.py

import sys
import os

if __name__ == '__main__':
  bDocker = False
  if len(sys.argv) > 1:
    if sys.argv[1] == 'docker':
      bDocker = True

  if bDocker:
    print ('configuring platform tests to use Blazegraph in Docker on {:s}'.format (sys.platform))
    os.system ('cp cimhubdocker.json cimhubconfig.json')
    if sys.platform == 'win32':
      os.system ('copy envars_docker.bat envars.bat')
      os.system ('copy DER\\envars_docker.bat DER\\envars.bat')
    else:
      os.system ('cp envars_docker.sh envars.sh')
      os.system ('cp DER/envars_docker.sh DER/envars.sh')
  else:
    print ('configuring platform tests to use Blazegraph in Jar file on {:s}'.format (sys.platform))
    print ('re-run with "docker" argument to use Blazegraph in Docker instead of Jar')
    os.system ('cp cimhubjar.json cimhubconfig.json')
    if sys.platform == 'win32':
      os.system ('copy envars_jar.bat envars.bat')
      os.system ('copy DER\\envars_jar.bat DER\\envars.bat')
    else:
      os.system ('cp envars_jar.sh envars.sh')
      os.system ('cp DER/envars_jar.sh DER/envars.sh')


