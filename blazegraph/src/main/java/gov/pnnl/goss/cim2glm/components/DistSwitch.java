package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistSwitch extends DistComponent {
	public static final String szQUERY = 
		"SELECT ?name ?id ?basev (group_concat(distinct ?bus;separator=\"\\n\") as ?buses) (group_concat(distinct ?phs;separator=\"\\n\") as ?phases) ?open WHERE {"+
		" ?s r:type c:LoadBreakSwitch."+
		" ?s c:IdentifiedObject.name ?name."+
		" ?s c:ConductingEquipment.BaseVoltage ?bv."+
		" ?bv c:BaseVoltage.nominalVoltage ?basev."+
		" ?s c:Switch.normalOpen ?open."+
		" ?t c:Terminal.ConductingEquipment ?s."+
		" ?t c:Terminal.ConnectivityNode ?cn."+
		" ?cn c:IdentifiedObject.name ?bus"+
		" bind(strafter(str(?s),\"#_\") as ?id)."+
		" OPTIONAL {?swp c:SwitchPhase.Switch ?s."+
		" ?swp c:SwitchPhase.phaseSide1 ?phsraw."+
		"   bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
		"}"+
		" GROUP BY ?name ?basev ?open ?id"+
		" ORDER BY ?name";

	public String id;
	public String name;
	public String bus1;
	public String bus2;
	public String phases;
	public boolean open;
	public double basev;

	public DistSwitch (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			basev = Double.parseDouble (soln.get("?basev").toString());
			String[] buses = soln.get("?buses").toString().split("\\n");
			bus1 = SafeName(buses[0]); 
			bus2 = SafeName(buses[1]); 
			phases = OptionalString (soln, "?phases", "ABC");
			open = Boolean.parseBoolean (soln.get("?open").toString());
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " from " + bus1 + " to " + bus2 + " basev=" + df2.format(basev) + " phases=" + phases + " open=" + Boolean.toString (open));
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
	public String GetJSONSymbols(HashMap<String,DistCoordinates> map) {
		DistCoordinates pt1 = map.get("LoadBreakSwitch:" + name + ":1");
		DistCoordinates pt2 = map.get("LoadBreakSwitch:" + name + ":2");
		StringBuilder lbl_phs = new StringBuilder ();
		if (phases.contains("A")) lbl_phs.append("A");
		if (phases.contains("B")) lbl_phs.append("B");
		if (phases.contains("C")) lbl_phs.append("C");
		if (phases.contains("s")) lbl_phs.append("S");
		if (lbl_phs.length() < 1) lbl_phs.append("ABC");

		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name + "\"");
		buf.append (",\"from\":\"" + bus1 + "\"");
		buf.append (",\"to\":\"" + bus2 + "\"");
		buf.append (",\"phases\":\"" + lbl_phs.toString() +"\"");
		buf.append (",\"open\":\"" + Boolean.toString(open) +"\"");
		buf.append (",\"x1\":" + Double.toString(pt1.x));
		buf.append (",\"y1\":" + Double.toString(pt1.y));
		buf.append (",\"x2\":" + Double.toString(pt2.x));
		buf.append (",\"y2\":" + Double.toString(pt2.y));
		buf.append ("}");
		return buf.toString();
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder ("new Line." + name);

		buf.append (" phases=" + Integer.toString(DSSPhaseCount(phases, false)) + 
								" bus1=" + DSSBusPhases(bus1, phases) + " bus2=" + DSSBusPhases (bus2, phases) + 
								" switch=y // CIM LoadBreakSwitch\n");
		if (open) {
			buf.append ("  open Line." + name + " 1\n");
		} else {
			buf.append ("  close Line." + name + " 1\n");
		}

		return buf.toString();
	}
}

