package gov.pnnl.goss.cim2glm;
// ----------------------------------------------------------
// Copyright (c) 2017-2019, Battelle Memorial Institute
// All rights reserved.
// ----------------------------------------------------------

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;
import java.util.UUID;

import org.apache.jena.query.*;

import gov.pnnl.goss.cim2glm.CIMImporter;

import gov.pnnl.goss.cim2glm.components.DistBaseVoltage;
import gov.pnnl.goss.cim2glm.components.DistBreaker;
import gov.pnnl.goss.cim2glm.components.DistCapacitor;
import gov.pnnl.goss.cim2glm.components.DistComponent;
import gov.pnnl.goss.cim2glm.components.DistConcentricNeutralCable;
import gov.pnnl.goss.cim2glm.components.DistCoordinates;
import gov.pnnl.goss.cim2glm.components.DistDisconnector;
import gov.pnnl.goss.cim2glm.components.DistFeeder;
import gov.pnnl.goss.cim2glm.components.DistFuse;
import gov.pnnl.goss.cim2glm.components.DistGroundDisconnector;
import gov.pnnl.goss.cim2glm.components.DistHouse;
import gov.pnnl.goss.cim2glm.components.DistJumper;
import gov.pnnl.goss.cim2glm.components.DistLineSegment;
import gov.pnnl.goss.cim2glm.components.DistLineSpacing;
import gov.pnnl.goss.cim2glm.components.DistLinesCodeZ;
import gov.pnnl.goss.cim2glm.components.DistLinesInstanceZ;
import gov.pnnl.goss.cim2glm.components.DistLinesSpacingZ;
import gov.pnnl.goss.cim2glm.components.DistLoad;
import gov.pnnl.goss.cim2glm.components.DistLoadBreakSwitch;
import gov.pnnl.goss.cim2glm.components.DistMeasurement;
import gov.pnnl.goss.cim2glm.components.DistOverheadWire;
import gov.pnnl.goss.cim2glm.components.DistPhaseMatrix;
import gov.pnnl.goss.cim2glm.components.DistPowerXfmrCore;
import gov.pnnl.goss.cim2glm.components.DistPowerXfmrMesh;
import gov.pnnl.goss.cim2glm.components.DistPowerXfmrWinding;
import gov.pnnl.goss.cim2glm.components.DistRecloser;
import gov.pnnl.goss.cim2glm.components.DistRegulator;
import gov.pnnl.goss.cim2glm.components.DistSectionaliser;
import gov.pnnl.goss.cim2glm.components.DistSequenceMatrix;
import gov.pnnl.goss.cim2glm.components.DistSolar;
import gov.pnnl.goss.cim2glm.components.DistStorage;
import gov.pnnl.goss.cim2glm.components.DistSubstation;
import gov.pnnl.goss.cim2glm.components.DistSwitch;
import gov.pnnl.goss.cim2glm.components.DistSyncMachine;
import gov.pnnl.goss.cim2glm.components.DistTapeShieldCable;
import gov.pnnl.goss.cim2glm.components.DistXfmrBank;
import gov.pnnl.goss.cim2glm.components.DistXfmrCodeOCTest;
import gov.pnnl.goss.cim2glm.components.DistXfmrCodeRating;
import gov.pnnl.goss.cim2glm.components.DistXfmrCodeSCTest;
import gov.pnnl.goss.cim2glm.components.DistXfmrTank;

import gov.pnnl.goss.cim2glm.queryhandler.QueryHandler;
import gov.pnnl.goss.cim2glm.queryhandler.impl.HTTPBlazegraphQueryHandler;

public class CIMWriter extends Object {
	QueryHandler queryHandler;

	HashMap<String,String> mapLocations = new HashMap<>();
	HashMap<String,List<String>> mapWindings = new HashMap<>();
	HashSet<String> setLocations = new HashSet<>();

	private static final String xnsCIM = "http://iec.ch/TC57/2009/CIM-schema-cim14#";
	private static final String xnsRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	private static final String szCN = 
		"SELECT ?name ?id WHERE {"+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?s c:ConnectivityNode.ConnectivityNodeContainer ?fdr."+
		" ?s r:type c:ConnectivityNode."+
		" ?s c:IdentifiedObject.name ?name."+
		" bind(strafter(str(?s),\"#\") as ?id)."+
		"} ORDER by ?name";

	private static final String szTRM = 
		"SELECT ?eqclass ?eqname ?seq ?name ?eqid ?tid ?cnid WHERE {"+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?eq c:Equipment.EquipmentContainer ?fdr."+
		" ?t c:Terminal.ConductingEquipment ?eq."+
		" ?t c:Terminal.ConnectivityNode ?cn. "+
		" ?t c:ACDCTerminal.sequenceNumber ?seq."+
		" bind(strafter(str(?eq),\"#\") as ?eqid)."+
		" bind(strafter(str(?t),\"#\") as ?tid)."+
		" bind(strafter(str(?cn),\"#\") as ?cnid)."+
		" ?t c:IdentifiedObject.name ?name."+
		" ?eq c:IdentifiedObject.name ?eqname."+
		" ?eq a ?classraw."+
		" bind(strafter(str(?classraw),\"CIM100#\") as ?eqclass)"+
		"} ORDER by ?eqclass ?eqname ?seq";

	private static final String szEND = 
		"SELECT ?endclass ?endid ?tid WHERE {"+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" {?pxf c:Equipment.EquipmentContainer ?fdr."+
		"  ?end c:PowerTransformerEnd.PowerTransformer ?pxf.}"+
		" UNION"+
		" {?tank c:Equipment.EquipmentContainer ?fdr."+
		"  ?end c:TransformerTankEnd.TransformerTank ?tank.}"+
		" ?end c:TransformerEnd.Terminal ?t."+
		" ?t c:ACDCTerminal.sequenceNumber ?seq."+
		" bind(strafter(str(?end),\"#\") as ?endid)."+
		" bind(strafter(str(?t),\"#\") as ?tid)."+
		" ?end a ?classraw."+
		" bind(strafter(str(?classraw),\"CIM100#\") as ?endclass)"+
	  "} ORDER by ?endclass ?endid";

	// TODO: this only gets locations for conducting equipment, not other types of PSR
	private static final String szLOC =
		"SELECT DISTINCT ?eqname ?name ?eqid ?locid WHERE {"+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?eq c:Equipment.EquipmentContainer ?fdr."+
		" ?eq c:PowerSystemResource.Location ?loc."+
		" bind(strafter(str(?loc),\"#\") as ?locid)."+
		" bind(strafter(str(?eq),\"#\") as ?eqid)."+
		" ?eq c:IdentifiedObject.name ?eqname."+
		" ?loc c:IdentifiedObject.name ?name."+
		"} ORDER by ?eqname";

	private static final String szPOS =
		"SELECT DISTINCT ?locid ?id ?seq ?x ?y WHERE {"+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?eq c:Equipment.EquipmentContainer ?fdr."+
		" ?eq c:PowerSystemResource.Location ?loc."+
		" ?p c:PositionPoint.Location ?loc."+
		" ?p c:PositionPoint.sequenceNumber ?seq."+
		" ?p c:PositionPoint.xPosition ?x."+
		" ?p c:PositionPoint.yPosition ?y."+
		" bind(strafter(str(?loc),\"#\") as ?locid)."+
		" bind(strafter(str(?p),\"#\") as ?id)."+
		"} ORDER by ?locid ?seq";

	private static final String szXFINF =
		"SELECT ?tname ?ename ?seq ?tid ?eid WHERE {"+
		" ?e c:TransformerEndInfo.TransformerTankInfo ?t."+
		" ?t c:IdentifiedObject.name ?tname."+
		" ?t c:IdentifiedObject.mRID ?tid."+
		" ?e c:IdentifiedObject.name ?ename."+
		" ?e c:IdentifiedObject.mRID ?eid."+
		" ?e c:TransformerEndInfo.endNumber ?seq."+
		"} ORDER BY ?tname ?ename ?seq";

	private String fdrID;
	private String rgnID;
	private String subrgnID;
	private String subID;

	static String ShortenUUID (String uuid) {
		return uuid.substring(1).replace ("-", "");
	}

	private void StartInstance (String root, String id, PrintWriter out) {
		out.println (String.format("<cim:%s rdf:ID=\"%s\">", root, id));
	}

	private void StartFreeInstance (String root, PrintWriter out) {
		String id = ShortenUUID ("_" + UUID.randomUUID().toString().toUpperCase());
		StartInstance (root, id, out);
	}

	private void EndInstance (String root, PrintWriter out) {
		out.println (String.format("</cim:%s>", root));
	}

	private void StringNode (String key, String val, PrintWriter out) {
		out.println (String.format("  <cim:%s>%s</cim:%s>", key, val, key));
	}

	private void DoubleNode (String key, double val, PrintWriter out) {
		out.println (String.format("  <cim:%s>%.8g</cim:%s>", key, val, key));
	}

	private void IntegerNode (String key, int val, PrintWriter out) {
		out.println (String.format("  <cim:%s>%d</cim:%s>", key, val, key));
	}

	private void BoolNode (String key, boolean val, PrintWriter out) {
		out.println (String.format("  <cim:%s>%s</cim:%s>", key, Boolean.toString(val), key));
	}

	private void RefNode (String key, String val, PrintWriter out) {
		out.println (String.format("  <cim:%s rdf:resource=\"#%s\"/>", key, val, key));
	}

	private void WindingConnectionEnum (String conn, PrintWriter out) {
		out.println (
				String.format("  <cim:WindingInfo.connectionKind rdf:resource=\"%sWindingConnection.%s\"/>", 
											xnsCIM, conn));
	}

	private void XfWindingConnectionEnum (String conn, PrintWriter out) {
		out.println (
				String.format("  <cim:TransformerWinding.connectionType rdf:resource=\"%sWindingConnection.%s\"/>", 
											xnsCIM, conn));
	}

	private void XfWindingTypeEnum (int endNumber, PrintWriter out) {
		String val;
		if (endNumber == 1) {
			val = "primary";
		} else if (endNumber == 2) {
			val = "secondary";
		} else if (endNumber == 3) {
			val = "tertiary";
		} else {
			val = "quaternary";
		}
		out.println(
				String.format("  <cim:TransformerWinding.connectionType rdf:resource=\"%sWindingType.%s\"/>", 
											xnsCIM, val));
	}

	private void PhasesEnum (String phs, PrintWriter out) {
		String val;
		if (phs.contains("s1")) {
			if (phs.contains ("s2")) {
				val = "splitSecondary12N";
			} else {
				val = "splitSecondary1N";
			}
		} else if (phs.contains("s2")) {
			val = "splitSecondary2N";
		} else {
			val = phs;
		}
		out.println(String.format("  <cim:ConductingEquipment.phases rdf:resource=\"%sPhaseCode.%s\"/>", xnsCIM, val));
	}

	private void LoadTransformerInfo (PrintWriter out) {
		ResultSet results = queryHandler.query (szXFINF);
		String lastName = "";
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			String tname = DistComponent.SafeName (soln.get("?tname").toString());
			String tid = ShortenUUID (soln.get("?tid").toString());
			String ename = DistComponent.SafeName (soln.get("?ename").toString());
			String eid = ShortenUUID (soln.get("?eid").toString());
			if (!tname.equals(lastName)) {
				StartInstance("TransformerInfo", tid, out);
				StringNode ("IdentifiedObject.mRID", tid, out);
				StringNode ("IdentifiedObject.name", tname, out);
				EndInstance ("TransformerInfo", out);
				mapWindings.put(tid, new ArrayList<String>());
				lastName = tname;
			}
			mapWindings.get(tid).add(eid);
		}
		((ResultSetCloseable)results).close();
	}

	private void LoadConnectivityNodes (PrintWriter out) {
		ResultSet results = queryHandler.query (szCN);
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			String name = DistComponent.SafeName (soln.get("?name").toString());
			String id = ShortenUUID (soln.get("?id").toString());
			StartInstance ("ConnectivityNode", id, out);
			StringNode ("IdentifiedObject.mRID", id, out);
			StringNode ("IdentifiedObject.name", name, out);
			EndInstance ("ConnectivityNode", out);
		}
		((ResultSetCloseable)results).close();
	}

	private void LoadTerminals (PrintWriter out) {
		HashMap<String,String> mapEnds = new HashMap<>();
		ResultSet resEnds = queryHandler.query (szEND);
		while (resEnds.hasNext()) {
			QuerySolution soln = resEnds.next();
			String endid = ShortenUUID (soln.get("?endid").toString());
			String tid = ShortenUUID (soln.get("?tid").toString());
			mapEnds.put (tid, endid);
		}
		((ResultSetCloseable)resEnds).close();

		ResultSet results = queryHandler.query (szTRM);
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			String name = DistComponent.SafeName (soln.get("?name").toString());
			String id = ShortenUUID (soln.get("?tid").toString());
			String eqid = ShortenUUID (soln.get("?eqid").toString());
			String cnid = ShortenUUID (soln.get("?cnid").toString());
			String eqclass = soln.get("?eqclass").toString();
			int seq = Integer.parseInt (soln.get("?seq").toString());
			StartInstance ("Terminal", id, out);
			StringNode ("IdentifiedObject.mRID", id, out);
			StringNode ("IdentifiedObject.name", name, out);
			StringNode ("Terminal.connected", "true", out);
			RefNode ("Terminal.ConnectivityNode", cnid, out);
			if (eqclass.equals("PowerTransformer")) {
				RefNode("Terminal.ConductingEquipment", mapEnds.get(id), out);
				IntegerNode ("Terminal.sequenceNumber", 1, out);
			} else {
				RefNode("Terminal.ConductingEquipment", eqid, out);
				IntegerNode ("Terminal.sequenceNumber", seq, out); // after CIM14, it's ACDCTerminal.sequenceNumber
			}
			EndInstance ("Terminal", out);
		}
		((ResultSetCloseable)results).close();
	}

	private void LoadLocations (PrintWriter out) {
		ResultSet results = queryHandler.query (szLOC);
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			String name = DistComponent.SafeName (soln.get("?name").toString());
			String id = ShortenUUID (soln.get("?locid").toString());
			String eqid = ShortenUUID (soln.get("?eqid").toString());
			if (setLocations.contains (id) == false) {
				StartInstance("GeoLocation", id, out);
				StringNode ("IdentifiedObject.mRID", id, out);
				StringNode ("IdentifiedObject.name", name, out);
				EndInstance ("GeoLocation", out);
				setLocations.add (id);
			}
			mapLocations.put (eqid, id);
		}
		((ResultSetCloseable)results).close();
	}

	private void LoadPositionPoints (PrintWriter out) {
		ResultSet results = queryHandler.query (szPOS);
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			String id = ShortenUUID (soln.get("?id").toString());
			String locid = ShortenUUID (soln.get("?locid").toString());
			int seq = Integer.parseInt (soln.get("?seq").toString());
			double x = Double.parseDouble (soln.get("?x").toString());
			double y = Double.parseDouble (soln.get("?y").toString());
			StartInstance ("PositionPoint", id, out);
			RefNode ("PositionPoint.Location", locid, out);
			IntegerNode ("PositionPoint.sequenceNumber", seq, out);
			DoubleNode ("PositionPoint.xPosition", x, out);
			DoubleNode ("PositionPoint.yPosition", y, out);
			EndInstance ("PositionPoint", out);
		}
		((ResultSetCloseable)results).close();
	}

	private void DistributionLineSegment (DistLineSegment obj, String psrtype, PrintWriter out) {
		String objID = ShortenUUID (obj.id);
		StartInstance ("DistributionLineSegment", objID, out);
		StringNode ("IdentifiedObject.mRID", objID, out);
		StringNode ("IdentifiedObject.name", obj.name, out);
		DoubleNode ("Conductor.length", obj.len, out);
		RefNode ("Equipment.EquipmentContainer", fdrID, out);
		RefNode ("PowerSystemResource.PSRType", psrtype, out);
		RefNode ("PowerSystemResource.GeoLocation", mapLocations.get (objID), out);
		PhasesEnum (obj.phases, out);
		EndInstance ("DistributionLineSegment", out);
	}

	private void StartSwitch (String className, DistSwitch obj, PrintWriter out) {
		String objID = ShortenUUID (obj.id);
		StartInstance (className, objID, out);
		StringNode ("IdentifiedObject.mRID", objID, out);
		StringNode ("IdentifiedObject.name", obj.name, out);
		RefNode ("Equipment.EquipmentContainer", fdrID, out);
		RefNode ("PowerSystemResource.GeoLocation", mapLocations.get (objID), out);
		BoolNode ("Switch.normalOpen", obj.open, out);
		PhasesEnum (obj.phases, out);
	}

	private void PSRType (String id, String name, PrintWriter out) {
		StartInstance ("PSRType", id, out);
		StringNode ("IdentifiedObject.name", name, out);
		EndInstance ("PSRType", out);
	}

	public void WriteCIMFile (CIMImporter mdl, QueryHandler qH, PrintWriter out)  {
		queryHandler = qH;
		String objID, eID, infID;

		out.println ("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		out.println ("<!-- un-comment this line to enable validation");
		out.println ("-->");
		out.println ("<rdf:RDF xmlns:cim=\"" + xnsCIM + "\" xmlns:rdf=\"" + xnsRDF + "\">");
		out.println ("<!--");
		out.println ("-->");

		StartFreeInstance ("IEC61970CIMVersion", out);
		StringNode ("IEC61970CIMVersion.version", "IEC61970CIM14v12", out);
		StringNode ("IEC61970CIMVersion.date", "2009-11-27T00:00:00", out);
		EndInstance ("IEC61970CIMVersion", out);

		for (HashMap.Entry<String,DistFeeder> fdrPair : mdl.mapFeeders.entrySet()) {
			DistFeeder fdr = fdrPair.getValue();
			fdrID = ShortenUUID (fdr.feederID);
			rgnID = ShortenUUID (fdr.regionID);
			subrgnID = ShortenUUID (fdr.subregionID);
			subID = ShortenUUID (fdr.substationID);

			StartInstance ("GeographicalRegion", rgnID, out);
			StringNode ("IdentifiedObject.mRID", rgnID, out);
			StringNode ("IdentifiedObject.name", fdr.regionName, out);
			StringNode ("IdentifiedObject.description", "Top-level region", out);
			EndInstance ("GeographicalRegion", out);

			StartInstance ("SubGeographicalRegion", subrgnID, out);
			StringNode ("IdentifiedObject.mRID", subrgnID, out);
			StringNode ("IdentifiedObject.name", fdr.subregionName, out);
			StringNode ("IdentifiedObject.description", "Lower-level region", out);
			RefNode ("SubGeographicalRegion.Region", rgnID, out);
			EndInstance ("SubGeographicalRegion", out);

			StartInstance ("Substation", subID, out);  // needs GeoLocation
			StringNode ("IdentifiedObject.mRID", subID, out);
			StringNode ("IdentifiedObject.name", fdr.substationName, out);
			EndInstance ("Substation", out);

			StartInstance ("Line", fdrID, out);
			StringNode ("IdentifiedObject.mRID", fdrID, out);
			StringNode ("IdentifiedObject.name", fdr.feederName, out);
			StringNode ("IdentifiedObject.description", "Feeder equipment container", out);
			RefNode ("Line.Region", subrgnID, out);
			EndInstance ("Line", out);

			LoadConnectivityNodes (out);
			LoadTerminals (out);
			LoadLocations (out);  // we need a lookup map of these before writing out ConductingEquipment
			LoadPositionPoints (out);

			for (HashMap.Entry<String,DistSubstation> pair : mdl.mapSubstations.entrySet()) {
				DistSubstation obj = pair.getValue();
				objID = ShortenUUID (obj.id);
				StartInstance ("EnergySource", objID, out);
				StringNode ("IdentifiedObject.mRID", objID, out);
				StringNode ("IdentifiedObject.name", obj.name, out);
				RefNode ("Equipment.EquipmentContainer", fdrID, out);
				RefNode ("PowerSystemResource.GeoLocation", mapLocations.get (objID), out);
//				DoubleNode ("EnergySource.baseVoltage", obj.basev, out);
				DoubleNode ("EnergySource.nominalVoltage", obj.nomv, out);
				DoubleNode ("EnergySource.voltageMagnitude", obj.vmag, out);
				DoubleNode ("EnergySource.voltageAngle", obj.vang, out);
				DoubleNode ("EnergySource.r", obj.r1, out);
				DoubleNode ("EnergySource.x", obj.x1, out);
				DoubleNode ("EnergySource.r0", obj.r0, out);
				DoubleNode ("EnergySource.x0", obj.x0, out);
				EndInstance ("EnergySource", out);
			}
			// For Survalent, we are writing line codes and spacings as named PSRTypes, 
			//   i.e., not with the actual data
			// If we later write the line code and spacing data, then we should NOT write
			//   PSRTypes that re-use the same UUIDs
			for (HashMap.Entry<String,DistLineSpacing> pair : mdl.mapSpacings.entrySet()) {
				DistLineSpacing obj = pair.getValue();
				PSRType (ShortenUUID (obj.id), "Spacing:" + obj.name, out);
			}
			for (HashMap.Entry<String,DistPhaseMatrix> pair : mdl.mapPhaseMatrices.entrySet()) {
				DistPhaseMatrix obj = pair.getValue();
				PSRType (ShortenUUID (obj.id), "PhaseZ:" + obj.name, out);
			}
			for (HashMap.Entry<String,DistSequenceMatrix> pair : mdl.mapSequenceMatrices.entrySet()) {
				DistSequenceMatrix obj = pair.getValue();
				PSRType (ShortenUUID (obj.id), "SequenceZ:" + obj.name, out);
			}
			String instZid = ShortenUUID ("_" + UUID.randomUUID().toString().toUpperCase());
			PSRType (instZid, "InstanceZ", out);

			for (HashMap.Entry<String,DistLinesSpacingZ> pair : mdl.mapLinesSpacingZ.entrySet()) {
				DistLinesSpacingZ obj = pair.getValue();
				DistributionLineSegment (obj, ShortenUUID (obj.spcid), out);
			}
			for (HashMap.Entry<String,DistLinesInstanceZ> pair : mdl.mapLinesInstanceZ.entrySet()) {
				DistLinesInstanceZ obj = pair.getValue();
				DistributionLineSegment (obj, instZid, out);
			}
			for (HashMap.Entry<String,DistLinesCodeZ> pair : mdl.mapLinesCodeZ.entrySet()) {
				DistLinesCodeZ obj = pair.getValue();
				DistributionLineSegment (obj, ShortenUUID (obj.codeid), out);
			}
			for (HashMap.Entry<String,DistFuse> pair : mdl.mapFuses.entrySet()) {
				DistFuse obj = pair.getValue();
				StartSwitch ("Fuse", obj, out);
				DoubleNode ("Fuse.ratingCurrent", obj.rated, out);
				EndInstance ("Fuse", out);
			}
			for (HashMap.Entry<String,DistBreaker> pair : mdl.mapBreakers.entrySet()) {
				DistBreaker obj = pair.getValue();
				StartSwitch ("Breaker", obj, out);
				EndInstance ("Breaker", out);
			}
			for (HashMap.Entry<String,DistLoadBreakSwitch> pair : mdl.mapLoadBreakSwitches.entrySet()) {
				DistLoadBreakSwitch obj = pair.getValue();
				StartSwitch ("LoadBreakSwitch", obj, out);
				DoubleNode ("LoadBreakSwitch.ratedCurrent", obj.rated, out);
				EndInstance ("LoadBreakSwitch", out);
			}
			for (HashMap.Entry<String,DistLoad> pair : mdl.mapLoads.entrySet()) {
				DistLoad obj = pair.getValue();
				objID = ShortenUUID (obj.id);
				StartInstance ("EnergyConsumer", objID, out);
				StringNode ("IdentifiedObject.mRID", objID, out);
				StringNode ("IdentifiedObject.name", obj.name, out);
				RefNode ("Equipment.EquipmentContainer", fdrID, out);
				RefNode ("PowerSystemResource.GeoLocation", mapLocations.get (objID), out);
				String phs = obj.phases.replace (":", ""); 
				PhasesEnum (phs, out);
				DoubleNode ("EnergyConsumer.pfixed", 1000.0 * obj.p, out);
				DoubleNode ("EnergyConsumer.qfixed", 1000.0 * obj.q, out);
				IntegerNode ("EnergyConsumer.customerCount", obj.cnt, out);
				EndInstance ("EnergyConsumer", out);
			}

			LoadTransformerInfo (out);

			HashMap<String,String> mapBanks = new HashMap<>();
			String secXfid = ShortenUUID ("_" + UUID.randomUUID().toString().toUpperCase());
			PSRType (secXfid, "SplitSecondary", out);
			String miscXfid = ShortenUUID ("_" + UUID.randomUUID().toString().toUpperCase());
			PSRType (miscXfid, "OtherTransformer", out);
			for (HashMap.Entry<String,DistXfmrBank> pair : mdl.mapBanks.entrySet()) {
				DistXfmrBank obj = pair.getValue();
				objID = ShortenUUID (obj.pid);
				StartInstance ("TransformerBank", objID, out);
				StringNode ("IdentifiedObject.mRID", objID, out);
				StringNode ("IdentifiedObject.name", obj.pname, out);
				RefNode ("Equipment.EquipmentContainer", fdrID, out);
				RefNode ("PowerSystemResource.GeoLocation", mapLocations.get (objID), out);
				StringNode ("TransformerBank.vectorGroup", obj.vgrp, out);
				if (obj.vgrp.contains ("Iii")) {
					RefNode("PowerSystemResource.PSRType", secXfid, out);
				} else {
					RefNode("PowerSystemResource.PSRType", miscXfid, out);
				}
				EndInstance ("TransformerBank", out);
				mapBanks.put (obj.pname, objID);
			}
			for (HashMap.Entry<String,DistXfmrTank> pair : mdl.mapTanks.entrySet()) {
				DistXfmrTank obj = pair.getValue();
				objID = ShortenUUID (obj.id);
				infID = ShortenUUID (obj.infoid);
				StartInstance ("DistributionTransformer", objID, out);
				StringNode ("IdentifiedObject.mRID", objID, out);
				StringNode ("IdentifiedObject.name", obj.tname, out);
				RefNode ("Equipment.EquipmentContainer", fdrID, out);
				RefNode ("PowerSystemResource.GeoLocation", mapLocations.get (objID), out);
				RefNode ("DistributionTransformer.TransformerBank", mapBanks.get (obj.pname), out);
				RefNode ("DistributionTransformer.TransformerInfo", infID, out);
				EndInstance ("DistributionTransformer", out);
				for (int i =0; i < obj.size; i++) {
					eID = ShortenUUID (obj.eid[i]);
					StartInstance ("DistributionTransformerWinding", eID, out);
					StringNode ("IdentifiedObject.mRID", eID, out);
					StringNode ("IdentifiedObject.name", obj.ename[i], out);
					RefNode ("Equipment.EquipmentContainer", fdrID, out);
					RefNode("DistributionTransformerWinding.WindingInfo", 
									mapWindings.get(infID).get(i), out); 
					PhasesEnum (obj.phs[i], out);
					BoolNode ("DistributionTransformerWinding.grounded", obj.grounded[i], out);
					DoubleNode ("DistributionTransformerWinding.rground", obj.rg[i], out);
					DoubleNode ("DistributionTransformerWinding.xground", obj.xg[i], out);
					RefNode ("DistributionTransformerWinding.DistributionTransformer", objID, out);
					RefNode ("PowerSystemResource.GeoLocation", mapLocations.get (objID), out);
					EndInstance ("DistributionTransformerWinding", out);
				}
			}
			for (HashMap.Entry<String,DistXfmrCodeRating> pair : mdl.mapCodeRatings.entrySet()) {
				DistXfmrCodeRating obj = pair.getValue();
				objID = ShortenUUID (obj.id);
				for (int i = 0; i < obj.size; i++) {
					eID = ShortenUUID (obj.eid[i]);
					StartInstance("WindingInfo", eID, out);
					StringNode ("IdentifiedObject.mRID", eID, out);
					StringNode ("IdentifiedObject.name", obj.ename[i], out);
					IntegerNode ("WindingInfo.sequenceNumber", obj.wdg[i], out);
					WindingConnectionEnum (obj.conn[i], out);
					IntegerNode ("WindingInfo.phaseAngle", obj.ang[i], out);
					DoubleNode ("WindingInfo.ratedS", obj.ratedS[i], out);
					DoubleNode ("WindingInfo.ratedU", obj.ratedU[i], out);
					DoubleNode ("WindingInfo.r", obj.r[i], out);
					RefNode ("WindingInfo.TransformerInfo", objID, out);
					EndInstance ("WindingInfo", out);
				}
			}
			for (HashMap.Entry<String,DistPowerXfmrWinding> pair : mdl.mapXfmrWindings.entrySet()) {
				DistPowerXfmrWinding obj = pair.getValue();
				objID = ShortenUUID (obj.id);
				StartInstance ("PowerTransformer", objID, out);
				StringNode ("IdentifiedObject.mRID", objID, out);
				StringNode ("IdentifiedObject.name", obj.name, out);
				RefNode ("Equipment.EquipmentContainer", fdrID, out);
				RefNode ("PowerSystemResource.GeoLocation", mapLocations.get (objID), out);
				EndInstance ("PowerTransformer", out);
				for (int i =0; i < obj.size; i++) {
					eID = ShortenUUID (obj.eid[i]);
					StartInstance ("TransformerWinding", eID, out);
					StringNode ("IdentifiedObject.mRID", eID, out);
					StringNode ("IdentifiedObject.name", obj.ename[i], out);
					RefNode ("Equipment.EquipmentContainer", fdrID, out);
					DoubleNode ("TransformerWinding.ratedS", obj.ratedS[i], out);
					DoubleNode ("TransformerWinding.ratedU", obj.ratedU[i], out);
					XfWindingConnectionEnum (obj.conn[i], out);
					XfWindingTypeEnum (obj.wdg[i], out);
					if (obj.wdg[i] == 1) {
						DistPowerXfmrMesh mesh = mdl.mapXfmrMeshes.get (obj.name);
						DoubleNode ("TransformerWinding.r", mesh.r[0], out);
						DoubleNode ("TransformerWinding.x", mesh.x[0], out);
					}
					BoolNode("TransformerWinding.grounded", obj.grounded[i], out);
					DoubleNode ("TransformerWinding.rground", obj.rg[i], out);
					DoubleNode ("TransformerWinding.xground", obj.xg[i], out);
					RefNode ("TransformerWinding.PowerTransformer", objID, out);
					RefNode ("PowerSystemResource.GeoLocation", mapLocations.get (objID), out);
					EndInstance ("TransformerWinding", out);
				}
			}
		}

		mapLocations.clear();
		setLocations.clear();
		mapWindings.clear();

		out.println ("</rdf:RDF>");
		out.close ();
	}
}

