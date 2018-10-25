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

	public String GetGLM () {
		StringBuilder buf = new StringBuilder ("object fuse {\n");

		buf.append ("  name \"swt_" + name + "\";\n");
		buf.append ("  from \"" + bus1 + "\";\n");
		buf.append ("  to \"" + bus2 + "\";\n");
		buf.append ("  phases " + glm_phases + ";\n");
		buf.append ("  current_limit " + df2.format (rated) + ";\n");
		if (open) {
			buf.append ("  status OPEN;\n");
		} else {
			buf.append ("  status CLOSED;\n");
		}
		buf.append ("  mean_replacement_time 3600;\n");
		buf.append("}\n");
		return buf.toString();
	}

	public String GetDSS () {
		StringBuilder buf = new StringBuilder (super.GetDSS());

		buf.append ("  new Fuse." + name + " MonitoredObj=Line." + name +
								" RatedCurrent=" + df2.format (rated) + "\n");
		return buf.toString();
	}
}


