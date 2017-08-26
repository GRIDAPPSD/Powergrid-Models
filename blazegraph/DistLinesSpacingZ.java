//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistLinesSpacingZ extends DistLineSegment {
	static final String szQUERY =
		"SELECT ?name ?basev (group_concat(distinct ?bus;separator=\"\\n\") as ?buses)"+
		"       (group_concat(distinct ?phs;separator=\"\\n\") as ?phases)"+
		"       ?len ?spacing ?wname ?wclass"+
		"       (group_concat(distinct ?phname;separator=\"\\n\") as ?phwires)"+
		"       (group_concat(distinct ?phclass;separator=\"\\n\") as ?phclasses) WHERE {"+
		" ?s r:type c:ACLineSegment."+
		" ?s c:IdentifiedObject.name ?name."+
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
		"       	bind(strafter(str(?classraw),\"cim16#\") as ?wclass)"+
		" }"+
		" ?t c:Terminal.ConductingEquipment ?s."+
		" ?t c:Terminal.ConnectivityNode ?cn."+
		" ?cn c:IdentifiedObject.name ?bus"+
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
		"       		bind(strafter(str(?phclassraw),\"cim16#\") as ?phclass)"+
		"       	}"+
		"       }"+
		" }"+
		" GROUP BY ?name ?basev ?len ?spacing ?wname ?wclass"+
		" ORDER BY ?name";

	public String spacing;
	public String wname;
	public String wclass;
	public int nwires;
	public String[] wire_phases;
	public String[] wire_names;
	public String[] wire_classes;

	public DistLinesSpacingZ (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = GLD_Name (soln.get("?name").toString(), false);
			String[] buses = soln.get("?buses").toString().split("\\n");
			bus1 = GLD_Name(buses[0], true); 
			bus2 = GLD_Name(buses[1], true); 
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
				String lastWire = phwire[0];
				String lastClass = phclass[0];
				for (int i = 0; i < nwires; i++) {
					if (i < phwire.length) {
						lastWire = phwire[i];
					}
					if (i < phclass.length) {
						lastClass = phclass[i];
					}
					wire_names[i] = lastWire;
					wire_classes[i] = lastClass;
				}
			}
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " from " + bus1 + " to " + bus2 + 
								" basev=" + df.format(basev) + " len=" + df.format(len) + " spacing=" + spacing);
		buf.append (" wname=" + wname + "wclass=" + wclass);
		for (int i = 0; i < nwires; i++) {
			buf.append ("\n  phs=" + wire_phases[i] + " wire=" + wire_names[i] + " class=" + wire_classes[i]);
		}
		return buf.toString();
	}

	public String GetGLM() {
		StringBuilder buf = new StringBuilder ("object overhead_line {\n");
		AppendSharedGLMAttributes (buf);
		buf.append("}\n");

		return buf.toString();
	}

	public String GetKey() {
		return name;
	}

	public String LabelString() {
		return spacing + ":" + wname;
	}
}

