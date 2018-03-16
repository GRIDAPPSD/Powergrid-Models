package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistFuse extends DistSwitch {
	public static final String szQUERY = szSELECT + " ?s r:type c:Fuse." + szWHERE;

	public DistFuse (ResultSet results) {
		super (results);
	}

	public String CIMClass() {
		return "Fuse";
	}
}


