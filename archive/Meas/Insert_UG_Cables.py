# -*- coding: utf-8 -*-
"""
Created on Mon Dec  3 08:52:23 2018

@author: mukh614
"""

from SPARQLWrapper import SPARQLWrapper2#, JSON
import sys
import re
import uuid

import constants

if len(sys.argv) < 2:
	print ('usage: python InsertMeasurements.py fname')
	print (' (Blazegraph server must already be started)')
	exit()


fp = open (sys.argv[1], 'r')
#fp = open ('CNCables_filled.csv', 'r')

sparql = SPARQLWrapper2 (constants.blazegraph_url)
sparql.method = 'POST'


lines = fp.readlines()
first_line = lines[0]
name = re.split('[,\s]+', first_line)
lines.pop(0)  # removing headers
for ln in lines:
    toks = re.split('[,\s]+', ln)
    print(toks)
    measid = uuid.uuid4()
    if (not str(measid).startswith("_")):
        measid = "_"+str(measid)
    resource = '<' + constants.blazegraph_url + '#' + str(measid) + '>'

    ln1 = resource + ' a c:' + 'ConcentricNeutralCableInfo. ' 
    ln2 = resource + ' c:IdentifiedObject.mRID \"' + str(measid) + '\". '
    ln3 = resource + ' c:'+ name[0] + '\"' + str(toks[0]) + '\". '
    ln4 = resource + ' c:' + name[2] + '\"' + toks[2] + '\".'
    ln5 = resource + ' c:' + name[3] + '\"' + toks[3] + '\".'
    ln6 = resource + ' c:' + name[4] + '\"' + toks[4] + '\".'
    ln7 = resource + ' c:' + name[5] + '\"' + toks[5] + '\".'
    ln8 = resource + ' c:' + name[6] + '\"' + toks[6] + '\".'
    ln9 = resource + ' c:' + name[7] + '\"' + toks[7] + '\".'
    ln10 = resource + ' c:' + name[8] + '\"' + toks[8] + '\".'
    ln11 = resource + ' c:' + name[9] + '\"' + toks[9] + '\".'
    ln12 = resource + ' c:' + name[10] + '\"' + toks[10] + '\".'
    ln13 = resource + ' c:' + name[11] + '\"' + toks[11] + '\".'
    ln14 = resource + ' c:' + name[12] + '\"' + toks[12] + '\".'
    ln15 = resource + ' c:' + name[13] + '\"' + toks[13] + '\".'
    ln16 = resource + ' c:' + name[14] + '\"' + toks[14] + '\".'
    ln17 = resource + ' c:' + name[15] + '\"' + toks[15] + '\".'
    ln18 = resource + ' c:' + name[16] + '\"' + toks[16] + '\".'
    ln19 = resource + ' c:' + name[17] + '\"' + toks[17] + '\".'
    ln20 = resource + ' c:' + name[18] + '\"' + toks[18] + '\".'
    ln21 = resource + ' c:' + name[19] + '\"' + toks[19] + '\".'
    ln22 = resource + ' c:' + name[20] + '\"' + toks[20] + '\".'
    ln23 = resource + ' c:' + name[21] + '\"' + toks[21] + '\".'
    ln24 = resource + ' c:' + name[22] + '\"' + toks[22] + '\".'
    ln25 = resource + ' c:' + name[23] + '\"' + toks[23] + '\".'
    ln26 = resource + ' c:' + name[24] + '\"' + toks[24] + '\".'
    ln27 = resource + ' c:' + name[25] + '\"' + toks[25] + '\".'


    qstr = (constants.prefix + 'INSERT DATA { ' + ln1 + ln2 + ln3 + ln4 +
		ln5 + ln6 + ln7 + ln8 + ln9 + ln10 + ln11 + ln12 + ln13 + ln14 + ln15 + ln16 +ln17 + ln18 + ln19 + ln20 + ln21 + ln22
        + ln23 + ln24 + ln25 + ln26 + ln27 + '}')
    print (qstr)
	
    sparql.setQuery(qstr)
    ret = sparql.query()
    print (ret)

