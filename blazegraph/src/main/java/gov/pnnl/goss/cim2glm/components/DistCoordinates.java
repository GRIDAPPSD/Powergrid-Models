package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;

public class DistCoordinates extends DistComponent {
	public static final String szQUERY =
		"SELECT ?class ?name ?seq ?x ?y WHERE {"+
		" ?eq c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?eq c:PowerSystemResource.Location ?loc."+
		" { ?eq c:IdentifiedObject.name ?name."+
    "   ?eq a ?classraw."+
    "   bind(strafter(str(?classraw),\"CIM100#\") as ?class)}"+
    "  UNION"+
    " { ?eq c:PowerElectronicsConnection.PowerElectronicsUnit ?unit."+
		"   ?unit c:IdentifiedObject.name ?name."+
    "   ?unit a ?classraw."+
    "   bind(strafter(str(?classraw),\"CIM100#\") as ?class)}"+
		" ?pt c:PositionPoint.Location ?loc."+
		" ?pt c:PositionPoint.xPosition ?x."+
		" ?pt c:PositionPoint.yPosition ?y."+
		" ?pt c:PositionPoint.sequenceNumber ?seq."+
		" FILTER (!regex(?class, \"Phase\"))."+
		" FILTER (!regex(?class, \"TapChanger\"))."+
		" FILTER (!regex(?class, \"Tank\"))."+
		" FILTER (!regex(?class, \"RegulatingControl\"))."+
		"}"+
		" ORDER BY ?class ?name ?seq ?x ?y";

	public String name;
	public double x;
	public double y;
	public int seq;
	public String cname;

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append ("}");
		return buf.toString();
	}

	public DistCoordinates (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			x = Double.parseDouble (soln.get("?x").toString());
			y = Double.parseDouble (soln.get("?y").toString());
			seq = Integer.parseInt (soln.get("?seq").toString());
			cname = soln.get("?class").toString();
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (cname + ":" + name + ":" + Integer.toString(seq) + " x=" + df4.format(x) + " y=" + df4.format(y));
		return buf.toString();
	}

	public String GetKey() {
		return cname + ":" + name + ":" + Integer.toString(seq);
	}
}

