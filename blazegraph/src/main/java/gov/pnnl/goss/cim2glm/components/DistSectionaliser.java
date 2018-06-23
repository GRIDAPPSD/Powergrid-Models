package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2018, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistSectionaliser extends DistSwitch {
	public static final String szQUERY = szSELECT + " ?s r:type c:Sectionaliser." + szWHERE;

	public DistSectionaliser (ResultSet results) {
		super (results);
	}

	public String GetGLM () {
		StringBuilder buf = new StringBuilder ("object sectionalizer {\n");

		buf.append ("  name \"swt_" + name + "\";\n");
		buf.append ("  from \"" + bus1 + "\";\n");
		buf.append ("  to \"" + bus2 + "\";\n");
		buf.append ("  phases " + glm_phases + ";\n");
		if (open) {
			buf.append ("  status OPEN;\n");
		} else {
			buf.append ("  status CLOSED;\n");
		}
		buf.append("}\n");
		return buf.toString();
	}

	public String CIMClass() {
		return "Sectionaliser";
	}
}


