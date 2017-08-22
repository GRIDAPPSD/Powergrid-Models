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

	public DistXfmrCodeOCTest (QuerySolution soln) {
		pname = GLD_Name (soln.get("?pname").toString(), false);
		tname = GLD_Name (soln.get("?tname").toString(), false);
		nll = new Double (soln.get("?nll").toString()).doubleValue();
		iexc = new Double (soln.get("?iexc").toString()).doubleValue();
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + ":" + tname + " NLL=" + df.format(nll) + " iexc=" + df.format(iexc) + "\n");
		return buf.toString();
	}
}

