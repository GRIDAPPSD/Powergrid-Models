from SPARQLWrapper import SPARQLWrapper2#, JSON
# constants.py is used for configuring blazegraph.
import constants

sparql = SPARQLWrapper2(constants.blazegraph_url)

sparql.setQuery(constants.prefix + 
    """
    SELECT ?feeder ?fid ?station ?sid ?subregion ?sgrid ?region ?rgnid WHERE {
     ?s r:type c:Feeder.
     ?s c:IdentifiedObject.name ?feeder.
     ?s c:IdentifiedObject.mRID ?fid.
     ?s c:Feeder.NormalEnergizingSubstation ?sub.
     ?sub c:IdentifiedObject.name ?station.
     ?sub c:IdentifiedObject.mRID ?sid.
     ?sub c:Substation.Region ?sgr.
     ?sgr c:IdentifiedObject.name ?subregion.
     ?sgr c:IdentifiedObject.mRID ?sgrid.
     ?sgr c:SubGeographicalRegion.Region ?rgn.
     ?rgn c:IdentifiedObject.name ?region.
     ?rgn c:IdentifiedObject.mRID ?rgnid.
    }
    ORDER by ?station ?feeder
""")

ret = sparql.query()
print ('binding keys are:',ret.variables)
for b in ret.bindings:
    print (b['feeder'].value,b['fid'].value)
   
