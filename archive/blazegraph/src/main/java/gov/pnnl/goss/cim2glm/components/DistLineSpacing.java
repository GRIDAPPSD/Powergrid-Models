package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.lang.Math.*;
import java.util.HashMap;
import java.util.HashSet;

public class DistLineSpacing extends DistComponent {
	public static final String szQUERY = 
		"SELECT DISTINCT ?name ?cable ?usage ?bundle_count ?bundle_sep ?id ?seq ?x ?y"+
		" WHERE {"+
		" ?eq r:type c:ACLineSegment."+
		" ?eq c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?eq c:ACLineSegment.WireSpacingInfo ?w."+
		" ?w c:IdentifiedObject.name ?name."+
		"   bind(strafter(str(?w),\"#\") as ?id)."+
		" ?pos c:WirePosition.WireSpacingInfo ?w."+
		" ?pos c:WirePosition.xCoord ?x."+
		" ?pos c:WirePosition.yCoord ?y."+
		" ?pos c:WirePosition.sequenceNumber ?seq."+
		" ?w c:WireSpacingInfo.isCable ?cable."+
		" ?w c:WireSpacingInfo.phaseWireCount ?bundle_count."+
		" ?w c:WireSpacingInfo.phaseWireSpacing ?bundle_sep."+
		" ?w c:WireSpacingInfo.usage ?useraw."+
		"   bind(strafter(str(?useraw),\"WireUsageKind.\") as ?usage)"+
		"} ORDER BY ?name ?seq";

	public static final String szCountQUERY =
		"SELECT ?key (count(?seq) as ?count) WHERE {"+
		" SELECT DISTINCT ?key ?seq WHERE {"+
		" ?eq r:type c:ACLineSegment."+
    " ?eq c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
    " ?eq c:ACLineSegment.WireSpacingInfo ?w."+
    " ?w c:IdentifiedObject.name ?key."+
    " ?pos c:WirePosition.WireSpacingInfo ?w."+
    " ?pos c:WirePosition.sequenceNumber ?seq."+
		"}} GROUP BY ?key ORDER BY ?key";

	public String name;
	public String id;
	public double[] xarray;
	public double[] yarray;
	public String usage;
	public int nwires;
	public boolean cable;
	public double b_sep;
	public int b_cnt;

	// only write the phasing permutations that are actually used
	private HashSet<String> perms = new HashSet<>();
	private boolean bTriplex;
	// nphases inferred locally from the permutation
	// bNeutral inferred locally from nwires > nphases

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"mRID\":\"" + id +"\"");
		buf.append ("}");
		return buf.toString();
	}

	public void MarkPermutationsUsed (String s) {
		if (s.contains ("ABC") && nwires >= 3) {
			perms.add ("ABC");
		} else if (s.contains ("ACB") && nwires >= 3) {
			perms.add ("ACB");
		} else if (s.contains ("BAC") && nwires >= 3) {
			perms.add ("BAC");
		} else if (s.contains ("BCA") && nwires >= 3) {
			perms.add ("BCA");
		} else if (s.contains ("CAB") && nwires >= 3) {
			perms.add ("CAB");
		} else if (s.contains ("CBA") && nwires >= 3) {
			perms.add ("CBA");
		} else if (s.contains ("AB") && nwires >= 2) {
			perms.add ("AB");
		} else if (s.contains ("BA") && nwires >= 2) {
			perms.add ("BA");
		} else if (s.contains ("BC") && nwires >= 2) {
			perms.add ("BC");
		} else if (s.contains ("CB") && nwires >= 2) {
			perms.add ("CB");
		} else if (s.contains ("AC") && nwires >= 2) {
			perms.add ("AC");
		} else if (s.contains ("CA") && nwires >= 2) {
			perms.add ("CA");
		} else if (s.contains ("A") && nwires >= 1) {
			perms.add ("A");
		} else if (s.contains ("B") && nwires >= 1) {
			perms.add ("B");
		} else if (s.contains ("C") && nwires >= 1) {
			perms.add ("C");
		}
	}

	public DistLineSpacing (ResultSet results, HashMap<String,Integer> map) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			cable = OptionalBoolean (soln, "?cable", false);
			usage = OptionalString (soln, "?usage", "distribution");
			b_sep = OptionalDouble (soln, "?bundle_sep", 0.0);
			b_cnt = OptionalInt (soln, "?bundle_count", 0);
			nwires = map.get (name);
			xarray = new double[nwires];
			yarray = new double[nwires];
			xarray[0] = OptionalDouble (soln, "?x", 0.0);
			yarray[0] = OptionalDouble (soln, "?y", 0.0);
			for (int i = 1; i < nwires; i++) {
				soln = results.next();
				xarray[i] = OptionalDouble (soln, "?x", 0.0);
				yarray[i] = OptionalDouble (soln, "?y", 0.0);
			}
		}
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " nwires=" + Integer.toString(nwires) + " cable=" + Boolean.toString(cable) + " usage=" + usage); 
		buf.append (" b_cnt=" + Integer.toString(b_cnt) + " b_sep=" + df4.format(b_sep));
		for (int i = 0; i < nwires; i++) {
				buf.append ("\n  x=" + df4.format(xarray[i]) + " y=" + df4.format(yarray[i]));
		}
		return buf.toString();
	}

	private void AppendDSSPermutation(StringBuilder buf, String perm) {
		int i;

		int nphases = perm.length();
		boolean has_neutral = false;
		if (nwires > nphases) {
			has_neutral = true;
		}

		buf.append ("new LineSpacing." + name + "_" + perm + " nconds=" + Integer.toString(nwires) + " nphases=" + Integer.toString(nphases) + " units=m\n");
		buf.append ("~ x=[");
		for (i = 0; i < nwires; i++) {
			buf.append (df4.format(xarray[i]));
			if (i+1 < nwires) {
				buf.append (",");
			}
		}
		buf.append ("]\n~ h=[");
		for (i = 0; i < nwires; i++) {
			buf.append (df4.format(yarray[i]));
			if (i+1 < nwires) {
				buf.append (",");
			}
		}
		buf.append ("]\n");
	}

	private double WireSeparation (int i, int j) {
		double dx = xarray[i] - xarray[j];
		double dy = yarray[i] - yarray[j];
		return Math.sqrt (dx * dx + dy * dy);
	}

	private void AppendGLMPermutation (StringBuilder buf, String perm) {
		int nphases = perm.length();
		boolean has_neutral = false;
		if (nwires > nphases) {
			has_neutral = true;
		}
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
			buf.append ("  distance_AE " + df4.format(gFTperM * yarray[idxA]) + ";\n");
		}
		if (perm.contains ("B")) {
			if (perm.contains ("C")) {
				buf.append ("  distance_BC " + df4.format(gFTperM * WireSeparation (idxB, idxC)) + ";\n");
			}
			if (has_neutral) {
				buf.append ("  distance_BN " + df4.format(gFTperM * WireSeparation (idxB, nwires-1)) + ";\n");
			}
			buf.append ("  distance_BE " + df4.format(gFTperM * yarray[idxB]) + ";\n");
		}
		if (perm.contains ("C")) {
			if (has_neutral) {
				buf.append ("  distance_CN " + df4.format(gFTperM * WireSeparation (idxC, nwires-1)) + ";\n");
			}
			buf.append ("  distance_CE " + df4.format(gFTperM * yarray[idxC]) + ";\n");
		}
		if (has_neutral) {
			buf.append ("  distance_NE " + df4.format(gFTperM * yarray[nwires-1]) + ";\n");
		}
		buf.append("}\n");
	}

	public String GetGLM() {
		StringBuilder buf = new StringBuilder ("");
		for (String phs: perms) {
			AppendGLMPermutation(buf, phs);
		}

		return buf.toString();
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder ("");
		for (String phs: perms) {
			AppendDSSPermutation(buf, phs);
		}

		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

