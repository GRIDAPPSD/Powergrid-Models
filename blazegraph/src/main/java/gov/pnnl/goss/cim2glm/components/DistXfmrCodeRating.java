package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;
import org.apache.commons.math3.complex.Complex;

public class DistXfmrCodeRating extends DistComponent {
	public static final String szQUERY = 
		"SELECT ?pname ?tname ?enum ?ratedS ?ratedU ?conn ?ang ?res ?id WHERE {"+
		" ?p r:type c:PowerTransformerInfo."+
		" ?t c:TransformerTankInfo.PowerTransformerInfo ?p."+
		" ?e c:TransformerEndInfo.TransformerTankInfo ?t."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?t c:IdentifiedObject.name ?tname."+
		" bind(strafter(str(?t),\"#_\") as ?id)."+
		" ?e c:TransformerEndInfo.endNumber ?enum."+
		" ?e c:TransformerEndInfo.ratedS ?ratedS."+
		" ?e c:TransformerEndInfo.ratedU ?ratedU."+
		" ?e c:TransformerEndInfo.r ?res."+
		" ?e c:TransformerEndInfo.phaseAngleClock ?ang."+
		" ?e c:TransformerEndInfo.connectionKind ?connraw."+
		"       		bind(strafter(str(?connraw),\"WindingConnection.\") as ?conn)"+
		"} ORDER BY ?pname ?tname ?enum";

	public static final String szCountQUERY =
		"SELECT ?key (count(?enum) as ?count) WHERE {"+
		" ?p r:type c:PowerTransformerInfo."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?t c:TransformerTankInfo.PowerTransformerInfo ?p."+
		" ?t c:IdentifiedObject.name ?key."+
		" ?e c:TransformerEndInfo.TransformerTankInfo ?t."+
		" ?e c:TransformerEndInfo.endNumber ?enum."+
		"} GROUP BY ?key ORDER BY ?key";

	public String pname;
	public String tname;
	public String id;
	public int[] wdg;
	public String[] conn;
	public int[] ang;
	public double[] ratedS; 
	public double[] ratedU;
	public double[] r;
	public int size;

	public boolean glmUsed;

	private void SetSize (int val) {
		size = val;
		wdg = new int[size];
		conn = new String[size];
		ang = new int[size];
		ratedS = new double[size];
		ratedU = new double[size];
		r = new double[size];
	}

	public DistXfmrCodeRating (ResultSet results, HashMap<String,Integer> map) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			String p = soln.get("?pname").toString();
			String t = soln.get("?tname").toString();
			pname = SafeName (p);
			tname = SafeName (t);
			id = soln.get("?id").toString();
			SetSize (map.get(tname));
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
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + ":" + tname);
		for (int i = 0; i < size; i++) {
			buf.append ("\n  wdg=" + Integer.toString(wdg[i]) + " conn=" + conn[i] + " ang=" + Integer.toString(ang[i]));
			buf.append (" U=" + df4.format(ratedU[i]) + " S=" + df4.format(ratedS[i]) + " r=" + df4.format(r[i]));
		}
		return buf.toString();
	}

	public String GetGLM (DistXfmrCodeSCTest sct, DistXfmrCodeOCTest oct) {
		StringBuilder buf = new StringBuilder("object transformer_configuration {\n");

		double rpu = 0.0;
		double zpu = 0.0;
		double zbase1 = ratedU[0] * ratedU[0] / ratedS[0];
		double zbase2 = ratedU[1] * ratedU[1] / ratedS[1];
		if ((sct.ll[0] > 0.0) && (size < 3)) {
			rpu = 1000.0 * sct.ll[0] / ratedS[0];
		} else {
			// hard-wired for SINGLE_PHASE_CENTER_TAPPED,
			// which is the only three-winding case that GridLAB-D supports
			rpu = (r[0] / zbase1) + 0.5 * (r[1] / zbase2);
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
			buf.append ("  primary_voltage " + df3.format(ratedU[0]) + ";\n");
			buf.append ("  secondary_voltage " + df3.format (ratedU[1]) + ";\n");
		} else {
			buf.append ("  primary_voltage " + df3.format(ratedU[0] / Math.sqrt(3.0)) + ";\n");
			buf.append ("  secondary_voltage " + df3.format (ratedU[1] / Math.sqrt(3.0)) + ";\n");
		}
		buf.append ("  power_rating " + df3.format (ratedS[0] * 0.001) + ";\n");
		if (sConnect.equals ("SINGLE_PHASE_CENTER_TAPPED")) {
			String impedance = CFormat (new Complex (0.5 * rpu, 0.8 * xpu));
			String impedance1 = CFormat (new Complex (rpu, 0.4 * xpu));
			String impedance2 = CFormat (new Complex (rpu, 0.4 * xpu));
			buf.append ("  impedance " + impedance + ";\n");
			buf.append ("  impedance1 " + impedance1 + ";\n");
			buf.append ("  impedance2 " + impedance2 + ";\n");
		} else {
			buf.append ("  resistance " + df6.format(rpu) + ";\n");
			buf.append ("  reactance " + df6.format (xpu) + ";\n");
		}
		if (oct.iexc > 0.0) {
			buf.append ("  shunt_reactance " + df6.format (100.0 / oct.iexc) + ";\n");
		}
		if (oct.nll > 0.0) {
			buf.append ("  shunt_resistance " + df6.format (ratedS[0] / oct.nll / 1000.0) + ";\n");
		}
		buf.append("}\n");
		return buf.toString();
	}

	public String GetDSS(DistXfmrCodeSCTest sct, DistXfmrCodeOCTest oct) {
		boolean bDelta;
		int phases = 3;
		double zbase, xpct;
		int fwdg, twdg, i;

		for (i = 0; i < size; i++) {
			if (conn[i].contains("I")) {
				phases = 1;
			}
		}
		StringBuilder buf = new StringBuilder("new Xfmrcode." + tname + " windings=" + Integer.toString(size) +
																					" phases=" + Integer.toString(phases));

		// short circuit tests - valid only up to 3 windings
		for (i = 0; i < sct.size; i++) {
			fwdg = sct.fwdg[i];
			twdg = sct.twdg[i];
			zbase = ratedU[fwdg-1] * ratedU[fwdg-1] / ratedS[fwdg-1];
			xpct = 100.0 * sct.z[i] / zbase; // not accounting for ll
			if ((fwdg == 1 && twdg == 2) || (fwdg == 2 && twdg == 1)) {
				buf.append(" xhl=" + df6.format(xpct));
			} else if ((fwdg == 1 && twdg == 3) || (fwdg == 3 && twdg == 1)) {
				buf.append(" xht=" + df6.format(xpct));
			} else if ((fwdg == 2 && twdg == 3) || (fwdg == 3 && twdg == 2)) {
				buf.append(" xlt=" + df6.format(xpct));
			}
		}
		// open circuit test
		buf.append (" %imag=" + df3.format(oct.iexc) + " %noloadloss=" + df3.format(0.001 * oct.nll / ratedS[0]) + "\n");

		// winding ratings
		for (i = 0; i < size; i++) {
			if (conn[i].contains("D")) {
				bDelta = true;
			} else {
				bDelta = false;
			}
			zbase = ratedU[i] * ratedU[i] / ratedS[i];
			buf.append("~ wdg=" + Integer.toString(i + 1) + " conn=" + DSSConn(bDelta) +
								 " kv=" + df3.format(0.001 * ratedU[i]) + " kva=" + df1.format(0.001 * ratedS[i]) +
								 " %r=" + df6.format(100.0 * r[i] / zbase) + "\n");
		}
		return buf.toString();
	}

	public String GetKey() {
		return tname;
	}
}

