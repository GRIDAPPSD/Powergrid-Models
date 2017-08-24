//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistXfmrCodeOCTest extends DistComponent {
	static final String szQUERY = 
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
			pname = GLD_Name (soln.get("?pname").toString(), false);
			tname = GLD_Name (soln.get("?tname").toString(), false);
			nll = Double.parseDouble (soln.get("?nll").toString());
			iexc = Double.parseDouble (soln.get("?iexc").toString());
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + ":" + tname + " NLL=" + df.format(nll) + " iexc=" + df.format(iexc));
		return buf.toString();
	}

	public String GetKey() {
		return pname + ":" + tname;
	}
}

