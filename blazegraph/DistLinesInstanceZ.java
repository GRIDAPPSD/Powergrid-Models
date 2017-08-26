//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistLinesInstanceZ extends DistLineSegment {
	static final String szQUERY = 
		"SELECT ?name (group_concat(distinct ?bus;separator=\"\\n\") as ?buses) ?len ?r ?x ?b ?r0 ?x0 ?b0 WHERE {"+
		" ?s r:type c:ACLineSegment."+
		" ?s c:IdentifiedObject.name ?name."+
		" ?s c:Conductor.length ?len."+
		" ?s c:ACLineSegment.r ?r."+
		" ?s c:ACLineSegment.x ?x."+
		" OPTIONAL {?s c:ACLineSegment.b ?b.}"+
		" OPTIONAL {?s c:ACLineSegment.r0 ?r0.}"+
		" OPTIONAL {?s c:ACLineSegment.x0 ?x0.}"+
		" OPTIONAL {?s c:ACLineSegment.b0 ?b0.}"+
		" ?t c:Terminal.ConductingEquipment ?s."+
		" ?t c:Terminal.ConnectivityNode ?cn. "+
		" ?cn c:IdentifiedObject.name ?bus"+
		"}"+
		" GROUP BY ?name ?len ?r ?x ?b ?r0 ?x0 ?b0"+
		" ORDER BY ?name";

	public double r1; 
	public double x1; 
	public double b1; 
	public double r0; 
	public double x0; 
	public double b0; 

	public DistLinesInstanceZ (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = GLD_Name (soln.get("?name").toString(), false);
			String[] buses = soln.get("?buses").toString().split("\\n");
			bus1 = GLD_Name(buses[0], true); 
			bus2 = GLD_Name(buses[1], true); 
			phases = "ABC";
			len = Double.parseDouble (soln.get("?len").toString());
			r1 = Double.parseDouble (soln.get("?r").toString());
			x1 = Double.parseDouble (soln.get("?x").toString());
			b1 = OptionalDouble (soln, "?b", 0.0);
			r0 = OptionalDouble (soln, "?r0", 0.0);
			x0 = OptionalDouble (soln, "?x0", 0.0);
			b0 = OptionalDouble (soln, "?b0", 0.0);
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " from " + bus1 + " to " + bus2 + " phases=" + phases + " len=" + df.format(len));
		buf.append (" r1=" + df.format(r1) + " x1=" + df.format(x1) + " b1=" + df.format(b1));
		buf.append (" r0=" + df.format(r0) + " x0=" + df.format(x0) + " b0=" + df.format(b0));
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}

	public String LabelString() {
		return "seqZ";
	}
}

