package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistLoadBreakSwitch extends DistSwitch {
	public static final String szQUERY = szSELECT + " ?s r:type c:LoadBreakSwitch." + szWHERE;

	public DistLoadBreakSwitch (ResultSet results) {
		super (results);
	}

	public String CIMClass() {
		return "LoadBreakSwitch";
	}
}


