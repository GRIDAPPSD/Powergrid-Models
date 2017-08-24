//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistXfmrTank extends DistComponent {
	static final String szQUERY =
		"SELECT ?pname ?tname ?xfmrcode ?vgrp ?enum ?bus ?phs ?grounded ?rground ?xground WHERE {"+
		" ?p r:type c:PowerTransformer."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?p c:PowerTransformer.vectorGroup ?vgrp."+
		" ?t c:TransformerTank.PowerTransformer ?p."+
		" ?t c:IdentifiedObject.name ?tname."+
		" ?asset c:Asset.PowerSystemResources ?t."+
		" ?asset c:Asset.AssetInfo ?inf."+
		" ?inf c:IdentifiedObject.name ?xfmrcode."+
		" ?end c:TransformerTankEnd.TransformerTank ?t."+
		" ?end c:TransformerTankEnd.phases ?phsraw."+
		"  bind(strafter(str(?phsraw),\"PhaseCode.\") as ?phs)"+
		" ?end c:TransformerEnd.endNumber ?enum."+
		" ?end c:TransformerEnd.grounded ?grounded."+
		" OPTIONAL {?end c:TransformerEnd.rground ?rground.}"+
		" OPTIONAL {?end c:TransformerEnd.xground ?xground.}"+
		" ?end c:TransformerEnd.Terminal ?trm."+
		" ?trm c:Terminal.ConnectivityNode ?cn."+ 
		" ?cn c:IdentifiedObject.name ?bus"+
		"}"+
		" ORDER BY ?pname ?tname ?enum"		;

	public String pname;
	public String tname;
	public String tankinfo;
	public String vgrp;
	public String[] bus;
	public String[] phs;
	public double[] rg;
	public double[] xg;
	public int[] wdg;
	public boolean[] grounded;

	public int size;

	private void SetSize (String p, String t) {
		size = 1;
		String szCount = "SELECT (count (?p) as ?count) WHERE {"+
			" ?p r:type c:PowerTransformer."+
			" ?p c:IdentifiedObject.name \"" + p + "\"."+
			" ?t c:TransformerTank.PowerTransformer ?p."+
			" ?t c:IdentifiedObject.name \"" + t + "\"."+
			" ?asset c:Asset.PowerSystemResources ?t."+
			" ?asset c:Asset.AssetInfo ?inf."+
			" ?inf c:IdentifiedObject.name ?xfmrcode."+
			" ?end c:TransformerTankEnd.TransformerTank ?t"+
			"}";
		ResultSet results = RunQuery (szCount);
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			size = soln.getLiteral("?count").getInt();
		}
		bus = new String[size];
		phs = new String[size];
		wdg = new int[size];
		grounded = new boolean[size];
		rg = new double[size];
		xg = new double[size];
	}

	public DistXfmrTank (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			String p = soln.get("?pname").toString();
			String t = soln.get("?tname").toString();
			pname = GLD_Name (p, false);
			tname = GLD_Name (t, false);
			tankinfo = GLD_Name (soln.get("?xfmrcode").toString(), false);
			vgrp = soln.get("?vgrp").toString();
			SetSize (p, t);
			for (int i = 0; i < size; i++) {
				bus[i] = GLD_Name (soln.get("?bus").toString(), true);
				phs[i] = soln.get("?phs").toString();
				rg[i] = OptionalDouble (soln, "?rground", 0.0);
				xg[i] = OptionalDouble (soln, "?xground", 0.0);
				wdg[i] = Integer.parseInt (soln.get("?enum").toString());
				grounded[i] = Boolean.parseBoolean (soln.get("?grounded").toString());
				if ((i + 1) < size) {
					soln = results.next();
				}
			}
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + ":" + tname + " tankinfo=" + tankinfo + " vgrp=" + vgrp);
		for (int i = 0; i < size; i++) {
			buf.append ("\n  " + Integer.toString(wdg[i]) + " bus=" + bus[i] + " phs=" + phs[i]);
			buf.append (" grounded=" + Boolean.toString(grounded[i]) + " rg=" + df.format(rg[i]) + " xg=" + df.format(xg[i]));
		}
		return buf.toString();
	}

	public String GetKey() {
		return pname + ":" + tname + ":" + bus;
	}
}

