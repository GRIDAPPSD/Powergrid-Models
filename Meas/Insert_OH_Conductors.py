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

    ln1 = resource + ' a c:' + 'OverheadWireInfo. ' 
    ln2 = resource + ' c:IdentifiedObject.mRID \"' + str(measid) + '\". '
    ln3 = resource + ' c:'+ name[0] + ' \"' + str(toks[0]) + '\". '
    ln4 = resource + ' c:' + name[2] + ' \"' + toks[2] + '\".'
    ln5 = resource + ' c:' + name[3] + ' \"' + toks[3] + '\".'
    ln6 = resource + ' c:' + name[4] + ' \"' + toks[4] + '\".'
    ln7 = resource + ' c:' + name[5] + ' \"' + toks[5] + '\".'
    ln8 = resource + ' c:' + name[6] + ' \"' + toks[6] + '\".'
    ln9 = resource + ' c:' + name[7] + ' \"' + toks[7] + '\".'
    ln10 = resource + ' c:' + name[8] + ' \"' + toks[8] + '\".'
    ln11 = resource + ' c:' + name[9] + ' \"' + toks[9] + '\".'
    ln12 = resource + ' c:' + name[10] + ' \"' + toks[10] + '\".'
    ln13 = resource + ' c:' + name[11] + ' \"' + toks[11] + '\".'
    ln14 = resource + ' c:' + name[12] + ' \"' + toks[12] + '\".'
    ln15 = resource + ' c:' + name[13] + ' \"' + toks[13] + '\".'
    ln16 = resource + ' c:' + name[14] + ' \"' + toks[14] + '\".'
    ln17 = resource + ' c:' + name[15] + ' \"' + toks[15] + '\"'

	    # qstr = (constants.prefix + 'INSERT DATA { ' + ln1 + ln2 + ln3 + ln4 +
		# ln5 + ln6 + ln7 + ln8 + ln9 + ln10 + ln11 + ln12 + ln13 + ln14 + ln15 + ln16 +ln17 + '}')
    qstr = (constants.prefix + 'INSERT DATA { ' + ln1 + ln2 + ln3 + ln4 + ln5 + ln6 + ln7 + ln8 + ln9 + ln10
	+ ln11 + ln12 + ln13 + ln14 + ln15 + ln16 + ln17 +'}')
    print (qstr)
	
    sparql.setQuery(qstr)
    ret = sparql.query()
    print (ret)

