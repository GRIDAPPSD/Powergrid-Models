//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistBaseVoltage extends DistComponent {
	static final String szQUERY = "SELECT ?vnom WHERE {"+
																	" ?lev r:type c:BaseVoltage."+
																	" ?lev c:BaseVoltage.nominalVoltage ?vnom."+
																	"} ORDER BY ?vnom";

	public String name;
	double vnom;

	public DistBaseVoltage (QuerySolution soln) {
		name = GLD_Name (soln.get("?vnom").toString(), false);
		double vnom = new Double (soln.get("?vnom").toString()).doubleValue();
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append ("vnom=" + df.format(vnom) + "\n");
		return buf.toString();
	}
}

