//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistCoordinates extends DistComponent {
	static final String szQUERY =
		"SELECT ?class ?name ?seq ?x ?y WHERE {"+
		" ?eq c:PowerSystemResource.Location ?loc."+
		" ?eq c:IdentifiedObject.name ?name."+
		" ?eq a ?classraw."+
		"  bind(strafter(str(?classraw),\"cim16#\") as ?class)"+
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

	public DistCoordinates (QuerySolution soln) {
		name = GLD_Name (soln.get("?name").toString(), false);
		x = Double.parseDouble (soln.get("?x").toString());
		y = Double.parseDouble (soln.get("?y").toString());
		seq = Integer.parseInt (soln.get("?seq").toString());
		cname = soln.get("?class").toString();
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (cname + ":" + name + ":" + Integer.toString(seq) + " x=" + df.format(x) + " y=" + df.format(y));
		return buf.toString();
	}

	public String GetKey() {
		return cname + ":" + name + ":" + Integer.toString(seq);
	}
}

