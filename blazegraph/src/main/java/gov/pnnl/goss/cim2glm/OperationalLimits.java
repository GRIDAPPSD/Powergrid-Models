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

import gov.pnnl.goss.cim2glm.queryhandler.QueryHandler;
import gov.pnnl.goss.cim2glm.queryhandler.impl.HTTPBlazegraphQueryHandler;

public class OperationalLimits extends Object {
	QueryHandler queryHandler;

	private static final String xnsCIM = "http://iec.ch/TC57/2009/CIM-schema-cim14#";
	private static final String xnsRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

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

	private void LoadVoltageMap () {
		ResultSet results = queryHandler.query (szVOLT);
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			String id = soln.get("?id").toString();
			String dir = soln.get("?dir").toString();
			double dur = Double.parseDouble (soln.get("?dur").toString());
			double val = Double.parseDouble (soln.get("?val").toString());
			System.out.println (id + ":" + dir + ":" + Double.toString(dur) + ":" + Double.toString(val));
		}
		((ResultSetCloseable)results).close();
	}

	public void BuildLimitMaps (CIMImporter mdl, QueryHandler qH)  {
		queryHandler = qH;
		LoadVoltageMap();
//		LoadCurrentMap();
	}
}

