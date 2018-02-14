package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistFeeder extends DistComponent {
	public static final String szQUERY = 
		"SELECT ?feeder ?fid ?station ?sid ?subregion ?sgrid ?region ?rgnid WHERE {"+
		"?s r:type c:Feeder."+
		"?s c:IdentifiedObject.name ?feeder."+
		"?s c:IdentifiedObject.mRID ?fidraw."+
		"  bind(strafter(str(?fidraw),\"_\") as ?fid)"+
		"?s c:Feeder.NormalEnergizingSubstation ?sub."+
		"?sub c:IdentifiedObject.name ?station."+
		"?sub c:IdentifiedObject.mRID ?sidraw."+
		"  bind(strafter(str(?sidraw),\"_\") as ?sid)"+
		"?sub c:Substation.Region ?sgr."+
		"?sgr c:IdentifiedObject.name ?subregion."+
		"?sgr c:IdentifiedObject.mRID ?sgridraw."+
		"  bind(strafter(str(?sgridraw),\"_\") as ?sgrid)"+
		"?sgr c:SubGeographicalRegion.Region ?rgn."+
		"?rgn c:IdentifiedObject.name ?region."+
		"?rgn c:IdentifiedObject.mRID ?rgnidraw."+
		"  bind(strafter(str(?rgnidraw),\"_\") as ?rgnid)"+
		"}"+
		" ORDER by ?station ?feeder";

	public String feederName;
	public String feederID;
	public String substationName;
	public String substationID;
	public String subregionName;
	public String subregionID;
	public String regionName;
	public String regionID;

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + feederName +"\"");
		buf.append (",\"mRID\":\"" + feederID +"\"");
		buf.append (",\"substationName\":\"" + substationName + "\"");
		buf.append (",\"substationID\":\"" + substationID + "\"");
		buf.append (",\"subregionName\":\"" + subregionName + "\"");
		buf.append (",\"subregionID\":\"" + subregionID + "\"");
		buf.append (",\"regionName\":\"" + regionName + "\"");
		buf.append (",\"regionID\":\"" + regionID + "\"");
		buf.append("}");
		return buf.toString();
	}

	public DistFeeder (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			feederName = SafeName (soln.get("?feeder").toString());
			feederID = soln.get("?fid").toString();
			substationName = SafeName (soln.get("?station").toString());
			substationID = soln.get("?sid").toString();
			subregionName = SafeName (soln.get("?subregion").toString());
			subregionID = soln.get("?sgrid").toString();
			regionName = SafeName (soln.get("?region").toString());
			regionID = soln.get("?rgnid").toString();
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (feederName + ":" + feederID + "\n");
		buf.append ("  " + substationName + ":" + substationID + "\n");
		buf.append ("  " + subregionName + ":" + subregionID + "\n");
		buf.append ("  " + regionName + ":" + regionID + "\n");
		return buf.toString();
	}

	public String GetKey() {
		return feederName;
	}
}

