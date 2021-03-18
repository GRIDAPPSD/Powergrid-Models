package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017-2019, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import org.apache.commons.math3.complex.Complex;

public class DistPhaseMatrix extends DistComponent {
	public static final String szQUERY = 
		"SELECT DISTINCT ?name ?cnt ?row ?col ?r ?x ?b ?id WHERE {"+
		" ?eq r:type c:ACLineSegment."+
		" ?eq c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		" ?eq c:ACLineSegment.PerLengthImpedance ?s."+
		" ?s r:type c:PerLengthPhaseImpedance."+
		" ?s c:IdentifiedObject.name ?name."+
		" ?s c:PerLengthPhaseImpedance.conductorCount ?cnt."+
		" bind(strafter(str(?s),\"#\") as ?id)."+
		" ?elm c:PhaseImpedanceData.PhaseImpedance ?s."+
		" ?elm c:PhaseImpedanceData.row ?row."+
		" ?elm c:PhaseImpedanceData.column ?col."+
		" ?elm c:PhaseImpedanceData.r ?r."+
		" ?elm c:PhaseImpedanceData.x ?x."+
		" ?elm c:PhaseImpedanceData.b ?b"+
		"} ORDER BY ?name ?row ?col";

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

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"mRID\":\"" + id +"\"");
		buf.append ("}");
		return buf.toString();
	}

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

	/** converts the [row,col] of nxn matrix into the lower-triangular sequence number
	(only valid for the lower triangle) *  
	@param n 2x2 matrix order 
	@param row first index of the element, 1-based 
	@param col second index, 1-based
	@return sequence number, 0-based */ 
	public int GetMatSeq (int row, int col) {
		if (col > row) { /** transposition */
			int val = row;
			row = col;
			col = val;
		}
		int n = row - 1;
		int offset = n * (n + 1) / 2;
		return offset + col - 1;
	}

	private int SetMatSize () {
		size = cnt * (cnt + 1) / 2;
		return size;
	}

	public DistPhaseMatrix (ResultSet results) {
		size = 0;
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			int row = Integer.parseInt (soln.get("?row").toString());
			int col = Integer.parseInt (soln.get("?col").toString());
			if (size == 0) {
				name = SafeName (soln.get("?name").toString());
				id = soln.get("?id").toString();
				cnt = Integer.parseInt (soln.get("?cnt").toString());
				SetMatSize();
				r = new double[size];
				x = new double[size];
				b = new double[size];
			}
			int seq = GetMatSeq(row, col);
			r[seq] = Double.parseDouble (soln.get("?r").toString());
			x[seq] = Double.parseDouble (soln.get("?x").toString());
			b[seq] = Double.parseDouble (soln.get("?b").toString());
			while (seq < size - 1) {
				soln = results.next();
				row = Integer.parseInt (soln.get("?row").toString());
				col = Integer.parseInt (soln.get("?col").toString());
				seq = GetMatSeq(row, col);
				r[seq] = Double.parseDouble (soln.get("?r").toString());
				x[seq] = Double.parseDouble (soln.get("?x").toString());
				b[seq] = Double.parseDouble (soln.get("?b").toString());
			}
		}
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " " + Integer.toString(cnt));
		for (int i = 1; i <= cnt; i++) {
			for (int j = 1; j <= cnt; j++) {
				int seq = GetMatSeq (i, j);
				buf.append ("\n  " + Integer.toString(seq) + 
									" [" + Integer.toString(i) + "," + Integer.toString(j) +"]" +
									" r=" + df4.format(r[seq]) + " x=" + df4.format(x[seq]) + " b=" + df4.format(b[seq]));
			}
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
				int seq = GetMatSeq (i+1, j+1);
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

		for (int i = 1; i <= cnt; i++) {  // lower triangular, go across the rows for OpenDSS
			for (int j = 1; j <= i; j++) {
				int seq = GetMatSeq (i, j);
				rBuf.append (String.format("%6g", r[seq] * gMperMILE) + " ");
				xBuf.append (String.format("%6g", x[seq] * gMperMILE) + " ");
				cBuf.append (String.format("%6g", b[seq] * gMperMILE * 1.0e9 / gOMEGA) + " ");
			}
			if (i < cnt) {
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

