package gov.pnnl.goss.cim2glm;
//      		----------------------------------------------------------
//      		Copyright (c) 2017, Battelle Memorial Institute
//      		All rights reserved.
//      		----------------------------------------------------------

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;

import org.apache.jena.query.*;

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
	HashMap<String,GldLineConfig> mapLineConfigs = new HashMap<>();

	HashMap<String,Integer> mapCountMesh = new HashMap<>();
	HashMap<String,Integer> mapCountWinding = new HashMap<>();
	HashMap<String,Integer> mapCountCodeRating = new HashMap<>();
	HashMap<String,Integer> mapCountCodeSCTest = new HashMap<>();
	HashMap<String,Integer> mapCountTank = new HashMap<>();
	HashMap<String,Integer> mapCountBank = new HashMap<>();

	HashMap<String,DistBaseVoltage> mapBaseVoltages = new HashMap<>();
	HashMap<String,DistBreaker> mapBreakers = new HashMap<>();
	HashMap<String,DistCapacitor> mapCapacitors = new HashMap<>();
	HashMap<String,DistConcentricNeutralCable> mapCNCables = new HashMap<>();
	HashMap<String,DistCoordinates> mapCoordinates = new HashMap<>();
	HashMap<String,DistDisconnector> mapDisconnectors = new HashMap<>();
	HashMap<String,DistFeeder> mapFeeders = new HashMap<>();
	HashMap<String,DistFuse> mapFuses = new HashMap<>();
	HashMap<String,DistGroundDisconnector> mapGroundDisconnectors = new HashMap<>();
	HashMap<String,DistJumper> mapJumpers = new HashMap<>();
	HashMap<String,DistLinesCodeZ> mapLinesCodeZ = new HashMap<>();
	HashMap<String,DistLinesInstanceZ> mapLinesInstanceZ = new HashMap<>();
	HashMap<String,DistLineSpacing> mapSpacings = new HashMap<>();
	HashMap<String,DistLinesSpacingZ> mapLinesSpacingZ = new HashMap<>();
	HashMap<String,DistLoad> mapLoads = new HashMap<>();
	HashMap<String,DistLoadBreakSwitch> mapLoadBreakSwitches = new HashMap<>();
	HashMap<String,DistOverheadWire> mapWires = new HashMap<>();
	HashMap<String,DistPhaseMatrix> mapPhaseMatrices = new HashMap<>();
	HashMap<String,DistPowerXfmrCore> mapXfmrCores = new HashMap<>();
	HashMap<String,DistPowerXfmrMesh> mapXfmrMeshes = new HashMap<>();
	HashMap<String,DistPowerXfmrWinding> mapXfmrWindings = new HashMap<>();
	HashMap<String,DistRecloser> mapReclosers = new HashMap<>();
	HashMap<String,DistRegulator> mapRegulators = new HashMap<>();
	HashMap<String,DistSectionaliser> mapSectionalisers = new HashMap<>();
	HashMap<String,DistSequenceMatrix> mapSequenceMatrices = new HashMap<>();
	HashMap<String,DistSolar> mapSolars = new HashMap<>();
	HashMap<String,DistStorage> mapStorages = new HashMap<>();
	HashMap<String,DistSubstation> mapSubstations = new HashMap<>();
	HashMap<String,DistSyncMachine> mapSyncMachines = new HashMap<>();
	HashMap<String,DistTapeShieldCable> mapTSCables = new HashMap<>();
	HashMap<String,DistXfmrCodeOCTest> mapCodeOCTests = new HashMap<>();
	HashMap<String,DistXfmrCodeRating> mapCodeRatings = new HashMap<>();
	HashMap<String,DistXfmrCodeSCTest> mapCodeSCTests = new HashMap<>();
	HashMap<String,DistXfmrTank> mapTanks = new HashMap<>();
	HashMap<String,DistXfmrBank> mapBanks = new HashMap<>();
	HashMap<String,DistMeasurement> mapMeasurements = new HashMap<>();
	HashMap<String,DistHouse> mapHouses = new HashMap<>();
	
	boolean allMapsLoaded = false;

	void LoadOneCountMap (String szQuery, HashMap<String,Integer> map) {
		ResultSet results = queryHandler.query (szQuery);
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			String key = DistComponent.SafeName (soln.get("?key").toString());
			int count = soln.getLiteral("?count").getInt();
			map.put (key, count);
		}
		((ResultSetCloseable)results).close();
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
		((ResultSetCloseable)results).close();
	}

	void LoadSubstations() {
		ResultSet results = queryHandler.query (DistSubstation.szQUERY);
		while (results.hasNext()) {
			DistSubstation obj = new DistSubstation (results);
			mapSubstations.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadSolars() {
		ResultSet results = queryHandler.query (DistSolar.szQUERY);
		while (results.hasNext()) {
			DistSolar obj = new DistSolar (results);
			mapSolars.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadMeasurements() {
		ResultSet results = queryHandler.query (DistMeasurement.szQUERY);
		while (results.hasNext()) {
			DistMeasurement obj = new DistMeasurement (results);
			mapMeasurements.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadStorages() {
		ResultSet results = queryHandler.query (DistStorage.szQUERY);
		while (results.hasNext()) {
			DistStorage obj = new DistStorage (results);
			mapStorages.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadCapacitors() {
		ResultSet results = queryHandler.query (DistCapacitor.szQUERY);
		while (results.hasNext()) {
			DistCapacitor obj = new DistCapacitor (results);
			mapCapacitors.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadLoads() {
		ResultSet results = queryHandler.query (DistLoad.szQUERY);
		while (results.hasNext()) {
			DistLoad obj = new DistLoad (results);
			mapLoads.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadPhaseMatrices() {
		ResultSet results = queryHandler.query (DistPhaseMatrix.szQUERY);
		while (results.hasNext()) {
			DistPhaseMatrix obj = new DistPhaseMatrix (results);
			mapPhaseMatrices.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadSequenceMatrices() {
		ResultSet results = queryHandler.query (DistSequenceMatrix.szQUERY);
		while (results.hasNext()) {
			DistSequenceMatrix obj = new DistSequenceMatrix (results);
			mapSequenceMatrices.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadXfmrCodeRatings() {
		ResultSet results = queryHandler.query (DistXfmrCodeRating.szQUERY);
		while (results.hasNext()) {
			DistXfmrCodeRating obj = new DistXfmrCodeRating (results, mapCountCodeRating);
			mapCodeRatings.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadXfmrCodeOCTests() {
		ResultSet results = queryHandler.query (DistXfmrCodeOCTest.szQUERY);
		while (results.hasNext()) {
			DistXfmrCodeOCTest obj = new DistXfmrCodeOCTest (results);
			mapCodeOCTests.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadXfmrCodeSCTests() {
		ResultSet results = queryHandler.query (DistXfmrCodeSCTest.szQUERY);
		while (results.hasNext()) {
			DistXfmrCodeSCTest obj = new DistXfmrCodeSCTest (results, mapCountCodeSCTest);
			mapCodeSCTests.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadPowerXfmrCore() {
		ResultSet results = queryHandler.query (DistPowerXfmrCore.szQUERY);
		while (results.hasNext()) {
			DistPowerXfmrCore obj = new DistPowerXfmrCore (results);
			mapXfmrCores.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadPowerXfmrMesh() {
		ResultSet results = queryHandler.query (DistPowerXfmrMesh.szQUERY);
		while (results.hasNext()) {
			DistPowerXfmrMesh obj = new DistPowerXfmrMesh (results, mapCountMesh);
			mapXfmrMeshes.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadOverheadWires() {
		ResultSet results = queryHandler.query (DistOverheadWire.szQUERY);
		while (results.hasNext()) {
			DistOverheadWire obj = new DistOverheadWire (results);
			mapWires.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadTapeShieldCables() {
		ResultSet results = queryHandler.query (DistTapeShieldCable.szQUERY);
		while (results.hasNext()) {
			DistTapeShieldCable obj = new DistTapeShieldCable (results);
			mapTSCables.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadConcentricNeutralCables() {
		ResultSet results = queryHandler.query (DistConcentricNeutralCable.szQUERY);
		while (results.hasNext()) {
			DistConcentricNeutralCable obj = new DistConcentricNeutralCable (results);
			mapCNCables.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadLineSpacings() {
		ResultSet results = queryHandler.query (DistLineSpacing.szQUERY);
		while (results.hasNext()) {
			DistLineSpacing obj = new DistLineSpacing (results);
			mapSpacings.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadLoadBreakSwitches() {
		ResultSet results = queryHandler.query (DistLoadBreakSwitch.szQUERY);
		while (results.hasNext()) {
			DistLoadBreakSwitch obj = new DistLoadBreakSwitch (results);
			mapLoadBreakSwitches.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadFuses() {
		ResultSet results = queryHandler.query (DistFuse.szQUERY);
		while (results.hasNext()) {
			DistFuse obj = new DistFuse (results);
			mapFuses.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadDisconnectors() {
		ResultSet results = queryHandler.query (DistDisconnector.szQUERY);
		while (results.hasNext()) {
			DistDisconnector obj = new DistDisconnector (results);
			mapDisconnectors.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadGroundDisconnectors() {
		ResultSet results = queryHandler.query (DistGroundDisconnector.szQUERY);
		while (results.hasNext()) {
			DistGroundDisconnector obj = new DistGroundDisconnector (results);
			mapGroundDisconnectors.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadJumpers() {
		ResultSet results = queryHandler.query (DistJumper.szQUERY);
		while (results.hasNext()) {
			DistJumper obj = new DistJumper (results);
			mapJumpers.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadBreakers() {
		ResultSet results = queryHandler.query (DistBreaker.szQUERY);
		while (results.hasNext()) {
			DistBreaker obj = new DistBreaker (results);
			mapBreakers.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadReclosers() {
		ResultSet results = queryHandler.query (DistRecloser.szQUERY);
		while (results.hasNext()) {
			DistRecloser obj = new DistRecloser (results);
			mapReclosers.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadSectionalisers() {
		ResultSet results = queryHandler.query (DistSectionaliser.szQUERY);
		while (results.hasNext()) {
			DistSectionaliser obj = new DistSectionaliser (results);
			mapSectionalisers.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadLinesInstanceZ() {
		ResultSet results = queryHandler.query (DistLinesInstanceZ.szQUERY);
		while (results.hasNext()) {
			DistLinesInstanceZ obj = new DistLinesInstanceZ (results);
			mapLinesInstanceZ.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadLinesCodeZ() {
		ResultSet results = queryHandler.query (DistLinesCodeZ.szQUERY);
		while (results.hasNext()) {
			DistLinesCodeZ obj = new DistLinesCodeZ (results);
			mapLinesCodeZ.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadLinesSpacingZ() {
		ResultSet results = queryHandler.query (DistLinesSpacingZ.szQUERY);
		while (results.hasNext()) {
			DistLinesSpacingZ obj = new DistLinesSpacingZ (results);
			mapLinesSpacingZ.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadRegulators() { 
		ResultSet results = queryHandler.query (DistRegulator.szQUERY);
		while (results.hasNext()) {
			DistRegulator obj = new DistRegulator (results, queryHandler);
			mapRegulators.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadXfmrTanks() {
		ResultSet results = queryHandler.query (DistXfmrTank.szQUERY);
		while (results.hasNext()) {
			DistXfmrTank obj = new DistXfmrTank (results, mapCountTank);
			mapTanks.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadXfmrBanks() {
		ResultSet results = queryHandler.query (DistXfmrBank.szQUERY);
		while (results.hasNext()) {
			DistXfmrBank obj = new DistXfmrBank (results, mapCountBank);
			mapBanks.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadPowerXfmrWindings() {
		ResultSet results = queryHandler.query (DistPowerXfmrWinding.szQUERY);
		while (results.hasNext()) {
			DistPowerXfmrWinding obj = new DistPowerXfmrWinding (results, mapCountWinding);
			mapXfmrWindings.put (obj.GetKey(), obj); 
		}
		((ResultSetCloseable)results).close();
	}

	void LoadCoordinates() {
		ResultSet results = queryHandler.query (DistCoordinates.szQUERY);
		while (results.hasNext()) {
			DistCoordinates obj = new DistCoordinates (results);
			mapCoordinates.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadFeeders() {
		ResultSet results = queryHandler.query (DistFeeder.szQUERY);
		while (results.hasNext()) {
			DistFeeder obj = new DistFeeder (results);
			mapFeeders.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}
	
	void LoadHouses() {
		ResultSet results = queryHandler.query (DistHouse.szQUERY);
		while (results.hasNext()) {
			DistHouse obj = new DistHouse (results);
			mapHouses.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
	}

	void LoadSyncMachines() {
		ResultSet results = queryHandler.query (DistSyncMachine.szQUERY);
		while (results.hasNext()) {
			DistSyncMachine obj = new DistSyncMachine (results);
			mapSyncMachines.put (obj.GetKey(), obj);
		}
		((ResultSetCloseable)results).close();
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
		PrintOneMap (mapBreakers, "** BREAKERS");
		PrintOneMap (mapReclosers, "** RECLOSERS");
		PrintOneMap (mapFuses, "** FUSES");
		PrintOneMap (mapLoadBreakSwitches, "** LOADBREAK SWITCHES");
		PrintOneMap (mapSectionalisers, "** SECTIONALISERS");
		PrintOneMap (mapJumpers, "** JUMPERS");
		PrintOneMap (mapDisconnectors, "** DISCONNECTORS");
		PrintOneMap (mapGroundDisconnectors, "** GROUND DISCONNECTORS");
		PrintOneMap (mapLoads, "** LOADS");
		PrintOneMap (mapWires, "** OVERHEAD WIRES");
		PrintOneMap (mapPhaseMatrices, "** PHASE IMPEDANCE MATRICES");
		PrintOneMap (mapXfmrCores, "** POWER XFMR CORE ADMITTANCES");
		PrintOneMap (mapXfmrMeshes, "** POWER XFMR MESH IMPEDANCES");
		PrintOneMap (mapXfmrWindings, "** POWER XFMR WINDINGS");
		PrintOneMap (mapRegulators, "** REGULATORS");
		PrintOneMap (mapSequenceMatrices, "** SEQUENCE IMPEDANCE MATRICES");
		PrintOneMap (mapSolars, "** SOLAR PV SOURCES");
		PrintOneMap (mapStorages, "** STORAGE SOURCES");
		PrintOneMap (mapSubstations, "** SUBSTATION SOURCES");
		PrintOneMap (mapTSCables, "** TS CABLES");
		PrintOneMap (mapCodeOCTests, "** XFMR CODE OC TESTS");
		PrintOneMap (mapCodeRatings, "** XFMR CODE WINDING RATINGS");
		PrintOneMap (mapCodeSCTests, "** XFMR CODE SC TESTS");
		PrintOneMap (mapBanks, "** XFMR BANKS");
		PrintOneMap (mapTanks, "** XFMR TANKS");
		PrintOneMap (mapHouses, "** HOUSES");
		PrintOneMap (mapSyncMachines, "** SYNC MACHINES");
	}

	public void LoadAllMaps() {
		LoadCountMaps();
		LoadBaseVoltages();
		LoadBreakers();
		LoadCapacitors();
		LoadConcentricNeutralCables();
		LoadCoordinates();
		LoadDisconnectors();
		LoadFuses();
		LoadLinesCodeZ();
		LoadLinesInstanceZ();
		LoadLineSpacings();
		LoadLinesSpacingZ();
		LoadLoadBreakSwitches();
		LoadLoads();
		LoadMeasurements();
		LoadOverheadWires();
		LoadPhaseMatrices();
		LoadPowerXfmrCore();
		LoadPowerXfmrMesh();
		LoadPowerXfmrWindings();
		LoadReclosers();
		LoadRegulators();
		LoadSectionalisers();
		LoadSequenceMatrices();
		LoadSolars();
		LoadStorages();
		LoadSubstations();
		LoadTapeShieldCables();
		LoadXfmrCodeOCTests();
		LoadXfmrCodeRatings();
		LoadXfmrCodeSCTests();
		LoadXfmrTanks();
		LoadXfmrBanks();
		LoadFeeders();
		LoadHouses();
		LoadSyncMachines();
		allMapsLoaded = true;
	}

	public boolean CheckMaps() {
		int nLinks, nNodes;

		if (mapSubstations.size() < 1) {
			throw new RuntimeException ("no substation source");
		}
		nLinks = mapLoadBreakSwitches.size() + mapLinesCodeZ.size() + mapLinesSpacingZ.size() + mapLinesInstanceZ.size() +
			mapXfmrWindings.size() + mapTanks.size() + mapFuses.size() + mapDisconnectors.size() + mapBreakers.size() +
			mapReclosers.size() + mapSectionalisers.size(); // standalone regulators not allowed in CIM
		if (nLinks < 1) {
			throw new RuntimeException ("no lines, transformers or switches");
		}
		nNodes = mapLoads.size() + mapCapacitors.size() + mapSolars.size() + mapStorages.size() + mapSyncMachines.size();
		if (nNodes < 1) {
			throw new RuntimeException ("no loads, capacitors, synchronous machines, solar PV or batteries");
		}
		return true;
	}

	
	public void WriteMapDictionary (HashMap<String,? extends DistComponent> map, String label, boolean bLast, PrintWriter out){
		WriteMapDictionary(map, label, bLast, out, -1);
	}

	public void WriteMapDictionary (HashMap<String,? extends DistComponent> map, String label, boolean bLast, PrintWriter out, int maxMeasurements) {
		int count = 1, last = map.size();
		out.println ("\"" + label + "\":[");

		SortedSet<String> keys = new TreeSet<String>(map.keySet());
		//If we only want a limited number of measurements restrict them
		if(maxMeasurements>=0 && keys.size()>maxMeasurements){
			List<String> tmp = new ArrayList<String>();
			tmp.addAll(keys);
			keys = new TreeSet<String>(tmp.subList(0, maxMeasurements));
		}
		for (String key : keys) {
			out.print (map.get(key).GetJSONEntry ());
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		if (bLast) {
			out.println("]");
		} else {
			out.println("],");
		}
	}

	public void WriteDictionaryFile (PrintWriter out, int maxMeasurements) {
		out.println("{\"feeders\":[");
		for (HashMap.Entry<String,DistFeeder> pair : mapFeeders.entrySet()) {
			DistFeeder fdr = pair.getValue();
			if (fdr.feederID.equals (queryHandler.getFeederSelection())) {
				out.println("{\"name\":\"" + fdr.feederName + "\",");
				out.println("\"mRID\":\"" + fdr.feederID + "\",");
				out.println("\"substation\":\"" + fdr.substationName + "\",");
				out.println("\"substationID\":\"" + fdr.substationID + "\",");
				out.println("\"subregion\":\"" + fdr.subregionName + "\",");
				out.println("\"subregionID\":\"" + fdr.subregionID + "\",");
				out.println("\"region\":\"" + fdr.regionName + "\",");
				out.println("\"regionID\":\"" + fdr.regionID + "\",");
			}
		}
		WriteMapDictionary (mapSyncMachines, "synchronousmachines", false, out);
		WriteMapDictionary (mapCapacitors, "capacitors", false, out);
		WriteMapDictionary (mapRegulators, "regulators", false, out);
		WriteMapDictionary (mapSolars, "solarpanels", false, out);
		WriteMapDictionary (mapStorages, "batteries", false, out);
		WriteMapDictionary (mapLoadBreakSwitches, "switches", false, out);
		WriteMapDictionary (mapFuses, "fuses", false, out);
		WriteMapDictionary (mapSectionalisers, "sectionalisers", false, out);
		WriteMapDictionary (mapBreakers, "breakers", false, out);
		WriteMapDictionary (mapReclosers, "reclosers", false, out);
		WriteMapDictionary (mapDisconnectors, "disconnectors", false, out);
		WriteMapDictionary (mapMeasurements, "measurements", true, out, maxMeasurements);
		out.println("}]}");
		out.close();
	}

	public void WriteMapSymbols (HashMap<String,? extends DistComponent> map, String label, boolean bLast, PrintWriter out) {
		int count = 1, last = map.size();
		out.println ("\"" + label + "\":[");

		SortedSet<String> keys = new TreeSet<String>(map.keySet());
		for (String key : keys) {
			out.print (map.get(key).GetJSONSymbols (mapCoordinates, mapTanks));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		if (bLast) {
			out.println("]");
		} else {
			out.println("],");
		}
	}

	public void WriteJSONSymbolFile (PrintWriter out)  {
		
		int count, last;

		out.println("{\"feeders\":[");
		for (HashMap.Entry<String,DistFeeder> pair : mapFeeders.entrySet()) {
			DistFeeder fdr = pair.getValue();
			if (fdr.feederID.equals (queryHandler.getFeederSelection())) {
				out.println("{\"name\":\"" + fdr.feederName + "\",");
				out.println("\"mRID\":\"" + fdr.feederID + "\",");
				out.println("\"substation\":\"" + fdr.substationName + "\",");
				out.println("\"substationID\":\"" + fdr.substationID + "\",");
				out.println("\"subregion\":\"" + fdr.subregionName + "\",");
				out.println("\"subregionID\":\"" + fdr.subregionID + "\",");
				out.println("\"region\":\"" + fdr.regionName + "\",");
				out.println("\"regionID\":\"" + fdr.regionID + "\",");
			}
		}

		WriteMapSymbols (mapSubstations, "swing_nodes", false, out);
		WriteMapSymbols (mapSyncMachines, "synchronousmachines", false, out);
		WriteMapSymbols (mapCapacitors, "capacitors", false, out);
		WriteMapSymbols (mapSolars, "solarpanels", false, out);
		WriteMapSymbols (mapStorages, "batteries", false, out);

		out.println("\"overhead_lines\":[");
		count = 1;
		last = mapLinesCodeZ.size() + mapLinesInstanceZ.size() + mapLinesSpacingZ.size();
		for (HashMap.Entry<String,DistLinesCodeZ> pair : mapLinesCodeZ.entrySet()) {
			out.print (pair.getValue().GetJSONSymbols(mapCoordinates, mapTanks));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		for (HashMap.Entry<String,DistLinesInstanceZ> pair : mapLinesInstanceZ.entrySet()) {
			out.print (pair.getValue().GetJSONSymbols(mapCoordinates, mapTanks));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		for (HashMap.Entry<String,DistLinesSpacingZ> pair : mapLinesSpacingZ.entrySet()) {
			out.print (pair.getValue().GetJSONSymbols(mapCoordinates, mapTanks));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		out.println("],");

		WriteMapSymbols (mapLoadBreakSwitches, "switches", false, out);
		WriteMapSymbols (mapFuses, "fuses", false, out);
		WriteMapSymbols (mapBreakers, "breakers", false, out);
		WriteMapSymbols (mapReclosers, "reclosers", false, out);
		WriteMapSymbols (mapSectionalisers, "sectionalisers", false, out);
		WriteMapSymbols (mapDisconnectors, "disconnectors", false, out);

		out.println("\"transformers\":[");
		count = 1;
		last =  mapXfmrWindings.size();
		for (HashMap.Entry<String,DistXfmrTank> pair : mapTanks.entrySet()) {
			if (pair.getValue().glmUsed) {
				last += 1;
			}
		}
		for (HashMap.Entry<String,DistPowerXfmrWinding> pair : mapXfmrWindings.entrySet()) {
			out.print (pair.getValue().GetJSONSymbols(mapCoordinates, mapTanks));
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		for (HashMap.Entry<String,DistXfmrTank> pair : mapTanks.entrySet()) {
			DistXfmrTank obj = pair.getValue();
			if (obj.glmUsed) {
				out.print(obj.GetJSONSymbols(mapCoordinates, mapTanks));
				if (count++ < last) {
					out.println (",");
				} else {
					out.println ("");
				}
			}
		}
		out.println("],");

		WriteMapSymbols (mapRegulators, "regulators", true, out);

		out.println("}]}");
		out.close();
	}

	private String GetGLMLineConfiguration (DistLinesSpacingZ ln) {
		String match_A = "";
		String match_B = "";
		String match_C = "";
		String match_N = "";
		String config_name;
		boolean bCable = false;
		StringBuilder buf = new StringBuilder ("spc_" + ln.spacing + "_");

		// what are we looking for?
		for (int i = 0; i < ln.nwires; i++) {
			if (ln.wire_classes[i].equals ("ConcentricNeutralCableInfo")) {
				bCable = true;
				break;
			}
			if (ln.wire_classes[i].equals ("TapeShieldCableInfo")) {
				bCable = true;
				break;
			}
		}
		for (int i = 0; i < ln.nwires; i++) {
			if (ln.wire_phases[i].equals ("A")) {
				match_A = GldLineConfig.GetMatchWire (ln.wire_classes[i], ln.wire_names[i]);
				buf.append ("A");
			}
			if (ln.wire_phases[i].equals ("B")) {
				match_B = GldLineConfig.GetMatchWire (ln.wire_classes[i], ln.wire_names[i]);
				buf.append ("B");
			}
			if (ln.wire_phases[i].equals ("C")) {
				match_C = GldLineConfig.GetMatchWire (ln.wire_classes[i], ln.wire_names[i]);
				buf.append ("C");
			}
			if (ln.wire_phases[i].equals ("N")) {
				if (bCable) {
					match_N = GldLineConfig.GetMatchWire(ln.wire_classes[0], ln.wire_names[0]); // we can't use any bare wires in GLD cables
				} else {
					match_N = GldLineConfig.GetMatchWire(ln.wire_classes[i], ln.wire_names[i]);
				}
				buf.append ("N");
			}
		}
		String match_SPC = buf.toString();

		// search for an existing one
		for (HashMap.Entry<String, GldLineConfig> pair: mapLineConfigs.entrySet()) {
			GldLineConfig cfg = pair.getValue();
			config_name = pair.getKey();
			if (cfg.spacing.equals (match_SPC)) {
				if (cfg.conductor_A.equals (match_A)) {
					if (cfg.conductor_B.equals (match_B)) {
						if (cfg.conductor_C.equals (match_C)) {
							if (cfg.conductor_N.equals (match_N)) {
								return config_name;
							}
						}
					}
				}
			}
		}

		// need to make a new one
		config_name = "lcon_" + ln.spacing + "_" + ln.name;
		GldLineConfig cfg = new GldLineConfig (config_name);
		cfg.spacing = match_SPC;
		cfg.conductor_A = match_A;
		cfg.conductor_B = match_B;
		cfg.conductor_C = match_C;
		cfg.conductor_N = match_N;
		mapLineConfigs.put (config_name, cfg);
		return config_name;
	}
	
	protected void WriteGLMFile (PrintWriter out, double load_scale, boolean bWantSched, String fSched, 
																	 boolean bWantZIP, boolean randomZIP, boolean useHouses, double Zcoeff, double Icoeff, double Pcoeff) {

		// build a polymorphic map of switches
		HashMap<String,DistSwitch> mapSwitches = new HashMap<>();
		mapSwitches.putAll (mapLoadBreakSwitches);
		mapSwitches.putAll (mapFuses);
		mapSwitches.putAll (mapBreakers);
		mapSwitches.putAll (mapReclosers);
		mapSwitches.putAll (mapSectionalisers);
		mapSwitches.putAll (mapDisconnectors);

		// preparatory steps to build the list of nodes
		ResultSet results = queryHandler.query (
				"SELECT ?name WHERE {"+
				" ?fdr c:IdentifiedObject.mRID ?fdrid."+
				" ?s c:ConnectivityNode.ConnectivityNodeContainer ?fdr."+
				" ?s r:type c:ConnectivityNode."+
				" ?s c:IdentifiedObject.name ?name."+
		//		" ?fdr c:IdentifiedObject.name ?feeder."+
			  "} ORDER by ?name");
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
					if (i > 1) {
						nd.bTertiaryWinding = true; // unsupported primary node in GridLAB-D - TODO: throw some kind of warning
					}
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
			nd.AccumulateLoads (obj.name, obj.phases, obj.p, obj.q, obj.pe, obj.qe, obj.pz, obj.pi, obj.pp, obj.qz, obj.qi, obj.qp, randomZIP);
		}
		for (HashMap.Entry<String,DistCapacitor> pair : mapCapacitors.entrySet()) {
			DistCapacitor obj = pair.getValue();
			GldNode nd = mapNodes.get (obj.bus);
			nd.nomvln = obj.basev / Math.sqrt(3.0);
			nd.AddPhases (obj.phs);
		}
		for (HashMap.Entry<String,DistSyncMachine> pair : mapSyncMachines.entrySet()) {
			DistSyncMachine obj = pair.getValue();
			GldNode nd = mapNodes.get (obj.bus);
			nd.bSyncMachines = true;
			nd.nomvln = obj.ratedU / Math.sqrt(3.0);
			nd.AddPhases (obj.phases);
		}
		for (HashMap.Entry<String, DistLinesInstanceZ> pair: mapLinesInstanceZ.entrySet()) {
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
			// TODO - make line configurations on the fly, mark the wires and spacings used
			obj.glm_config = GetGLMLineConfiguration (obj);
			DistLineSpacing spc = mapSpacings.get (obj.spacing);
			if (spc != null) {
				spc.MarkGLMPermutationsUsed(obj.phases);
			}
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
			GldNode nd2 = mapNodes.get (obj.bus2);
			if (obj.glm_phases.equals("S")) {  // TODO - we should be using a graph component like networkx (but for Java) to assign phasing
				String phs1 = nd1.GetPhases();
				String phs2 = nd2.GetPhases();
				if (phs1.length() > 1 && phs1.contains ("S")) {
					obj.glm_phases = nd1.GetPhases();
					nd2.ResetPhases (phs1);
				} else if (phs2.length() > 1 && phs2.contains ("S")) {
					obj.glm_phases = nd2.GetPhases();
					nd1.ResetPhases (phs2);
				}
			} else {
				nd1.nomvln = obj.basev / Math.sqrt(3.0);
				nd1.AddPhases (obj.phases);
				nd2.nomvln = nd1.nomvln;
				nd2.AddPhases (obj.phases);
			}
		}
		for (HashMap.Entry<String,DistPowerXfmrWinding> pair : mapXfmrWindings.entrySet()) {
			DistPowerXfmrWinding obj = pair.getValue();
			for (int i = 0; i < obj.size; i++) {
				GldNode nd = mapNodes.get(obj.bus[i]);
				nd.nomvln = obj.basev[i] / Math.sqrt(3.0);
				nd.AddPhases ("ABC");
				if (i > 1) {
					nd.bTertiaryWinding = true; // unsupported node in GridLAB-D - TODO: throw some kind of warning
				}
			}
		}
		for (HashMap.Entry<String,DistSolar> pair : mapSolars.entrySet()) {
			DistSolar obj = pair.getValue();
			GldNode nd = mapNodes.get (obj.bus);
			nd.bSolarInverters = true;
			if (nd.nomvln < 0.0) {
				if (obj.phases.equals("ABC") || obj.phases.equals("AB") || obj.phases.equals("AC") || obj.phases.equals("BC")) {
					nd.nomvln = obj.ratedU / Math.sqrt(3.0);
				} else {
					nd.nomvln = obj.ratedU;
				}
			}
			nd.AddPhases (obj.phases);
			if (nd.bSecondary) {
				obj.phases = nd.GetPhases();
			}
		}
		for (HashMap.Entry<String,DistStorage> pair : mapStorages.entrySet()) {
			DistStorage obj = pair.getValue();
			GldNode nd = mapNodes.get (obj.bus);
			nd.bStorageInverters = true;
			if (nd.nomvln < 0.0) {
				if (obj.phases.equals("ABC") || obj.phases.equals("AB") || obj.phases.equals("AC") || obj.phases.equals("BC")) {
					nd.nomvln = obj.ratedU / Math.sqrt(3.0);
				} else {
					nd.nomvln = obj.ratedU;
				}
			}
			nd.AddPhases (obj.phases);
			if (nd.bSecondary) {
				obj.phases = nd.GetPhases();
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
		for (HashMap.Entry<String,DistOverheadWire> pair : mapWires.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,DistConcentricNeutralCable> pair : mapCNCables.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,DistTapeShieldCable> pair : mapTSCables.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,DistLineSpacing> pair : mapSpacings.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,GldLineConfig> pair : mapLineConfigs.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
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
		for (HashMap.Entry<String,DistSolar> pair : mapSolars.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,DistStorage> pair : mapStorages.entrySet()) {
			out.print (pair.getValue().GetGLM());
		}
		for (HashMap.Entry<String,DistSyncMachine> pair : mapSyncMachines.entrySet()) {
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
			DistSwitch obj = pair.getValue();
			if (obj.glm_phases.contains ("S")) { // need to parent the nodes instead of writing a switch - TODO: this is hard-wired to PNNL taxonomy
				GldNode nd1 = mapNodes.get (obj.bus1);
				GldNode nd2 = mapNodes.get (obj.bus2);
				if (nd1.name.contains ("_tn_")) {
					nd2.CopyLoad (nd1);
					mapNodes.remove (obj.bus1);
				} else {
					nd1.CopyLoad (nd2);
					mapNodes.remove (obj.bus2);
				}
			} else {
				out.print(obj.GetGLM());
			}
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
			out.print (pair.getValue().GetGLM (load_scale, bWantSched, fSched, bWantZIP, useHouses, Zcoeff, Icoeff, Pcoeff));
		}
		
		// GLM houses
		if (useHouses) {
			for (HashMap.Entry<String, DistHouse> pair : mapHouses.entrySet()) {
				out.print((pair.getValue().GetGLM()));
			}
		}

		// try to link all CIM measurements to the GridLAB-D objects
		for (HashMap.Entry<String,DistMeasurement> pair : mapMeasurements.entrySet()) {
			DistMeasurement obj = pair.getValue();
			GldNode nd = mapNodes.get (obj.bus);
			obj.FindSimObject (nd.loadname, nd.phases, nd.bStorageInverters, nd.bSolarInverters, nd.bSyncMachines);
		}

		out.close();
	}
	
	protected void WriteDSSCoordinates (PrintWriter out)  {
		String bus;
		DistCoordinates pt1, pt2;
		HashMap<String,Double[]> mapBusXY = new HashMap<String,Double[]>();

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
				} else if (obj.cname.equals("BatteryUnit")) {
					bus = mapStorages.get(obj.name).bus;
					mapBusXY.put(bus, new Double[] {obj.x, obj.y});
				} else if (obj.cname.equals("PhotovoltaicUnit")) {
					bus = mapSolars.get(obj.name).bus;
					mapBusXY.put(bus, new Double[] {obj.x, obj.y});
				} else if (obj.cname.equals("SynchronousMachine")) {
					bus = mapSyncMachines.get(obj.name).bus;
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

		// switches and lines have bus locations; should be in correct order using ACDCTerminal.sequenceNumber
		for (HashMap.Entry<String,DistLinesCodeZ> pair : mapLinesCodeZ.entrySet()) {
			DistLineSegment obj = pair.getValue();
			pt1 = mapCoordinates.get("ACLineSegment:" + obj.name + ":1");
			pt2 = mapCoordinates.get("ACLineSegment:" + obj.name + ":2");
			mapBusXY.put (obj.bus1, new Double [] {pt1.x, pt1.y});
			mapBusXY.put (obj.bus2, new Double [] {pt2.x, pt2.y});
//			setSegXY.add(new DSSSegmentXY (obj.bus1, pt1.x, pt1.y, obj.bus2, pt2.x, pt2.y));
		}
		for (HashMap.Entry<String,DistLinesInstanceZ> pair : mapLinesInstanceZ.entrySet()) {
			DistLineSegment obj = pair.getValue();
			pt1 = mapCoordinates.get("ACLineSegment:" + obj.name + ":1");
			pt2 = mapCoordinates.get("ACLineSegment:" + obj.name + ":2");
			mapBusXY.put (obj.bus1, new Double [] {pt1.x, pt1.y});
			mapBusXY.put (obj.bus2, new Double [] {pt2.x, pt2.y});
		}
		for (HashMap.Entry<String,DistLinesSpacingZ> pair : mapLinesSpacingZ.entrySet()) {
			DistLineSegment obj = pair.getValue();
			pt1 = mapCoordinates.get("ACLineSegment:" + obj.name + ":1");
			pt2 = mapCoordinates.get("ACLineSegment:" + obj.name + ":2");
			mapBusXY.put (obj.bus1, new Double [] {pt1.x, pt1.y});
			mapBusXY.put (obj.bus2, new Double [] {pt2.x, pt2.y});
		}
		for (HashMap.Entry<String,DistLoadBreakSwitch> pair : mapLoadBreakSwitches.entrySet()) {
			DistLoadBreakSwitch obj = pair.getValue();
			pt1 = mapCoordinates.get("LoadBreakSwitch:" + obj.name + ":1");
			pt2 = mapCoordinates.get("LoadBreakSwitch:" + obj.name + ":2");
			mapBusXY.put (obj.bus1, new Double [] {pt1.x, pt1.y});
			mapBusXY.put (obj.bus2, new Double [] {pt2.x, pt2.y});
		}
		for (HashMap.Entry<String,DistFuse> pair : mapFuses.entrySet()) { // TODO - polymorphic switch maps
			DistFuse obj = pair.getValue();
			pt1 = mapCoordinates.get("Fuse:" + obj.name + ":1");
			pt2 = mapCoordinates.get("Fuse:" + obj.name + ":2");
			mapBusXY.put (obj.bus1, new Double [] {pt1.x, pt1.y});
			mapBusXY.put (obj.bus2, new Double [] {pt2.x, pt2.y});
		}
		for (HashMap.Entry<String,DistRecloser> pair : mapReclosers.entrySet()) {
			DistRecloser obj = pair.getValue();
			pt1 = mapCoordinates.get("Recloser:" + obj.name + ":1");
			pt2 = mapCoordinates.get("Recloser:" + obj.name + ":2");
			mapBusXY.put (obj.bus1, new Double [] {pt1.x, pt1.y});
			mapBusXY.put (obj.bus2, new Double [] {pt2.x, pt2.y});
		}
		for (HashMap.Entry<String,DistBreaker> pair : mapBreakers.entrySet()) {
			DistBreaker obj = pair.getValue();
			pt1 = mapCoordinates.get("Breaker:" + obj.name + ":1");
			pt2 = mapCoordinates.get("Breaker:" + obj.name + ":2");
			mapBusXY.put (obj.bus1, new Double [] {pt1.x, pt1.y});
			mapBusXY.put (obj.bus2, new Double [] {pt2.x, pt2.y});
		}
		for (HashMap.Entry<String,DistSectionaliser> pair : mapSectionalisers.entrySet()) {
			DistSectionaliser obj = pair.getValue();
			pt1 = mapCoordinates.get("Sectionaliser:" + obj.name + ":1");
			pt2 = mapCoordinates.get("Sectionaliser:" + obj.name + ":2");
			mapBusXY.put (obj.bus1, new Double [] {pt1.x, pt1.y});
			mapBusXY.put (obj.bus2, new Double [] {pt2.x, pt2.y});
		}
		for (HashMap.Entry<String,DistDisconnector> pair : mapDisconnectors.entrySet()) {
			DistDisconnector obj = pair.getValue();
			pt1 = mapCoordinates.get("Disconnector:" + obj.name + ":1");
			pt2 = mapCoordinates.get("Disconnector:" + obj.name + ":2");
			mapBusXY.put (obj.bus1, new Double [] {pt1.x, pt1.y});
			mapBusXY.put (obj.bus2, new Double [] {pt2.x, pt2.y});
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

	protected String GUIDfromCIMmRID (String id) {
		int idx = id.indexOf ("_");
		if (idx >= 0) {
			return id.substring(idx+1);
		}
		return id;
	}

	protected void WriteDSSFile (PrintWriter out, PrintWriter outID, String fXY, String fID, double load_scale, boolean bWantZIP, 
														double Zcoeff, double Icoeff, double Pcoeff)  {

		for (HashMap.Entry<String,DistSubstation> pair : mapSubstations.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Circuit." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}

		out.println();
		for (HashMap.Entry<String,DistOverheadWire> pair : mapWires.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Wiredata." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistConcentricNeutralCable> pair : mapCNCables.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("CNData." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistTapeShieldCable> pair : mapTSCables.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("TSData." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistLineSpacing> pair : mapSpacings.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("LineSpacing." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistPhaseMatrix> pair : mapPhaseMatrices.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Linecode." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistSequenceMatrix> pair : mapSequenceMatrices.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Linecode." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistXfmrCodeRating> pair : mapCodeRatings.entrySet()) {
			DistXfmrCodeRating obj = pair.getValue();
			DistXfmrCodeSCTest sct = mapCodeSCTests.get (obj.tname);
			DistXfmrCodeOCTest oct = mapCodeOCTests.get (obj.tname);
			out.print (obj.GetDSS(sct, oct));
			outID.println ("Xfmrcode." + obj.tname + "\t" + GUIDfromCIMmRID (obj.id));
		}

		out.println();
		for (HashMap.Entry<String,DistSolar> pair : mapSolars.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("PVSystem." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistStorage> pair : mapStorages.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Storage." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistSyncMachine> pair : mapSyncMachines.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Generator." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistLoad> pair : mapLoads.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Load." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistLoadBreakSwitch> pair : mapLoadBreakSwitches.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistFuse> pair : mapFuses.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistRecloser> pair : mapReclosers.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistSectionaliser> pair : mapSectionalisers.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistBreaker> pair : mapBreakers.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistDisconnector> pair : mapDisconnectors.entrySet()) { // TODO - polymorphic mapSwitches
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistLinesCodeZ> pair : mapLinesCodeZ.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistLinesSpacingZ> pair : mapLinesSpacingZ.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistLinesInstanceZ> pair : mapLinesInstanceZ.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Line." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistPowerXfmrWinding> pair : mapXfmrWindings.entrySet()) {
			DistPowerXfmrWinding obj = pair.getValue();
			DistPowerXfmrMesh mesh = mapXfmrMeshes.get (obj.name);
			DistPowerXfmrCore core = mapXfmrCores.get (obj.name);
			out.print (obj.GetDSS(mesh, core));
			outID.println ("Transformer." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistXfmrTank> pair : mapTanks.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Transformer." + pair.getValue().tname + "\t" + GUIDfromCIMmRID (pair.getValue().id));
		}
		out.println();
		for (HashMap.Entry<String,DistRegulator> pair : mapRegulators.entrySet()) {
			DistRegulator obj = pair.getValue();
			out.print(obj.GetDSS());
			for (int i = 0; i < obj.size; i++) {
				outID.println("RegControl." + obj.rname[i] + "\t" + GUIDfromCIMmRID (obj.id[i]));
			}
		}
		out.println(); // capacitors last in case the capcontrols reference a preceeding element
		for (HashMap.Entry<String,DistCapacitor> pair : mapCapacitors.entrySet()) {
			out.print (pair.getValue().GetDSS());
			outID.println ("Capacitor." + pair.getValue().name + "\t" + GUIDfromCIMmRID (pair.getValue().id));
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
	
	protected void WriteIndexFile (PrintWriter out)  {
		LoadFeeders ();
		PrintOneMap (mapFeeders, "*** FEEDERS ***");

		out.println("{\"feeders\":[");

		int count = 1, last = mapFeeders.size();

		for (HashMap.Entry<String,DistFeeder> pair : mapFeeders.entrySet()) {
			out.print (pair.getValue().GetJSONEntry());
			if (count++ < last) {
				out.println (",");
			} else {
				out.println ("");
			}
		}
		out.println("]}");
		out.close();
	}

	public void start(QueryHandler queryHandler, String fTarget, String fRoot, String fSched, double load_scale, boolean bWantSched, boolean bWantZIP, boolean randomZIP, boolean useHouses, double Zcoeff, double Icoeff, double Pcoeff) throws FileNotFoundException{
		start(queryHandler, fTarget, fRoot, fSched, load_scale, bWantSched, bWantZIP, randomZIP, useHouses, Zcoeff, Icoeff, Pcoeff, -1);
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
	public void start(QueryHandler queryHandler, String fTarget, String fRoot, String fSched, double load_scale, boolean bWantSched, boolean bWantZIP, boolean randomZIP, boolean useHouses, double Zcoeff, double Icoeff, double Pcoeff, int maxMeasurements) throws FileNotFoundException{
		this.queryHandler = queryHandler;
		String fOut, fXY, fID, fDict;		

		if (fTarget.equals("glm")) {
			LoadAllMaps();
			CheckMaps();
//			PrintOneMap (mapSolars, "** SOLAR PV SOURCES");
//			PrintOneMap (mapStorages, "** STORAGE SOURCES");
//			PrintAllMaps();
			fDict = fRoot + "_dict.json";
			fOut = fRoot + "_base.glm";
			fXY = fRoot + "_symbols.json";
			PrintWriter pOut = new PrintWriter(fOut);
			WriteGLMFile(pOut, load_scale, bWantSched, fSched, bWantZIP, randomZIP, useHouses, Zcoeff, Icoeff, Pcoeff);
			PrintWriter pXY = new PrintWriter(fXY);
			WriteJSONSymbolFile (pXY);
			PrintWriter pDict = new PrintWriter(fDict);
			WriteDictionaryFile (pDict, maxMeasurements);
		} else if (fTarget.equals("dss")) {
			LoadAllMaps();
			CheckMaps();
			fDict = fRoot + "_dict.json";
			fOut = fRoot + "_base.dss";
			fXY = fRoot + "_busxy.dss";
			fID = fRoot + "_guid.dss";
			PrintWriter pOut = new PrintWriter(fOut);
			PrintWriter pID = new PrintWriter(fID);
			WriteDSSFile (pOut, pID, fXY, fID, load_scale, bWantZIP, Zcoeff, Icoeff, Pcoeff);
			PrintWriter pXY = new PrintWriter(fXY);
			WriteDSSCoordinates (pXY);
			PrintWriter pSym = new PrintWriter (fRoot + "_symbols.json");
			WriteJSONSymbolFile (pSym);
			PrintWriter pDict = new PrintWriter(fDict);
			WriteDictionaryFile (pDict, maxMeasurements);
		}	else if (fTarget.equals("idx")) {
			fOut = fRoot + "_feeder_index.json";
			PrintWriter pOut = new PrintWriter(fOut);
			WriteIndexFile (pOut);
		}
	}
	
	
	
	/**
	 * 
	 * @param queryHandler
	 * @param out
	 * @param fSched
	 * @param load_scale
	 * @param bWantSched
	 * @param bWantZIP
	 * @param Zcoeff
	 * @param Icoeff
	 * @param Pcoeff
	 */
	public void generateGLMFile(QueryHandler queryHandler, PrintWriter out, String fSched, double load_scale, boolean bWantSched, boolean bWantZIP, boolean randomZIP, boolean useHouses, double Zcoeff, double Icoeff, double Pcoeff) {
		this.queryHandler = queryHandler;
		if(!allMapsLoaded){
			LoadAllMaps();
		}
		CheckMaps();
		WriteGLMFile (out, load_scale, bWantSched, fSched, bWantZIP, randomZIP, useHouses, Zcoeff, Icoeff, Pcoeff);
	}
	
	/**
	 * 
	 * @param queryHandler
	 * @param out
	 */
	public void generateJSONSymbolFile(QueryHandler queryHandler, PrintWriter out){
		this.queryHandler = queryHandler;
		if(!allMapsLoaded){
			LoadAllMaps();
		}
		CheckMaps();
		WriteJSONSymbolFile(out);
	}
	
	
	public void generateDictionaryFile(QueryHandler queryHandler, PrintWriter out){
		generateDictionaryFile(queryHandler, out, -1);
	}
	/**
	 * 
	 * @param queryHandler
	 * @param out
	 */
	public void generateDictionaryFile(QueryHandler queryHandler, PrintWriter out, int maxMeasurements){
		this.queryHandler = queryHandler;
		if(!allMapsLoaded){
			LoadAllMaps();
		}
		CheckMaps();
		WriteDictionaryFile(out, maxMeasurements);
	}
	
	/**
	 * 
	 * @param queryHandler
	 * @param out
	 * @param outID
	 * @param fXY
	 * @param fID
	 * @param load_scale
	 * @param bWantZIP
	 * @param Zcoeff
	 * @param Icoeff
	 * @param Pcoeff
	 */
	public void generateDSSFile(QueryHandler queryHandler, PrintWriter out, PrintWriter outID, String fXY, String fID, double load_scale, boolean bWantZIP, 
														double Zcoeff, double Icoeff, double Pcoeff){
		this.queryHandler = queryHandler;
		if(!allMapsLoaded){
			LoadAllMaps();
		}
		CheckMaps();
		WriteDSSFile(out, outID, fXY, fID, load_scale, bWantZIP, Zcoeff, Icoeff, Pcoeff);
	}
	
	/**
	 * 
	 * @param queryHandler
	 * @param out
	 */
	public void generateDSSCoordinates(QueryHandler queryHandler, PrintWriter out){
		this.queryHandler = queryHandler;
		if(!allMapsLoaded){
			LoadAllMaps();
		}
		CheckMaps();
		
		WriteDSSCoordinates(out);
	}
	
	/**
	 * 
	 * @param queryHandler
	 * @param out
	 */
	public void generateFeederIndexFile(QueryHandler queryHandler, PrintWriter out){
		this.queryHandler = queryHandler;
		
		WriteIndexFile(out);
	}
	
	public static void main (String args[]) throws FileNotFoundException {
		String fRoot = "";
		double freq = 60.0, load_scale = 1.0;
		boolean bWantSched = false, bWantZIP = false, bSelectFeeder = false, randomZIP = false, useHouses = false;
		String fSched = "";
		String fTarget = "dss";
		String feeder_mRID = "";
		double Zcoeff = 0.0, Icoeff = 0.0, Pcoeff = 0.0;
		String blazegraphURI = "http://localhost:9999/blazegraph/namespace/kb/sparql";
		if (args.length < 1) {
			System.out.println ("Usage: java CIMImporter [options] output_root");
			System.out.println ("       -s={mRID}          // select one feeder by CIM mRID; selects all feeders if not specified");
			System.out.println ("       -o={glm|dss|idx}   // output format; defaults to glm");
			System.out.println ("       -l={0..1}          // load scaling factor; defaults to 1");
			System.out.println ("       -f={50|60}         // system frequency; defaults to 60");
			System.out.println ("       -n={schedule_name} // root filename for scheduled ZIP loads (defaults to none), valid only for -o=glm");
			System.out.println ("       -z={0..1}          // constant Z portion (defaults to 0 for CIM-defined LoadResponseCharacteristic)");
			System.out.println ("       -i={0..1}          // constant I portion (defaults to 0 for CIM-defined LoadResponseCharacteristic)");
			System.out.println ("       -p={0..1}          // constant P portion (defaults to 0 for CIM-defined LoadResponseCharacteristic)");
			System.out.println ("       -r={0, 1}          // determine ZIP load fraction based on given xml file or randomized fractions");
			System.out.println ("       -h={0, 1}          // determine if house load objects should be added to the model or not");
			System.out.println ("       -u={http://localhost:9999/blazegraph/namespace/kb/sparql} // blazegraph uri (if connecting over HTTP); defaults to http://localhost:9999/blazegraph/namespace/kb/sparql");

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
				} else if (opt == 'r' && Integer.parseInt(optVal) == 1) {
					randomZIP = true;
				} else if (opt == 'h' && Integer.parseInt(optVal) == 1) {
					useHouses = true;
				} else if (opt == 's') {
					feeder_mRID = optVal;
					bSelectFeeder = true;
				} else if (opt == 'u') {
					blazegraphURI = optVal;
				}
			} else {
				if (fTarget.equals("glm")) {
					fRoot = args[i];
				} else if (fTarget.equals("dss")) {
					fRoot = args[i];
				} else if (fTarget.equals("idx")) {
					fRoot = args[i];
				} else {
					System.out.println ("Unknown target type " + fTarget);
					System.exit(0);
				}
			}
			++i;
		}
		
		try {
			HTTPBlazegraphQueryHandler qh = new HTTPBlazegraphQueryHandler(blazegraphURI);
			if (bSelectFeeder) {
				qh.addFeederSelection (feeder_mRID);
//				System.out.println ("Selecting only feeder " + feeder_mRID);
			}
			new CIMImporter().start(qh, fTarget, fRoot, fSched, load_scale,
															bWantSched, bWantZIP, randomZIP, useHouses, Zcoeff, Icoeff, Pcoeff);
		} catch (RuntimeException e) {
			System.out.println ("Can not produce a model: " + e.getMessage());
			e.printStackTrace();
		}
		
	}
}

