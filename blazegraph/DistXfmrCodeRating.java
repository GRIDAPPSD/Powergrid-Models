//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;
import org.apache.commons.math3.complex.Complex;

public class DistXfmrCodeRating extends DistComponent {
	static final String szQUERY = 
		"SELECT ?pname ?tname ?enum ?ratedS ?ratedU ?conn ?ang ?res WHERE {"+
		" ?p r:type c:PowerTransformerInfo."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?t c:TransformerTankInfo.PowerTransformerInfo ?p."+
		" ?t c:IdentifiedObject.name ?tname."+
		" ?e c:TransformerEndInfo.TransformerTankInfo ?t."+
		" ?e c:TransformerEndInfo.endNumber ?enum."+
		" ?e c:TransformerEndInfo.ratedS ?ratedS."+
		" ?e c:TransformerEndInfo.ratedU ?ratedU."+
		" ?e c:TransformerEndInfo.r ?res."+
		" ?e c:TransformerEndInfo.phaseAngleClock ?ang."+
		" ?e c:TransformerEndInfo.connectionKind ?connraw."+
		"       		bind(strafter(str(?connraw),\"WindingConnection.\") as ?conn)"+
		"} ORDER BY ?pname ?tname ?enum";

	public String pname;
	public String tname;
	public int[] wdg;
	public String[] conn;
	public int[] ang;
	public double[] ratedS; 
	public double[] ratedU;
	public double[] r;
	public int size;

	public boolean glmUsed;

	private void SetSize (String p, String t) {
		size = 1;
		String szCount = "SELECT (count (?p) as ?count) WHERE {"+
			" ?p r:type c:PowerTransformerInfo."+
			" ?p c:IdentifiedObject.name \"" + p + "\"."+
			" ?t c:TransformerTankInfo.PowerTransformerInfo ?p."+
			" ?t c:IdentifiedObject.name \"" + t + "\"."+
			" ?e c:TransformerEndInfo.TransformerTankInfo ?t"+
			"}";
		ResultSet results = RunQuery (szCount);
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			size = soln.getLiteral("?count").getInt();
		}
		wdg = new int[size];
		conn = new String[size];
		ang = new int[size];
		ratedS = new double[size];
		ratedU = new double[size];
		r = new double[size];
	}

	public DistXfmrCodeRating (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			String p = soln.get("?pname").toString();
			String t = soln.get("?tname").toString();
			pname = GLD_Name (p, false);
			tname = GLD_Name (t, false);
			SetSize (p, t);
			for (int i = 0; i < size; i++) {
				wdg[i] = Integer.parseInt (soln.get("?enum").toString());
				conn[i] = soln.get("?conn").toString();
				ang[i] = Integer.parseInt (soln.get("?ang").toString());
				ratedS[i] = Double.parseDouble (soln.get("?ratedS").toString());
				ratedU[i] = Double.parseDouble (soln.get("?ratedU").toString());
				r[i] = Double.parseDouble (soln.get("?res").toString());
				if ((i + 1) < size) {
					soln = results.next();
				}
			}
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#0.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + ":" + tname);
		for (int i = 0; i < size; i++) {
			buf.append ("\n  wdg=" + Integer.toString(wdg[i]) + " conn=" + conn[i] + " ang=" + Integer.toString(ang[i]));
			buf.append (" U=" + df.format(ratedU[i]) + " S=" + df.format(ratedS[i]) + " r=" + df.format(r[i]));
		}
		return buf.toString();
	}

	public String GetGLM (DistXfmrCodeSCTest sct, DistXfmrCodeOCTest oct) {
		StringBuilder buf = new StringBuilder("object transformer_configuration {\n");
		DecimalFormat dfv = new DecimalFormat("#0.000");
		DecimalFormat dfz = new DecimalFormat("#0.000000");

		double rpu = 0.0;
		double zpu = 0.0;
		double zbase1 = ratedU[0] * ratedU[0] / ratedS[0];
		double zbase2 = ratedU[1] * ratedU[1] / ratedS[1];
		if (sct.ll[0] > 0.0) {
			rpu = sct.ll[0] / ratedS[0];
		} else {
			rpu = (r[0] / zbase1) + (r[1] / zbase2);
		}
		if (sct.fwdg[0] == 1) {
			zpu = sct.z[0] / zbase1;
		} else if (sct.fwdg[0] == 2) {
			zpu = sct.z[0] / zbase2;
		}
		double xpu = zpu;
		if (zpu >= rpu) {
//			xpu = Math.sqrt (zpu * zpu - rpu * rpu);  // TODO: this adjustment is correct, but was not done in RC1
		}

		String sConnect = GetGldTransformerConnection (conn, size);
		buf.append ("  name \"xcon_" + tname + "\";\n");
		buf.append ("  connect_type " + sConnect + ";\n");
		if (conn[0].equals("I")) {
			buf.append ("  primary_voltage " + dfv.format(ratedU[0]) + ";\n");
			buf.append ("  secondary_voltage " + dfv.format (ratedU[1]) + ";\n");
		} else {
			buf.append ("  primary_voltage " + dfv.format(ratedU[0] / Math.sqrt(3.0)) + ";\n");
			buf.append ("  secondary_voltage " + dfv.format (ratedU[1] / Math.sqrt(3.0)) + ";\n");
		}
		buf.append ("  power_rating " + dfv.format (ratedS[0] * 0.001) + ";\n");
		if (sConnect.equals ("SINGLE_PHASE_CENTER_TAPPED")) {
			String impedance = CFormat (new Complex (0.5 * rpu, 0.8 * xpu));
			String impedance1 = CFormat (new Complex (rpu, 0.4 * xpu));
			String impedance2 = CFormat (new Complex (rpu, 0.4 * xpu));
			buf.append ("  impedance " + impedance + ";\n");
			buf.append ("  impedance1 " + impedance1 + ";\n");
			buf.append ("  impedance2 " + impedance2 + ";\n");
		} else {
			buf.append ("  resistance " + dfz.format(rpu) + ";\n");
			buf.append ("  reactance " + dfz.format (xpu) + ";\n");
		}
		if (oct.iexc > 0.0) {
			buf.append ("  shunt_reactance " + dfz.format (100.0 / oct.iexc) + ";\n");
		}
		if (oct.nll > 0.0) {
			buf.append ("  shunt_resistance " + dfz.format (ratedS[0] / oct.nll) + ";\n");
		}
		buf.append("}\n");
		return buf.toString();
	}

	public String GetKey() {
		return tname;
	}
}

