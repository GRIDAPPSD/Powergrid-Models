package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import org.apache.commons.math3.complex.Complex;

public class DistLinesInstanceZ extends DistLineSegment {
	public static final String szQUERY = 
		"SELECT ?name ?id ?basev (group_concat(distinct ?bus;separator=\"\\n\") as ?buses) ?len ?r ?x ?b ?r0 ?x0 ?b0 WHERE {"+
		" ?s r:type c:ACLineSegment."+
		" ?s c:IdentifiedObject.name ?name."+
		" bind(strafter(str(?s),\"#_\") as ?id)."+
		" ?s c:ConductingEquipment.BaseVoltage ?bv."+
		" ?bv c:BaseVoltage.nominalVoltage ?basev."+
		" ?s c:Conductor.length ?len."+
		" ?s c:ACLineSegment.r ?r."+
		" ?s c:ACLineSegment.x ?x."+
		" OPTIONAL {?s c:ACLineSegment.b ?b.}"+
		" OPTIONAL {?s c:ACLineSegment.r0 ?r0.}"+
		" OPTIONAL {?s c:ACLineSegment.x0 ?x0.}"+
		" OPTIONAL {?s c:ACLineSegment.b0 ?b0.}"+
		" ?t c:Terminal.ConductingEquipment ?s."+
		" ?t c:Terminal.ConnectivityNode ?cn. "+
		" ?cn c:IdentifiedObject.name ?bus"+
		"}"+
		" GROUP BY ?name ?id ?basev ?len ?r ?x ?b ?r0 ?x0 ?b0"+
		" ORDER BY ?name";

	public double r1; 
	public double x1; 
	public double b1; 
	public double r0; 
	public double x0; 
	public double b0; 

	public DistLinesInstanceZ (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			String[] buses = soln.get("?buses").toString().split("\\n");
			bus1 = SafeName(buses[0]); 
			bus2 = SafeName(buses[1]); 
			phases = "ABC";
			len = Double.parseDouble (soln.get("?len").toString());
			basev = Double.parseDouble (soln.get("?basev").toString());
			r1 = Double.parseDouble (soln.get("?r").toString());
			x1 = Double.parseDouble (soln.get("?x").toString());
			b1 = OptionalDouble (soln, "?b", 0.0);
			r0 = OptionalDouble (soln, "?r0", 0.0);
			x0 = OptionalDouble (soln, "?x0", 0.0);
			b0 = OptionalDouble (soln, "?b0", 0.0);
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " from " + bus1 + " to " + bus2 + " phases=" + phases + " basev=" + df4.format(basev) + " len=" + df4.format(len));
		buf.append (" r1=" + df4.format(r1) + " x1=" + df4.format(x1) + " b1=" + df4.format(b1));
		buf.append (" r0=" + df4.format(r0) + " x0=" + df4.format(x0) + " b0=" + df4.format(b0));
		return buf.toString();
	}

	public String GetGLM() {
		StringBuilder buf = new StringBuilder ();
		AppendSharedGLMAttributes (buf, name);

		String seqZs = CFormat (new Complex ((r0 + 2.0 * r1) / 3.0, (x0 + 2.0 * x1) / 3.0));
		String seqZm = CFormat (new Complex ((r0 - r1) / 3.0, (x0 - x1) / 3.0));
		String seqCs = df4.format(1.0e9 * (b0 + 2.0 * b1) / 3.0 / gOMEGA);
		String seqCm = df4.format(1.0e9 * (b0 - b1) / 3.0 / gOMEGA);

		buf.append ("object line_configuration {\n");
		buf.append ("  name \"lcon_" + name + "_ABC\";\n");
		for (int i = 1; i <= 3; i++) {
			for (int j = 1; j <= 3; j++) {
				String indices = Integer.toString(i) + Integer.toString(j) + " ";
				if (i == j) {
					buf.append ("  z" + indices + seqZs + ";\n");
					buf.append ("  c" + indices + seqCs + ";\n");
				} else {
					buf.append ("  z" + indices + seqZm + ";\n");
					buf.append ("  c" + indices + seqCm + ";\n");
				}
			}
		}
		buf.append ("}\n");
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}

	public String LabelString() {
		return "seqZ";
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder ("new Line." + name);

		buf.append (" phases=" + Integer.toString(DSSPhaseCount(phases, false)) + 
								" bus1=" + DSSBusPhases(bus1, phases) + " bus2=" + DSSBusPhases (bus2, phases) + 
								" length=" + df1.format(len * gFTperM) + " units=ft\n");

		return buf.toString();
	}
}

