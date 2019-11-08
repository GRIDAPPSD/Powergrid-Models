package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public abstract class DistSwitch extends DistComponent {

	protected static final String szSELECT = 
		"SELECT ?name ?id ?bus1 ?bus2 ?basev ?rated ?breaking (group_concat(distinct ?phs;separator=\"\\n\") as ?phases) ?open ?fdrid WHERE {";

	protected static final String szWHERE = 
		" ?s c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?s c:IdentifiedObject.name ?name."+
		" ?s c:ConductingEquipment.BaseVoltage ?bv."+
		" ?bv c:BaseVoltage.nominalVoltage ?basev."+
		" ?s c:Switch.normalOpen ?open."+
		" ?s c:Switch.ratedCurrent ?rated."+
		" OPTIONAL {?s c:ProtectedSwitch.breakingCapacity ?breaking.}"+
		" ?t1 c:Terminal.ConductingEquipment ?s."+
		" ?t1 c:Terminal.ConnectivityNode ?cn1."+
		" ?t1 c:ACDCTerminal.sequenceNumber \"1\"."+
		" ?cn1 c:IdentifiedObject.name ?bus1."+
		" ?t2 c:Terminal.ConductingEquipment ?s."+
		" ?t2 c:Terminal.ConnectivityNode ?cn2."+
		" ?t2 c:ACDCTerminal.sequenceNumber \"2\"."+
		" ?cn2 c:IdentifiedObject.name ?bus2."+
		" bind(strafter(str(?s),\"#\") as ?id)."+
		" OPTIONAL {?swp c:SwitchPhase.Switch ?s."+
		" ?swp c:SwitchPhase.phaseSide1 ?phsraw."+
		"   bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
		"}"+
		" GROUP BY ?name ?basev ?bus1 ?bus2 ?rated ?breaking ?open ?id ?fdrid"+
		" ORDER BY ?name";

	public String id;
	public String name;
	public String bus1;
	public String bus2;
	public String phases;
	public boolean open;
	public double basev;
	public double rated;
	public double breaking;

	public double normalCurrentLimit = 0.0;
	public double emergencyCurrentLimit = 0.0;

	public String glm_phases;

	public abstract String CIMClass();

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"mRID\":\"" + id +"\"");
		buf.append (",\"CN1\":\"" + bus1 + "\"");
		buf.append (",\"CN2\":\"" + bus2 + "\"");
		buf.append (",\"phases\":\"" + phases + "\"");
		buf.append (",\"ratedCurrent\":" + df1.format(rated));
		buf.append (",\"breakingCapacity\":" + df1.format(breaking));
		if (open) {
			buf.append (",\"normalOpen\":true");
		} else {
			buf.append (",\"normalOpen\":false");
		}
		buf.append("}");
		return buf.toString();
	}

	public DistSwitch (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			basev = Double.parseDouble (soln.get("?basev").toString());
			rated = Double.parseDouble (soln.get("?rated").toString());
			breaking = OptionalDouble (soln, "?breaking", 0.0);
			bus1 = SafeName (soln.get("?bus1").toString()); 
			bus2 = SafeName (soln.get("?bus2").toString()); 
			phases = OptionalString (soln, "?phases", "ABC");
			open = Boolean.parseBoolean (soln.get("?open").toString());
			StringBuilder glm_phs = new StringBuilder ();
			if (phases.contains("A")) glm_phs.append("A");
			if (phases.contains("B")) glm_phs.append("B");
			if (phases.contains("C")) glm_phs.append("C");
			if (phases.contains("s")) glm_phs.append("S");
			if (glm_phs.length() < 1) glm_phs.append("ABC");
			if (glm_phs.toString().equals("AB") && basev <= 208.1) { // TODO - artifact of non-triplex secondaries in CIM and OpenDSS
				glm_phases = "S"; // need to figure out AS, BS, or CS from connected triplex
			} else {
				glm_phases = glm_phs.toString();
			}
		}		
	}
	
	public DistSwitch (String name, boolean open) {
		this.name = SafeName (name);
		this.open = open;
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " from " + bus1 + " to " + bus2 + " basev=" + df2.format(basev) + " rated=" + df1.format(rated) +
								" breaking=" + df1.format(breaking) +	" phases=" + glm_phases + " open=" + Boolean.toString (open));
		return buf.toString();
	}

	public String GetGLM () {
		StringBuilder buf = new StringBuilder ("object switch { // CIM " + CIMClass() + "\n");

		buf.append ("  name \"swt_" + name + "\";\n");
		buf.append ("  from \"" + bus1 + "\";\n");
		buf.append ("  to \"" + bus2 + "\";\n");
		buf.append ("  phases " + glm_phases + ";\n");
		if (open) {
			buf.append ("  status OPEN;\n");
		} else {
			buf.append ("  status CLOSED;\n");
		}
		AppendGLMRatings (buf, glm_phases, normalCurrentLimit, emergencyCurrentLimit);
		buf.append("}\n");
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}

	public String GetJSONSymbols(HashMap<String,DistCoordinates> map) {
		DistCoordinates pt1 = map.get(CIMClass() + ":" + name + ":1");
		DistCoordinates pt2 = map.get(CIMClass() + ":" + name + ":2");

		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name + "\"");
		buf.append (",\"from\":\"" + bus1 + "\"");
		buf.append (",\"to\":\"" + bus2 + "\"");
		buf.append (",\"phases\":\"" + glm_phases +"\"");
		buf.append (",\"open\":\"" + Boolean.toString(open) +"\"");
		buf.append (",\"x1\":" + Double.toString(pt1.x));
		buf.append (",\"y1\":" + Double.toString(pt1.y));
		buf.append (",\"x2\":" + Double.toString(pt2.x));
		buf.append (",\"y2\":" + Double.toString(pt2.y));
		buf.append ("}");
		return buf.toString();
	}

	public String GetDSS () {
		StringBuilder buf = new StringBuilder ("new Line." + name);

		buf.append (" phases=" + Integer.toString(DSSPhaseCount(phases, false)) + 
								" bus1=" + DSSBusPhases(bus1, phases) + " bus2=" + DSSBusPhases (bus2, phases) + 
								" switch=y // CIM " + CIMClass() + "\n");
		AppendDSSRatings (buf, normalCurrentLimit, emergencyCurrentLimit);
		if (open) {
			buf.append ("  open Line." + name + " 1\n");
		} else {
			buf.append ("  close Line." + name + " 1\n");
		}

		return buf.toString();
	}
}

