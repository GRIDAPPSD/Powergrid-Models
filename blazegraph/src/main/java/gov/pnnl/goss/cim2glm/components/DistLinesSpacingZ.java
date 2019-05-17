package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;

public class DistLinesSpacingZ extends DistLineSegment {
	public static final String szQUERY =
		"SELECT ?name ?id ?basev ?bus1 ?bus2 ?fdrid ?len ?spacing ?wname ?wclass"+
		"       (group_concat(distinct ?phs;separator=\"\\n\") as ?phases)"+
		"       (group_concat(distinct ?phname;separator=\"\\n\") as ?phwires)"+
		"       (group_concat(distinct ?phclass;separator=\"\\n\") as ?phclasses) WHERE {"+
		" SELECT ?name ?id ?basev ?bus1 ?bus2 ?fdrid ?len ?spacing ?wname ?wclass ?phs ?phname ?phclass"+
		" WHERE {"+
		" ?s r:type c:ACLineSegment."+
		" ?s c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?s c:IdentifiedObject.name ?name."+
		" bind(strafter(str(?s),\"#\") as ?id)."+
		" ?s c:ConductingEquipment.BaseVoltage ?bv."+
		" ?bv c:BaseVoltage.nominalVoltage ?basev."+
		" ?s c:Conductor.length ?len."+
		" ?asset c:Asset.PowerSystemResources ?s."+
		" ?asset c:Asset.AssetInfo ?inf."+
		" ?inf c:IdentifiedObject.name ?spacing."+
		" ?inf a c:WireSpacingInfo."+
		" OPTIONAL {"+
		"       ?wasset c:Asset.PowerSystemResources ?s."+
		"       ?wasset c:Asset.AssetInfo ?winf."+
		"       ?winf c:WireInfo.radius ?rad."+
		"       ?winf c:IdentifiedObject.name ?wname."+
		"       ?winf a ?classraw."+
		"       	bind(strafter(str(?classraw),\"cim17#\") as ?wclass)"+
		" }"+
		" ?t1 c:Terminal.ConductingEquipment ?s."+
		" ?t1 c:Terminal.ConnectivityNode ?cn1."+
		" ?t1 c:ACDCTerminal.sequenceNumber \"1\"."+
		" ?cn1 c:IdentifiedObject.name ?bus1."+
		" ?t2 c:Terminal.ConductingEquipment ?s."+
		" ?t2 c:Terminal.ConnectivityNode ?cn2."+
		" ?t2 c:ACDCTerminal.sequenceNumber \"2\"."+
		" ?cn2 c:IdentifiedObject.name ?bus2"+
		" OPTIONAL {"+
		"       ?acp c:ACLineSegmentPhase.ACLineSegment ?s."+
		"       ?acp c:ACLineSegmentPhase.phase ?phsraw."+
		"       	bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs)"+
		"       OPTIONAL {"+
		"       	?phasset c:Asset.PowerSystemResources ?acp."+
		"       	?phasset c:Asset.AssetInfo ?phinf."+
		"       	?phinf c:WireInfo.radius ?phrad."+
		"       	?phinf c:IdentifiedObject.name ?phname."+
		"       	?phinf a ?phclassraw."+
		"       		bind(strafter(str(?phclassraw),\"cim17#\") as ?phclass)"+
		"       	}"+
		"       }"+
		" } ORDER BY ?name ?phs"+
		" }"+
		" GROUP BY ?name ?id ?basev ?bus1 ?bus2 ?fdrid ?len ?spacing ?wname ?wclass"+
		" ORDER BY ?name";

	public String spacing;
	public String wname;
	public String wclass;
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

	public DistLinesSpacingZ (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			bus1 = SafeName (soln.get("?bus1").toString()); 
			bus2 = SafeName (soln.get("?bus2").toString()); 
			len = Double.parseDouble (soln.get("?len").toString());
			basev = Double.parseDouble (soln.get("?basev").toString());
			spacing = soln.get("?spacing").toString();
			wname = soln.get("?wname").toString();
			wclass = soln.get("?wclass").toString();
			nwires = 0;
			phases = OptionalString (soln, "?phases", "");
			if (phases.length() > 0) {
				String phwires = OptionalString (soln, "?phwires", "");
				String phclasses = OptionalString (soln, "?phclasses", "");
				wire_phases = phases.split("\\n");
				nwires = wire_phases.length;
				wire_names = new String[nwires];
				wire_classes = new String[nwires];
				String[] phwire = phwires.split("\\n");
				String[] phclass = phclasses.split("\\n");
				int idxWire = phwire.length - 1;
				int idxClass = phclass.length - 1;
				for (int i = nwires - 1; i >= 0; i--) {
					wire_names[i] = phwire[idxWire];
					wire_classes[i] = phclass[idxClass];
					if (idxClass > 0) --idxClass;
					if (idxWire > 0) --idxWire;
				}
			}
			phases = phases.replace ('\n', ':');
//			System.out.println (DisplayString());
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " from " + bus1 + " to " + bus2 + 
								" basev=" + df4.format(basev) + " len=" + df4.format(len) + " spacing=" + spacing);
		buf.append (" wname=" + wname + " wclass=" + wclass);
		for (int i = 0; i < nwires; i++) {
			buf.append ("\n  phs=" + wire_phases[i] + " wire=" + wire_names[i] + " class=" + wire_classes[i]);
		}
		return buf.toString();
	}

	public String GetGLM() {
		StringBuilder buf = new StringBuilder ();
		if (wclass.equals("OverheadWireInfo")) {
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
		return spacing + ":" + wname;
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder ("new Line." + name);
		boolean bCable = false;

		buf.append (" phases=" + Integer.toString(DSSPhaseCount(phases, false)) + 
								" bus1=" + DSSBusPhases(bus1, phases) + " bus2=" + DSSBusPhases (bus2, phases) + 
								" length=" + df1.format(len * gFTperM) + " spacing=" + spacing + " units=ft\n");
		if (wclass.equals("OverheadWireInfo")) {
			buf.append ("~ wires=[");
		} else if (wclass.equals("ConcentricNeutralCableInfo")) {
			buf.append ("~ CNCables=[");
			bCable = true;
		} else if (wclass.equals("TapeShieldCableInfo")) {
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

