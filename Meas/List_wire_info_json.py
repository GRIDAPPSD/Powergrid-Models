# -*- coding: utf-8 -*-
"""
Created on Mon Dec  3 15:40:05 2018

@author: mukh614
"""

from SPARQLWrapper import SPARQLWrapper2#, JSON
import sys
# constants.py is used for configuring blazegraph.
import constants
import json

sparql = SPARQLWrapper2(constants.blazegraph_url)


qstr_OH = constants.prefix + """SELECT DISTINCT ?name ?mrid ?rad ?corerad ?gmr ?rdc ?r25 ?r50 ?r75 ?amps ?ins ?insmat ?insthick WHERE {
# feeder selection options - if all commented out, query matches all feeders
#VALUES ?fdrid {"_C1C3E687-6FFD-C753-582B-632A27E28507"}  # 123 bus
#VALUES ?fdrid {"_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62"}  # 13 bus
#VALUES ?fdrid {"_5B816B93-7A5F-B64C-8460-47C17D6E4B0F"}  # 13 bus assets
#VALUES ?fdrid {"_4F76A5F9-271D-9EB8-5E31-AA362D86F2C3"}  # 8500 node
#VALUES ?fdrid {"_67AB291F-DCCD-31B7-B499-338206B9828F"}  # J1
#VALUES ?fdrid {"_9CE150A8-8CC5-A0F9-B67E-BBD8C79D3095"}  # R2 12.47 3
 ?w r:type c:OverheadWireInfo.
 ?w c:IdentifiedObject.mRID ?mrid.
 ?w c:IdentifiedObject.name ?name.
 ?w c:WireInfo.radius ?rad.
 ?w c:WireInfo.gmr ?gmr.
 OPTIONAL {?w c:WireInfo.rDC20 ?rdc.}
 OPTIONAL {?w c:WireInfo.rAC25 ?r25.}
 OPTIONAL {?w c:WireInfo.rAC50 ?r50.}
 OPTIONAL {?w c:WireInfo.rAC75 ?r75.}
 OPTIONAL {?w c:WireInfo.coreRadius ?corerad.}
 OPTIONAL {?w c:WireInfo.ratedCurrent ?amps.}
 OPTIONAL {?w c:WireInfo.insulationMaterial ?insraw.
     bind(strafter(str(?insraw),"WireInsulationKind.") as ?insmat)}
 OPTIONAL {?w c:WireInfo.insulated ?ins.}
 OPTIONAL {?w c:WireInfo.insulationThickness ?insthick.}
}
ORDER BY ?name
"""


qstr_CN = constants.prefix + """SELECT DISTINCT ?name ?rad ?corerad ?gmr ?rdc ?r25 ?r50 ?r75 ?amps ?ins ?insmat ?insthick ?diacore ?diains ?diascreen ?diajacket ?dianeut ?sheathneutral 
       ?strand_cnt ?strand_rad ?strand_gmr ?strand_rdc
WHERE {
# feeder selection options - if all commented out, query matches all feeders
#VALUES ?fdrid {"_C1C3E687-6FFD-C753-582B-632A27E28507"}  # 123 bus
#VALUES ?fdrid {"_49AD8E07-3BF9-A4E2-CB8F-C3722F837B62"}  # 13 bus
#VALUES ?fdrid {"_5B816B93-7A5F-B64C-8460-47C17D6E4B0F"}  # 13 bus assets
#VALUES ?fdrid {"_4F76A5F9-271D-9EB8-5E31-AA362D86F2C3"}  # 8500 node
#VALUES ?fdrid {"_67AB291F-DCCD-31B7-B499-338206B9828F"}  # J1
#VALUES ?fdrid {"_9CE150A8-8CC5-A0F9-B67E-BBD8C79D3095"}  # R2 12.47 3
 ?w r:type c:ConcentricNeutralCableInfo.
 ?w c:IdentifiedObject.name ?name.
 ?w c:WireInfo.radius ?rad.
 ?w c:WireInfo.gmr ?gmr.
 OPTIONAL {?w c:WireInfo.rDC20 ?rdc.}
 OPTIONAL {?w c:WireInfo.rAC25 ?r25.}
 OPTIONAL {?w c:WireInfo.rAC50 ?r50.}
 OPTIONAL {?w c:WireInfo.rAC75 ?r75.}
 OPTIONAL {?w c:WireInfo.coreRadius ?corerad.}
 OPTIONAL {?w c:WireInfo.ratedCurrent ?amps.}
 OPTIONAL {?w c:WireInfo.insulationMaterial ?insraw.
     bind(strafter(str(?insraw),"WireInsulationKind.") as ?insmat)}
 OPTIONAL {?w c:WireInfo.insulated ?ins.}
 OPTIONAL {?w c:WireInfo.insulationThickness ?insthick.}
 OPTIONAL {?w c:CableInfo.diameterOverCore ?diacore.}
 OPTIONAL {?w c:CableInfo.diameterOverJacket ?diajacket.}
 OPTIONAL {?w c:CableInfo.diameterOverInsulation ?diains.}
 OPTIONAL {?w c:CableInfo.diameterOverScreen ?diascreen.}
 OPTIONAL {?w c:CableInfo.sheathAsNeutral ?sheathneutral.}
 OPTIONAL {?w c:ConcentricNeutralCableInfo.diameterOverNeutral ?dianeut.}
 OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandCount ?strand_cnt.}
 OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandGmr ?strand_gmr.}
 OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandRadius ?strand_rad.}
 OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandRDC20 ?strand_rdc.}
}
ORDER BY ?name
"""
if 'overhead' in sys.argv[1]:
    sparql.setQuery(qstr_OH)
if 'underground' in sys.argv[1]:
    sparql.setQuery(qstr_CN)

ret = sparql.query()
Property = {}
for b in ret.bindings:
    for keys in b:
        if 'name' in keys:
            object_name = b[keys].value
            print(object_name)
            Property[object_name] =  {}
        else:
            Property[object_name][keys] =  b[keys].value
    print(Property)
file_name = str(sys.argv[1])+'.json'
with open(file_name, 'w') as fp:
    json.dump(Property, fp, sort_keys=True, indent=4)        
    
