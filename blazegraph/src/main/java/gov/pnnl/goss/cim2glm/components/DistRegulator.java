package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;

import gov.pnnl.goss.cim2glm.queryhandler.QueryHandler;

import java.util.HashMap;

public class DistRegulator extends DistComponent {
	public static final String szQUERY =
		"SELECT ?rname ?pname ?tname ?wnum ?phs ?incr ?mode ?enabled ?highStep ?lowStep ?neutralStep"+
		" ?normalStep ?neutralU ?step ?initDelay ?subDelay ?ltc ?vlim"+
		" ?vset ?vbw ?ldc ?fwdR ?fwdX ?revR ?revX ?discrete ?ctl_enabled ?ctlmode"+
		" ?monphs ?ctRating ?ctRatio ?ptRatio ?id"+
		" WHERE {"+
		" ?rtc r:type c:RatioTapChanger."+
		" ?rtc c:IdentifiedObject.name ?rname."+
		" ?rtc c:RatioTapChanger.TransformerEnd ?end."+
		" ?end c:TransformerEnd.endNumber ?wnum."+
		" OPTIONAL {?end c:TransformerTankEnd.phases ?phsraw."+
		"  bind(strafter(str(?phsraw),\"PhaseCode.\") as ?phs)}"+
		" ?end c:TransformerTankEnd.TransformerTank ?tank."+
		" ?tank c:TransformerTank.PowerTransformer ?pxf."+
		" ?pxf c:IdentifiedObject.name ?pname."+
		" ?tank c:IdentifiedObject.name ?tname."+
		" ?rtc c:RatioTapChanger.stepVoltageIncrement ?incr."+
		" ?rtc c:RatioTapChanger.tculControlMode ?moderaw."+
		"  bind(strafter(str(?moderaw),\"TransformerControlMode.\") as ?mode)"+
		" ?rtc c:TapChanger.controlEnabled ?enabled."+
		" ?rtc c:TapChanger.highStep ?highStep."+
		" ?rtc c:TapChanger.initialDelay ?initDelay."+
		" ?rtc c:TapChanger.lowStep ?lowStep."+
		" ?rtc c:TapChanger.ltcFlag ?ltc."+
		" ?rtc c:TapChanger.neutralStep ?neutralStep."+
		" ?rtc c:TapChanger.neutralU ?neutralU."+
		" ?rtc c:TapChanger.normalStep ?normalStep."+
		" ?rtc c:TapChanger.step ?step."+
		" ?rtc c:TapChanger.subsequentDelay ?subDelay."+
		" ?rtc c:TapChanger.TapChangerControl ?ctl."+
		" ?ctl c:TapChangerControl.limitVoltage ?vlim."+
		" ?ctl c:TapChangerControl.lineDropCompensation ?ldc."+
		" ?ctl c:TapChangerControl.lineDropR ?fwdR."+
		" ?ctl c:TapChangerControl.lineDropX ?fwdX."+
		" ?ctl c:TapChangerControl.reverseLineDropR ?revR."+
		" ?ctl c:TapChangerControl.reverseLineDropX ?revX."+
		" ?ctl c:RegulatingControl.discrete ?discrete."+
		" ?ctl c:RegulatingControl.enabled ?ctl_enabled."+
		" ?ctl c:RegulatingControl.mode ?ctlmoderaw."+
		"  bind(strafter(str(?ctlmoderaw),\"RegulatingControlModeKind.\") as ?ctlmode)"+
		" ?ctl c:RegulatingControl.monitoredPhase ?monraw."+
		"  bind(strafter(str(?monraw),\"PhaseCode.\") as ?monphs)"+
		" ?ctl c:RegulatingControl.targetDeadband ?vbw."+
		" ?ctl c:RegulatingControl.targetValue ?vset."+
		" ?asset c:Asset.PowerSystemResources ?rtc."+
		" ?asset c:Asset.AssetInfo ?inf."+
		" ?inf c:TapChangerInfo.ctRating ?ctRating."+
		" ?inf c:TapChangerInfo.ctRatio ?ctRatio."+
		" ?inf c:TapChangerInfo.ptRatio ?ptRatio."+
		" bind(strafter(str(?rtc),\"#_\") as ?id)"+
		"}"+
		" ORDER BY ?pname ?rname ?tname ?wnum";

	public String pname;
	public String bankphases;

	// GridLAB-D only supports different bank parameters for tap (step), R and X
	public double[] step;
	public double[] fwdR;
	public double[] fwdX;
	// GridLAB-D codes phs variations into certain attribute labels
	public String[] phs;
	// TODO: if any of these vary within the bank, should write separate single-phase instances for GridLAB-D
	public String[] tname;
	public String[] rname;
	public String[] id;
	public String[] monphs;
	public String[] mode;
	public String[] ctlmode;
	public int[] wnum;
	public int[] highStep;
	public int[] lowStep;
	public int[] neutralStep;
	public int[] normalStep;
	public boolean[] enabled;
	public boolean[] ldc;
	public boolean[] ltc;
	public boolean[] discrete; 
	public boolean[] ctl_enabled;
	public double[] incr;
	public double[] neutralU;
	public double[] initDelay; 
	public double[] subDelay;
	public double[] vlim;
	public double[] vset;
	public double[] vbw;
	public double[] revR;
	public double[] revX;
	public double[] ctRating;
	public double[] ctRatio;
	public double[] ptRatio;

	public int size;

	private void SetSize (String p, QueryHandler queryHandler) {
		size = 1;
		String szCount = "SELECT (count (?tank) as ?count) WHERE {"+
			" ?tank c:TransformerTank.PowerTransformer ?pxf."+
			" ?pxf c:IdentifiedObject.name \"" + p + "\"."+
			"}";
		ResultSet results = queryHandler.query (szCount);
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			size = soln.getLiteral("?count").getInt();
		}
		phs = new String[size];
		rname = new String[size];
		tname = new String[size];
		id = new String[size];
		monphs = new String[size];
		mode = new String[size];
		ctlmode = new String[size];
		wnum = new int[size];
		highStep = new int[size];
		lowStep = new int[size];
		neutralStep = new int[size];
		normalStep = new int[size];
		enabled = new boolean[size];
		ldc = new boolean[size];
		ltc = new boolean[size];
		discrete = new boolean[size]; 
		ctl_enabled = new boolean[size];
		incr = new double[size];
		neutralU = new double[size];
		initDelay = new double[size]; 
		subDelay = new double[size];
		vlim = new double[size];
		vset = new double[size];
		vbw = new double[size];
		step = new double[size];
		fwdR = new double[size];
		fwdX = new double[size];
		revR = new double[size];
		revX = new double[size];
		ctRating = new double[size];
		ctRatio = new double[size];
		ptRatio = new double[size];
	}

	public DistRegulator (ResultSet results, QueryHandler queryHandler) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			pname = SafeName (soln.get("?pname").toString());
			SetSize (pname, queryHandler);
			for (int i = 0; i < size; i++) {
				id[i] = soln.get("?id").toString();
				rname[i] = SafeName (soln.get("?rname").toString());
				tname[i] = SafeName (soln.get("?tname").toString());
				phs[i] = soln.get("?phs").toString();
				monphs[i] = soln.get("?monphs").toString();
				mode[i] = soln.get("?mode").toString();
				ctlmode[i] = soln.get("?ctlmode").toString();
				wnum[i] = Integer.parseInt (soln.get("?wnum").toString());
				highStep[i] = Integer.parseInt (soln.get("?highStep").toString());
				lowStep[i] = Integer.parseInt (soln.get("?lowStep").toString());
				neutralStep[i] = Integer.parseInt (soln.get("?neutralStep").toString());
				normalStep[i] = Integer.parseInt (soln.get("?normalStep").toString());
				enabled[i] = Boolean.parseBoolean (soln.get("?enabled").toString());
				ldc[i] = Boolean.parseBoolean (soln.get("?ldc").toString());
				ltc[i] = Boolean.parseBoolean (soln.get("?ltc").toString());
				discrete[i] = Boolean.parseBoolean (soln.get("?discrete").toString());
				ctl_enabled[i] = Boolean.parseBoolean (soln.get("?ctl_enabled").toString());
				incr[i] = Double.parseDouble (soln.get("?incr").toString());
				neutralU[i] = Double.parseDouble (soln.get("?neutralU").toString());
				step[i] = Double.parseDouble (soln.get("?step").toString());
				initDelay[i] = Double.parseDouble (soln.get("?initDelay").toString());
				subDelay[i] = Double.parseDouble (soln.get("?subDelay").toString());
				vlim[i] = Double.parseDouble (soln.get("?vlim").toString());
				vset[i] = Double.parseDouble (soln.get("?vset").toString());
				vbw[i] = Double.parseDouble (soln.get("?vbw").toString());
				fwdR[i] = Double.parseDouble (soln.get("?fwdR").toString());
				fwdX[i] = Double.parseDouble (soln.get("?fwdX").toString());
				revR[i] = Double.parseDouble (soln.get("?revR").toString());
				revX[i] = Double.parseDouble (soln.get("?revX").toString());
				ctRating[i] = Double.parseDouble (soln.get("?ctRating").toString());
				ctRatio[i] = Double.parseDouble (soln.get("?ctRatio").toString());
				ptRatio[i] = Double.parseDouble (soln.get("?ptRatio").toString());
				if ((i + 1) < size) {
					soln = results.next();
				}
			}
			StringBuilder buf = new StringBuilder ();
			for (int i = 0; i < size; i++) {
				buf.append (phs[i]);
			}
			bankphases = buf.toString();
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + " bankphases=" + bankphases);
		for (int i = 0; i < size; i++) {
			buf.append ("\n  " + Integer.toString(i));
			buf.append (" " + Integer.toString(wnum[i]) + ":" +rname[i] + ":" + phs[i]);
			buf.append (" tank=" + tname[i]);
			buf.append (" mode=" + mode[i]);
			buf.append (" ctlmode=" + ctlmode[i]);
			buf.append (" monphs=" + monphs[i]);
			buf.append (" enabled=" + Boolean.toString(enabled[i]));
			buf.append (" ctl_enabled=" + Boolean.toString(ctl_enabled[i]));
			buf.append (" discrete=" + Boolean.toString(discrete[i]));
			buf.append (" ltc=" + Boolean.toString(ltc[i]));
			buf.append (" ldc=" + Boolean.toString(ldc[i]));
			buf.append (" highStep=" + Integer.toString(highStep[i]));
			buf.append (" lowStep=" + Integer.toString(lowStep[i]));
			buf.append (" neutralStep=" + Integer.toString(neutralStep[i]));
			buf.append (" normalStep=" + Integer.toString(normalStep[i]));
			buf.append (" neutralU=" + df4.format(neutralU[i]));
			buf.append (" step=" + df4.format(step[i]));
			buf.append (" incr=" + df4.format(incr[i]));
			buf.append (" initDelay=" + df4.format(initDelay[i]));
			buf.append (" subDelay=" + df4.format(subDelay[i]));
			buf.append (" vlim=" + df4.format(vlim[i]));
			buf.append (" vset=" + df4.format(vset[i]));
			buf.append (" vbw=" + df4.format(vbw[i]));
			buf.append (" fwdR=" + df4.format(fwdR[i]));
			buf.append (" fwdX=" + df4.format(fwdX[i]));
			buf.append (" revR=" + df4.format(revR[i]));
			buf.append (" revX=" + df4.format(revX[i]));
			buf.append (" ctRating=" + df4.format(ctRating[i]));
			buf.append (" ctRatio=" + df4.format(ctRatio[i]));
			buf.append (" ptRatio=" + df4.format(ptRatio[i]));
		}
		return buf.toString();
	}

	public String GetJSONSymbols(HashMap<String,DistCoordinates> map, HashMap<String,DistXfmrTank> mapTank) {
		DistCoordinates pt1 = map.get("PowerTransformer:" + pname + ":1");
		DistCoordinates pt2 = map.get("PowerTransformer:" + pname + ":2");
		DistXfmrTank xfmr = mapTank.get(tname[0]);
		String bus1 = xfmr.bus[0];
		String bus2 = xfmr.bus[1];

		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + pname + "\"");
		buf.append (",\"from\":\"" + bus1 + "\"");
		buf.append (",\"to\":\"" + bus2 + "\"");
		buf.append (",\"phases\":\"" + bankphases +"\"");
		buf.append (",\"x1\":" + Double.toString(pt1.x));
		buf.append (",\"y1\":" + Double.toString(pt1.y));
		buf.append (",\"x2\":" + Double.toString(pt2.x));
		buf.append (",\"y2\":" + Double.toString(pt2.y));
		buf.append ("}");
		return buf.toString();
	}

	public String GetGLM (DistXfmrTank tank) {
		StringBuilder buf = new StringBuilder ("object regulator_configuration {\n");
		double dReg = 0.01 * 0.5 * incr[0] * (highStep[0] - lowStep[0]);

		buf.append ("  name \"rcon_" + pname + "\";\n");
		if (tank.vgrp.contains("D") || tank.vgrp.contains("d"))  {
			buf.append ("  connect_type CLOSED_DELTA;\n");
		} else {
			buf.append ("  connect_type WYE_WYE;\n");
		}
		if (vset[0] > 0.0 && vbw[0] > 0.0 && ltc[0]) {  // for GridAPPS-D, we don't actually use the control modes from CIM
			if (ldc[0]) {
				buf.append("	Control MANUAL; // LINE_DROP_COMP;\n");
			} else {
				buf.append("	Control MANUAL; // OUTPUT_VOLTAGE;\n");
			}
		} else {
			buf.append("	Control MANUAL;\n");
		}
		buf.append ("  // use these for OUTPUT_VOLTAGE mode\n");
		buf.append ("  // band_center " + df6.format(vset[0] * ptRatio[0]) + ";\n");
		buf.append ("  // band_width " + df6.format(vbw[0] * ptRatio[0]) + ";\n");
		buf.append ("  // use these for LINE_DROP_COMP mode\n");
		buf.append ("  // band_center " + df6.format(vset[0]) + ";\n");
		buf.append ("  // band_width " + df6.format(vbw[0]) + ";\n");
		buf.append ("  // transducer ratios only apply to LINE_DROP_COMP mode\n");
		buf.append ("  current_transducer_ratio " + df6.format(ctRatio[0]) + ";\n");
		buf.append ("  power_transducer_ratio " + df6.format(ptRatio[0]) + ";\n");
		buf.append ("  dwell_time " + df6.format(initDelay[0]) + ";\n");
		buf.append ("  raise_taps " + Integer.toString(Math.abs (highStep[0] - neutralStep[0])) + ";\n");
		buf.append ("  lower_taps " + Integer.toString(Math.abs (neutralStep[0] - lowStep[0])) + ";\n");
		buf.append ("  regulation " + df6.format(dReg) + ";\n");
		buf.append ("  Type B;\n");
		for (int i = 0; i < size; i++) {
			int iTap = (int) Math.round((step[i] - 1.0) / incr[i] * 100.0);	// TODO - verify this should be an offset from neutralStep
			buf.append ("  compensator_r_setting_" + phs[i] + " " + df6.format(fwdR[i]) + ";\n");
			buf.append ("  compensator_x_setting_" + phs[i] + " " + df6.format(fwdX[i]) + ";\n");
			buf.append ("  // comment out the manual tap setting if using automatic control\n");
			buf.append ("  tap_pos_" + phs[i] + " " + Integer.toString(iTap) + ";\n");
		}
		buf.append ("}\n");

		buf.append ("object regulator {\n");
		buf.append ("  name \"reg_" + pname + "\";\n");
		buf.append ("  from \"" + tank.bus[0] + "\";\n");
		buf.append ("  to \"" + tank.bus[1] + "\";\n");
		buf.append ("  phases " + bankphases + ";\n");
		buf.append ("  configuration \"rcon_" + pname + "\";\n");
		buf.append ("}\n");
		return buf.toString();
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder("");
		String xfName;

		for (int i = 0; i < size; i++) {
			if (size > 1) {
				xfName = tname[i];
			} else {
				xfName = pname;
			}
			buf.append("new RegControl." + rname[i] + " transformer=" + xfName + " winding=" + Integer.toString(wnum[i]));
			buf.append(" vreg=" + df2.format(vset[i]) + " band=" + df2.format(vbw[i]) + " ptratio=" + df2.format(ptRatio[i]) +
								 " ctprim=" + df2.format(ctRating[i]) + " r=" + df2.format(fwdR[i]) +
								 " x=" + df2.format(fwdX[i]) + " revr=" + df2.format(revR[i]) + " revx=" + df2.format(revX[i]) +
								 " delay=" + df2.format(initDelay[i]) + " tapdelay=" + df2.format(subDelay[i]) + " vlimit=" + df2.format(vlim[i]));
			// ptphase, enabled
			buf.append ("\nedit transformer." + xfName + " wdg=" + Integer.toString(wnum[i]) + " tap=" + df6.format(step));
			buf.append("\n");
		}

		return buf.toString();
	}

	public String GetKey() {
		return pname;
	}
}

