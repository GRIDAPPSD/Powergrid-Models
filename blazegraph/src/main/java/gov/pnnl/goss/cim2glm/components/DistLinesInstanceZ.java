package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import org.apache.commons.math3.complex.Complex;

public class DistLinesInstanceZ extends DistLineSegment {
	public static final String szQUERY = 
		"SELECT ?name ?id ?basev ?bus1 ?bus2 ?len ?r ?x ?b ?r0 ?x0 ?b0 ?fdrid WHERE {"+
		" ?s r:type c:ACLineSegment."+
		" ?s c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?s c:IdentifiedObject.name ?name."+
		" bind(strafter(str(?s),\"#\") as ?id)."+
		" ?s c:ConductingEquipment.BaseVoltage ?bv."+
		" ?bv c:BaseVoltage.nominalVoltage ?basev."+
		" ?s c:Conductor.length ?len."+
		" ?s c:ACLineSegment.r ?r."+
		" ?s c:ACLineSegment.x ?x."+
		" OPTIONAL {?s c:ACLineSegment.bch ?b.}"+
		" OPTIONAL {?s c:ACLineSegment.r0 ?r0.}"+
		" OPTIONAL {?s c:ACLineSegment.x0 ?x0.}"+
		" OPTIONAL {?s c:ACLineSegment.b0ch ?b0.}"+
		" ?t1 c:Terminal.ConductingEquipment ?s."+
		" ?t1 c:Terminal.ConnectivityNode ?cn1."+
		" ?t1 c:ACDCTerminal.sequenceNumber \"1\"."+
		" ?cn1 c:IdentifiedObject.name ?bus1."+
		" ?t2 c:Terminal.ConductingEquipment ?s."+
		" ?t2 c:Terminal.ConnectivityNode ?cn2."+
		" ?t2 c:ACDCTerminal.sequenceNumber \"2\"."+
		" ?cn2 c:IdentifiedObject.name ?bus2"+
		"}"+
		" GROUP BY ?name ?id ?basev ?bus1 ?bus2 ?len ?r ?x ?b ?r0 ?x0 ?b0 ?fdrid"+
		" ORDER BY ?name";

	public double r1; 
	public double x1; 
	public double b1; 
	public double r0; 
	public double x0; 
	public double b0; 

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"mRID\":\"" + id +"\"");
		buf.append ("}");
		return buf.toString();
	}

	public DistLinesInstanceZ (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			bus1 = SafeName (soln.get("?bus1").toString()); 
			bus2 = SafeName (soln.get("?bus2").toString()); 
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
		AppendSharedGLMAttributes (buf, name, false);

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
								" length=" + df1.format(len * gFTperM) + " units=ft" +
								" r1=" + df6.format(r1) + " x1=" + df6.format(x1) + " c1=" + df6.format(1.0e9 * b1 / gOMEGA) + 
								" r0=" + df6.format(r0) + " x0=" + df6.format(x0) + " c0=" + df6.format(1.0e9 * b0 / gOMEGA) + "\n");

		return buf.toString();
	}
}

