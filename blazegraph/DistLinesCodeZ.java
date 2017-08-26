//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistLinesCodeZ extends DistLineSegment {
	static final String szQUERY =
		"SELECT ?name (group_concat(distinct ?bus;separator=\"\\n\") as ?buses) (group_concat(distinct ?phs;separator=\"\\n\") as ?phases) ?len ?lname WHERE {"+
		" ?s r:type c:ACLineSegment."+
		" ?s c:IdentifiedObject.name ?name."+
		" ?s c:Conductor.length ?len."+
		" ?s c:ACLineSegment.PerLengthImpedance ?lcode."+
		" ?lcode c:IdentifiedObject.name ?lname."+
		" ?t c:Terminal.ConductingEquipment ?s."+
		" ?t c:Terminal.ConnectivityNode ?cn."+
		" ?cn c:IdentifiedObject.name ?bus"+
		" OPTIONAL {?acp c:ACLineSegmentPhase.ACLineSegment ?s."+
		" ?acp c:ACLineSegmentPhase.phase ?phsraw."+
		"       		bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
		"}"+
		" GROUP BY ?name ?len ?lname"+
		" ORDER BY ?name";

	public double len;
	public String lname;

	public DistLinesCodeZ (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = GLD_Name (soln.get("?name").toString(), false);
			String[] buses = soln.get("?buses").toString().split("\\n");
			bus1 = GLD_Name(buses[0], true); 
			bus2 = GLD_Name(buses[1], true); 
			phases = OptionalString (soln, "?phases", "ABC");
			phases = phases.replace ('\n', ':');
			len = Double.parseDouble (soln.get("?len").toString());
			lname = soln.get("?lname").toString();
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " from " + bus1 + " to " + bus2 + " phases=" + phases + " len=" + df.format(len)  + " linecode=" + lname);
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}

	public String LabelString() {
		return lname;
	}
}

