//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistSequenceMatrix extends DistComponent {
	static final String szQUERY = 
		"SELECT ?name ?r1 ?x1 ?b1 ?r0 ?x0 ?b0 WHERE {"+
		" ?s r:type c:PerLengthSequenceImpedance."+
		" ?s c:IdentifiedObject.name ?name."+
		" ?s c:PerLengthSequenceImpedance.r ?r."+
		" ?s c:PerLengthSequenceImpedance.x ?x."+
		" ?s c:PerLengthSequenceImpedance.b ?b."+
		" ?s c:PerLengthSequenceImpedance.r0 ?r0."+
		" ?s c:PerLengthSequenceImpedance.x0 ?x0."+
		" ?s c:PerLengthSequenceImpedance.b0 ?b0"+
		"} ORDER BY ?name";

	public String name;
	public double r1;
	public double x1;
	public double b1;
	public double r0;
	public double x0;
	public double b0;

	public DistSequenceMatrix (QuerySolution soln) {
		name = GLD_Name (soln.get("?name").toString(), false);
		r1 = new Double (soln.get("?r").toString()).doubleValue();
		x1 = new Double (soln.get("?x").toString()).doubleValue();
		b1 = new Double (soln.get("?b").toString()).doubleValue();
		r0 = new Double (soln.get("?r0").toString()).doubleValue();
		x0 = new Double (soln.get("?x0").toString()).doubleValue();
		b0 = new Double (soln.get("?b0").toString()).doubleValue();
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " r1=" + df.format(r1) + " x1=" + df.format(x1) + " b1=" + df.format(b1));
		buf.append (" r0=" + df.format(r0) + " x0=" + df.format(x0) + " b0=" + df.format(b0));
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

