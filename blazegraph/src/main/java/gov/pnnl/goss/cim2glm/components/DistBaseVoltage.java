package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistBaseVoltage extends DistComponent {
	public static final String szQUERY = "SELECT ?vnom WHERE {"+
																	" ?lev r:type c:BaseVoltage."+
																	" ?lev c:BaseVoltage.nominalVoltage ?vnom."+
																	"} ORDER BY ?vnom";

	public String name;
	double vnom;

	public DistBaseVoltage (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = soln.get("?vnom").toString();
			vnom = Double.parseDouble (soln.get("?vnom").toString());
		}
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#0.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append ("vnom=" + df.format(vnom));
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

