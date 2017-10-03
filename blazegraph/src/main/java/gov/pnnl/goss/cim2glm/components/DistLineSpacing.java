package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistLineSpacing extends DistComponent {
	public static final String szQUERY = 
		"SELECT ?name ?cable ?usage ?bundle_count ?bundle_sep"+
		" (group_concat(?phs;separator=\"\\n\") as ?phases)"+
		" (group_concat(?x;separator=\"\\n\") as ?xarray)"+
		" (group_concat(?y;separator=\"\\n\") as ?yarray) WHERE {"+
		" ?w r:type c:WireSpacingInfo."+
		" ?w c:IdentifiedObject.name ?name."+
		" ?pos c:WirePosition.WireSpacingInfo ?w."+
		" ?pos c:WirePosition.xCoord ?x."+
		" ?pos c:WirePosition.yCoord ?y."+
		" ?pos c:WirePosition.phase ?phsraw."+
		"       bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs)"+ 
		" OPTIONAL {?w c:WireSacingInfo.isCable ?cable.}"+
		" OPTIONAL {?w c:WireSpacingInfo.phaseWireCount ?bundle_count.}"+
		" OPTIONAL {?w c:WireSpacingInfo.phaseWireSpacing ?bundle_sep.}"+
		" OPTIONAL {?w c:WireSpacingInfo.usage ?useraw."+
		"       bind(strafter(str(?useraw),\"WireUsageKind.\") as ?usage)}"+
		"} GROUP BY ?name ?cable ?usage ?bundle_count ?bundle_sep ORDER BY ?name";

	public String name;
	public String[] phases;
	public String[] xarray;
	public String[] yarray;
	public String usage;
	public int nwires;
	public boolean cable;
	public double b_sep;
	public int b_cnt;

	public DistLineSpacing (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			phases = soln.get("?phases").toString().split("\\n");
			xarray = soln.get("?xarray").toString().split("\\n");
			yarray = soln.get("?yarray").toString().split("\\n");
			nwires = phases.length;
			cable = OptionalBoolean (soln, "?cable", false);
			usage = OptionalString (soln, "?usage", "distribution");
			b_sep = OptionalDouble (soln, "?bundle_sep", 0.0);
			b_cnt = OptionalInt (soln, "?bundle_count", 0);
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#0.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " nwires=" + Integer.toString(nwires) + " cable=" + Boolean.toString(cable) + " usage=" + usage); 
		buf.append (" b_cnt=" + Integer.toString(b_cnt) + " b_sep=" + df.format(b_sep));
		for (int i = 0; i < nwires; i++) {
				buf.append ("\n  phs=" + phases[i] + " x=" + xarray[i] + " y=" + yarray[i]);
		}
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

