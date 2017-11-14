package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import org.apache.commons.math3.complex.Complex;

public class DistPhaseMatrix extends DistComponent {
	public static final String szQUERY = 
		"SELECT ?name ?cnt ?seq ?r ?x ?b ?id WHERE {"+
		" ?s r:type c:PerLengthPhaseImpedance."+
		" ?s c:IdentifiedObject.name ?name."+
		" ?s c:PerLengthPhaseImpedance.conductorCount ?cnt."+
		" bind(strafter(str(?s),\"#_\") as ?id)."+
		" ?elm c:PhaseImpedanceData.PhaseImpedance ?s."+
		" ?elm c:PhaseImpedanceData.sequenceNumber ?seq."+
		" ?elm c:PhaseImpedanceData.r ?r."+
		" ?elm c:PhaseImpedanceData.x ?x."+
		" ?elm c:PhaseImpedanceData.b ?b"+
		"} ORDER BY ?name ?seq";

	public String id;
	public String name;
	public int cnt; 
	public double[] r;
	public double[] x;
	public double[] b;

	// only write the phasing permutations that are actually used
	private boolean glmABC;
	private boolean glmAB;
	private boolean glmAC;
	private boolean glmBC;
	private boolean glmA;
	private boolean glmB;
	private boolean glmC;
	private boolean glmTriplex;

	public void MarkGLMPermutationsUsed (String s) {
		if (cnt == 3) {
			glmABC = true;
		} else if (cnt == 2) {
			if (s.contains ("A") && s.contains ("B")) glmAB = true;
			if (s.contains ("A") && s.contains ("C")) glmAC = true;
			if (s.contains ("B") && s.contains ("C")) glmBC = true;
			if (s.contains ("s")) glmTriplex = true;
		} else if (cnt == 1) {
			if (s.contains ("A")) glmA = true;
			if (s.contains ("B")) glmB = true;
			if (s.contains ("C")) glmC = true;
		}
	}

	private int size;

	/** converts the [row,col] of nxn matrix into the sequence number for CIM PerLengthPhaseImpedanceData
	(only valid for the lower triangle) *  
	@param n 2x2 matrix order 
	@param row first index of the element 
	@param col second index 
	@return sequence number */ 
	public int GetMatSeq (int n, int row, int col) {
		int seq = -1;
		int i, j;
		for (j = 0; j < col; j++) {
			seq += (n - j);
		}
		for (i = col; i <= row; i++) {
			++seq;
		}
		return seq;
	}

	public int GetMatRow (int seq) {
		int row = 1;
		if (cnt == 2) {
			if (seq == 2) row = 1;
			if (seq == 3) row = 2;
		}
		if (cnt == 3) {
			if (seq == 2) row = 1;
			if (seq == 3) row = 1;
			if (seq == 4) row = 2;
			if (seq == 5) row = 2;
			if (seq == 6) row = 3;
		}
		return row;
	}

	public int GetMatCol (int seq) {
		int col = 1;
		if (cnt == 2) {
			if (seq == 2) col = 2;
			if (seq == 3) col = 2;
		}
		if (cnt == 3) {
			if (seq == 2) col = 2;
			if (seq == 3) col = 3;
			if (seq == 4) col = 2;
			if (seq == 5) col = 3;
			if (seq == 6) col = 3;
		}
		return col;
	}

	private int SetMatSize () {
		size = 0;
		for (int i = 0; i < cnt; i++) {
			for (int j = i; j < cnt; j++) {
				++size;
			}
		}
		return size;
	}

	public DistPhaseMatrix (ResultSet results) {
		size = 0;
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			int seq = Integer.parseInt (soln.get("?seq").toString());
			if (size == 0) {
				name = SafeName (soln.get("?name").toString());
				id = soln.get("?id").toString();
				cnt = Integer.parseInt (soln.get("?cnt").toString());
				SetMatSize();
				r = new double[size];
				x = new double[size];
				b = new double[size];
			}
			r[seq-1] = Double.parseDouble (soln.get("?r").toString());
			x[seq-1] = Double.parseDouble (soln.get("?x").toString());
			b[seq-1] = Double.parseDouble (soln.get("?b").toString());
			while (seq < size) {
				soln = results.next();
				seq = Integer.parseInt (soln.get("?seq").toString());
				r[seq-1] = Double.parseDouble (soln.get("?r").toString());
				x[seq-1] = Double.parseDouble (soln.get("?x").toString());
				b[seq-1] = Double.parseDouble (soln.get("?b").toString());
			}
		}
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " " + Integer.toString(cnt));
		for (int i = 0; i < size; i++) {
			int seq = i+1;
			buf.append ("\n  " + Integer.toString(seq) + 
									" [" + Integer.toString(GetMatRow(seq)) + "," + Integer.toString(GetMatCol(seq)) +"]" +
									" r=" + df4.format(r[i]) + " x=" + df4.format(x[i]) + " b=" + df4.format(b[i]));
		}
		return buf.toString();
	}

	private void AppendPermutation (StringBuilder buf, String perm, int[] permidx) {
		if (glmTriplex) {
			buf.append("object triplex_line_configuration {\n");
			buf.append("  name \"tcon_" + name + "_" + perm + "\";\n");
		} else {
			buf.append("object line_configuration {\n");
			buf.append("  name \"lcon_" + name + "_" + perm + "\";\n");
		}
		for (int i = 0; i < cnt; i++) {
			for (int j = 0; j < cnt; j++) {
				int seq = GetMatSeq (cnt, i, j);
				// want ohms/mile and nF/mile
				String indices = Integer.toString(permidx[i]) + Integer.toString(permidx[j]) + " ";
				buf.append ("  z" + indices + CFormat (new Complex(gMperMILE * r[seq], gMperMILE * x[seq])) + ";\n");
				if (!glmTriplex) {
					buf.append("  c" + indices + df4.format(1.0e9 * gMperMILE * b[seq] / gOMEGA) + ";\n");
				}
			}
		}
		buf.append("}\n");
	}

	public String GetGLM() {
		StringBuilder buf = new StringBuilder ("");
		if ((cnt == 3) && glmABC) {
			AppendPermutation (buf, "ABC", new int[] {1, 2, 3});
		} else if (cnt == 2) {
			if (glmAB) AppendPermutation (buf, "AB", new int[] {1, 2});
			if (glmAC) AppendPermutation (buf, "AC", new int[] {1, 3});
			if (glmBC) AppendPermutation (buf, "BC", new int[] {2, 3});
			if (glmTriplex) AppendPermutation (buf, "12", new int[] {1, 2});
		} else if (cnt == 1) {
			if (glmA) AppendPermutation (buf, "A", new int[] {1});
			if (glmB) AppendPermutation (buf, "B", new int[] {2});
			if (glmC) AppendPermutation (buf, "C", new int[] {3});
		}

		return buf.toString();
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder ("new Linecode." + name + " nphases=" + Integer.toString(cnt) + " units=mi");
		StringBuilder rBuf = new StringBuilder (" rmatrix=[");
		StringBuilder xBuf = new StringBuilder (" xmatrix=[");
		StringBuilder cBuf = new StringBuilder (" cmatrix=[");

		for (int i = 0; i < cnt; i++) {  // lower triangular, go across the rows for OpenDSS
			for (int j = 0; j <= i; j++) {
				int seq = GetMatSeq (cnt, i, j);
				rBuf.append (String.format("%6g", r[seq] * gMperMILE) + " ");
				xBuf.append (String.format("%6g", x[seq] * gMperMILE) + " ");
				cBuf.append (String.format("%6g", b[seq] * gMperMILE * 1.0e9 / gOMEGA) + " ");
			}
			if ((i+1) < cnt) {
				rBuf.append ("| ");
				xBuf.append ("| ");
				cBuf.append ("| ");
			}
		}

		buf.append (rBuf + "]");
		buf.append (xBuf + "]");
		buf.append (cBuf + "]\n");

		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

