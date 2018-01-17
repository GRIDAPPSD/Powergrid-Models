package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;

public class DistXfmrCodeOCTest extends DistComponent {
	public static final String szQUERY = 
		"SELECT ?pname ?tname ?nll ?iexc WHERE {"+
		" ?p r:type c:PowerTransformerInfo."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?t c:TransformerTankInfo.PowerTransformerInfo ?p."+
		" ?t c:IdentifiedObject.name ?tname."+
		" ?e c:TransformerEndInfo.TransformerTankInfo ?t."+
		" ?nlt c:NoLoadTest.EnergisedEnd ?e."+
		" ?nlt c:NoLoadTest.loss ?nll."+
		" ?nlt c:NoLoadTest.excitingCurrent ?iexc."+
		"} ORDER BY ?pname ?tname";

	public String pname;
	public String tname;
	public double nll;
	public double iexc;

	public DistXfmrCodeOCTest (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			pname = SafeName (soln.get("?pname").toString());
			tname = SafeName (soln.get("?tname").toString());
			nll = Double.parseDouble (soln.get("?nll").toString());
			iexc = Double.parseDouble (soln.get("?iexc").toString());
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + ":" + tname + " NLL=" + df4.format(nll) + " iexc=" + df4.format(iexc));
		return buf.toString();
	}

	public String GetKey() {
		return tname;
	}
}

