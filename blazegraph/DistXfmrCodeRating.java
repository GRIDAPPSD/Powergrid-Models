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
	public int wdg;
	public String conn;
	public int ang;
	public double S; 
	public double U;
	public double r; 

	public DistXfmrCodeRating (QuerySolution soln) {
		pname = GLD_Name (soln.get("?pname").toString(), false);
		tname = GLD_Name (soln.get("?tname").toString(), false);
		wdg = new Integer (soln.get("?enum").toString()).intValue();
		conn = soln.get("?conn").toString();
		ang = new Integer (soln.get("?ang").toString()).intValue();
		S = new Double (soln.get("?ratedS").toString()).doubleValue();
		U = new Double (soln.get("?ratedU").toString()).doubleValue();
		r = new Double (soln.get("?res").toString()).doubleValue();
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + ":" + tname + " wdg=" + Integer.toString(wdg) + " conn=" + conn + " ang=" + Integer.toString(ang));
		buf.append (" U=" + df.format(U) + " S=" + df.format(S) + " r=" + df.format(r) + "\n");
		return buf.toString();
	}
}

