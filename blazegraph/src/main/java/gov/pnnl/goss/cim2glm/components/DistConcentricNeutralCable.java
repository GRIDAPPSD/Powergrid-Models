package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;

public class DistConcentricNeutralCable extends DistCable {
	public static final String szQUERY = 
		"SELECT ?name ?rad ?corerad ?gmr ?rdc ?r25 ?r50 ?r75 ?amps ?ins ?insmat ?id"+
		" ?insthick ?diacore ?diains ?diascreen ?diajacket ?sheathneutral"+
		" ?strand_cnt ?strand_rad ?strand_gmr ?strand_rdc WHERE {"+
		" ?w r:type c:ConcentricNeutralCableInfo."+
		" ?w c:IdentifiedObject.name ?name."+
		" bind(strafter(str(?w),\"#_\") as ?id)."+
		" ?w c:WireInfo.radius ?rad."+
		" ?w c:WireInfo.gmr ?gmr."+
		" OPTIONAL {?w c:WireInfo.rDC20 ?rdc.}"+
		" OPTIONAL {?w c:WireInfo.rAC25 ?r25.}"+
		" OPTIONAL {?w c:WireInfo.rAC50 ?r50.}"+
		" OPTIONAL {?w c:WireInfo.rAC75 ?r75.}"+
		" OPTIONAL {?w c:WireInfo.coreRadius ?corerad.}"+
		" OPTIONAL {?w c:WireInfo.ratedCurrent ?amps.}"+
		" OPTIONAL {?w c:WireInfo.insulationMaterial ?insraw."+
		"       	bind(strafter(str(?insraw),\"WireInsulationKind.\") as ?insmat)}"+
		" OPTIONAL {?w c:WireInfo.insulated ?ins.}"+
		" OPTIONAL {?w c:WireInfo.insulationThickness ?insthick.}"+
		" OPTIONAL {?w c:CableInfo.diameterOverCore ?diacore.}"+
		" OPTIONAL {?w c:CableInfo.diameterOverJacket ?diajacket.}"+
		" OPTIONAL {?w c:CableInfo.diameterOverInsulation ?diains.}"+
		" OPTIONAL {?w c:CableInfo.diameterOverScreen ?diascreen.}"+
		" OPTIONAL {?w c:CableInfo.sheathAsNeutral ?sheathneutral.}"+
		" OPTIONAL {?w c:ConcentricNeutralCableInfo.diameterOverNeutral ?dianeut.}"+
		" OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandCount ?strand_cnt.}"+
		" OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandGmr ?strand_gmr.}"+
		" OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandRadius ?strand_rad.}"+
		" OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandRDC20 ?strand_rdc.}"+
		"} ORDER BY ?name";

	public double dneut;
	public int strand_cnt; 
	public double strand_gmr;
	public double strand_rad;
	public double strand_rdc;

	public DistConcentricNeutralCable (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			rad = Double.parseDouble (soln.get("?rad").toString());
			gmr = Double.parseDouble (soln.get("?gmr").toString());
			rdc = OptionalDouble (soln, "?rdc", 0.0);
			r25 = OptionalDouble (soln, "?r25", 0.0);
			r50 = OptionalDouble (soln, "?r50", 0.0);
			r75 = OptionalDouble (soln, "?r75", 0.0);
			corerad = OptionalDouble (soln, "?corerad", 0.0);
			amps = OptionalDouble (soln, "?amps", 0.0);
			insthick = OptionalDouble (soln, "?insthick", 0.0);
			ins = OptionalBoolean (soln, "?ins", false);
			insmat = OptionalString (soln, "?insmat", "N/A");
			dcore = OptionalDouble (soln, "?diacore", 0.0);
			djacket = OptionalDouble (soln, "?diajacket", 0.0);
			dins = OptionalDouble (soln, "?diains", 0.0);
			dscreen = OptionalDouble (soln, "?diascreen", 0.0);
			sheathNeutral = OptionalBoolean (soln, "?sheathneutral", false);
			dneut = OptionalDouble (soln, "?dianeut", 0.0);
			strand_cnt = OptionalInt (soln, "?strand_cnt", 0);
			strand_gmr = OptionalDouble (soln, "?strand_gmr", 0.0);
			strand_rad = OptionalDouble (soln, "?strand_rad", 0.0);
			strand_rdc = OptionalDouble (soln, "?strand_rdc", 0.0);
		}
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		AppendCableDisplay (buf);
		buf.append (" dneut=" + df6.format(dneut) + " strand_cnt=" + Integer.toString(strand_cnt));
		buf.append (" strand_gmr=" + df6.format(strand_gmr) + " strand_rad=" + df6.format(strand_rad) + " strand_rdc=" + df6.format(strand_rdc));
		return buf.toString();
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder ("new CNData.");
		AppendDSSCableAttributes (buf);
		buf.append ("\n~ k=" + Integer.toString(strand_cnt) + " GmrStrand=" + df6.format(strand_gmr) +
								" DiaStrand=" + df6.format(2.0 * strand_rad) + " Rstrand=" + df6.format(strand_rdc));
		buf.append ("\n");
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

