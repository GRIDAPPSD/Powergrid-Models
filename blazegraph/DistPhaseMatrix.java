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
	public double[] r;
	public double[] x;
	public double[] b;

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
				name = GLD_Name (soln.get("?name").toString(), false);
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
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " " + Integer.toString(cnt));
		for (int i = 0; i < size; i++) {
			int seq = i+1;
			buf.append ("\n  " + Integer.toString(seq) + 
									" [" + Integer.toString(GetMatRow(seq)) + "," + Integer.toString(GetMatCol(seq)) +"]" +
									" r=" + df.format(r[i]) + " x=" + df.format(x[i]) + " b=" + df.format(b[i]));
		}
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

