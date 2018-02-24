from SPARQLWrapper import SPARQLWrapper, SPARQLWrapper2, JSON

#sparql = SPARQLWrapper("http://localhost:9999/blazegraph/namespace/kb/sparql")
sparql = SPARQLWrapper2("http://localhost:9999/blazegraph/namespace/kb/sparql")

sparql.setQuery("""
    PREFIX r: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX c: <http://iec.ch/TC57/2012/CIM-schema-cim17#>
    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
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
#print (ret.bindings)
for b in ret.bindings:
	print (b['feeder'].value,b['fid'].value)

#sparql.setReturnFormat(JSON)
#results = sparql.query().convert()
#print (results)
#for result in results["results"]["bindings"]:
#    print(result["feeder"]["value"],result["fid"]["value"],result["station"]["value"],
#          result["subregion"]["value"],result["region"]["value"])


   
