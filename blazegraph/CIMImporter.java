//      		----------------------------------------------------------
//      		Copyright (c) 2017, Battelle Memorial Institute
//      		All rights reserved.
//      		----------------------------------------------------------

// package gov.pnnl.gridlabd.cim ;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;

// import gov.pnnl.gridlabd.cim.GldNode;

/**
 * <p>This class runs an example SQARQL query against Blazegraph triple-store</p> 
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
	static final String szEND = "http://localhost:9999/blazegraph/namespace/kb/sparql";
	static final String nsCIM = "http://iec.ch/TC57/2012/CIM-schema-cim16#";
	static final String nsRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	static ResultSet RunQuery(String szQuery) {
		String qPrefix = "PREFIX r: <" + nsRDF + "> PREFIX c: <" + nsCIM + "> ";
		Query query = QueryFactory.create (qPrefix + szQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService (szEND, query);
		return qexec.execSelect();
	}

	static HashMap<String,GldNode> mapNodes = new HashMap<>();
	static HashMap<String,DistBaseVoltage> mapBaseVoltages = new HashMap<>();
	static HashMap<String,DistCapacitor> mapCapacitors = new HashMap<>();
	static HashMap<String,DistConcentricNeutralCable> mapCNCables = new HashMap<>();
	static HashMap<String,DistCoordinates> mapCoordinates = new HashMap<>();
	static HashMap<String,DistLinesCodeZ> mapLinesCodeZ = new HashMap<>();
	static HashMap<String,DistLinesInstanceZ> mapLinesInstanceZ = new HashMap<>();
	static HashMap<String,DistLineSpacing> mapSpacings = new HashMap<>();
	static HashMap<String,DistLinesSpacingZ> mapLinesSpacingZ = new HashMap<>();
	static HashMap<String,DistLoad> mapLoads = new HashMap<>();
	static HashMap<String,DistOverheadWire> mapWires = new HashMap<>();
	static HashMap<String,DistPhaseMatrix> mapPhaseMatrices = new HashMap<>();
	static HashMap<String,DistPowerXfmrCore> mapXfmrCores = new HashMap<>();
	static HashMap<String,DistPowerXfmrMesh> mapXfmrMeshes = new HashMap<>();
	static HashMap<String,DistPowerXfmrWinding> mapXfmrWindings = new HashMap<>();
	static HashMap<String,DistRegulator> mapRegulators = new HashMap<>();
	static HashMap<String,DistSequenceMatrix> mapSequenceMatrices = new HashMap<>();
	static HashMap<String,DistSubstation> mapSubstations = new HashMap<>();
	static HashMap<String,DistSwitch> mapSwitches = new HashMap<>();
	static HashMap<String,DistTapeShieldCable> mapTSCables = new HashMap<>();
	static HashMap<String,DistXfmrCodeOCTest> mapCodeOCTests = new HashMap<>();
	static HashMap<String,DistXfmrCodeRating> mapCodeRatings = new HashMap<>();
	static HashMap<String,DistXfmrCodeSCTest> mapCodeSCTests = new HashMap<>();
	static HashMap<String,DistXfmrTank> mapTanks = new HashMap<>();

	static void LoadBaseVoltages() {
		ResultSet results = RunQuery (DistBaseVoltage.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistBaseVoltage obj = new DistBaseVoltage (soln);
			mapBaseVoltages.put (obj.GetKey(), obj);
		}
	}

	static void LoadSubstations() {
		ResultSet results = RunQuery (DistSubstation.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistSubstation obj = new DistSubstation (soln);
			mapSubstations.put (obj.GetKey(), obj);
		}
	}

	static void LoadCapacitors() {
		ResultSet results = RunQuery (DistCapacitor.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistCapacitor obj = new DistCapacitor (soln);
			mapCapacitors.put (obj.GetKey(), obj);
		}
	}

	static void LoadLoads() {
		ResultSet results = RunQuery (DistLoad.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistLoad obj = new DistLoad (soln);
			mapLoads.put (obj.GetKey(), obj);
		}
	}

	static void LoadPhaseMatrices() {
		ResultSet results = RunQuery (DistPhaseMatrix.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistPhaseMatrix obj = new DistPhaseMatrix (soln);
//			DistPhaseMatrix obj = new DistPhaseMatrix (results);
			mapPhaseMatrices.put (obj.GetKey(), obj);
		}
	}

	static void LoadSequenceMatrices() {
		ResultSet results = RunQuery (DistSequenceMatrix.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistSequenceMatrix obj = new DistSequenceMatrix (soln);
			mapSequenceMatrices.put (obj.GetKey(), obj);
		}
	}

	static void LoadXfmrCodeRatings() {
		ResultSet results = RunQuery (DistXfmrCodeRating.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistXfmrCodeRating obj = new DistXfmrCodeRating (soln);
			mapCodeRatings.put (obj.GetKey(), obj);
		}
	}

	static void LoadXfmrCodeOCTests() {
		ResultSet results = RunQuery (DistXfmrCodeOCTest.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistXfmrCodeOCTest obj = new DistXfmrCodeOCTest (soln);
			mapCodeOCTests.put (obj.GetKey(), obj);
		}
	}

	static void LoadXfmrCodeSCTests() {
		ResultSet results = RunQuery (DistXfmrCodeSCTest.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistXfmrCodeSCTest obj = new DistXfmrCodeSCTest (soln);
			mapCodeSCTests.put (obj.GetKey(), obj);
		}
	}

	static void LoadPowerXfmrCore() {
		ResultSet results = RunQuery (DistPowerXfmrCore.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistPowerXfmrCore obj = new DistPowerXfmrCore (soln);
			mapXfmrCores.put (obj.GetKey(), obj);
		}
	}

	static void LoadPowerXfmrMesh() {
		ResultSet results = RunQuery (DistPowerXfmrMesh.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistPowerXfmrMesh obj = new DistPowerXfmrMesh (soln);
			mapXfmrMeshes.put (obj.GetKey(), obj);
		}
	}

	static void LoadOverheadWires() {
		ResultSet results = RunQuery (DistOverheadWire.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistOverheadWire obj = new DistOverheadWire (soln);
			mapWires.put (obj.GetKey(), obj);
		}
	}

	static void LoadTapeShieldCables() {
		ResultSet results = RunQuery (DistTapeShieldCable.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistTapeShieldCable obj = new DistTapeShieldCable (soln);
			mapTSCables.put (obj.GetKey(), obj);
		}
	}

	static void LoadConcentricNeutralCables() {
		ResultSet results = RunQuery (DistConcentricNeutralCable.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistConcentricNeutralCable obj = new DistConcentricNeutralCable (soln);
			mapCNCables.put (obj.GetKey(), obj);
		}
	}

	static void LoadLineSpacings() {
		ResultSet results = RunQuery (DistLineSpacing.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistLineSpacing obj = new DistLineSpacing (soln);
			mapSpacings.put (obj.GetKey(), obj);
		}
	}

	static void LoadSwitches() {
		ResultSet results = RunQuery (DistSwitch.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistSwitch obj = new DistSwitch (soln);
			mapSwitches.put (obj.GetKey(), obj);
		}
	}

	static void LoadLinesInstanceZ() {
		ResultSet results = RunQuery (DistLinesInstanceZ.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistLinesInstanceZ obj = new DistLinesInstanceZ (soln);
			mapLinesInstanceZ.put (obj.GetKey(), obj);
		}
	}

	static void LoadLinesCodeZ() {
		ResultSet results = RunQuery (DistLinesCodeZ.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistLinesCodeZ obj = new DistLinesCodeZ (soln);
			mapLinesCodeZ.put (obj.GetKey(), obj);
		}
	}

	static void LoadLinesSpacingZ() {
		ResultSet results = RunQuery (DistLinesSpacingZ.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistLinesSpacingZ obj = new DistLinesSpacingZ (soln);
			mapLinesSpacingZ.put (obj.GetKey(), obj);
		}
	}

	static void LoadRegulators() {
		ResultSet results = RunQuery (DistRegulator.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistRegulator obj = new DistRegulator (soln);
			mapRegulators.put (obj.GetKey(), obj);
		}
	}

	static void LoadXfmrTanks() {
		ResultSet results = RunQuery (DistXfmrTank.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistXfmrTank obj = new DistXfmrTank (soln);
			mapTanks.put (obj.GetKey(), obj);
		}
	}

	static void LoadPowerXfmrWindings() {
		ResultSet results = RunQuery (DistPowerXfmrWinding.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistPowerXfmrWinding obj = new DistPowerXfmrWinding (soln);
			mapXfmrWindings.put (obj.GetKey(), obj); 
		}
	}

	static void LoadCoordinates() {
		ResultSet results = RunQuery (DistCoordinates.szQUERY);
		QuerySolution soln;
		while (results.hasNext()) {
			soln = results.next();
			DistCoordinates obj = new DistCoordinates (soln);
			mapCoordinates.put (obj.GetKey(), obj);
		}
	}

	public static void PrintOneMap(HashMap<String,? extends DistComponent> map, String label) {
		System.out.println(label);
		SortedSet<String> keys = new TreeSet<String>(map.keySet());
		for (String key : keys) {
			System.out.println (map.get(key).DisplayString());
		}
	}

	public static void PrintAllMaps() {
		// this is the un-sorted method of display
//		for (HashMap.Entry<String,DistBaseVoltage> pair : mapBaseVoltages.entrySet()) {
//			System.out.println (pair.getValue().DisplayString());
//		}

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
		PrintOneMap (mapTanks, "** XFMR TANKS");
	}

	public static void LoadAllMaps() {
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
	}

	public static void main (String args[]) throws UnsupportedEncodingException, FileNotFoundException {
		LoadAllMaps();
		PrintAllMaps();
	}
}

