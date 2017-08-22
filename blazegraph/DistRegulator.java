//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

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

	public DistRegulator (QuerySolution soln) {
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
		incr = new Double (soln.get("?incr").toString()).doubleValue();
		neutralU = new Double (soln.get("?neutralU").toString()).doubleValue();
		step = new Double (soln.get("?step").toString()).doubleValue();
		initDelay = new Double (soln.get("?initDelay").toString()).doubleValue();
		subDelay = new Double (soln.get("?subDelay").toString()).doubleValue();
		vlim = new Double (soln.get("?vlim").toString()).doubleValue();
		vset = new Double (soln.get("?vset").toString()).doubleValue();
		vbw = new Double (soln.get("?vbw").toString()).doubleValue();
		fwdR = new Double (soln.get("?fwdR").toString()).doubleValue();
		fwdX = new Double (soln.get("?fwdX").toString()).doubleValue();
		revR = new Double (soln.get("?revR").toString()).doubleValue();
		revX = new Double (soln.get("?revX").toString()).doubleValue();
		ctRating = new Double (soln.get("?ctRating").toString()).doubleValue();
		ctRatio = new Double (soln.get("?ctRatio").toString()).doubleValue();
		ptRatio = new Double (soln.get("?ptRatio").toString()).doubleValue();
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
		buf.append ("\n");
		return buf.toString();
	}
}

