//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistXfmrCodeRating extends DistComponent {
	static final String szQUERY = 
		"SELECT ?pname ?tname ?enum ?ratedS ?ratedU ?conn ?ang ?res WHERE {"+
		" ?p r:type c:PowerTransformerInfo."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?t c:TransformerTankInfo.PowerTransformerInfo ?p."+
		" ?t c:IdentifiedObject.name ?tname."+
		" ?e c:TransformerEndInfo.TransformerTankInfo ?t."+
		" ?e c:TransformerEndInfo.endNumber ?enum."+
		" ?e c:TransformerEndInfo.ratedS ?ratedS."+
		" ?e c:TransformerEndInfo.ratedU ?ratedU."+
		" ?e c:TransformerEndInfo.r ?res."+
		" ?e c:TransformerEndInfo.phaseAngleClock ?ang."+
		" ?e c:TransformerEndInfo.connectionKind ?connraw."+
		"       		bind(strafter(str(?connraw),\"WindingConnection.\") as ?conn)"+
		"} ORDER BY ?pname ?tname ?enum";

	public String pname;
	public String tname;
	public int[] wdg;
	public String[] conn;
	public int[] ang;
	public double[] ratedS; 
	public double[] ratedU;
	public double[] r;
	public int size;

	private void SetSize (String p, String t) {
		size = 1;
		String szCount = "SELECT (count (?p) as ?count) WHERE {"+
			" ?p r:type c:PowerTransformerInfo."+
			" ?p c:IdentifiedObject.name \"" + p + "\"."+
			" ?t c:TransformerTankInfo.PowerTransformerInfo ?p."+
			" ?t c:IdentifiedObject.name \"" + t + "\"."+
			" ?e c:TransformerEndInfo.TransformerTankInfo ?t"+
			"}";
		ResultSet results = RunQuery (szCount);
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			size = soln.getLiteral("?count").getInt();
		}
		wdg = new int[size];
		conn = new String[size];
		ang = new int[size];
		ratedS = new double[size];
		ratedU = new double[size];
		r = new double[size];
	}

	public DistXfmrCodeRating (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			String p = soln.get("?pname").toString();
			String t = soln.get("?tname").toString();
			pname = GLD_Name (p, false);
			tname = GLD_Name (t, false);
			SetSize (p, t);
			for (int i = 0; i < size; i++) {
				wdg[i] = Integer.parseInt (soln.get("?enum").toString());
				conn[i] = soln.get("?conn").toString();
				ang[i] = Integer.parseInt (soln.get("?ang").toString());
				ratedS[i] = Double.parseDouble (soln.get("?ratedS").toString());
				ratedU[i] = Double.parseDouble (soln.get("?ratedU").toString());
				r[i] = Double.parseDouble (soln.get("?res").toString());
				if ((i + 1) < size) {
					soln = results.next();
				}
			}
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + ":" + tname);
		for (int i = 0; i < size; i++) {
			buf.append ("\n  wdg=" + Integer.toString(wdg[i]) + " conn=" + conn[i] + " ang=" + Integer.toString(ang[i]));
			buf.append (" U=" + df.format(ratedU[i]) + " S=" + df.format(ratedS[i]) + " r=" + df.format(r[i]));
		}
		return buf.toString();
	}

	public String GetKey() {
		return pname + ":" + tname;
	}
}

