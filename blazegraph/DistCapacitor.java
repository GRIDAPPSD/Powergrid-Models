//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistCapacitor extends DistComponent {
    static final String szQUERY = "SELECT ?name ?nomu ?bsection ?bus ?conn ?grnd ?phs"+
			 " ?ctrlenabled ?discrete ?mode ?deadband ?setpoint ?monclass ?moneq ?monbus ?monphs WHERE {"+
       " ?s r:type c:LinearShuntCompensator."+
       " ?s c:IdentifiedObject.name ?name."+
       " ?s c:ShuntCompensator.nomU ?nomu."+
       " ?s c:LinearShuntCompensator.bPerSection ?bsection."+ 
       " ?s c:ShuntCompensator.phaseConnection ?connraw."+
       " 	bind(strafter(str(?connraw),\"PhaseShuntConnectionKind.\") as ?conn)"+
       " ?s c:ShuntCompensator.grounded ?grnd."+
       " OPTIONAL {?scp c:ShuntCompensatorPhase.ShuntCompensator ?s."+
       " 	?scp c:ShuntCompensatorPhase.phase ?phsraw."+
       " 	bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
       " OPTIONAL {?ctl c:RegulatingControl.RegulatingCondEq ?s."+
       " 	?ctl c:RegulatingControl.discrete ?discrete."+
       " 	?ctl c:RegulatingControl.enabled ?ctrlenabled."+
       " 	?ctl c:RegulatingControl.mode ?moderaw."+
       " 		bind(strafter(str(?moderaw),\"RegulatingControlModeKind.\") as ?mode)"+
       " 	?ctl c:RegulatingControl.monitoredPhase ?monraw."+
       " 		bind(strafter(str(?monraw),\"PhaseCode.\") as ?monphs)"+
       " 	?ctl c:RegulatingControl.targetDeadband ?deadband."+
       " 	?ctl c:RegulatingControl.targetValue ?setpoint."+
       " 	?ctl c:RegulatingControl.Terminal ?trm."+
       " 	?trm c:Terminal.ConductingEquipment ?eq."+
       " 	?eq a ?classraw."+
       " 		bind(strafter(str(?classraw),\"cim16#\") as ?monclass)"+
       " 	?eq c:IdentifiedObject.name ?moneq."+
       " 	?trm c:Terminal.ConnectivityNode ?moncn."+
       " 	?moncn c:IdentifiedObject.name ?monbus."+
       "  }" +
       " ?t c:Terminal.ConductingEquipment ?s."+
       " ?t c:Terminal.ConnectivityNode ?cn."+ 
       " ?cn c:IdentifiedObject.name ?bus" + 
       "}";

	public String name;
	public String bus;
	public String phs;
	public String conn;
	public String grnd;
	public String ctrl;
	public double nomu;
	public double kvar;
	public String mode;
	public double setpoint;
	public double deadband;
	public String moneq;
	public String monclass;
	public String monbus;
	public String monphs;

	public DistCapacitor (QuerySolution soln) {
		name = GLD_Name (soln.get("?name").toString(), false);
		bus = GLD_Name (soln.get("?bus").toString(), true);
		phs = OptionalString (soln, "?phs", "ABC");
		conn = soln.get("?conn").toString();
		grnd = soln.get("?grnd").toString();
		ctrl = OptionalString (soln, "?ctrlenabled", "false");
		nomu = new Double (soln.get("?nomu").toString()).doubleValue();
		double bsection = new Double (soln.get("?bsection").toString()).doubleValue();
		kvar = nomu * nomu * bsection / 1000.0;
		if (ctrl.equals ("true")) {
			mode = soln.get("?mode").toString();
			setpoint = new Double (soln.get("?setpoint").toString()).doubleValue();
			deadband = new Double (soln.get("?deadband").toString()).doubleValue();
			moneq = soln.get("?moneq").toString();
			monclass = soln.get("?monclass").toString();
			monbus = soln.get("?monbus").toString();
			monphs = soln.get("?monphs").toString();
		}
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " @ " + bus + " on " + phs);
		buf.append (" " + df.format(nomu/1000.0) + " [kV] " + df.format(kvar) + " [kvar] " + "conn=" + conn + " grnd=" + grnd);
		if (ctrl.equals ("true")) {
			buf.append("\n	control mode=" + mode + " set=" + df.format(setpoint) + " bandwidth=" + df.format(deadband));
			buf.append(" monitoring: " + moneq + ":" + monclass + ":" + monbus + ":" + monphs);
		}
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

