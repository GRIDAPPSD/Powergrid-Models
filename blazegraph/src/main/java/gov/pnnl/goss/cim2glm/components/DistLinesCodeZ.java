package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017-2019, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistLinesCodeZ extends DistLineSegment {
	public static final String szQUERY =
		"SELECT ?name ?id ?basev ?bus1 ?bus2 ?len ?lname ?codeid ?fdrid ?seq ?phs WHERE {"+
		" ?s r:type c:ACLineSegment."+
		" ?s c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?s c:IdentifiedObject.name ?name."+
		" ?s c:ConductingEquipment.BaseVoltage ?bv."+
		" ?bv c:BaseVoltage.nominalVoltage ?basev."+
		" ?s c:Conductor.length ?len."+
		" ?s c:ACLineSegment.PerLengthImpedance ?lcode."+
		" ?lcode c:IdentifiedObject.name ?lname."+
		" bind(strafter(str(?lcode),\"#\") as ?codeid)."+
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
		" ?acp c:ACLineSegmentPhase.sequenceNumber ?seq."+
		" ?acp c:ACLineSegmentPhase.phase ?phsraw."+
		"   bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
		"}"+
		" ORDER BY ?name ?seq ?phs";

	public String lname;
	public String codeid;

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"mRID\":\"" + id +"\"");
		buf.append ("}");
		return buf.toString();
	}

	public DistLinesCodeZ (ResultSet results, HashMap<String,Integer> map) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			bus1 = SafeName (soln.get("?bus1").toString()); 
			bus2 = SafeName (soln.get("?bus2").toString());
			basev = Double.parseDouble (soln.get("?basev").toString());
			len = Double.parseDouble (soln.get("?len").toString());
			lname = soln.get("?lname").toString();
			codeid = soln.get("?codeid").toString();
			int nphs = map.get (name);
			if (nphs > 0) {
				StringBuilder buf = new StringBuilder(soln.get("?phs").toString());
				for (int i = 1; i < nphs; i++) {
					soln = results.next();
					buf.append (soln.get("?phs").toString());
				}
				phases = buf.toString();
			} else {
				phases = "ABC";
			}
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
		AppendDSSRatings (buf, normalCurrentLimit, emergencyCurrentLimit);

		return buf.toString();
	}
}

