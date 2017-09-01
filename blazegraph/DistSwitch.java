//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistSwitch extends DistComponent {
	static final String szQUERY = 
		"SELECT ?name ?basev (group_concat(distinct ?bus;separator=\"\\n\") as ?buses) (group_concat(distinct ?phs;separator=\"\\n\") as ?phases) ?open WHERE {"+
		" ?s r:type c:LoadBreakSwitch."+
		" ?s c:IdentifiedObject.name ?name."+
		" ?s c:ConductingEquipment.BaseVoltage ?bv."+
		" ?bv c:BaseVoltage.nominalVoltage ?basev."+
		" ?s c:Switch.normalOpen ?open."+
		" ?t c:Terminal.ConductingEquipment ?s."+
		" ?t c:Terminal.ConnectivityNode ?cn."+
		" ?cn c:IdentifiedObject.name ?bus"+
		" OPTIONAL {?swp c:SwitchPhase.Switch ?s."+
		" ?swp c:SwitchPhase.phaseSide1 ?phsraw."+
		"       		bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
		"}"+
		" GROUP BY ?name ?basev ?open"+
		" ORDER BY ?name";

	public String name;
	public String bus1;
	public String bus2;
	public String phases;
	public boolean open;
	public double basev;

	public DistSwitch (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = GLD_Name (soln.get("?name").toString(), false);
			basev = Double.parseDouble (soln.get("?basev").toString());
			String[] buses = soln.get("?buses").toString().split("\\n");
			bus1 = GLD_Name(buses[0], true); 
			bus2 = GLD_Name(buses[1], true); 
			phases = OptionalString (soln, "?phases", "ABC");
			open = Boolean.parseBoolean (soln.get("?open").toString());
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#0.00");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " from " + bus1 + " to " + bus2 + " basev=" + df.format(basev) + " phases=" + phases + " open=" + Boolean.toString (open));
		return buf.toString();
	}

	public String GetGLM () {
		StringBuilder buf = new StringBuilder ("object switch {\n");

		buf.append ("  name \"swt_" + name + "\";\n");
		buf.append ("  from \"" + bus1 + "\";\n");
		buf.append ("  to \"" + bus2 + "\";\n");
		buf.append ("  phases " + phases + ";\n");
		if (open) {
			buf.append ("  status OPEN;\n");
		} else {
			buf.append ("  status CLOSED;\n");
		}
		buf.append("}\n");
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

