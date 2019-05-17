package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;

public class DistOverheadWire extends DistWire {
	public static final String szQUERY =  
		"SELECT DISTINCT ?name ?rad ?corerad ?gmr ?rdc ?r25 ?r50 ?r75 ?amps ?ins ?insmat ?insthick ?id WHERE {"+
		" ?eq r:type c:ACLineSegment."+
		" ?eq c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" { ?asset c:Asset.PowerSystemResources ?eq."+
		"   ?asset c:Asset.AssetInfo ?w.}"+
		" UNION"+
		" { ?acp c:ACLineSegmentPhase.ACLineSegment ?eq."+
		"   ?phasset c:Asset.PowerSystemResources ?acp."+
		"   ?phasset c:Asset.AssetInfo ?w.}"+
		" ?w r:type c:OverheadWireInfo."+
		" ?w c:IdentifiedObject.name ?name."+
		" bind(strafter(str(?w),\"#\") as ?id)."+
		" ?w c:WireInfo.radius ?rad."+
		" ?w c:WireInfo.gmr ?gmr."+
		" OPTIONAL {?w c:WireInfo.rDC20 ?rdc.}"+
		" OPTIONAL {?w c:WireInfo.rAC25 ?r25.}"+
		" OPTIONAL {?w c:WireInfo.rAC50 ?r50.}"+
		" OPTIONAL {?w c:WireInfo.rAC75 ?r75.}"+
		" OPTIONAL {?w c:WireInfo.coreRadius ?corerad.}"+
		" OPTIONAL {?w c:WireInfo.ratedCurrent ?amps.}"+
		" OPTIONAL {?w c:WireInfo.insulationMaterial ?insraw."+
		"       bind(strafter(str(?insraw),\"WireInsulationKind.\") as ?insmat)}"+
		" OPTIONAL {?w c:WireInfo.insulated ?ins.}"+
		" OPTIONAL {?w c:WireInfo.insulationThickness ?insthick.}"+
		"} ORDER BY ?name";

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"mRID\":\"" + id +"\"");
		buf.append ("}");
		return buf.toString();
	}

	public DistOverheadWire (ResultSet results) {
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
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		AppendWireDisplay (buf);
		return buf.toString();
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder ("new WireData.");
		AppendDSSWireAttributes (buf);
		buf.append ("\n");
		return buf.toString();
	}

	public String GetGLM() {
		StringBuilder buf = new StringBuilder("object overhead_line_conductor {\n");

		buf.append ("  name \"wire_" + name + "\";\n");
		buf.append ("  geometric_mean_radius " + df6.format (gmr * gFTperM) + ";\n");
		buf.append ("  diameter " + df6.format (2.0 * rad * gFTperM * 12.0) + ";\n");
		buf.append ("  resistance " + df6.format (r50 * gMperMILE) + ";\n");
		AppendGLMWireAttributes (buf);
		buf.append("}\n");
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

