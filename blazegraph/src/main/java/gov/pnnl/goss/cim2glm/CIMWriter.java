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

	public void WriteCIMFile (CIMImporter mdl, PrintWriter out)  {
		mdl.PrintOneMap (mdl.mapFeeders, "*** FEEDERS ***");
		out.println("{\"feeders\":[");
		for (HashMap.Entry<String,DistFeeder> pair : mdl.mapFeeders.entrySet()) {
			DistFeeder fdr = pair.getValue();
			out.println("{\"name\":\"" + fdr.feederName + "\",");
			out.println("\"mRID\":\"" + fdr.feederID + "\",");
			out.println("\"substation\":\"" + fdr.substationName + "\",");
			out.println("\"substationID\":\"" + fdr.substationID + "\",");
			out.println("\"subregion\":\"" + fdr.subregionName + "\",");
			out.println("\"subregionID\":\"" + fdr.subregionID + "\",");
			out.println("\"region\":\"" + fdr.regionName + "\",");
			out.println("\"regionID\":\"" + fdr.regionID + "\",");
		}
		out.println("]}");
		out.close();
	}
}

