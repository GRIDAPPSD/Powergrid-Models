//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistXfmrCodeSCTest extends DistComponent {
	static final String szQUERY = 
		"SELECT ?pname ?tname ?enum ?gnum ?z ?ll WHERE {"+
		" ?p r:type c:PowerTransformerInfo."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?t c:TransformerTankInfo.PowerTransformerInfo ?p."+
		" ?t c:IdentifiedObject.name ?tname."+
		" ?e c:TransformerEndInfo.TransformerTankInfo ?t."+
		" ?e c:TransformerEndInfo.endNumber ?enum."+
		" ?sct c:ShortCircuitTest.EnergisedEnd ?e."+
		" ?sct c:ShortCircuitTest.leakageImpedance ?z."+
		" ?sct c:ShortCircuitTest.loss ?ll."+
		" ?sct c:ShortCircuitTest.GroundedEnds ?grnd."+
		" ?grnd c:TransformerEndInfo.endNumber ?gnum."+
		"} ORDER BY ?pname ?tname ?enum ?gnum";

	public String pname;
	public String tname;
	public int fwdg;
	public int twdg;
	public double z;
	public double ll;

	public DistXfmrCodeSCTest (QuerySolution soln) {
		pname = GLD_Name (soln.get("?pname").toString(), false);
		tname = GLD_Name (soln.get("?tname").toString(), false);
		fwdg = new Integer (soln.get("?enum").toString()).intValue();
		twdg = new Integer (soln.get("?gnum").toString()).intValue();
		z = new Double (soln.get("?z").toString()).doubleValue();
		ll = new Double (soln.get("?ll").toString()).doubleValue();
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + ":" + tname + " fwdg=" + Integer.toString(fwdg) + " twdg=" + Integer.toString(twdg) +
								" z=" + df.format(z) + " LL=" + df.format(ll));
		return buf.toString();
	}

	public String GetKey() {
		return pname + ":" + tname + ":" + Integer.toString(fwdg) + ":" + Integer.toString(twdg);
	}
}

