//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistPowerXfmrCore extends DistComponent {
	static final String szQUERY =
		"SELECT ?pname ?enum ?b ?g WHERE {"+
		" ?p r:type c:PowerTransformer."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?end c:PowerTransformerEnd.PowerTransformer ?p."+
		" ?adm c:TransformerCoreAdmittance.TransformerEnd ?end."+
		" ?end c:TransformerEnd.endNumber ?enum."+
		" ?adm c:TransformerCoreAdmittance.b ?b."+
		" ?adm c:TransformerCoreAdmittance.g ?g."+
		"} ORDER BY ?pname";

	public String name;
	public int wdg;
	public double b;
	public double g;

	public DistPowerXfmrCore (QuerySolution soln) {
		name = GLD_Name (soln.get("?pname").toString(), false);
		wdg = new Integer (soln.get("?enum").toString()).intValue();
		b = new Double (soln.get("?b").toString()).doubleValue();
		g = new Double (soln.get("?g").toString()).doubleValue();
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " wdg=" + Integer.toString(wdg) + " g=" + df.format(g) + " b=" + df.format(b));
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

