//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistPowerXfmrMesh extends DistComponent {
	static final String szQUERY = 
		"SELECT ?pname ?fnum ?tnum ?r ?x WHERE {"+
		" ?p r:type c:PowerTransformer."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?from c:PowerTransformerEnd.PowerTransformer ?p."+
		" ?imp c:TransformerMeshImpedance.FromTransformerEnd ?from."+
		" ?imp c:TransformerMeshImpedance.ToTransformerEnd ?to."+
		" ?imp c:TransformerMeshImpedance.r ?r."+
		" ?imp c:TransformerMeshImpedance.x ?x."+
		" ?from c:TransformerEnd.endNumber ?fnum."+
		" ?to c:TransformerEnd.endNumber ?tnum."+
		"} ORDER BY ?pname ?fnum ?tnum";

	public String pname;
	public int fwdg;
	public int twdg;
	public double r;
	public double x;

	public DistPowerXfmrMesh (QuerySolution soln) {
		pname = GLD_Name (soln.get("?pname").toString(), false);
		fwdg = new Integer (soln.get("?fnum").toString()).intValue();
		twdg = new Integer (soln.get("?tnum").toString()).intValue();
		r = new Double (soln.get("?r").toString()).doubleValue();
		x = new Double (soln.get("?x").toString()).doubleValue();
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + " fwdg=" + Integer.toString(fwdg) + " twdg=" + Integer.toString(twdg) + 
								" r=" + df.format(r) + " x=" + df.format(x) + "\n");
		return buf.toString();
	}
}

