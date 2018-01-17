package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;

public class DistPowerXfmrCore extends DistComponent {
	public static final String szQUERY =
		"SELECT ?pname ?enum ?b ?g WHERE {"+
		" ?p r:type c:PowerTransformer."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?end c:PowerTransformerEnd.PowerTransformer ?p."+
		" ?adm c:TransformerCoreAdmittance.TransformerEnd ?end."+
		" ?end c:TransformerEnd.endNumber ?enum."+
		" ?adm c:TransformerCoreAdmittance.b ?b."+
		" ?adm c:TransformerCoreAdmittance.g ?g."+
		"} ORDER BY ?pname";

	public String name;
	public int wdg;
	public double b;
	public double g;

	public DistPowerXfmrCore (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?pname").toString());
			wdg = Integer.parseInt (soln.get("?enum").toString());
			b = Double.parseDouble (soln.get("?b").toString());
			g = Double.parseDouble (soln.get("?g").toString());
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " wdg=" + Integer.toString(wdg) + " g=" + df4.format(g) + " b=" + df4.format(b));
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

