package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2018, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistMeasurement extends DistComponent {
	public static final String szQUERY = 
		"SELECT ?class ?type ?name ?bus ?phases ?eqtype ?eqname ?eqid ?trmid ?id WHERE {"+
    " ?eq c:Equipment.EquipmentContainer ?fdr."+
    " ?fdr c:IdentifiedObject.mRID ?fdrid."+ 
		" { ?s r:type c:Discrete. bind (\"Discrete\" as ?class)}"+
		"   UNION"+
		" { ?s r:type c:Analog. bind (\"Analog\" as ?class)}"+
		"  ?s c:IdentifiedObject.name ?name ."+
		"  ?s c:IdentifiedObject.mRID ?id ."+
		"  ?s c:Measurement.PowerSystemResource ?eq ."+
		"  ?s c:Measurement.Terminal ?trm ."+
		"  ?s c:Measurement.measurementType ?type ."+
		"  ?trm c:IdentifiedObject.mRID ?trmid."+
		"  ?eq c:IdentifiedObject.mRID ?eqid."+
		"  ?eq c:IdentifiedObject.name ?eqname."+
		"  ?eq r:type ?typeraw."+
		"   bind(strafter(str(?typeraw),\"#\") as ?eqtype)"+
		"  ?trm c:Terminal.ConnectivityNode ?cn."+
		"  ?cn c:IdentifiedObject.name ?bus."+
		"  ?s c:Measurement.phases ?phsraw ."+
		"    {bind(strafter(str(?phsraw),\"PhaseCode.\") as ?phases)}"+
		" } ORDER BY ?class ?type ?name";

	public String id;
	public String eqid;
	public String trmid;
	public String name;
	public String bus;
	public String measType;
	public String measClass;
	public String phases;
	public String eqname;
	public String eqtype;
	public String simobj;

	public DistMeasurement (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			eqname = SafeName (soln.get("?eqname").toString());
			eqtype = SafeName (soln.get("?eqtype").toString());
			measType = SafeName (soln.get("?type").toString());
			measClass = SafeName (soln.get("?class").toString());
			id = soln.get("?id").toString();
			eqid = soln.get("?eqid").toString();
			trmid = soln.get("?trmid").toString();
			bus = SafeName (soln.get("?bus").toString());
			phases = OptionalString (soln, "?phases", "ABC");
		}		
//		System.out.println (DisplayString());
	}

	public void FindSimObject (String loadname, String busphases, boolean bStorage, boolean bSolar, boolean bSyncMachines) {
		if (eqtype.equals ("LinearShuntCompensator")) {
			simobj = "cap_" + eqname;
		} else if (eqtype.equals ("PowerElectronicsConnection")) {
			if (bStorage) {
				simobj = bus + "_stmtr";
			} else if (bSolar) {
				simobj = bus + "_pvmtr";
			} else {
				simobj = "UKNOWN INVERTER";
			}
		} else if (eqtype.equals("ACLineSegment")) {
			if (phases.contains ("s")) {
				simobj = "tpx_" + eqname;
			} else {
				simobj = "line_" + eqname;
			}
		} else if (eqtype.equals ("PowerTransformer")) { // RatioTapChanger or PowerTransformer
			if (measClass.equals("Discrete")) {
				simobj = "reg_" + eqname;
			} else {
				simobj = "xf_" + eqname;
			}
		} else if (eqtype.equals("LoadBreakSwitch")) {
			simobj = "swt_" + eqname;
		} else if (eqtype.equals ("Recloser")) {
			simobj = "swt_" + eqname;
		} else if (eqtype.equals ("Breaker")) {
			simobj = "swt_" + eqname;
		} else if (eqtype.equals ("SynchronousMachine")) {
			simobj = bus + "_dgmtr";
		} else if (eqtype.equals ("EnergyConsumer")) {
			simobj = loadname;
		} else {
			simobj = "UKNOWN";
		}
	}

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"mRID\":\"" + id +"\"");
		buf.append (",\"ConductingEquipment_mRID\":\"" + eqid +"\"");
		buf.append (",\"Terminal_mRID\":\"" + trmid +"\"");
		buf.append (",\"measurementType\":\"" + measType + "\"");
		buf.append (",\"phases\":\"" + phases + "\"");
		buf.append (",\"MeasurementClass\":\"" + measClass + "\"");
		buf.append (",\"ConductingEquipment_type\":\"" + eqtype + "\"");
		buf.append (",\"ConductingEquipment_name\":\"" + eqname + "\"");
		buf.append (",\"ConnectivityNode\":\"" + bus + "\"");
		buf.append (",\"SimObject\":\"" + simobj + "\"");
		buf.append ("}");
		return buf.toString();
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + ":" + id + ":" + eqid + ":" + trmid + ":" + measType + ":" + phases
								 + ":" + measClass + ":" + eqtype + ":" + eqname + ":" + bus);
		return buf.toString();
	}

	public String GetKey() {
		return id;
	}
}

