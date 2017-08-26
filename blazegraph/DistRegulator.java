//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;
import java.util.HashMap;

public class DistRegulator extends DistComponent {
	static final String szQUERY =
		"SELECT ?rname ?pname ?wnum ?phs ?incr ?mode ?enabled ?highStep ?lowStep ?neutralStep"+
		" ?normalStep ?neutralU ?step ?initDelay ?subDelay ?ltc ?vlim"+
		" ?vset ?vbw ?ldc ?fwdR ?fwdX ?revR ?revX ?discrete ?ctl_enabled ?ctlmode"+
		" ?monphs ?ctRating ?ctRatio ?ptRatio"+
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
		"}"+
		" ORDER BY ?rname ?wnum";

	public String rname;
	public String pname;
	public String phs;
	public String monphs;
	public String mode;
	public String ctlmode;
	public int wnum;
	public int highStep;
	public int lowStep;
	public int neutralStep;
	public int normalStep;
	public boolean enabled;
	public boolean ldc;
	public boolean ltc;
	public boolean discrete; 
	public boolean ctl_enabled;
	public double incr;
	public double neutralU;
	public double step;
	public double initDelay; 
	public double subDelay;
	public double vlim;
	public double vset;
	public double vbw;
	public double fwdR;
	public double fwdX;
	public double revR;
	public double revX;
	public double ctRating;
	public double ctRatio;
	public double ptRatio;

	public DistRegulator (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			rname = GLD_Name (soln.get("?rname").toString(), false);
			pname = GLD_Name (soln.get("?pname").toString(), false);
			phs = soln.get("?phs").toString();
			monphs = soln.get("?monphs").toString();
			mode = soln.get("?mode").toString();
			ctlmode = soln.get("?ctlmode").toString();
			wnum = Integer.parseInt (soln.get("?wnum").toString());
			highStep = Integer.parseInt (soln.get("?highStep").toString());
			lowStep = Integer.parseInt (soln.get("?lowStep").toString());
			neutralStep = Integer.parseInt (soln.get("?neutralStep").toString());
			normalStep = Integer.parseInt (soln.get("?normalStep").toString());
			enabled = Boolean.parseBoolean (soln.get("?enabled").toString());
			ldc = Boolean.parseBoolean (soln.get("?ldc").toString());
			ltc = Boolean.parseBoolean (soln.get("?ltc").toString());
			discrete = Boolean.parseBoolean (soln.get("?discrete").toString());
			ctl_enabled = Boolean.parseBoolean (soln.get("?ctl_enabled").toString());
			incr = Double.parseDouble (soln.get("?incr").toString());
			neutralU = Double.parseDouble (soln.get("?neutralU").toString());
			step = Double.parseDouble (soln.get("?step").toString());
			initDelay = Double.parseDouble (soln.get("?initDelay").toString());
			subDelay = Double.parseDouble (soln.get("?subDelay").toString());
			vlim = Double.parseDouble (soln.get("?vlim").toString());
			vset = Double.parseDouble (soln.get("?vset").toString());
			vbw = Double.parseDouble (soln.get("?vbw").toString());
			fwdR = Double.parseDouble (soln.get("?fwdR").toString());
			fwdX = Double.parseDouble (soln.get("?fwdX").toString());
			revR = Double.parseDouble (soln.get("?revR").toString());
			revX = Double.parseDouble (soln.get("?revX").toString());
			ctRating = Double.parseDouble (soln.get("?ctRating").toString());
			ctRatio = Double.parseDouble (soln.get("?ctRatio").toString());
			ptRatio = Double.parseDouble (soln.get("?ptRatio").toString());
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (rname + ":" + pname + ":" + Integer.toString(wnum) + ":" + phs + " mode=" + mode + " ctlmode=" + ctlmode + " monphs=" + monphs);
		buf.append (" enabled=" + Boolean.toString(enabled));
		buf.append (" ctl_enabled=" + Boolean.toString(ctl_enabled));
		buf.append (" discrete=" + Boolean.toString(discrete));
		buf.append (" ltc=" + Boolean.toString(ltc));
		buf.append (" ldc=" + Boolean.toString(ldc));
		buf.append (" highStep=" + Integer.toString(highStep));
		buf.append (" lowStep=" + Integer.toString(lowStep));
		buf.append (" neutralStep=" + Integer.toString(neutralStep));
		buf.append (" normalStep=" + Integer.toString(normalStep));
		buf.append (" neutralU=" + df.format(neutralU));
		buf.append (" step=" + df.format(step));
		buf.append (" incr=" + df.format(incr));
		buf.append (" initDelay=" + df.format(initDelay));
		buf.append (" subDelay=" + df.format(subDelay));
		buf.append (" vlim=" + df.format(vlim));
		buf.append (" vset=" + df.format(vset));
		buf.append (" vbw=" + df.format(vbw));
		buf.append (" fwdR=" + df.format(fwdR));
		buf.append (" fwdX=" + df.format(fwdX));
		buf.append (" revR=" + df.format(revR));
		buf.append (" revX=" + df.format(revX));
		buf.append (" ctRating=" + df.format(ctRating));
		buf.append (" ctRatio=" + df.format(ctRatio));
		buf.append (" ptRatio=" + df.format(ptRatio));
		return buf.toString();
	}

	public String GetJSONSymbols(HashMap<String,DistCoordinates> map, HashMap<String,DistXfmrTank> mapTank) {
		DistCoordinates pt1 = map.get("PowerTransformer:" + pname + ":1");
		DistCoordinates pt2 = map.get("PowerTransformer:" + pname + ":2");
		DistXfmrTank xfmr = mapTank.get(rname);
		String bus1 = xfmr.bus[0];
		String bus2 = xfmr.bus[1];

		DecimalFormat df = new DecimalFormat("#0.00");
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + pname + "\"");
		buf.append (",\"from\":\"" + bus1 + "\"");
		buf.append (",\"to\":\"" + bus2 + "\"");
		buf.append (",\"phases\":\"" + phs +"\"");
		buf.append (",\"configuration\":\"" + rname + "\"");
		buf.append (",\"x1\":" + Double.toString(pt1.x));
		buf.append (",\"y1\":" + Double.toString(pt1.y));
		buf.append (",\"x2\":" + Double.toString(pt2.x));
		buf.append (",\"y2\":" + Double.toString(pt2.y));
		buf.append ("}");
		return buf.toString();
	}

	public String GetGLM (DistXfmrCodeRating code, DistXfmrTank tank) {
		StringBuilder buf = new StringBuilder ("object regulator_configuration {\n");
		DecimalFormat df = new DecimalFormat("#0.000000");
		boolean bA = true, bB = true, bC = true;
		double dReg = 0.01 * 0.5 * step * (highStep - lowStep);

		buf.append ("  name \"rcon_" + rname + "\";\n");
		if (tank.vgrp.contains("D") || tank.vgrp.contains("d"))  {
			buf.append ("  connect_type CLOSED_DELTA;\n");
		} else {
			buf.append ("  connect_type WYE_WYE;\n");
		}
		buf.append ("  band_center " + df.format(vset) + ";\n");
		buf.append ("  band_width " + df.format(vbw) + ";\n");
		buf.append ("  dwell_time " + df.format(initDelay) + ";\n");
		buf.append ("  raise_taps " + Integer.toString(Math.abs (highStep - neutralStep)) + ";\n");
		buf.append ("  lower_taps " + Integer.toString(Math.abs (neutralStep - lowStep)) + ";\n");
		buf.append ("  regulation " + df.format(dReg) + ";\n");
		buf.append ("  Type B;\n");
		if (vset > 0.0 && vbw > 0.0 && ltc) {  // for GridAPPS-D, we don't actually use the control modes from CIM
			if (ldc) {
				buf.append("	Control MANUAL; // LINE_DROP_COMP;\n");
			} else {
				buf.append("	Control MANUAL; // OUTPUT_VOLTAGE;\n");
			}
		} else {
			buf.append("	Control MANUAL;\n");
		}
//		if (bA)  buf.append ("	tap_pos_A " + Integer.toString(iTapA) + ";\n");
//		if (bB)  buf.append ("	tap_pos_B " + Integer.toString(iTapB) + ";\n");
//		if (bC)  buf.append ("	tap_pos_C " + Integer.toString(iTapC) + ";\n");
		buf.append ("  current_transducer_ratio " + df.format(ctRatio) + ";\n");
		buf.append ("  power_transducer_ratio " + df.format(ptRatio) + ";\n");
		if (bA)  {
			buf.append ("  compensator_r_setting_A " + df.format(fwdR) + ";\n");
			buf.append ("  compensator_x_setting_A " + df.format(fwdX) + ";\n");
		}
		if (bB)  {
			buf.append ("  compensator_r_setting_B " + df.format(fwdR) + ";\n");
			buf.append ("  compensator_x_setting_B " + df.format(fwdX) + ";\n");
		}
		if (bC)  {
			buf.append ("  compensator_r_setting_C " + df.format(fwdR) + ";\n");
			buf.append ("  compensator_x_setting_C " + df.format(fwdX) + ";\n");
		}
		buf.append ("}\n");

		buf.append ("object regulator {\n");
		buf.append ("  name \"reg_" + rname + "\";\n");
		buf.append ("  from \"" + tank.bus[0] + "\";\n");
		buf.append ("  to \"" + tank.bus[1] + "\";\n");
		buf.append ("  phases " + tank.phs[0] + ";\n");
		buf.append ("  configuration \"rcon_" + rname + "\";\n");
		buf.append ("}\n");
		return buf.toString();
	}

	public String GetKey() {
		return rname;
	}
}

