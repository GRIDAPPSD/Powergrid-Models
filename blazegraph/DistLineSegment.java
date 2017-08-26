//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;

public abstract class DistLineSegment extends DistComponent {
	public String name;
	public String bus1;
	public String bus2;
	public String phases;
	public double len;

	public abstract String LabelString();

	public String GetJSONSymbols(HashMap<String,DistCoordinates> map) {
		DistCoordinates pt1 = map.get("ACLineSegment:" + name + ":1");
		DistCoordinates pt2 = map.get("ACLineSegment:" + name + ":2");
		StringBuilder lbl_phs = new StringBuilder ();
		if (phases.contains("A")) lbl_phs.append("A");
		if (phases.contains("B")) lbl_phs.append("B");
		if (phases.contains("C")) lbl_phs.append("C");
		if (phases.contains("s")) lbl_phs.append("S");
		if (lbl_phs.length() < 1) lbl_phs.append("ABC");

		DecimalFormat df = new DecimalFormat("#0.00");
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name + "\"");
		buf.append (",\"from\":\"" + bus1 + "\"");
		buf.append (",\"to\":\"" + bus2 + "\"");
		buf.append (",\"phases\":\"" + lbl_phs.toString() +"\"");
		buf.append (",\"length\":" + df.format(len * 3.2809));
		buf.append (",\"configuration\":\"" + LabelString() + "\"");
		buf.append (",\"x1\":" + Double.toString(pt1.x));
		buf.append (",\"y1\":" + Double.toString(pt1.y));
		buf.append (",\"x2\":" + Double.toString(pt2.x));
		buf.append (",\"y2\":" + Double.toString(pt2.y));
		buf.append ("}");
		return buf.toString();
	}
}

