from SPARQLWrapper import SPARQLWrapper2#, JSON
import sys
# constants.py is used for configuring blazegraph.
import constants

# if a SPARQL triple is OPTIONAL, then it won't appear in the result bindings
# this helper function checks the binding first, then assigns default as needed
def optionalValue (row, key, default):
	if key in row:
		return row[key].value
	return default

if len(sys.argv) < 2:
	print ('usage: python ListOverheadWires.py output_filename')
	print (' (Blazegraph server must already be started, with model data loaded)')
	exit()

fname = sys.argv[1]
op = open (fname, 'w')
sparql = SPARQLWrapper2(constants.blazegraph_url)

qstr = constants.prefix16 + """SELECT DISTINCT ?name ?mRID ?radius ?gmr ?rDC20 ?rAC25 ?rAC50 ?rAC75 
 ?ratedCurrent ?material ?insulated ?insulationMaterial ?insulationThickness ?strandCount ?coreStrandCount
WHERE {
 ?w r:type c:ConcentricNeutralCableInfo.
 ?w c:IdentifiedObject.name ?name.
 ?w c:IdentifiedObject.mRID ?mRID.
 ?w c:WireInfo.radius ?radius.
 ?w c:WireInfo.gmr ?gmr.
 OPTIONAL {?w c:WireInfo.strandCount ?strandCount.}
 OPTIONAL {?w c:WireInfo.coreStrandCount ?coreStrandCount.}
 OPTIONAL {?w c:WireInfo.rDC20 ?rDC20.}
 OPTIONAL {?w c:WireInfo.rAC25 ?rAC25.}
 OPTIONAL {?w c:WireInfo.rAC50 ?rAC50.}
 OPTIONAL {?w c:WireInfo.rAC75 ?rAC75.}
 OPTIONAL {?w c:WireInfo.coreRadius ?coreRadius.}
 OPTIONAL {?w c:WireInfo.ratedCurrent ?ratedCurrent.}
 OPTIONAL {?w c:WireInfo.material ?matraw.
     bind(strafter(str(?matraw),"WireMaterialKind.") as ?material)}
 OPTIONAL {?w c:WireInfo.insulationMaterial ?insraw.
     bind(strafter(str(?insraw),"WireInsulationKind.") as ?insulationMaterial)}
 OPTIONAL {?w c:WireInfo.insulated ?insulated.}
 OPTIONAL {?w c:WireInfo.insulationThickness ?insulationThickness.}
 OPTIONAL {?w c:CableInfo.diameterOverCore ?diameterOverCore.}
 OPTIONAL {?w c:CableInfo.diameterOverJacket ?diameterOverJacket.}
 OPTIONAL {?w c:CableInfo.diameterOverInsulation ?diameterOverInsulation.}
 OPTIONAL {?w c:CableInfo.diameterOverScreen ?diameterOverScreen.}
 OPTIONAL {?w c:CableInfo.sheathAsNeutral ?sheathAsNeutral.}
 OPTIONAL {?w c:ConcentricNeutralCableInfo.diameterOverNeutral ?diameterOverNeutral.}
 OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandCount ?neutralStrandCount.}
 OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandGmr ?neutralStrandGmr.}
 OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandRadius ?neutralStrandRadius.}
 OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandRDC20 ?neutralStrandRDC20.}
}
ORDER BY ?name 
"""
#print (qstr)
sparql.setQuery(qstr)
ret = sparql.query()
#print ('\nConcentricNeutralCableInfo binding keys are:',ret.variables)
print ('IdentifiedObject.name', 'IdentifiedObject.mRID', 'WireInfo.radius', 'WireInfo.gmr',
			 'WireInfo.rDC20', 'WireInfo.rAC25', 'WireInfo.rAC50', 'WireInfo.rAC75', 'WireInfo.ratedCurrent',
			 'WireInfo.material', 'WireInfo.insulated', 'WireInfo.insulationMaterial', 'WireInfo.insulationThickness',
			 'WireInfo.coreRadius', 'WireInfo.strandCount', 'WireInfo.coreStrandCount', 'CableInfo.diameterOverCore',
			 'CableInfo.diameterOverJacket', 'CableInfo.diameterOverInsulation', 'CableInfo.diameterOverScreen', 
			 'CableInfo.sheathAsNeutral', 'ConcentricNeutralCableInfo.diameterOverNeutral',
			 'ConcentricNeutralCableInfo.neutralStrandCount', 'ConcentricNeutralCableInfo.neutralStrandGmr',
			 'ConcentricNeutralCableInfo.neutralStrandRadius', 'ConcentricNeutralCableInfo.neutralStrandRDC20',
			 sep=',', file=op)
for b in ret.bindings:
	print (b['name'].value, b['mRID'].value, b['radius'].value, b['gmr'].value,
				 optionalValue (b, 'rDC20', 0), 
				 optionalValue (b, 'rAC25', 0), 
				 optionalValue (b, 'rAC50', 0), 
				 optionalValue (b, 'rAC75', 0), 
				 optionalValue (b, 'ratedCurrent', 0), 
				 optionalValue (b, 'material', 'aluminum'), 
				 optionalValue (b, 'insulated', 'True'), 
				 optionalValue (b, 'insulationMaterial', 'crosslinkedPolyethylene'), 
				 optionalValue (b, 'insulationThickness', 0), 
				 optionalValue (b, 'coreRadius', 0), 
				 optionalValue (b, 'strandCount', 0), 
				 optionalValue (b, 'coreStrandCount', 0), 
				 optionalValue (b, 'diameterOverCore', 0), 
				 optionalValue (b, 'diameterOverJacket', 0), 
				 optionalValue (b, 'diameterOverInsulation', 0), 
				 optionalValue (b, 'diameterOverScreen', 0), 
				 optionalValue (b, 'sheathAsNeutral', 'True'), 
				 optionalValue (b, 'diameterOverNeutral', 0), 
				 optionalValue (b, 'neutralStrandCount', 0), 
				 optionalValue (b, 'neutralStrandGmr', 0), 
				 optionalValue (b, 'neutralStrandRadius', 0), 
				 optionalValue (b, 'neutralStrandRDC20', 0), 
				 sep=',', file=op)

op.close()
