package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017-2019, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistLinesSpacingZ extends DistLineSegment {
	public static final String szQUERY =
		"SELECT ?name ?id ?basev ?bus1 ?bus2 ?fdrid ?len ?spacing ?spcid ?phs ?phname ?phclass"+
		" WHERE {"+
		" ?s r:type c:ACLineSegment."+
		" ?s c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?s c:IdentifiedObject.name ?name."+
		"   bind(strafter(str(?s),\"#\") as ?id)."+
		" ?s c:ConductingEquipment.BaseVoltage ?bv."+
		" ?bv c:BaseVoltage.nominalVoltage ?basev."+
		" ?s c:Conductor.length ?len."+
		" ?s c:ACLineSegment.WireSpacingInfo ?inf."+
		"   bind(strafter(str(?inf),\"#\") as ?spcid)."+
		" ?inf c:IdentifiedObject.name ?spacing."+
		" ?t1 c:Terminal.ConductingEquipment ?s."+
		" ?t1 c:Terminal.ConnectivityNode ?cn1."+
		" ?t1 c:ACDCTerminal.sequenceNumber \"1\"."+
		" ?cn1 c:IdentifiedObject.name ?bus1."+
		" ?t2 c:Terminal.ConductingEquipment ?s."+
		" ?t2 c:Terminal.ConnectivityNode ?cn2."+
		" ?t2 c:ACDCTerminal.sequenceNumber \"2\"."+
		" ?cn2 c:IdentifiedObject.name ?bus2."+
		" ?acp c:ACLineSegmentPhase.ACLineSegment ?s."+
		" ?acp c:ACLineSegmentPhase.phase ?phsraw."+
		"   bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs)."+
		" ?acp c:ACLineSegmentPhase.WireInfo ?phinf."+
		" ?phinf c:IdentifiedObject.name ?phname."+
		" ?phinf a ?phclassraw."+
		"   bind(strafter(str(?phclassraw),\"CIM100#\") as ?phclass)"+
		" }"+
		" ORDER BY ?id ?name ?phs";

	public String spacing;
	public String spcid;
	public int nwires;
	public String[] wire_phases;
	public String[] wire_names;
	public String[] wire_classes;

	public String glm_config;

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"mRID\":\"" + id +"\"");
		buf.append ("}");
		return buf.toString();
	}

	public DistLinesSpacingZ (ResultSet results, HashMap<String,Integer> map) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			bus1 = SafeName (soln.get("?bus1").toString()); 
			bus2 = SafeName (soln.get("?bus2").toString()); 
			len = Double.parseDouble (soln.get("?len").toString());
			basev = Double.parseDouble (soln.get("?basev").toString());
			spacing = soln.get("?spacing").toString();
			spcid = soln.get("?spcid").toString();
			nwires = map.get (name);
			wire_phases = new String[nwires];
			wire_names = new String[nwires];
			wire_classes = new String[nwires];
			StringBuilder buf = new StringBuilder ("");
			for (int i = 0; i < nwires; i++) {
				wire_phases[i] = soln.get("?phs").toString();
				wire_classes[i] = soln.get("?phclass").toString();
				wire_names[i] = soln.get("?phname").toString();
				if (wire_phases[i].equals("N") == false) {
					buf.append (wire_phases[i]);
				}
				if ((i + 1) < nwires) {
					soln = results.next();
				}
			}
			phases = buf.toString();
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " from " + bus1 + " to " + bus2 + 
								" basev=" + df4.format(basev) + " len=" + df4.format(len) + " spacing=" + spacing);
		for (int i = 0; i < nwires; i++) {
			buf.append ("\n  phs=" + wire_phases[i] + " wire=" + wire_names[i] + " class=" + wire_classes[i]);
		}
		return buf.toString();
	}

	public String GetGLM() {
		StringBuilder buf = new StringBuilder ();
		if (wire_classes[0].equals("OverheadWireInfo")) {
			bCable = false;
		} else {
			bCable = true;
		}
		AppendSharedGLMAttributes(buf, glm_config, true);
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}

	public String LabelString() {
		return spacing + ":" + wire_names[0];
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder ("new Line." + name);
		boolean bCable = false;

		buf.append (" phases=" + Integer.toString(DSSPhaseCount(phases, false)) + 
								" bus1=" + DSSBusPhases(bus1, phases) + " bus2=" + DSSBusPhases (bus2, phases) + 
								" length=" + df1.format(len * gFTperM) + " spacing=" + spacing + "_" + phases + " units=ft\n");
		if (wire_classes[0].equals("OverheadWireInfo")) {
			buf.append ("~ wires=[");
		} else if (wire_classes[0].equals("ConcentricNeutralCableInfo")) {
			buf.append ("~ CNCables=[");
			bCable = true;
		} else if (wire_classes[0].equals("TapeShieldCableInfo")) {
			buf.append ("~ TSCables=[");
			bCable = true;
		}
		for (int i = 0; i < nwires; i++) {
			if (bCable == true && wire_classes[i].equals("OverheadWireInfo")) {
				buf.append ("] wires=[");
			} else if (i > 0) {
				buf.append (",");
			}
			buf.append(wire_names[i]);
		}
		buf.append("]\n");
		return buf.toString();
	}
}

