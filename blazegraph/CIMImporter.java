//      ----------------------------------------------------------
//      Copyright (c) 2017, Battelle Memorial Institute
//      All rights reserved.
//      ----------------------------------------------------------

// package gov.pnnl.gridlabd.cim ;

import java.io.*;
import java.text.DecimalFormat;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;

/**
 * <p>This class runs an example SQARQL query against Blazegraph triple-store</p> 
 *  
 * <p>Invoke as a console-mode program</p> 
 *  
 * @see CIMImporter#main 
 *  
 * @author Tom McDermott 
 * @version 1.0 
 *  
 */

public class CIMImporter extends Object {
  static final String szEND = "http://localhost:9999/blazegraph/namespace/kb/sparql";
  static final String nsCIM = "http://iec.ch/TC57/2012/CIM-schema-cim16#";
  static final String nsRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	static ResultSet RunQuery(String szQuery) {
		String qPrefix = "PREFIX r: <" + nsRDF + "> PREFIX c: <" + nsCIM + "> ";
		Query query = QueryFactory.create (qPrefix + szQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService (szEND, query);
		return qexec.execSelect();
	}

	static String OptionalString (QuerySolution soln, String parm, String def) {
		RDFNode nd = soln.get(parm);
		if (nd != null) {
			return nd.toString();
		}
		return def;
	}

	/** prefix all bus names with `nd_` for GridLAB-D, so they "should" be unique
	 *  @param arg the root bus name, aka CIM name
	 *  @return nd_arg
	 */
	static String GldPrefixedNodeName (String arg) {
		return "nd_" + arg;
	}

	/** 
	 *  convert a CIM name to GridLAB-D name, replacing unallowed characters and prefixing for a bus/node
	 *  @param arg the root bus or component name, aka CIM name
	 *  @param bus to flag whether `nd_` should be prepended
	 *  @return the compatible name for GridLAB-D
	 */  
	static String GLD_Name (String arg, boolean bus) {	// GLD conversion
		String s = arg.replace (' ', '_');
		s = s.replace ('.', '_');
		s = s.replace ('=', '_');
		s = s.replace ('+', '_');
		s = s.replace ('^', '_');
		s = s.replace ('$', '_');
		s = s.replace ('*', '_');
		s = s.replace ('|', '_');
		s = s.replace ('[', '_');
		s = s.replace (']', '_');
		s = s.replace ('{', '_');
		s = s.replace ('}', '_');
		s = s.replace ('(', '_');
		s = s.replace (')', '_');
		if (bus) return GldPrefixedNodeName (s);
		return s;
	}

	static String GetSubstation() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?name ?bus ?nomv ?vmag ?vang ?r1 ?x1 ?r0 ?x0 WHERE {" +
																	" ?s r:type c:EnergySource." +
																	" ?s c:IdentifiedObject.name ?name." +
																	" ?s c:EnergySource.nominalVoltage ?nomv." + 
																	" ?s c:EnergySource.voltageMagnitude ?vmag." + 
																	" ?s c:EnergySource.voltageAngle ?vang." + 
																	" ?s c:EnergySource.r ?r1." + 
																	" ?s c:EnergySource.x ?x1." + 
																	" ?s c:EnergySource.r0 ?r0." + 
																	" ?s c:EnergySource.x0 ?x0." + 
																	" ?t c:Terminal.ConductingEquipment ?s." +
																	" ?t c:Terminal.ConnectivityNode ?cn." + 
																	" ?cn c:IdentifiedObject.name ?bus" +
																	"}");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();

			String name = GLD_Name (soln.get("?name").toString(), false);
			String bus = GLD_Name (soln.get("?bus").toString(), true);
			double nomv = new Double (soln.get("?nomv").toString()).doubleValue();
			double vmag = new Double (soln.get("?vmag").toString()).doubleValue();
			double vang = new Double (soln.get("?vang").toString()).doubleValue();
			double r1 = new Double (soln.get("?r1").toString()).doubleValue();
			double x1 = new Double (soln.get("?x1").toString()).doubleValue();
			double r0 = new Double (soln.get("?r0").toString()).doubleValue();
			double x0 = new Double (soln.get("?x0").toString()).doubleValue();

			buf.append (name + " @ " + bus + " nomv=" + df.format(nomv));
			buf.append (" vmag=" + df.format(vmag));
			buf.append (" vang=" + df.format(vang));
			buf.append (" r1=" + df.format(r1));
			buf.append (" x1=" + df.format(x1));
			buf.append (" r0=" + df.format(r0));
			buf.append (" x0=" + df.format(x0));
		}
		return buf.toString();
	}

	static String GetCapacitors() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?name ?nomu ?bsection ?bus ?conn ?grnd ?phs ?ctrlenabled ?discrete ?mode ?deadband ?setpoint ?monclass ?moneq ?monbus ?monphs WHERE {"+
																	" ?s r:type c:LinearShuntCompensator."+
																	" ?s c:IdentifiedObject.name ?name."+
																	" ?s c:ShuntCompensator.nomU ?nomu."+
																	" ?s c:LinearShuntCompensator.bPerSection ?bsection."+ 
																	" ?s c:ShuntCompensator.phaseConnection ?connraw."+
																	"   bind(strafter(str(?connraw),\"PhaseShuntConnectionKind.\") as ?conn)"+
																	" ?s c:ShuntCompensator.grounded ?grnd."+
																	" OPTIONAL {?scp c:ShuntCompensatorPhase.ShuntCompensator ?s."+
																	"   ?scp c:ShuntCompensatorPhase.phase ?phsraw."+
																	"   bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
																	" OPTIONAL {?ctl c:RegulatingControl.RegulatingCondEq ?s."+
																	"   ?ctl c:RegulatingControl.discrete ?discrete."+
																	"   ?ctl c:RegulatingControl.enabled ?ctrlenabled."+
																	"   ?ctl c:RegulatingControl.mode ?moderaw."+
																	"     bind(strafter(str(?moderaw),\"RegulatingControlModeKind.\") as ?mode)"+
																	"   ?ctl c:RegulatingControl.monitoredPhase ?monraw."+
																	"     bind(strafter(str(?monraw),\"PhaseCode.\") as ?monphs)"+
																	"   ?ctl c:RegulatingControl.targetDeadband ?deadband."+
																	"   ?ctl c:RegulatingControl.targetValue ?setpoint."+
																	"   ?ctl c:RegulatingControl.Terminal ?trm."+
																	"   ?trm c:Terminal.ConductingEquipment ?eq."+
																	"   ?eq a ?classraw."+
																	"     bind(strafter(str(?classraw),\"cim16#\") as ?monclass)"+
																	"   ?eq c:IdentifiedObject.name ?moneq."+
																	"   ?trm c:Terminal.ConnectivityNode ?moncn."+
																	"   ?moncn c:IdentifiedObject.name ?monbus."+
																	"  }" +
																	" ?t c:Terminal.ConductingEquipment ?s."+
																	" ?t c:Terminal.ConnectivityNode ?cn."+ 
																	" ?cn c:IdentifiedObject.name ?bus" + 
																	"}");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();

			String name = GLD_Name (soln.get("?name").toString(), false);
			String bus = GLD_Name (soln.get("?bus").toString(), true);
			String phs = OptionalString (soln, "?phs", "ABC");
			String conn = soln.get("?conn").toString();
			String grnd = soln.get("?grnd").toString();
			String ctrl = OptionalString (soln, "?ctrlenabled", "false");
			double nomu = new Double (soln.get("?nomu").toString()).doubleValue();
			double bsection = new Double (soln.get("?bsection").toString()).doubleValue();
			double kvar = nomu * nomu * bsection / 1000.0;

			buf.append (name + " @ " + bus + " on " + phs);
			buf.append (" " + df.format(nomu/1000.0) + " [kV] " + df.format(kvar) + " [kvar] " + "conn=" + conn + " grnd=" + grnd + "\n");
			if (ctrl.equals ("true")) {
				String mode = soln.get("?mode").toString();
				double setpoint = new Double (soln.get("?setpoint").toString()).doubleValue();
				double deadband = new Double (soln.get("?deadband").toString()).doubleValue();
				String moneq = soln.get("?moneq").toString();
				String monclass = soln.get("?monclass").toString();
				String monbus = soln.get("?monbus").toString();
				String monphs = soln.get("?monphs").toString();
				buf.append("  control mode=" + mode + " set=" + df.format(setpoint) + " bandwidth=" + df.format(deadband));
				buf.append(" monitoring: " + moneq + ":" + monclass + ":" + monbus + ":" + monphs);
				buf.append ("\n");
			}
		}
		return buf.toString();
	}

  public static void main (String args[]) throws UnsupportedEncodingException, FileNotFoundException {
		System.out.println(GetSubstation());
		System.out.println(GetCapacitors());
	}
       
}

