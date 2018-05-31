package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2018, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistRecloser extends DistSwitch {
	public static final String szQUERY = szSELECT + " ?s r:type c:Recloser." + szWHERE;

	public DistRecloser (ResultSet results) {
		super (results);
	}

	public String CIMClass() {
		return "Recloser";
	}
}


