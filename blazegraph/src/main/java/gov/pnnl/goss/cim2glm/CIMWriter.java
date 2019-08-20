package gov.pnnl.goss.cim2glm;
//      		----------------------------------------------------------
//      		Copyright (c) 2017-2019, Battelle Memorial Institute
//      		All rights reserved.
//      		----------------------------------------------------------

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;
import java.util.UUID;

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

public class CIMWriter extends Object {
	private static final String xnsCIM = "http://iec.ch/TC57/2009/CIM-schema-cim14#";
	private static final String xnsRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	private String fdrID;

	private void StartInstance (String root, String id, PrintWriter out) {
		out.println (String.format("<cim:%s rdf:ID=\"%s\">", root, id));
	}

	private void StartFreeInstance (String root, PrintWriter out) {
		String id = "_" + UUID.randomUUID().toString().toUpperCase();
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

	private void RefNode (String key, String val, PrintWriter out) {
		out.println (String.format("  <cim:%s rdf:resource=\"#%s\"/>", key, val, key));
	}

	private void PhasesEnum (String val, PrintWriter out) {
		out.println (String.format("  <cim:ConductingEquipment.phases rdf:resource=\"%sPhaseCode.%s\"/>", xnsCIM, val));
	}

	private void DistributionLineSegment (DistLineSegment obj, PrintWriter out) {
		StartInstance ("DistributionLineSegment", obj.id, out);
		StringNode ("IdentifiedObject.name", obj.name, out);
		DoubleNode ("Conductor.length", obj.len, out);
		RefNode ("Equipment.EquipmentContainer", fdrID, out);
		PhasesEnum (obj.phases, out);
		EndInstance ("DistributionLineSegment", out);  // needs GeoLocation and PSRType
	}

	public void WriteCIMFile (CIMImporter mdl, PrintWriter out)  {
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
			fdrID = fdr.feederID;

			StartInstance ("GeographicalRegion", fdr.regionID, out);
			StringNode ("IdentifiedObject.name", fdr.regionName, out);
			StringNode ("IdentifiedObject.description", "Top-level region", out);
			EndInstance ("GeographicalRegion", out);

			StartInstance ("SubGeographicalRegion", fdr.subregionID, out);
			StringNode ("IdentifiedObject.name", fdr.subregionName, out);
			StringNode ("IdentifiedObject.description", "Lower-level region", out);
			RefNode ("SubGeographicalRegion.Region", fdr.regionID, out);
			EndInstance ("SubGeographicalRegion", out);

			StartInstance ("Substation", fdr.substationID, out);  // needs GeoLocation
			StringNode ("IdentifiedObject.name", fdr.substationName, out);
			EndInstance ("Substation", out);

			StartInstance ("Line", fdrID, out);
			StringNode ("IdentifiedObject.name", fdr.feederName, out);
			StringNode ("IdentifiedObject.description", "Feeder equipment container", out);
			RefNode ("Line.Region", fdr.subregionID, out);
			EndInstance ("Line", out);

			for (HashMap.Entry<String,DistLinesSpacingZ> pair : mdl.mapLinesSpacingZ.entrySet()) {
				DistLinesSpacingZ obj = pair.getValue();
				DistributionLineSegment (obj, out);
			}
			for (HashMap.Entry<String,DistLinesInstanceZ> pair : mdl.mapLinesInstanceZ.entrySet()) {
				DistLinesInstanceZ obj = pair.getValue();
				DistributionLineSegment (obj, out);
			}
			for (HashMap.Entry<String,DistLinesCodeZ> pair : mdl.mapLinesCodeZ.entrySet()) {
				DistLinesCodeZ obj = pair.getValue();
				DistributionLineSegment (obj, out);
			}
		}

		out.println ("</rdf:RDF>");
		out.close ();
	}
}

