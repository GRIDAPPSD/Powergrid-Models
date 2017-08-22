//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistPhaseMatrix extends DistComponent {
	static final String szQUERY = 
		"SELECT ?name ?cnt ?seq ?r ?x ?b WHERE {"+
		" ?s r:type c:PerLengthPhaseImpedance."+
		" ?s c:IdentifiedObject.name ?name."+
		" ?s c:PerLengthPhaseImpedance.conductorCount ?cnt."+
		" ?elm c:PhaseImpedanceData.PhaseImpedance ?s."+
		" ?elm c:PhaseImpedanceData.sequenceNumber ?seq."+
		" ?elm c:PhaseImpedanceData.r ?r."+
		" ?elm c:PhaseImpedanceData.x ?x."+
		" ?elm c:PhaseImpedanceData.b ?b"+
		"} ORDER BY ?name ?seq";

	public String name;
	public int cnt; 
	public int seq;
	public double r;
	public double x;
	public double b;

	public DistPhaseMatrix (QuerySolution soln) {
		name = GLD_Name (soln.get("?name").toString(), false);
		cnt = new Integer (soln.get("?cnt").toString()).intValue();
		seq = new Integer (soln.get("?seq").toString()).intValue();
		r = new Double (soln.get("?r").toString()).doubleValue();
		x = new Double (soln.get("?x").toString()).doubleValue();
		b = new Double (soln.get("?b").toString()).doubleValue();
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " " + Integer.toString(seq) + ":" + Integer.toString(cnt) + " r=" + df.format(r) + " x=" + df.format(x) + " b=" + df.format(b));
		return buf.toString();
	}

	public String GetKey() {
		return name + ":" + Integer.toString(seq);
	}
}

