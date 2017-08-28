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
	public int[] fwdg;
	public int[] twdg;
	public double[] z;
	public double[] ll;

	public int size;

	private void SetSize (String p, String t) {
		size = 1;
		String szCount = "SELECT (count (?p) as ?count) WHERE {"+
			" ?p r:type c:PowerTransformerInfo."+
			" ?p c:IdentifiedObject.name \"" + p + "\"."+
			" ?t c:TransformerTankInfo.PowerTransformerInfo ?p."+
			" ?t c:IdentifiedObject.name \"" + t + "\"."+
			" ?e c:TransformerEndInfo.TransformerTankInfo ?t."+
			" ?sct c:ShortCircuitTest.EnergisedEnd ?e"+
			"}";
		ResultSet results = RunQuery (szCount);
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			size = soln.getLiteral("?count").getInt();
		}
		fwdg = new int[size];
		twdg = new int[size];
		z = new double[size];
		ll = new double[size];
	}

	public DistXfmrCodeSCTest (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			String p = soln.get("?pname").toString();
			String t = soln.get("?tname").toString();
			pname = GLD_Name (p, false);
			tname = GLD_Name (t, false);
			SetSize (p, t);
			for (int i = 0; i < size; i++) {
				fwdg[i] = Integer.parseInt (soln.get("?enum").toString());
				twdg[i] = Integer.parseInt (soln.get("?gnum").toString());
				z[i] = Double.parseDouble (soln.get("?z").toString());
				ll[i] = Double.parseDouble (soln.get("?ll").toString());
				if ((i + 1) < size) {
					soln = results.next();
				}
			}
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#0.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + ":" + tname);
		for (int i = 0; i < size; i++) {
			buf.append ("\n  fwdg=" + Integer.toString(fwdg[i]) + " twdg=" + Integer.toString(twdg[i]) +
								" z=" + df.format(z[i]) + " LL=" + df.format(ll[i]));
		}
		return buf.toString();
	}

	public String GetKey() {
		return tname;
	}
}

