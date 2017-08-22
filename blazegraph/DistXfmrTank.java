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
	public String bus;
	public String phs;
	public String vgrp;
	public String tankinfo;
	public double rg;
	public double xg;
	public int wdg;
	public boolean grounded;

	public DistXfmrTank (QuerySolution soln) {
		pname = GLD_Name (soln.get("?pname").toString(), false);
		tname = GLD_Name (soln.get("?tname").toString(), false);
		bus = GLD_Name (soln.get("?bus").toString(), true);
		phs = soln.get("?phs").toString();
		vgrp = soln.get("?vgrp").toString();
		tankinfo = GLD_Name (soln.get("?xfmrcode").toString(), false);
		rg = OptionalDouble (soln, "?rground", 0.0);
		xg = OptionalDouble (soln, "?xground", 0.0);
		wdg = Integer.parseInt (soln.get("?enum").toString());
		grounded = Boolean.parseBoolean (soln.get("?grounded").toString());
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + ":" + tname + ":" + Integer.toString(wdg) + " on " + bus + ":" + phs);
		buf.append (" vgrp=" + vgrp + " tankinfo=" + tankinfo);
		buf.append (" grounded=" + Boolean.toString(grounded) + " rg=" + df.format(rg) + " xg=" + df.format(xg));
		return buf.toString();
	}

	public String GetKey() {
		return pname + ":" + tname + ":" + bus;
	}
}

