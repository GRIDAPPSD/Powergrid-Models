//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistPowerXfmrWinding extends DistComponent {
	static final String szQUERY = 
		"SELECT ?pname ?vgrp ?enum ?bus ?conn ?ratedS ?ratedU ?r ?ang ?grounded ?rground ?xground WHERE {"+
		" ?p r:type c:PowerTransformer."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?p c:PowerTransformer.vectorGroup ?vgrp."+
		" ?end c:PowerTransformerEnd.PowerTransformer ?p."+
		" ?end c:TransformerEnd.endNumber ?enum."+
		" ?end c:PowerTransformerEnd.ratedS ?ratedS."+
		" ?end c:PowerTransformerEnd.ratedU ?ratedU."+
		" ?end c:PowerTransformerEnd.r ?r."+
		" ?end c:PowerTransformerEnd.phaseAngleClock ?ang."+
		" ?end c:PowerTransformerEnd.connectionKind ?connraw."+  
		"  bind(strafter(str(?connraw),\"WindingConnection.\") as ?conn)"+
		" ?end c:TransformerEnd.grounded ?grounded."+
		" OPTIONAL {?end c:TransformerEnd.rground ?rground.}"+
		" OPTIONAL {?end c:TransformerEnd.xground ?xground.}"+
		" ?end c:TransformerEnd.Terminal ?trm."+
		" ?trm c:Terminal.ConnectivityNode ?cn. "+
		" ?cn c:IdentifiedObject.name ?bus"+
		"}"+
		" ORDER BY ?pname ?enum"		;

	public String name;
	public String vgrp;
	public String[] bus;
	public String[] conn;
	public double[] ratedU;
	public double[] ratedS;
	public double[] r;
	public int[] wdg;
	public int[] ang;
	public boolean[] grounded;
	public double[] rg;
	public double[] xg;
	public int size;

	private void SetSize (String pname) {
		size = 1;
		String szCount = "SELECT (count (?p) as ?count) WHERE {"+
			" ?p r:type c:PowerTransformer."+
			" ?p c:IdentifiedObject.name \"" + pname + "\"."+
			" ?end c:PowerTransformerEnd.PowerTransformer ?p."+
			"}";
		ResultSet results = RunQuery (szCount);
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			size = soln.getLiteral("?count").getInt();
		}
		bus = new String[size];
		conn = new String[size];
		ratedU = new double[size];
		ratedS = new double[size];
		r = new double[size];
		wdg = new int[size];
		ang = new int[size];
		grounded = new boolean[size];
		rg = new double[size];
		xg = new double[size];
	}

	public DistPowerXfmrWinding (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			String pname = soln.get("?pname").toString();
			name = GLD_Name (pname, false);
			vgrp = soln.get("?vgrp").toString();
			SetSize (pname);
			for (int i = 0; i < size; i++) {
				bus[i] = GLD_Name (soln.get("?bus").toString(), true);
				conn[i] = soln.get("?conn").toString();
				ratedU[i] = Double.parseDouble (soln.get("?ratedU").toString());
				ratedS[i] = Double.parseDouble (soln.get("?ratedS").toString());
				r[i] = Double.parseDouble (soln.get("?r").toString());
				wdg[i] = Integer.parseInt (soln.get("?enum").toString());
				ang[i] = Integer.parseInt (soln.get("?ang").toString());
				grounded[i] = Boolean.parseBoolean (soln.get("?grounded").toString());
				rg[i] = OptionalDouble (soln, "?rground", 0.0);
				xg[i] = OptionalDouble (soln, "?xground", 0.0);
				if ((i + 1) < size) {
					soln = results.next();
				}
			}
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " " + vgrp);
		for (int i = 0; i < size; i++) {
			buf.append("\n  bus=" + bus[i] + " conn=" + conn[i] + " ang=" + Integer.toString(ang[i]));
			buf.append (" U=" + df.format(ratedU[i]) + " S=" + df.format(ratedS[i]) + " r=" + df.format(r[i]));
			buf.append (" grounded=" + Boolean.toString(grounded[i]) + " rg=" + df.format(rg[i]) + " xg=" + df.format(xg[i]));
		}
		return buf.toString();
	}

	public String GetKey() {
		return name + ":" + bus;
	}
}

