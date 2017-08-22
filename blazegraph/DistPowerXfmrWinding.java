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
	public String bus;
	public String phs;
	public String vgrp;
	public String conn;
	public double ratedU;
	public double ratedS;
	public double r;
	public int wdg;
	public int ang;
	public boolean grounded;
	public double rg;
	public double xg;

	public DistPowerXfmrWinding (QuerySolution soln) {
		name = GLD_Name (soln.get("?pname").toString(), false);
		bus = GLD_Name (soln.get("?bus").toString(), true);
		phs = "ABC";
		vgrp = soln.get("?vgrp").toString();
		conn = soln.get("?conn").toString();
		ratedU = Double.parseDouble (soln.get("?ratedU").toString());
		ratedS = Double.parseDouble (soln.get("?ratedS").toString());
		r = Double.parseDouble (soln.get("?r").toString());
		wdg = Integer.parseInt (soln.get("?enum").toString());
		ang = Integer.parseInt (soln.get("?ang").toString());
		grounded = Boolean.parseBoolean (soln.get("?grounded").toString());
		rg = OptionalDouble (soln, "?rground", 0.0);
		xg = OptionalDouble (soln, "?xground", 0.0);
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + ":" + Integer.toString(wdg) + " on " + bus + ":" + phs);
		buf.append (" vgrp=" + vgrp + " conn=" + conn + " ang=" + Integer.toString(ang));
		buf.append (" U=" + df.format(ratedU) + " S=" + df.format(ratedS) + " r=" + df.format(r));
		buf.append (" grounded=" + Boolean.toString(grounded) + " rg=" + df.format(rg) + " xg=" + df.format(xg));
		return buf.toString();
	}

	public String GetKey() {
		return name + ":" + bus;
	}
}

