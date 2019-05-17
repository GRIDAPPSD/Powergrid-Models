package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;

public class DistLinesCodeZ extends DistLineSegment {
	public static final String szQUERY =
		"SELECT ?name ?id ?basev ?bus1 ?bus2 (group_concat(distinct ?phs;separator=\"\\n\") as ?phases) ?len ?lname ?fdrid WHERE {"+
		" ?s r:type c:ACLineSegment."+
		" ?s c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?s c:IdentifiedObject.name ?name."+
		" ?s c:ConductingEquipment.BaseVoltage ?bv."+
		" ?bv c:BaseVoltage.nominalVoltage ?basev."+
		" ?s c:Conductor.length ?len."+
		" ?s c:ACLineSegment.PerLengthImpedance ?lcode."+
		" ?lcode c:IdentifiedObject.name ?lname."+
		" ?t1 c:Terminal.ConductingEquipment ?s."+
		" ?t1 c:Terminal.ConnectivityNode ?cn1."+
		" ?t1 c:ACDCTerminal.sequenceNumber \"1\"."+
		" ?cn1 c:IdentifiedObject.name ?bus1."+
		" ?t2 c:Terminal.ConductingEquipment ?s."+
		" ?t2 c:Terminal.ConnectivityNode ?cn2."+
		" ?t2 c:ACDCTerminal.sequenceNumber \"2\"."+
		" ?cn2 c:IdentifiedObject.name ?bus2."+
		" bind(strafter(str(?s),\"#\") as ?id)."+
		" OPTIONAL {?acp c:ACLineSegmentPhase.ACLineSegment ?s."+
		" ?acp c:ACLineSegmentPhase.phase ?phsraw."+
		"   bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
		"}"+
		" GROUP BY ?name ?bus1 ?bus2 ?id ?len ?lname ?basev ?fdrid"+
		" ORDER BY ?name";

	public String lname;

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"mRID\":\"" + id +"\"");
		buf.append ("}");
		return buf.toString();
	}

	public DistLinesCodeZ (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			bus1 = SafeName (soln.get("?bus1").toString()); 
			bus2 = SafeName (soln.get("?bus2").toString()); 
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
		AppendSharedGLMAttributes (buf, lname, false);
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

