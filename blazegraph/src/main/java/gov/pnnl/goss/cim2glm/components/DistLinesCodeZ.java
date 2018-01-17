package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;

public class DistLinesCodeZ extends DistLineSegment {
	public static final String szQUERY =
		"SELECT ?name ?id ?basev (group_concat(distinct ?bus;separator=\"\\n\") as ?buses) (group_concat(distinct ?phs;separator=\"\\n\") as ?phases) ?len ?lname WHERE {"+
		" ?s r:type c:ACLineSegment."+
		" ?s c:IdentifiedObject.name ?name."+
		" ?s c:ConductingEquipment.BaseVoltage ?bv."+
		" ?bv c:BaseVoltage.nominalVoltage ?basev."+
		" ?s c:Conductor.length ?len."+
		" ?s c:ACLineSegment.PerLengthImpedance ?lcode."+
		" ?lcode c:IdentifiedObject.name ?lname."+
		" ?t c:Terminal.ConductingEquipment ?s."+
		" ?t c:Terminal.ConnectivityNode ?cn."+
		" ?cn c:IdentifiedObject.name ?bus"+
		" bind(strafter(str(?s),\"#_\") as ?id)."+
		" OPTIONAL {?acp c:ACLineSegmentPhase.ACLineSegment ?s."+
		" ?acp c:ACLineSegmentPhase.phase ?phsraw."+
		"   bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
		"}"+
		" GROUP BY ?name ?id ?len ?lname ?basev"+
		" ORDER BY ?name";

	public String lname;

	public DistLinesCodeZ (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			String[] buses = soln.get("?buses").toString().split("\\n");
			bus1 = SafeName(buses[0]); 
			bus2 = SafeName(buses[1]); 
			phases = OptionalString (soln, "?phases", "ABC");
			phases = phases.replace ('\n', ':');
			basev = Double.parseDouble (soln.get("?basev").toString());
			len = Double.parseDouble (soln.get("?len").toString());
			lname = soln.get("?lname").toString();
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " from " + bus1 + " to " + bus2 + " phases=" + phases + " basev=" + df4.format(basev) + " len=" + df4.format(len)  + " linecode=" + lname);
		return buf.toString();
	}

	public String GetGLM() {
		StringBuilder buf = new StringBuilder ();
		AppendSharedGLMAttributes (buf, lname);
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}

	public String LabelString() {
		return lname;
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder ("new Line." + name);

		buf.append (" phases=" + Integer.toString(DSSPhaseCount(phases, false)) + 
								" bus1=" + DSSBusPhases(bus1, phases) + " bus2=" + DSSBusPhases (bus2, phases) + 
								" length=" + df3.format(len * gFTperM) + " linecode=" + lname + " units=ft\n");

		return buf.toString();
	}
}

