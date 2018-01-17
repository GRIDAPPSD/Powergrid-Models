package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistXfmrCodeSCTest extends DistComponent {
	public static final String szQUERY = 
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

	public static final String szCountQUERY =
		"SELECT ?key (count(?sct) as ?count) WHERE {"+
		" ?p r:type c:PowerTransformerInfo."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?t c:TransformerTankInfo.PowerTransformerInfo ?p."+
		" ?t c:IdentifiedObject.name ?key."+
		" ?e c:TransformerEndInfo.TransformerTankInfo ?t."+
		" ?sct c:ShortCircuitTest.EnergisedEnd ?e."+
		"} GROUP BY ?key ORDER BY ?key";

	public String pname;
	public String tname;
	public int[] fwdg;
	public int[] twdg;
	public double[] z;
	public double[] ll;

	public int size;

	private void SetSize (int val) {
		size = val;
		fwdg = new int[size];
		twdg = new int[size];
		z = new double[size];
		ll = new double[size];
	}

	public DistXfmrCodeSCTest (ResultSet results, HashMap<String,Integer> map) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			String p = soln.get("?pname").toString();
			String t = soln.get("?tname").toString();
			pname = SafeName (p);
			tname = SafeName (t);
			SetSize (map.get(tname));
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
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + ":" + tname);
		for (int i = 0; i < size; i++) {
			buf.append ("\n  fwdg=" + Integer.toString(fwdg[i]) + " twdg=" + Integer.toString(twdg[i]) +
								" z=" + df4.format(z[i]) + " LL=" + df4.format(ll[i]));
		}
		return buf.toString();
	}

	public String GetKey() {
		return tname;
	}
}

