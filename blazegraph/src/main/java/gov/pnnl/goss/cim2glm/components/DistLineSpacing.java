package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.lang.Math.*;

public class DistLineSpacing extends DistComponent {
	public static final String szQUERY = 
		"SELECT ?name ?cable ?usage ?bundle_count ?bundle_sep ?id"+
		" (group_concat(?phs;separator=\"\\n\") as ?phases)"+
		" (group_concat(?x;separator=\"\\n\") as ?xarray)"+
		" (group_concat(?y;separator=\"\\n\") as ?yarray) WHERE {"+
		" SELECT ?name ?cable ?usage ?bundle_count ?bundle_sep ?id ?phs ?x ?y"+
		" WHERE {"+
		" ?w r:type c:WireSpacingInfo."+
		" ?w c:IdentifiedObject.name ?name."+
		" bind(strafter(str(?w),\"#_\") as ?id)."+
		" ?pos c:WirePosition.WireSpacingInfo ?w."+
		" ?pos c:WirePosition.xCoord ?x."+
		" ?pos c:WirePosition.yCoord ?y."+
		" ?pos c:WirePosition.phase ?phsraw."+
		"       bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs)"+ 
		" OPTIONAL {?w c:WireSacingInfo.isCable ?cable.}"+
		" OPTIONAL {?w c:WireSpacingInfo.phaseWireCount ?bundle_count.}"+
		" OPTIONAL {?w c:WireSpacingInfo.phaseWireSpacing ?bundle_sep.}"+
		" OPTIONAL {?w c:WireSpacingInfo.usage ?useraw."+
		"       bind(strafter(str(?useraw),\"WireUsageKind.\") as ?usage)}"+
		"} ORDER BY ?name ?phs"+
		"} GROUP BY ?name ?cable ?usage ?bundle_count ?bundle_sep ?id ORDER BY ?name";

	public String name;
	public String id;
	public String[] phases;
	public String[] xarray;
	public String[] yarray;
	public String usage;
	public int nwires;
	public boolean cable;
	public double b_sep;
	public int b_cnt;

	// only write the phasing permutations that are actually used
	private boolean glmABC;
	private boolean glmAB;
	private boolean glmAC;
	private boolean glmBC;
	private boolean glmA;
	private boolean glmB;
	private boolean glmC;
	private boolean has_neutral;
	private int nphases;

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"mRID\":\"" + id +"\"");
		buf.append ("}");
		return buf.toString();
	}

	private void FindNeutral () {
		has_neutral = false;
		nphases = nwires;
		for (int i = 0; i < nwires; i++) {
			if (phases[i].contains("N")) {
				has_neutral = true;
				--nphases;
				break;
			}
		}
	}

	public void MarkGLMPermutationsUsed (String s) {
		if (s.contains ("A") && s.contains ("B") && s.contains ("C") && nphases >= 3) {
			glmABC = true;
		} else if (s.contains ("A") && s.contains ("B") && nphases >= 2) {
			glmAB = true;
		} else if (s.contains ("A") && s.contains ("C") && nphases >= 2) {
			glmAC = true;
		} else if (s.contains ("B") && s.contains ("C") && nphases >= 2) {
			glmBC = true;
		} else if (s.contains ("A") && nphases >= 1) {
			glmA = true;
		} else if (s.contains ("B") && nphases >= 1) {
			glmB = true;
		} else if (s.contains ("C") && nphases >= 1) {
			glmC = true;
		}
	}

	public DistLineSpacing (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			phases = soln.get("?phases").toString().split("\\n");
			xarray = soln.get("?xarray").toString().split("\\n");
			yarray = soln.get("?yarray").toString().split("\\n");
			nwires = phases.length;
			cable = OptionalBoolean (soln, "?cable", false);
			usage = OptionalString (soln, "?usage", "distribution");
			b_sep = OptionalDouble (soln, "?bundle_sep", 0.0);
			b_cnt = OptionalInt (soln, "?bundle_count", 0);
			for (int i = 0; i < nwires; i++) {
				xarray[i] = df4.format (Double.parseDouble (xarray[i]));
				yarray[i] = df4.format (Double.parseDouble (yarray[i]));
			}
			FindNeutral();
		}
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " nwires=" + Integer.toString(nwires) + " cable=" + Boolean.toString(cable) + " usage=" + usage); 
		buf.append (" b_cnt=" + Integer.toString(b_cnt) + " b_sep=" + df4.format(b_sep));
		for (int i = 0; i < nwires; i++) {
				buf.append ("\n  phs=" + phases[i] + " x=" + xarray[i] + " y=" + yarray[i]);
		}
		return buf.toString();
	}

	public String GetDSS() {
//		int nphases = nwires;  // TODO - remove this block if FindNeutral and private vars work
//		if (phases[nwires-1].equals("N")) {
//			--nphases;
//		}
		int i;

		StringBuilder buf = new StringBuilder("new LineSpacing." + name + " nconds=" + Integer.toString(nwires) +
																					 " nphases=" + Integer.toString(nphases) + " units=m\n");
		buf.append ("~ x=[");
		for (i = 0; i < nwires; i++) {
			buf.append (xarray[i]);
			if (i+1 < nwires) {
				buf.append (",");
			}
		}
		buf.append ("]\n~ h=[");
		for (i = 0; i < nwires; i++) {
			buf.append (yarray[i]);
			if (i+1 < nwires) {
				buf.append (",");
			}
		}
		buf.append ("]\n");
		return buf.toString();
	}

	private double WireSeparation (int i, int j) {
		double dx = Double.parseDouble (xarray[i]) - Double.parseDouble (xarray[j]);
		double dy = Double.parseDouble (yarray[i]) - Double.parseDouble (yarray[j]);
		return Math.sqrt (dx * dx + dy * dy);
	}

	private void AppendPermutation (StringBuilder buf, String perm) {
		buf.append("object line_spacing {\n");
		if (has_neutral) {
			buf.append("  name \"spc_" + name + "_" + perm + "N\";\n");
		} else {
			buf.append("  name \"spc_" + name + "_" + perm + "\";\n");
		}

		int idxA = 0;
		int idxB = 1;
		int idxC = 2;
		if (nphases == 1) {
			idxB = 0;
			idxC = 0;
		} else if (nphases == 2) {
			if (perm.contains ("AC")) {
				idxC = 1;
			} else if (perm.contains ("BC")) {
				idxB = 0;
				idxC = 1;
			}
		}

		if (perm.contains("A")) {
			if (perm.contains ("B")) {
				buf.append ("  distance_AB " + df4.format(gFTperM * WireSeparation (idxA, idxB)) + ";\n");
			}
			if (perm.contains ("C")) {
				buf.append ("  distance_AC " + df4.format(gFTperM * WireSeparation (idxA, idxC)) + ";\n");
			}
			if (has_neutral) {
				buf.append ("  distance_AN " + df4.format(gFTperM * WireSeparation (idxA, nwires-1)) + ";\n");
			}
		}
		if (perm.contains ("B")) {
			if (perm.contains ("C")) {
				buf.append ("  distance_BC " + df4.format(gFTperM * WireSeparation (idxB, idxC)) + ";\n");
			}
			if (has_neutral) {
				buf.append ("  distance_BN " + df4.format(gFTperM * WireSeparation (idxB, nwires-1)) + ";\n");
			}
		}
		if (perm.contains ("C")) {
			if (has_neutral) {
				buf.append ("  distance_CN " + df4.format(gFTperM * WireSeparation (idxC, nwires-1)) + ";\n");
			}
		}
		buf.append("}\n");
	}

	public String GetGLM() {
//		if (nphases == 3) {
//			MarkGLMPermutationsUsed("ABC");
//		} else if (nphases == 2) {
//			MarkGLMPermutationsUsed("AB");
//			MarkGLMPermutationsUsed("AC");
//			MarkGLMPermutationsUsed("BC");
//		} else if (nphases == 1) {
//			MarkGLMPermutationsUsed("A");
//			MarkGLMPermutationsUsed("B");
//			MarkGLMPermutationsUsed("C");
//		}
//
		StringBuilder buf = new StringBuilder ("");
		if (glmABC) AppendPermutation (buf, "ABC");
		if (glmAB) AppendPermutation (buf, "AB");
		if (glmAC) AppendPermutation (buf, "AC");
		if (glmBC) AppendPermutation (buf, "BC");
		if (glmA) AppendPermutation (buf, "A");
		if (glmB) AppendPermutation (buf, "B");
		if (glmC) AppendPermutation (buf, "C");

		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

