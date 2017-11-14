package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;

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
		StringBuilder buf = new StringBuilder ("");
		buf.append ("vnom=" + df4.format(vnom));
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (df3.format(0.001 * vnom) + " ");
		return buf.toString();
	}
}

