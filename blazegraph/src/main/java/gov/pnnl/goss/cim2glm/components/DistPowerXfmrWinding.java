package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import org.apache.jena.query.*;
import java.text.DecimalFormat;
import java.util.HashMap;

public class DistPowerXfmrWinding extends DistComponent {
	public static final String szQUERY = 
		"SELECT ?pname ?vgrp ?enum ?bus ?basev ?conn ?ratedS ?ratedU ?r ?ang ?grounded ?rground ?xground WHERE {"+
		" ?p r:type c:PowerTransformer."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?p c:PowerTransformer.vectorGroup ?vgrp."+
		" ?end c:PowerTransformerEnd.PowerTransformer ?p."+
		" ?end c:TransformerEnd.endNumber ?enum."+
		" ?end c:PowerTransformerEnd.ratedS ?ratedS."+
		" ?end c:PowerTransformerEnd.ratedU ?ratedU."+
		" ?end c:PowerTransformerEnd.r ?r."+
		" ?end c:PowerTransformerEnd.phaseAngleClock ?ang."+
		" ?end c:PowerTransformerEnd.connectionKind ?connraw."+  
		"  bind(strafter(str(?connraw),\"WindingConnection.\") as ?conn)"+
		" ?end c:TransformerEnd.grounded ?grounded."+
		" OPTIONAL {?end c:TransformerEnd.rground ?rground.}"+
		" OPTIONAL {?end c:TransformerEnd.xground ?xground.}"+
		" ?end c:TransformerEnd.Terminal ?trm."+
		" ?trm c:Terminal.ConnectivityNode ?cn. "+
		" ?cn c:IdentifiedObject.name ?bus."+
		" ?end c:TransformerEnd.BaseVoltage ?bv."+
		" ?bv c:BaseVoltage.nominalVoltage ?basev"+
		"}"+
		" ORDER BY ?pname ?enum"		;

	public static final String szCountQUERY =
		"SELECT ?key (count(?p) as ?count) WHERE {"+
		" ?p r:type c:PowerTransformer."+
		" ?p c:IdentifiedObject.name ?key."+
		" ?end c:PowerTransformerEnd.PowerTransformer ?p."+
		"} GROUP BY ?key ORDER BY ?key";

	public String name;
	public String vgrp;
	public String[] bus;
	public String[] conn;
	public double[] basev;
	public double[] ratedU;
	public double[] ratedS;
	public double[] r;
	public int[] wdg;
	public int[] ang;
	public boolean[] grounded;
	public double[] rg;
	public double[] xg;
	public int size;

	private void SetSize (int val) {
		size = val;
		bus = new String[size];
		conn = new String[size];
		basev = new double[size];
		ratedU = new double[size];
		ratedS = new double[size];
		r = new double[size];
		wdg = new int[size];
		ang = new int[size];
		grounded = new boolean[size];
		rg = new double[size];
		xg = new double[size];
	}

	public DistPowerXfmrWinding (ResultSet results, HashMap<String,Integer> map) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			String pname = soln.get("?pname").toString();
			name = SafeName (pname);
			vgrp = soln.get("?vgrp").toString();
			SetSize (map.get(pname));
			for (int i = 0; i < size; i++) {
				bus[i] = SafeName (soln.get("?bus").toString());
				basev[i] = Double.parseDouble (soln.get("?basev").toString());
				conn[i] = soln.get("?conn").toString();
				ratedU[i] = Double.parseDouble (soln.get("?ratedU").toString());
				ratedS[i] = Double.parseDouble (soln.get("?ratedS").toString());
				r[i] = Double.parseDouble (soln.get("?r").toString());
				wdg[i] = Integer.parseInt (soln.get("?enum").toString());
				ang[i] = Integer.parseInt (soln.get("?ang").toString());
				grounded[i] = Boolean.parseBoolean (soln.get("?grounded").toString());
				rg[i] = OptionalDouble (soln, "?rground", 0.0);
				xg[i] = OptionalDouble (soln, "?xground", 0.0);
				if ((i + 1) < size) {
					soln = results.next();
				}
			}
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#0.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " " + vgrp);
		for (int i = 0; i < size; i++) {
			buf.append("\n  bus=" + bus[i] + " basev=" + df.format(basev[i]) + " conn=" + conn[i] + " ang=" + Integer.toString(ang[i]));
			buf.append (" U=" + df.format(ratedU[i]) + " S=" + df.format(ratedS[i]) + " r=" + df.format(r[i]));
			buf.append (" grounded=" + Boolean.toString(grounded[i]) + " rg=" + df.format(rg[i]) + " xg=" + df.format(xg[i]));
		}
		return buf.toString();
	}

	public String GetJSONSymbols(HashMap<String,DistCoordinates> map) {
		DistCoordinates pt1 = map.get("PowerTransformer:" + name + ":1");
		DistCoordinates pt2 = map.get("PowerTransformer:" + name + ":2");
		String bus1 = bus[0];
		String bus2 = bus[1];

//		DecimalFormat df = new DecimalFormat("#0.00");
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name + "\"");
		buf.append (",\"from\":\"" + bus1 + "\"");
		buf.append (",\"to\":\"" + bus2 + "\"");
		buf.append (",\"phases\":\"ABC\"");
		buf.append (",\"configuration\":\"" + vgrp + "\"");
		buf.append (",\"x1\":" + Double.toString(pt1.x));
		buf.append (",\"y1\":" + Double.toString(pt1.y));
		buf.append (",\"x2\":" + Double.toString(pt2.x));
		buf.append (",\"y2\":" + Double.toString(pt2.y));
		buf.append ("}");
		return buf.toString();
	}

	public String GetGLM (DistPowerXfmrMesh mesh, DistPowerXfmrCore core) {
		DecimalFormat dfv = new DecimalFormat ("#0.000");
		DecimalFormat dfz = new DecimalFormat ("#0.000000");

		StringBuilder buf = new StringBuilder ("object transformer_configuration {\n"); 
		buf.append ("  name \"xcon_" + name + "\";\n");
		buf.append ("  connect_type " + GetGldTransformerConnection (conn, size) + ";\n");
		buf.append ("  primary_voltage " + dfv.format (ratedU[0] / Math.sqrt(3.0)) + ";\n");
		buf.append ("  secondary_voltage " + dfv.format (ratedU[1] / Math.sqrt(3.0)) + ";\n");
		buf.append ("  power_rating " + dfv.format (ratedS[0] * 0.001) + ";\n");
		int idx;
		double Zbase;
		double rpu = 0.0, xpu = 0.0;
		for (int i = 0; i < mesh.size; i++) {
			if ((mesh.fwdg[i] == 1) && (mesh.twdg[i] == 2)) {
				Zbase = ratedU[0] * ratedU[0] / ratedS[0];
				rpu = mesh.r[i] / Zbase;
				xpu = mesh.x[i] / Zbase;
				break;
			}
			if ((mesh.fwdg[i] == 2) && (mesh.twdg[i] == 1)) {
				Zbase = ratedU[1] * ratedU[1] / ratedS[1];
				rpu = mesh.r[i] / Zbase;
				xpu = mesh.x[i] / Zbase;
				break;
			}
		}
		buf.append ("  resistance " + dfz.format (rpu) + ";\n");
		buf.append ("  reactance " + dfz.format (xpu) + ";\n");
		idx = core.wdg - 1;
		Zbase = ratedU[idx] * ratedU[idx] / ratedS[idx];
		if (core.b > 0.0) {
			buf.append ("  shunt_reactance " + dfz.format (Zbase / core.b) + ";\n");
		}
		if (core.g > 0.0) {
			buf.append ("  shunt_resistance " + dfz.format (Zbase / core.b) + ";\n");
		}
		buf.append ("}\n");

		buf.append ("object transformer {\n");
		buf.append ("  name \"xf_" + name + "\";\n");
		buf.append ("  from \"" + bus[0] + "\";\n");
		buf.append ("  to \"" + bus[1] + "\";\n");
		buf.append ("  phases ABC;\n");
		buf.append ("  configuration \"xcon_" + name + "\";\n");
		buf.append ("  // vector group " + vgrp + ";\n");
		buf.append("}\n");

		return buf.toString();
	}

	/*

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

*/

	public String GetKey() {
		return name;
	}
}

