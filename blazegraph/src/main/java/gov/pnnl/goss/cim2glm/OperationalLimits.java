package gov.pnnl.goss.cim2glm;
// ----------------------------------------------------------
// Copyright (c) 2017-2019, Battelle Memorial Institute
// All rights reserved.
// ----------------------------------------------------------

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.DecimalFormat;

import org.apache.jena.query.*;

import gov.pnnl.goss.cim2glm.CIMImporter;
import gov.pnnl.goss.cim2glm.queryhandler.QueryHandler;
import gov.pnnl.goss.cim2glm.queryhandler.impl.HTTPBlazegraphQueryHandler;

public class OperationalLimits extends Object {
	QueryHandler queryHandler;
	// an ascending list of voltage thresholds, indexed by ConnectivityNode ID
	// normally the indexing will be 0:ANSI B Range Low, 1:A Low, 2:A High, 3: B High
	HashMap<String,double[]> mapVoltageLimits = new HashMap<>();
	// an ascending list of current magnitude thresholds, indexed by equipment ID
	// normally the indexing will be 0:normal, 1:emergency
	HashMap<String,double[]> mapCurrentLimits = new HashMap<>();

	private static final String szVOLT = 
		"SELECT ?id ?val ?dur ?dir WHERE {"+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?s c:ConnectivityNode.ConnectivityNodeContainer ?fdr."+
		" ?s r:type c:ConnectivityNode."+
		" ?s c:IdentifiedObject.mRID ?id."+
		" ?s c:ConnectivityNode.OperationalLimitSet ?ols."+
		" ?vlim c:OperationalLimit.OperationalLimitSet ?ols."+
		" ?vlim r:type c:VoltageLimit."+
		" ?vlim c:OperationalLimit.OperationalLimitType ?olt."+
		" ?olt c:OperationalLimitType.acceptableDuration ?dur."+
		" ?olt c:OperationalLimitType.direction ?rawdir."+
		"  bind(strafter(str(?rawdir),\"OperationalLimitDirectionKind.\") as ?dir)"+
		" ?vlim c:VoltageLimit.value ?val."+
		"} ORDER by ?id ?val";

	private static final String szCURR = 
		"SELECT ?id ?val ?dur ?dir WHERE {"+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?eq c:Equipment.EquipmentContainer ?fdr."+
		" ?eq c:IdentifiedObject.mRID ?id."+
		" ?t c:Terminal.ConductingEquipment ?eq."+
		" ?t c:ACDCTerminal.OperationalLimitSet ?ols."+
		" ?clim c:OperationalLimit.OperationalLimitSet ?ols."+
		" ?clim r:type c:CurrentLimit."+
		" ?clim c:OperationalLimit.OperationalLimitType ?olt."+
		" ?olt c:OperationalLimitType.acceptableDuration ?dur."+
		" ?olt c:OperationalLimitType.direction ?rawdir."+
		" bind(strafter(str(?rawdir),\"OperationalLimitDirectionKind.\") as ?dir)"+
		" ?clim c:CurrentLimit.value ?val."+
		"} ORDER by ?id ?val";

	private void LoadVoltageMap () {
		ResultSet results = queryHandler.query (szVOLT);
		String lastID = "";
		double Alo = 0.0, Ahi = 1.0e9, Blo = 1.0e9, Bhi = 0.0;
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			String id = soln.get("?id").toString();
			if (!id.equals(lastID)) {
				if (!lastID.equals("")) {
					double [] vals = {Blo, Alo, Ahi, Bhi};
					mapVoltageLimits.put(lastID, vals);
				}
				Alo = 0.0;   // want the highest low value
				Blo = 1.0e9; // want the lowest low value
				Ahi = 1.0e9; // want the lowest high value
				Bhi = 0.0;   // want the highest high vale
				lastID = id;
			}
			String dir = soln.get("?dir").toString();
			double dur = Double.parseDouble (soln.get("?dur").toString());
			double val = Double.parseDouble (soln.get("?val").toString());
			if (dir.equals("low")) {
				if (val > Alo) {
					Alo = val;
				}
				if (val < Blo) {
					Blo = val;
				}
			} else if (dir.equals("high")) {
				if (val < Ahi) {
					Ahi = val;
				}
				if (val > Bhi) {
					Bhi = val;
				}
			}
		}
		((ResultSetCloseable)results).close();
		if (!mapVoltageLimits.containsKey (lastID)) {
			double [] vals = {Blo, Alo, Ahi, Bhi};
			mapVoltageLimits.put(lastID, vals);
		}
	}

	public void VoltageMapToJSON (PrintWriter out) {
		DecimalFormat df2 = new DecimalFormat("#0.00");
		int idxLast = mapVoltageLimits.size() - 1;
		int idx = 0;
		String sTerm = "},";
		for (HashMap.Entry<String,double[]> pair : mapVoltageLimits.entrySet()) {
			double []vals = pair.getValue();
			String id = pair.getKey();
			if (idx == idxLast) {
				sTerm = "}";
			}
			out.println("{\"id\":\"" + id + "\"" +
									 ",\"Blo\":" + df2.format (vals[0]) +
									 ",\"Alo\":" + df2.format (vals[1]) +
									 ",\"Ahi\":" + df2.format (vals[2]) +
									 ",\"Bhi\":" + df2.format (vals[3]) + sTerm);
			++idx;
		}
	}

	public void CurrentMapToJSON (PrintWriter out) {
		DecimalFormat df2 = new DecimalFormat("#0.00");
		int idxLast = mapCurrentLimits.size() - 1;
		int idx = 0;
		String sTerm = "},";
		for (HashMap.Entry<String,double[]> pair : mapCurrentLimits.entrySet()) {
			double []vals = pair.getValue();
			String id = pair.getKey();
			if (idx == idxLast) {
				sTerm = "}";
			}
			out.println("{\"id\":\"" + id + "\"" +
									 ",\"Normal\":" + df2.format (vals[0]) +
									 ",\"Emergency\":" + df2.format (vals[1]) + sTerm);
			++idx;
		}
	}

	private void LoadCurrentMap () {
		ResultSet results = queryHandler.query (szCURR);
		String lastID = "";
		double Norm = 1.0e9, Emer = 0.0;
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			String id = soln.get("?id").toString();
			if (!id.equals(lastID)) {
				if (!lastID.equals("")) {
					double [] vals = {Norm, Emer};
					mapCurrentLimits.put(lastID, vals);
				}
				Norm = 1.0e9;
				Emer = 0.0;
				lastID = id;
			}
			String dir = soln.get("?dir").toString();
			double dur = Double.parseDouble (soln.get("?dur").toString());
			double val = Double.parseDouble (soln.get("?val").toString());
			if (val < Norm) {
				Norm = val;
				if (Norm > Emer) {
					Emer = Norm;
				}
			}
			if (val > Emer) {
				Emer = val;
			}
		}
		((ResultSetCloseable)results).close();
		if (!mapCurrentLimits.containsKey (lastID)) {
			double [] vals = {Norm, Emer};
			mapCurrentLimits.put(lastID, vals);
		}
	}

	public void BuildLimitMaps (CIMImporter mdl, QueryHandler qH)  {
		queryHandler = qH;
		LoadVoltageMap();
		LoadCurrentMap();
	}
}

