//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistOverheadWire extends DistComponent {
	static final String szQUERY =  
		"SELECT ?name ?rad ?corerad ?gmr ?rdc ?r25 ?r50 ?r75 ?amps ?ins ?insmat ?insthick WHERE {"+
		" ?w r:type c:OverheadWireInfo."+
		" ?w c:IdentifiedObject.name ?name."+
		" ?w c:WireInfo.radius ?rad."+
		" ?w c:WireInfo.gmr ?gmr."+
		" OPTIONAL {?w c:WireInfo.rDC20 ?rdc.}"+
		" OPTIONAL {?w c:WireInfo.rAC25 ?r25.}"+
		" OPTIONAL {?w c:WireInfo.rAC50 ?r50.}"+
		" OPTIONAL {?w c:WireInfo.rAC75 ?r75.}"+
		" OPTIONAL {?w c:WireInfo.coreRadius ?corerad.}"+
		" OPTIONAL {?w c:WireInfo.ratedCurrent ?amps.}"+
		" OPTIONAL {?w c:WireInfo.insulationMaterial ?insraw."+
		"       bind(strafter(str(?insraw),\"WireInsulationKind.\") as ?insmat)}"+
		" OPTIONAL {?w c:WireInfo.insulated ?ins.}"+
		" OPTIONAL {?w c:WireInfo.insulationThickness ?insthick.}"+
		"} ORDER BY ?name";

	public String name;
	public double rad;
	public double gmr;
	public double rdc;
	public double r25;
	public double r50;
	public double r75;
	public double corerad; 
	public double amps;
	public double insthick;
	public boolean ins;
	public String insmat;

	public DistOverheadWire (QuerySolution soln) {
		name = GLD_Name (soln.get("?name").toString(), false);
		rad = new Double (soln.get("?rad").toString()).doubleValue();
		gmr = new Double (soln.get("?gmr").toString()).doubleValue();
		rdc = OptionalDouble (soln, "?rdc", 0.0);
		r25 = OptionalDouble (soln, "?r25", 0.0);
		r50 = OptionalDouble (soln, "?r50", 0.0);
		r75 = OptionalDouble (soln, "?r75", 0.0);
		corerad = OptionalDouble (soln, "?corerad", 0.0);
		amps = OptionalDouble (soln, "?amps", 0.0);
		insthick = OptionalDouble (soln, "?insthick", 0.0);
		ins = OptionalBoolean (soln, "?ins", false);
		insmat = OptionalString (soln, "?insmat", "N/A");
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " rad=" + df.format(rad) + " gmr=" + df.format(gmr) + " rdc=" + df.format(rdc)); 
		buf.append (" r25=" + df.format(r25) + " r50=" + df.format(r50) + " r75=" + df.format(r75)); 
		buf.append (" corerad=" + df.format(corerad) + " amps=" + df.format(amps)); 
		buf.append (" ins=" + Boolean.toString(ins) + " insmat=" + insmat + " insthick=" + df.format(insthick)); 
		buf.append ("\n");
		return buf.toString();
	}
}

