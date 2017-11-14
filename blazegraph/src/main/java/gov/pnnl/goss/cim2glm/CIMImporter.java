package gov.pnnl.goss.cim2glm;
//      		----------------------------------------------------------
//      		Copyright (c) 2017, Battelle Memorial Institute
//      		All rights reserved.
//      		----------------------------------------------------------

import java.io.*;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.jena.query.*;

import gov.pnnl.goss.cim2glm.components.DistBaseVoltage;
import gov.pnnl.goss.cim2glm.components.DistCapacitor;
import gov.pnnl.goss.cim2glm.components.DistComponent;
import gov.pnnl.goss.cim2glm.components.DistConcentricNeutralCable;
import gov.pnnl.goss.cim2glm.components.DistCoordinates;
import gov.pnnl.goss.cim2glm.components.DistLineSegment;
import gov.pnnl.goss.cim2glm.components.DistLineSpacing;
import gov.pnnl.goss.cim2glm.components.DistLinesCodeZ;
import gov.pnnl.goss.cim2glm.components.DistLinesInstanceZ;
import gov.pnnl.goss.cim2glm.components.DistLinesSpacingZ;
import gov.pnnl.goss.cim2glm.components.DistLoad;
import gov.pnnl.goss.cim2glm.components.DistOverheadWire;
import gov.pnnl.goss.cim2glm.components.DistPhaseMatrix;
import gov.pnnl.goss.cim2glm.components.DistPowerXfmrCore;
import gov.pnnl.goss.cim2glm.components.DistPowerXfmrMesh;
import gov.pnnl.goss.cim2glm.components.DistPowerXfmrWinding;
import gov.pnnl.goss.cim2glm.components.DistRegulator;
import gov.pnnl.goss.cim2glm.components.DistSequenceMatrix;
import gov.pnnl.goss.cim2glm.components.DistSubstation;
import gov.pnnl.goss.cim2glm.components.DistSwitch;
import gov.pnnl.goss.cim2glm.components.DistTapeShieldCable;
import gov.pnnl.goss.cim2glm.components.DistXfmrBank;
import gov.pnnl.goss.cim2glm.components.DistXfmrCodeOCTest;
import gov.pnnl.goss.cim2glm.components.DistXfmrCodeRating;
import gov.pnnl.goss.cim2glm.components.DistXfmrCodeSCTest;
import gov.pnnl.goss.cim2glm.components.DistXfmrTank;
import gov.pnnl.goss.cim2glm.queryhandler.QueryHandler;
import gov.pnnl.goss.cim2glm.queryhandler.impl.HTTPBlazegraphQueryHandler;

/**
 * <p>This class builds a GridLAB-D or OpenDSS model by running 
 * SQARQL queries against Blazegraph 
 * triple-store</p> 
 *      
 * <p>Invoke as a console-mode program</p> 
 *      
 * @see CIMImporter#main 
 *      
 * @author Tom McDermott 
 * @version 1.0 
 *      
 */

public class CIMImporter extends Object {
	QueryHandler queryHandler;
	
	HashMap<String,GldNode> mapNodes = new HashMap<>();

	HashMap<String,Integer> mapCountMesh = new HashMap<>();
	HashMap<String,Integer> mapCountWinding = new HashMap<>();
	HashMap<String,Integer> mapCountCodeRating = new HashMap<>();
	HashMap<String,Integer> mapCountCodeSCTest = new HashMap<>();
	HashMap<String,Integer> mapCountTank = new HashMap<>();
	HashMap<String,Integer> mapCountBank = new HashMap<>();

	HashMap<String,DistBaseVoltage> mapBaseVoltages = new HashMap<>();
	HashMap<String,DistCapacitor> mapCapacitors = new HashMap<>();
	HashMap<String,DistConcentricNeutralCable> mapCNCables = new HashMap<>();
	HashMap<String,DistCoordinates> mapCoordinates = new HashMap<>();
	HashMap<String,DistLinesCodeZ> mapLinesCodeZ = new HashMap<>();
	HashMap<String,DistLinesInstanceZ> mapLinesInstanceZ = new HashMap<>();
	HashMap<String,DistLineSpacing> mapSpacings = new HashMap<>();
	HashMap<String,DistLinesSpacingZ> mapLinesSpacingZ = new HashMap<>();
	HashMap<String,DistLoad> mapLoads = new HashMap<>();
	HashMap<String,DistOverheadWire> mapWires = new HashMap<>();
	HashMap<String,DistPhaseMatrix> mapPhaseMatrices = new HashMap<>();
	HashMap<String,DistPowerXfmrCore> mapXfmrCores = new HashMap<>();
	HashMap<String,DistPowerXfmrMesh> mapXfmrMeshes = new HashMap<>();
	HashMap<String,DistPowerXfmrWinding> mapXfmrWindings = new HashMap<>();
	HashMap<String,DistRegulator> mapRegulators = new HashMap<>();
	HashMap<String,DistSequenceMatrix> mapSequenceMatrices = new HashMap<>();
	HashMap<String,DistSubstation> mapSubstations = new HashMap<>();
	HashMap<String,DistSwitch> mapSwitches = new HashMap<>();
	HashMap<String,DistTapeShieldCable> mapTSCables = new HashMap<>();
	HashMap<String,DistXfmrCodeOCTest> mapCodeOCTests = new HashMap<>();
	HashMap<String,DistXfmrCodeRating> mapCodeRatings = new HashMap<>();
	HashMap<String,DistXfmrCodeSCTest> mapCodeSCTests = new HashMap<>();
	HashMap<String,DistXfmrTank> mapTanks = new HashMap<>();
	HashMap<String,DistXfmrBank> mapBanks = new HashMap<>();

	void LoadOneCountMap (String szQuery, HashMap<String,Integer> map) {
		ResultSet results = queryHandler.query (szQuery);
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			String key = DistComponent.SafeName (soln.get("?key").toString());
			int count = soln.getLiteral("?count").getInt();
			map.put (key, count);
		}
	}

	void LoadCountMaps() {
		LoadOneCountMap (DistXfmrBank.szCountQUERY, mapCountBank);
		LoadOneCountMap (DistXfmrTank.szCountQUERY, mapCountTank);
		LoadOneCountMap (DistPowerXfmrMesh.szCountQUERY, mapCountMesh);
		LoadOneCountMap (DistPowerXfmrWinding.szCountQUERY, mapCountWinding);
		LoadOneCountMap (DistXfmrCodeRating.szCountQUERY, mapCountCodeRating);
		LoadOneCountMap (DistXfmrCodeSCTest.szCountQUERY, mapCountCodeSCTest);
	}

	void LoadBaseVoltages() {
		ResultSet results = queryHandler.query (DistBaseVoltage.szQUERY);
		while (results.hasNext()) {
			DistBaseVoltage obj = new DistBaseVoltage (results);
			mapBaseVoltages.put (obj.GetKey(), obj);
		}
	}

	void LoadSubstations() {
		ResultSet results = queryHandler.query (DistSubstation.szQUERY);
		while (results.hasNext()) {
			DistSubstation obj = new DistSubstation (results);
			mapSubstations.put (obj.GetKey(), obj);
		}
	}

	void LoadCapacitors() {
		ResultSet results = queryHandler.query (DistCapacitor.szQUERY);
		while (results.hasNext()) {
			DistCapacitor obj = new DistCapacitor (results);
			mapCapacitors.put (obj.GetKey(), obj);
		}
	}

	void LoadLoads() {
		ResultSet results = queryHandler.query (DistLoad.szQUERY);
		while (results.hasNext()) {
			DistLoad obj = new DistLoad (results);
			mapLoads.put (obj.GetKey(), obj);
		}
	}

	void LoadPhaseMatrices() {
		ResultSet results = queryHandler.query (DistPhaseMatrix.szQUERY);
		while (results.hasNext()) {
			DistPhaseMatrix obj = new DistPhaseMatrix (results);
			mapPhaseMatrices.put (obj.GetKey(), obj);
		}
	}

	void LoadSequenceMatrices() {
		ResultSet results = queryHandler.query (DistSequenceMatrix.szQUERY);
		while (results.hasNext()) {
			DistSequenceMatrix obj = new DistSequenceMatrix (results);
			mapSequenceMatrices.put (obj.GetKey(), obj);
		}
	}

	void LoadXfmrCodeRatings() {
		ResultSet results = queryHandler.query (DistXfmrCodeRating.szQUERY);
		while (results.hasNext()) {
			DistXfmrCodeRating obj = new DistXfmrCodeRating (results, mapCountCodeRating);
			mapCodeRatings.put (obj.GetKey(), obj);
		}
	}

	void LoadXfmrCodeOCTests() {
		ResultSet results = queryHandler.query (DistXfmrCodeOCTest.szQUERY);
		while (results.hasNext()) {
			DistXfmrCodeOCTest obj = new DistXfmrCodeOCTest (results);
			mapCodeOCTests.put (obj.GetKey(), obj);
		}
	}

	void LoadXfmrCodeSCTests() {
		ResultSet results = queryHandler.query (DistXfmrCodeSCTest.szQUERY);
		while (results.hasNext()) {
			DistXfmrCodeSCTest obj = new DistXfmrCodeSCTest (results, mapCountCodeSCTest);
			mapCodeSCTests.put (obj.GetKey(), obj);
		}
	}

	void LoadPowerXfmrCore() {
		ResultSet results = queryHandler.query (DistPowerXfmrCore.szQUERY);
		while (results.hasNext()) {
			DistPowerXfmrCore obj = new DistPowerXfmrCore (results);
			mapXfmrCores.put (obj.GetKey(), obj);
		}
	}

	void LoadPowerXfmrMesh() {
		ResultSet results = queryHandler.query (DistPowerXfmrMesh.szQUERY);
		while (results.hasNext()) {
			DistPowerXfmrMesh obj = new DistPowerXfmrMesh (results, mapCountMesh);
			mapXfmrMeshes.put (obj.GetKey(), obj);
		}
	}

	void LoadOverheadWires() {
		ResultSet results = queryHandler.query (DistOverheadWire.szQUERY);
		while (results.hasNext()) {
			DistOverheadWire obj = new DistOverheadWire (results);
			mapWires.put (obj.GetKey(), obj);
		}
	}

	void LoadTapeShieldCables() {
		ResultSet results = queryHandler.query (DistTapeShieldCable.szQUERY);
		while (results.hasNext()) {
			DistTapeShieldCable obj = new DistTapeShieldCable (results);
			mapTSCables.put (obj.GetKey(), obj);
		}
	}

	void LoadConcentricNeutralCables() {
		ResultSet results = queryHandler.query (DistConcentricNeutralCable.szQUERY);
		while (results.hasNext()) {
			DistConcentricNeutralCable obj = new DistConcentricNeutralCable (results);
			mapCNCables.put (obj.GetKey(), obj);
		}
	}

	void LoadLineSpacings() {
		ResultSet results = queryHandler.query (DistLineSpacing.szQUERY);
		while (results.hasNext()) {
			DistLineSpacing obj = new DistLineSpacing (results);
			mapSpacings.put (obj.GetKey(), obj);
		}
	}

	void LoadSwitches() {
		ResultSet results = queryHandler.query (DistSwitch.szQUERY);
		while (results.hasNext()) {
			DistSwitch obj = new DistSwitch (results);
			mapSwitches.put (obj.GetKey(), obj);
		}
	}

	void LoadLinesInstanceZ() {
		ResultSet results = queryHandler.query (DistLinesInstanceZ.szQUERY);
		while (results.hasNext()) {
			DistLinesInstanceZ obj = new DistLinesInstanceZ (results);
			mapLinesInstanceZ.put (obj.GetKey(), obj);
		}
	}

	void LoadLinesCodeZ() {
		ResultSet results = queryHandler.query (DistLinesCodeZ.szQUERY);
		while (results.hasNext()) {
			DistLinesCodeZ obj = new DistLinesCodeZ (results);
			mapLinesCodeZ.put (obj.GetKey(), obj);
		}
	}

	void LoadLinesSpacingZ() {
		ResultSet results = queryHandler.query (DistLinesSpacingZ.szQUERY);
		while (results.hasNext()) {
			DistLinesSpacingZ obj = new DistLinesSpacingZ (results);
			mapLinesSpacingZ.put (obj.GetKey(), obj);
		}
	}

	void LoadRegulators() { 
		ResultSet results = queryHandler.query (DistRegulator.szQUERY);
		while (results.hasNext()) {
			DistRegulator obj = new DistRegulator (results, queryHandler);
			mapRegulators.put (obj.GetKey(), obj);
		}
	}

	void LoadXfmrTanks() {
		ResultSet results = queryHandler.query (DistXfmrTank.szQUERY);
		while (results.hasNext()) {
			DistXfmrTank obj = new DistXfmrTank (results, mapCountTank);
			mapTanks.put (obj.GetKey(), obj);
		}
	}

	void LoadXfmrBanks() {
		ResultSet results = queryHandler.query (DistXfmrBank.szQUERY);
		while (results.hasNext()) {
			DistXfmrBank obj = new DistXfmrBank (results, mapCountBank);
			mapBanks.put (obj.GetKey(), obj);
		}
	}

	void LoadPowerXfmrWindings() {
		ResultSet results = queryHandler.query (DistPowerXfmrWinding.szQUERY);
		while (results.hasNext()) {
			DistPowerXfmrWinding obj = new DistPowerXfmrWinding (results, mapCountWinding);
			mapXfmrWindings.put (obj.GetKey(), obj); 
		}
	}

	void LoadCoordinates() {
		ResultSet results = queryHandler.query (DistCoordinates.szQUERY);
		while (results.hasNext()) {
			DistCoordinates obj = new DistCoordinates (results);
			mapCoordinates.put (obj.GetKey(), obj);
		}
	}

	public void PrintOneMap(HashMap<String,? extends DistComponent> map, String label) {
		System.out.println(label);
		SortedSet<String> keys = new TreeSet<String>(map.keySet());
		for (String key : keys) {
			System.out.println (map.get(key).DisplayString());
		}
	}

	public void PrintAllMaps() {
		PrintOneMap (mapBaseVoltages, "** BASE VOLTAGES");
		PrintOneMap (mapCapacitors, "** CAPACITORS");
		PrintOneMap (mapCNCables, "** CN CABLES");
		PrintOneMap (mapCoordinates, "** COMPONENT XY COORDINATES");
		PrintOneMap (mapLinesCodeZ, "** LINES REFERENCING MATRICES");
		PrintOneMap (mapLinesInstanceZ, "** LINES WITH IMPEDANCE ATTRIBUTES");
		PrintOneMap (mapSpacings, "** LINE SPACINGS");
		PrintOneMap (mapLinesSpacingZ, "** LINES REFERENCING SPACINGS");
		PrintOneMap (mapLoads, "** LOADS");
		PrintOneMap (mapWires, "** OVERHEAD WIRES");
		PrintOneMap (mapPhaseMatrices, "** PHASE IMPEDANCE MATRICES");
		PrintOneMap (mapXfmrCores, "** POWER XFMR CORE ADMITTANCES");
		PrintOneMap (mapXfmrMeshes, "** POWER XFMR MESH IMPEDANCES");
		PrintOneMap (mapXfmrWindings, "** POWER XFMR WINDINGS");
		PrintOneMap (mapRegulators, "** REGULATORS");
		PrintOneMap (mapSequenceMatrices, "** SEQUENCE IMPEDANCE MATRICES");
		PrintOneMap (mapSubstations, "** SUBSTATION SOURCES");
		PrintOneMap (mapSwitches, "** LOADBREAK SWITCHES");
		PrintOneMap (mapTSCables, "** TS CABLES");
		PrintOneMap (mapCodeOCTests, "** XFMR CODE OC TESTS");
		PrintOneMap (mapCodeRatings, "** XFMR CODE WINDING RATINGS");
		PrintOneMap (mapCodeSCTests, "** XFMR CODE SC TESTS");
		PrintOneMap (mapBanks, "** XFMR BANKS");
		PrintOneMap (mapTanks, "** XFMR TANKS");
	}

	public void LoadAllMaps() {
		LoadCountMaps();
		LoadBaseVoltages();
		LoadCapacitors();
		LoadConcentricNeutralCables();
		LoadCoordinates();
		LoadLinesCodeZ();
		LoadLinesInstanceZ();
		LoadLineSpacings();
		LoadLinesSpacingZ();
		LoadLoads();
		LoadOverheadWires();
		LoadPhaseMatrices();
		LoadPowerXfmrCore();
		LoadPowerXfmrMesh();
		LoadPowerXfmrWindings();
		LoadRegulators();
		LoadSequenceMatrices();
		LoadSubstations();
		LoadSwitches();
		LoadTapeShieldCables();
		LoadXfmrCodeOCTests();
		LoadXfmrCodeRatings();
		LoadXfmrCodeSCTests();
		LoadXfmrTanks();
		LoadXfmrBanks();
	}

	public boolean CheckMaps() {
		int nLinks, nNodes;

		if (mapSubstations.size() < 1) {
			throw new RuntimeException ("no substation source");
		}
		nLinks = mapSwitches.size() + mapLinesCodeZ.size() + mapLinesSpacingZ.size() + mapLinesInstanceZ.size() +
			mapXfmrWindings.size() + mapTanks.size(); // standalone regulators not allowed in CIM
		if (nLinks < 1) {
			throw new RuntimeException ("no lines, transformers or switches");
		}
		nNodes = mapLoads.size() + mapCapacitors.size(); // TODO - DER
		if (nLinks < 1) {
			throw new RuntimeException ("no loads or capacitors");
		}
		return true;
	}

	public void WriteJSONSymbolFile (String fXY) throws FileNotFoundException {
		PrintWriter out = new PrintWriter (fXY);
		int count, last;

		out.println("{\"feeder\":[");

		out.println("{\"swing_nodes\":[");
		count = 1;
		last = mapSubstations.size();
		for (HashMap.Entry<String,DistSubstation> pair : mapSubstations.entrySet()) {
			out.print (pair.getValue().GetJSONSymbols(mapCoordinates));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		out.println("]},");

		out.println("{\"capacitors\":[");
		count = 1;
		last = mapCapacitors.size();
		for (HashMap.Entry<String,DistCapacitor> pair : mapCapacitors.entrySet()) {
			out.print (pair.getValue().GetJSONSymbols(mapCoordinates));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		out.println("]},");

		out.println("{\"overhead_lines\":[");
		count = 1;
		last = mapLinesCodeZ.size() + mapLinesInstanceZ.size() + mapLinesSpacingZ.size();
		for (HashMap.Entry<String,DistLinesCodeZ> pair : mapLinesCodeZ.entrySet()) {
			out.print (pair.getValue().GetJSONSymbols(mapCoordinates));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		for (HashMap.Entry<String,DistLinesInstanceZ> pair : mapLinesInstanceZ.entrySet()) {
			out.print (pair.getValue().GetJSONSymbols(mapCoordinates));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		for (HashMap.Entry<String,DistLinesSpacingZ> pair : mapLinesSpacingZ.entrySet()) {
			out.print (pair.getValue().GetJSONSymbols(mapCoordinates));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		out.println("]},");

		out.println("{\"switches\":[");
		count = 1;
		last = mapSwitches.size();
		for (HashMap.Entry<String,DistSwitch> pair : mapSwitches.entrySet()) {
			out.print (pair.getValue().GetJSONSymbols(mapCoordinates));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		out.println("]},");

		out.println("{\"transformers\":[");
		count = 1;
		last =  mapXfmrWindings.size();
		for (HashMap.Entry<String,DistXfmrTank> pair : mapTanks.entrySet()) {
			if (pair.getValue().glmUsed) {
				last += 1;
			}
		}
		for (HashMap.Entry<String,DistPowerXfmrWinding> pair : mapXfmrWindings.entrySet()) {
			out.print (pair.getValue().GetJSONSymbols(mapCoordinates));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		for (HashMap.Entry<String,DistXfmrTank> pair : mapTanks.entrySet()) {
			DistXfmrTank obj = pair.getValue();
			if (obj.glmUsed) {
				out.print(obj.GetJSONSymbols(mapCoordinates));
				if (count++ < last) {
					out.println (",");
				} else {
					out.println ("");
				}
			}
		}
		out.println("]},");

		out.println("{\"regulators\":[");
		count = 1;
		last = mapRegulators.size();
		for (HashMap.Entry<String,DistRegulator> pair : mapRegulators.entrySet()) {
			out.print (pair.getValue().GetJSONSymbols(mapCoordinates, mapTanks));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		out.println("]}");

		out.println("]}");
		out.close();
	}

	public void WriteGLMFile (String fOut, double load_scale, boolean bWantSched, String fSched, 
																	 boolean bWantZIP, double Zcoeff, double Icoeff, double Pcoeff) throws FileNotFoundException {
		PrintWriter out = new PrintWriter (fOut);

		// preparatory steps to build the list of nodes
		ResultSet results = queryHandler.query (
				"SELECT ?name WHERE {"+
				" ?s r:type c:ConnectivityNode."+
				" ?s c:IdentifiedObject.name ?name} ORDER by ?name");
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			String bus = DistComponent.SafeName (soln.get ("?name").toString());
			mapNodes.put (bus, new GldNode(bus));
		}
		for (HashMap.Entry<String,DistSubstation> pair : mapSubstations.entrySet()) {
			DistSubstation obj = pair.getValue();
			GldNode nd = mapNodes.get (obj.bus);
			nd.bSwing = true;
			nd.nomvln = obj.basev / Math.sqrt(3.0);
			nd.phases = "ABC";
		}
		// do the Tanks first, because they assign primary and secondary phasings
		for (HashMap.Entry<String,DistXfmrTank> pair : mapTanks.entrySet()) {
			DistXfmrTank obj = pair.getValue();
			DistXfmrCodeRating code = mapCodeRatings.get (obj.tankinfo);
			code.glmUsed = true;
			boolean bServiceTransformer = false;
			String primaryPhase = "";
			for (int i = 0; i < obj.size; i++) {
				GldNode nd = mapNodes.get(obj.bus[i]);
				nd.nomvln = obj.basev[i] / Math.sqrt(3.0);
				nd.AddPhases (obj.phs[i]);
				if (nd.bSecondary) {
					bServiceTransformer = true;
				} else {
					primaryPhase = obj.phs[i];
				}
			}
			if (bServiceTransformer) {
				for (int i = 0; i < obj.size; i++) {
					GldNode nd = mapNodes.get(obj.bus[i]);
					if (nd.bSecondary) {
						nd.AddPhases (primaryPhase);
						DistCoordinates pt1 = mapCoordinates.get("PowerTransformer:" + obj.pname + ":1");
						DistCoordinates pt2 = mapCoordinates.get("PowerTransformer:" + obj.pname + ":2");
						if (pt1.x == 0.0 && pt1.y == 0.0) {
							if (pt2.x != 0.0 || pt2.y != 0.0) {
								pt1.x = pt2.x + 3.0;
								pt1.y = pt2.y + 0.0;
							}
						} else if (pt2.x == 0.0 && pt2.y == 0.0) {
							if (pt1.x != 0.0 || pt1.y != 0.0) {
								pt2.x = pt1.x + 3.0;
								pt2.y = pt1.y + 0.0;
							}
						}
					}
				}
			}
		}
		for (HashMap.Entry<String,DistLoad> pair : mapLoads.entrySet()) {
			DistLoad obj = pair.getValue();
			GldNode nd = mapNodes.get (obj.bus);
			nd.nomvln = obj.basev / Math.sqrt(3.0);
			nd.AccumulateLoads (obj.phases, obj.p, obj.q, obj.pe, obj.qe, obj.pz, obj.pi, obj.pp, obj.qz, obj.qi, obj.qp);
		}
		for (HashMap.Entry<String,DistCapacitor> pair : mapCapacitors.entrySet()) {
			DistCapacitor obj = pair.getValue();
			GldNode nd = mapNodes.get (obj.bus);
			nd.nomvln = obj.basev / Math.sqrt(3.0);
			nd.AddPhases (obj.phs);
		}
		for (HashMap.Entry<String,DistLinesInstanceZ> pair : mapLinesInstanceZ.entrySet()) {
			DistLinesInstanceZ obj = pair.getValue();
			GldNode nd1 = mapNodes.get (obj.bus1);
			nd1.nomvln = obj.basev / Math.sqrt(3.0);
			nd1.AddPhases (obj.phases);
			GldNode nd2 = mapNodes.get (obj.bus2);
			nd2.nomvln = nd1.nomvln;
			nd2.AddPhases (obj.phases);
		}
		for (HashMap.Entry<String,DistLinesCodeZ> pair : mapLinesCodeZ.entrySet()) {
			DistLinesCodeZ obj = pair.getValue();
			DistPhaseMatrix zmat = mapPhaseMatrices.get (obj.lname);
			if (zmat != null) {
				zmat.MarkGLMPermutationsUsed(obj.phases);
			} else {
				DistSequenceMatrix zseq = mapSequenceMatrices.get (obj.lname);
				if (zseq != null) {
//					System.out.println ("Sequence Z " + zseq.name + " using " + obj.phases + " for " + obj.name);
				}
			}
			GldNode nd1 = mapNodes.get (obj.bus1);
			nd1.nomvln = obj.basev / Math.sqrt(3.0);
			GldNode nd2 = mapNodes.get (obj.bus2);
			nd2.nomvln = nd1.nomvln;
			if (obj.phases.contains("s")) {  // add primary phase to this triplex
				nd1.bSecondary = true;
				nd2.bSecondary = true;
				if (nd2.phases.length() > 0) {
					nd1.AddPhases (nd2.phases);
					obj.phases = obj.phases + ":" + nd2.phases;
				} else if (nd1.phases.length() > 0) {
					nd2.AddPhases (nd1.phases);
					obj.phases = obj.phases + ":" + nd1.phases;
				}
				DistCoordinates pt1 = mapCoordinates.get("ACLineSegment:" + obj.name + ":1");
				DistCoordinates pt2 = mapCoordinates.get("ACLineSegment:" + obj.name + ":2");
				if (pt1.x == 0.0 && pt1.y == 0.0) {
					if (pt2.x != 0.0 || pt2.y != 0.0) {
						pt1.x = pt2.x + 3.0;
						pt1.y = pt2.y + 0.0;
					}
				} else if (pt2.x == 0.0 && pt2.y == 0.0) {
					if (pt1.x != 0.0 || pt1.y != 0.0) {
						pt2.x = pt1.x + 3.0;
						pt2.y = pt1.y + 0.0;
					}
				}
			} else {
				nd1.AddPhases (obj.phases);
				nd2.AddPhases (obj.phases);
			}
		}
		for (HashMap.Entry<String,DistLinesSpacingZ> pair : mapLinesSpacingZ.entrySet()) {
			DistLinesSpacingZ obj = pair.getValue();
			GldNode nd1 = mapNodes.get (obj.bus1);
			nd1.nomvln = obj.basev / Math.sqrt(3.0);
			nd1.AddPhases (obj.phases);
			GldNode nd2 = mapNodes.get (obj.bus2);
			nd2.nomvln = nd1.nomvln;
			nd2.AddPhases (obj.phases);
		}
		for (HashMap.Entry<String,DistSwitch> pair : mapSwitches.entrySet()) {
			DistSwitch obj = pair.getValue();
			GldNode nd1 = mapNodes.get (obj.bus1);
			nd1.nomvln = obj.basev / Math.sqrt(3.0);
			nd1.AddPhases (obj.phases);
			GldNode nd2 = mapNodes.get (obj.bus2);
			nd2.nomvln = nd1.nomvln;
			nd2.AddPhases (obj.phases);
		}
		for (HashMap.Entry<String,DistPowerXfmrWinding> pair : mapXfmrWindings.entrySet()) {
			DistPowerXfmrWinding obj = pair.getValue();
			for (int i = 0; i < obj.size; i++) {
				GldNode nd = mapNodes.get(obj.bus[i]);
				nd.nomvln = obj.basev[i] / Math.sqrt(3.0);
				nd.AddPhases ("ABC");
			}
		}
		for (HashMap.Entry<String,DistRegulator> pair : mapRegulators.entrySet()) {
			DistRegulator reg = pair.getValue();
			DistXfmrTank tank = mapTanks.get (reg.tname[0]); // TODO: revisit if GridLAB-D can model un-banked regulator tanks
			DistXfmrCodeRating code = mapCodeRatings.get (tank.tankinfo);
			out.print (reg.GetGLM (tank));
			code.glmUsed = false;
			tank.glmUsed = false;
			for (int i = 1; i < reg.size; i++) {
				tank = mapTanks.get (reg.tname[i]);
				code = mapCodeRatings.get (tank.tankinfo);
				code.glmUsed = false;
				tank.glmUsed = false;
			}
		}

		// GLM configurations
		for (HashMap.Entry<String,DistPhaseMatrix> pair : mapPhaseMatrices.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,DistSequenceMatrix> pair : mapSequenceMatrices.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,DistXfmrCodeRating> pair : mapCodeRatings.entrySet()) {
			DistXfmrCodeRating code = pair.getValue();
			if (code.glmUsed) {
				DistXfmrCodeSCTest sct = mapCodeSCTests.get (code.tname);
				DistXfmrCodeOCTest oct = mapCodeOCTests.get (code.tname);
				out.print (code.GetGLM(sct, oct));
			}
		}

		// GLM circuit components
		for (HashMap.Entry<String,DistCapacitor> pair : mapCapacitors.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,DistLinesSpacingZ> pair : mapLinesSpacingZ.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,DistLinesCodeZ> pair : mapLinesCodeZ.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,DistLinesInstanceZ> pair : mapLinesInstanceZ.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,DistSwitch> pair : mapSwitches.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,DistPowerXfmrWinding> pair : mapXfmrWindings.entrySet()) {
			DistPowerXfmrWinding obj = pair.getValue();
			DistPowerXfmrMesh mesh = mapXfmrMeshes.get (obj.name);
			DistPowerXfmrCore core = mapXfmrCores.get (obj.name);
			out.print (pair.getValue().GetGLM(mesh, core));
		}
		for (HashMap.Entry<String,DistXfmrTank> pair : mapTanks.entrySet()) {
			DistXfmrTank obj = pair.getValue();
			if (obj.glmUsed) {
				out.print (obj.GetGLM());
			}
		}

		// GLM nodes and loads
		for (HashMap.Entry<String,GldNode> pair : mapNodes.entrySet()) {
			out.print (pair.getValue().GetGLM (load_scale, bWantSched, fSched, bWantZIP, Zcoeff, Icoeff, Pcoeff));
		}

		out.close();
	}
	
	// helper class to keep track of the conductor counts for WireSpacingInfo instances
	static class DSSSegmentXY {
		public String bus1;
		public String bus2;
		public double x1;
		public double y1;
		public double x2;
		public double y2;

		public DSSSegmentXY(String bus1, double x1, double y1, String bus2, double x2, double y2) {
			this.bus1 = bus1;
			this.bus2 = bus2;
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
	}

	public void WriteDSSCoordinates (String fXY) throws FileNotFoundException {
		PrintWriter out = new PrintWriter (fXY);
		String bus;
		DistCoordinates pt1, pt2;
		HashMap<String,Double[]> mapBusXY = new HashMap<String,Double[]>();
		HashSet<DSSSegmentXY> setSegXY = new HashSet<DSSSegmentXY>();

		// loads, capacitors, transformers and energy sources have a single bus location, assumed to be correct
		for (HashMap.Entry<String,DistCoordinates> pair : mapCoordinates.entrySet()) {
			DistCoordinates obj = pair.getValue();
			if ((obj.x != 0) || (obj.y != 0)) {
				if (obj.cname.equals("EnergyConsumer")) {
					bus = mapLoads.get(obj.name).bus;
					mapBusXY.put(bus, new Double[] {obj.x, obj.y});
				} else if (obj.cname.equals("LinearShuntCompensator")) {
					bus = mapCapacitors.get(obj.name).bus;
					mapBusXY.put(bus, new Double[] {obj.x, obj.y});
				} else if (obj.cname.equals("EnergySource")) {
					bus = mapSubstations.get(obj.name).bus;
					mapBusXY.put(bus, new Double[] {obj.x, obj.y});
				} else if (obj.cname.equals("PowerTransformer")) {
					DistXfmrTank tnk = mapTanks.get(obj.name);
					if (tnk != null) {
						for (int i = 0; i < tnk.size; i++) {
							mapBusXY.put(tnk.bus[i], new Double[] { obj.x, obj.y });
						}
					} else {
						DistPowerXfmrWinding wdg = mapXfmrWindings.get(obj.name);
						if (wdg != null) {
							mapBusXY.put(wdg.bus[obj.seq - 1], new Double[] { obj.x, obj.y });
						}
					}
				}
			}
		}

		// switches and lines have two unordered bus locations; 
		// below, bus1 and bus2 could be in the wrong order w.r.t [x1,y1] and [x2,y2]
		for (HashMap.Entry<String,DistLinesCodeZ> pair : mapLinesCodeZ.entrySet()) {
			DistLineSegment obj = pair.getValue();
			pt1 = mapCoordinates.get("ACLineSegment:" + obj.name + ":1");
			pt2 = mapCoordinates.get("ACLineSegment:" + obj.name + ":2");
			setSegXY.add(new DSSSegmentXY (obj.bus1, pt1.x, pt1.y, obj.bus2, pt2.x, pt2.y));
		}
		for (HashMap.Entry<String,DistLinesInstanceZ> pair : mapLinesInstanceZ.entrySet()) {
			DistLineSegment obj = pair.getValue();
			pt1 = mapCoordinates.get("ACLineSegment:" + obj.name + ":1");
			pt2 = mapCoordinates.get("ACLineSegment:" + obj.name + ":2");
			setSegXY.add(new DSSSegmentXY (obj.bus1, pt1.x, pt1.y, obj.bus2, pt2.x, pt2.y));
		}
		for (HashMap.Entry<String,DistLinesSpacingZ> pair : mapLinesSpacingZ.entrySet()) {
			DistLineSegment obj = pair.getValue();
			pt1 = mapCoordinates.get("ACLineSegment:" + obj.name + ":1");
			pt2 = mapCoordinates.get("ACLineSegment:" + obj.name + ":2");
			setSegXY.add(new DSSSegmentXY (obj.bus1, pt1.x, pt1.y, obj.bus2, pt2.x, pt2.y));
		}
		for (HashMap.Entry<String,DistSwitch> pair : mapSwitches.entrySet()) {
			DistSwitch obj = pair.getValue();
			pt1 = mapCoordinates.get("LoadBreakSwitch:" + obj.name + ":1");
			pt2 = mapCoordinates.get("LoadBreakSwitch:" + obj.name + ":2");
			setSegXY.add(new DSSSegmentXY (obj.bus1, pt1.x, pt1.y, obj.bus2, pt2.x, pt2.y));
		}

		// Now sweep through the mapSegXY and shift new, known-good coordinates onto the mapBusXY list
		// A coordinate is known-good if it's already in the list, but we don't move it;
		//   instead, we move the paired coordinate onto mapSegXY, only if the paired coordinate isn't there
		// Each time we visit a mapSegXY with one or two known-good coordinates, we remove it from the
		//   map, whether or not it was added to mapBusXY
		// Stop when mapSegXY is empty, or no further shifts can be made

//		out.println("// bus locations - before");
//		for (HashMap.Entry<String,Double[]> pair : mapBusXY.entrySet()) {
//			Double[] xy = pair.getValue();
//			bus = pair.getKey();
//			out.println(bus + "," + Double.toString(xy[0]) + "," + Double.toString(xy[1]));
//		}

//		out.println("// mapSegXY contents - before");
//		for (DSSSegmentXY seg : setSegXY) {
//			out.println("// " + seg.bus1 + "," + Double.toString(seg.x1) + "," + Double.toString(seg.y1)
//									 + "," + seg.bus2 + "," + Double.toString(seg.x2) + "," + Double.toString(seg.y2));
//		}

		boolean found = true;
		Double eps = 1.0e-6;
		while (found) {
			found = false;
			Iterator<DSSSegmentXY> it = setSegXY.iterator();
			while (it.hasNext()) {
				DSSSegmentXY seg = it.next();
				Double [] loc1 = mapBusXY.get(seg.bus1);
				Double [] loc2 = mapBusXY.get(seg.bus2);
				if (loc1 != null) {
					found = true;
					if (loc2 == null) {
						if ((Math.abs(seg.x1 - loc1[0])) < eps && (Math.abs(seg.y1 - loc1[1]) < eps)) {
							mapBusXY.put(seg.bus2, new Double[] {seg.x2, seg.y2});
						} else {
							mapBusXY.put(seg.bus2, new Double[] {seg.x1, seg.y1});
						}
					}
				}
				if (loc2 != null) {
					found = true;
					if (loc1 == null) {
						if ((Math.abs(seg.x1 - loc2[0])) < eps && (Math.abs(seg.y1 - loc2[1]) < eps)) {
							mapBusXY.put(seg.bus1, new Double[] {seg.x2, seg.y2});
						} else {
							mapBusXY.put(seg.bus1, new Double[] {seg.x1, seg.y1});
						}
					}
				}
				if (found) {
					it.remove();
				}
			}		
		}

		out.println("// mapSegXY contents - after");
		for (DSSSegmentXY seg : setSegXY) {
			out.println("// " + seg.bus1 + "," + Double.toString(seg.x1) + "," + Double.toString(seg.y1)
									 + "," + seg.bus2 + "," + Double.toString(seg.x2) + "," + Double.toString(seg.y2));
		}

		// The bus locations in mapBusXY should now be unique, and topologically consistent, so write them.
		out.println("// bus locations - after");
		for (HashMap.Entry<String,Double[]> pair : mapBusXY.entrySet()) {
			Double[] xy = pair.getValue();
			bus = pair.getKey();
			out.println(bus + "," + Double.toString(xy[0]) + "," + Double.toString(xy[1]));
		}

		out.close();
	}

	public void WriteDSSFile (String fOut, String fXY, String fID, double load_scale, boolean bWantZIP, 
														double Zcoeff, double Icoeff, double Pcoeff) throws FileNotFoundException {
		PrintWriter out = new PrintWriter (fOut);
		PrintWriter outID = new PrintWriter (fID);

		for (HashMap.Entry<String,DistSubstation> pair : mapSubstations.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Circuit." + pair.getValue().name + "\t" + pair.getValue().id);
		}

		out.println();
		for (HashMap.Entry<String,DistOverheadWire> pair : mapWires.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Wiredata." + pair.getValue().name + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistConcentricNeutralCable> pair : mapCNCables.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("CNData." + pair.getValue().name + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistTapeShieldCable> pair : mapTSCables.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("TSData." + pair.getValue().name + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistLineSpacing> pair : mapSpacings.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("LineSpacing." + pair.getValue().name + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistPhaseMatrix> pair : mapPhaseMatrices.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Linecode." + pair.getValue().name + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistSequenceMatrix> pair : mapSequenceMatrices.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Linecode." + pair.getValue().name + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistXfmrCodeRating> pair : mapCodeRatings.entrySet()) {
			DistXfmrCodeRating obj = pair.getValue();
			DistXfmrCodeSCTest sct = mapCodeSCTests.get (obj.tname);
			DistXfmrCodeOCTest oct = mapCodeOCTests.get (obj.tname);
			out.print (obj.GetDSS(sct, oct));
			outID.println ("Xfmrcode." + obj.tname + "\t" + obj.id);
		}

		out.println();
		for (HashMap.Entry<String,DistLoad> pair : mapLoads.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Load." + pair.getValue().name + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistSwitch> pair : mapSwitches.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistLinesCodeZ> pair : mapLinesCodeZ.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistLinesSpacingZ> pair : mapLinesSpacingZ.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistLinesInstanceZ> pair : mapLinesInstanceZ.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistPowerXfmrWinding> pair : mapXfmrWindings.entrySet()) {
			DistPowerXfmrWinding obj = pair.getValue();
			DistPowerXfmrMesh mesh = mapXfmrMeshes.get (obj.name);
			DistPowerXfmrCore core = mapXfmrCores.get (obj.name);
			out.print (obj.GetDSS(mesh, core));
			outID.println ("Transformer." + pair.getValue().name + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistXfmrTank> pair : mapTanks.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Transformer." + pair.getValue().tname + "\t" + pair.getValue().id);
		}
		out.println();
		for (HashMap.Entry<String,DistRegulator> pair : mapRegulators.entrySet()) {
			DistRegulator obj = pair.getValue();
			out.print(obj.GetDSS());
			for (int i = 0; i < obj.size; i++) {
				outID.println("RegControl." + obj.rname[i] + "\t" + obj.id[i]);
			}
		}
		out.println(); // capacitors last in case the capcontrols reference a preceeding element
		for (HashMap.Entry<String,DistCapacitor> pair : mapCapacitors.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Capacitor." + pair.getValue().name + "\t" + pair.getValue().id);
		}

		out.println();
		out.print ("set voltagebases=[");
		for (HashMap.Entry<String,DistBaseVoltage> pair : mapBaseVoltages.entrySet()) {
			out.print (pair.getValue().GetDSS());
		}
		out.println ("]");

		out.println();
		out.println ("calcv");
		out.println ("buscoords " + fXY);
		out.println ("guids " + fID);
		out.println ("// solve");

		out.println();
		out.println ("// export summary");
		out.println ("// show voltages ln");

		out.close();
		outID.close();
	}
	
	
	/**
	 * 
	 * @param fOut
	 * @param fSched
	 * @param load_scale
	 * @param bWantSched
	 * @param bWantZIP
	 * @param Zcoeff
	 * @param Icoeff
	 * @param Pcoeff
	 * @param fXY
	 * @throws FileNotFoundException
	 */
	public void start(QueryHandler queryHandler, String fTarget, String fRoot, String fSched, 
										double load_scale, boolean bWantSched, boolean bWantZIP, double Zcoeff, 
										double Icoeff, double Pcoeff) throws FileNotFoundException{
		this.queryHandler = queryHandler;
		String fOut, fXY, fID;

		LoadAllMaps();
		CheckMaps();

//		PrintAllMaps();
//		PrintOneMap (mapBanks, "** BANKS");
		if (fTarget.equals("glm")) {
			fOut = fRoot + "_base.glm";
			fXY = fRoot + "_symbols.json";
			WriteGLMFile(fOut, load_scale, bWantSched, fSched, bWantZIP, Zcoeff, Icoeff, Pcoeff);
			WriteJSONSymbolFile (fXY);
		} else if (fTarget.equals("dss")) {
			fOut = fRoot + "_base.dss";
			fXY = fRoot + "_busxy.dss";
			fID = fRoot + "_guid.dss";
			WriteDSSFile (fOut, fXY, fID, load_scale, bWantZIP, Zcoeff, Icoeff, Pcoeff);
			WriteDSSCoordinates (fXY);
		}
	}

	public static void main (String args[]) throws FileNotFoundException {
		String fRoot = "";
		double freq = 60.0, load_scale = 1.0;
		boolean bWantSched = false, bWantZIP = false;
		String fSched = "";
		String fTarget = "glm";
		double Zcoeff = 0.0, Icoeff = 0.0, Pcoeff = 0.0;
		String blazegraphURI = "http://localhost:9999/blazegraph/namespace/kb/sparql";
		if (args.length < 1) {
			System.out.println ("Usage: java CIMImporter [options] output_root");
			System.out.println ("       -o={glm|dss}       // output format; defaults to glm");
			System.out.println ("       -l={0..1}          // load scaling factor; defaults to 1");
			System.out.println ("       -f={50|60}         // system frequency; defaults to 60");
			System.out.println ("       -n={schedule_name} // root filename for scheduled ZIP loads (defaults to none), valid only for -o=glm");
			System.out.println ("       -z={0..1}          // constant Z portion (defaults to 0 for CIM-defined LoadResponseCharacteristic)");
			System.out.println ("       -i={0..1}          // constant I portion (defaults to 0 for CIM-defined LoadResponseCharacteristic)");
			System.out.println ("       -p={0..1}          // constant P portion (defaults to 0 for CIM-defined LoadResponseCharacteristic)");
			System.out.println ("       -u={http://localhost:9999} // blazegraph uri (if connecting over HTTP); defaults to http://localhost:9999");

			System.out.println ("Example 1: java CIMImporter -l=1 -i=1 -n=zipload_schedule ieee8500");
			System.out.println ("   assuming Jena and Commons-Math are in Java's classpath, this will produce two output files");
			System.out.println ("   1) ieee8500_base.glm with GridLAB-D components for a constant-current model at peak load,");
			System.out.println ("      where each load's base_power attributes reference zipload_schedule.player");
			System.out.println ("      This file includes an adjustable source voltage, and manual capacitor/tap changer states.");
			System.out.println ("      It should be invoked from a separate GridLAB-D file that sets up the clock, solver, recorders, etc.");
			System.out.println ("      For example, these two GridLAB-D input lines set up 1.05 per-unit source voltage on a 115-kV system:");
			System.out.println ("          #define VSOURCE=69715.065 // 66395.3 * 1.05");
			System.out.println ("          #include \"ieee8500_base.glm\"");
			System.out.println ("      If there were capacitor/tap changer controls in the CIM input file, that data was written to");
			System.out.println ("          ieee8500_base.glm as comments, which can be recovered through manual edits.");
			System.out.println ("   2) ieee8500_symbols.json with component labels and geographic coordinates, used in GridAPPS-D but not GridLAB-D");
			System.out.println ("Example 2: java CIMImporter -o=dss ieee8500");
			System.out.println ("   assuming Jena and Commons-Math are in Java's classpath, this will produce three output files");
			System.out.println ("   1) ieee8500_base.dss with OpenDSS components for the CIM LoadResponseCharacteristic at peak load");
			System.out.println ("      It should be invoked from a separate OpenDSS file that sets up the solution and options.");
			System.out.println ("   2) ieee8500_busxy.dss with node xy coordinates");
			System.out.println ("   3) ieee8500_guid.dss with CIM mRID values for the components");
			System.out.println ("TODO: implement argument for CIM EquipmentContainer selection");
			System.exit (0);
		}

		int i = 0;
		while (i < args.length) {
			if (args[i].charAt(0) == '-') {
				char opt = args[i].charAt(1);
				String optVal = args[i].substring(3);
				if (opt == 'l') {
					load_scale = Double.parseDouble(optVal);
				} else if (opt == 'o') {
					fTarget = optVal;
				} else if (opt == 'f') {
					freq = Double.parseDouble(optVal);  // TODO: set this into DistComponent
				} else if (opt == 'n') {
					fSched = optVal;
					bWantSched = true;
				} else if (opt == 'z') {
					Zcoeff = Double.parseDouble(optVal);
					bWantZIP = true;
				} else if (opt == 'i') {
					Icoeff = Double.parseDouble(optVal);
					bWantZIP = true;
				} else if (opt == 'p') {
					Pcoeff = Double.parseDouble(optVal);
					bWantZIP = true;
				}else if (opt == 'u') {
					blazegraphURI = optVal;
				}
			} else {
				if (fTarget.equals("glm")) {
					fRoot = args[i];
				} else if (fTarget.equals("dss")) {
					fRoot = args[i];
				} else {
					System.out.println ("Unknown target type " + fTarget);
					System.exit(0);
				}
			}
			++i;
		}
		
		try {
			new CIMImporter().start(new HTTPBlazegraphQueryHandler(blazegraphURI), fTarget, fRoot, fSched, load_scale,
															bWantSched, bWantZIP, Zcoeff, Icoeff, Pcoeff);
		} catch (RuntimeException e) {
			System.out.println ("Can not produce a model: " + e.getMessage());
			e.printStackTrace();
		}
		
	}
}

