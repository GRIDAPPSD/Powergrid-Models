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
			String str = nd.toString();
			if (str.length() > 0) {
				return str;
			}
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

	static String GetLoads() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?name ?bus ?p ?q ?conn ?pz ?qz ?pi ?qi ?pp ?qp ?pe ?qe ?phs WHERE {"+
																	" ?s r:type c:EnergyConsumer."+
																	" ?s c:IdentifiedObject.name ?name."+
																	" ?s c:EnergyConsumer.pfixed ?p."+
																	" ?s c:EnergyConsumer.qfixed ?q."+
																	" ?s c:EnergyConsumer.phaseConnection ?connraw."+
																	" 	bind(strafter(str(?connraw),\"PhaseShuntConnectionKind.\") as ?conn)"+
																	" ?s c:EnergyConsumer.LoadResponse ?lr."+
																	" ?lr c:LoadResponseCharacteristic.pConstantImpedance ?pz."+
																	" ?lr c:LoadResponseCharacteristic.qConstantImpedance ?qz."+
																	" ?lr c:LoadResponseCharacteristic.pConstantCurrent ?pi."+
																	" ?lr c:LoadResponseCharacteristic.qConstantCurrent ?qi."+
																	" ?lr c:LoadResponseCharacteristic.pConstantPower ?pp."+
																	" ?lr c:LoadResponseCharacteristic.qConstantPower ?qp."+
																	" ?lr c:LoadResponseCharacteristic.pVoltageExponent ?pe."+
																	" ?lr c:LoadResponseCharacteristic.qVoltageExponent ?qe."+
																	" OPTIONAL {?ecp c:EnergyConsumerPhase.EnergyConsumer ?s."+
																	" ?ecp c:EnergyConsumerPhase.phase ?phsraw."+
																	" 	bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
																	" ?t c:Terminal.ConductingEquipment ?s."+
																	" ?t c:Terminal.ConnectivityNode ?cn."+
																	" ?cn c:IdentifiedObject.name ?bus"+
																	"} ORDER BY ?name ?phs");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String name = GLD_Name (soln.get("?name").toString(), false);
			String bus = GLD_Name (soln.get("?bus").toString(), true);
			String phs = OptionalString (soln, "?phs", "ABC");
			String conn = soln.get("?conn").toString();
			double p = 0.001 * new Double (soln.get("?p").toString()).doubleValue();
			double q = 0.001 * new Double (soln.get("?q").toString()).doubleValue();
			double pz = new Double (soln.get("?pz").toString()).doubleValue();
			double qz = new Double (soln.get("?qz").toString()).doubleValue();
			double pi = new Double (soln.get("?pi").toString()).doubleValue();
			double qi = new Double (soln.get("?qi").toString()).doubleValue();
			double pp = new Double (soln.get("?pp").toString()).doubleValue();
			double qp = new Double (soln.get("?qp").toString()).doubleValue();
			buf.append (name + " @ " + bus + " phs=" + phs + " conn=" + conn);
			buf.append (" kw=" + df.format(p) + " kvar=" + df.format(q));
			buf.append (" Real ZIP=" + df.format(pz) + ":" + df.format(pi) + ":" + df.format(pp));
			buf.append (" Reactive ZIP=" + df.format(qz) + ":" + df.format(qi) + ":" + df.format(qp));
			buf.append ("\n");
		}
		return buf.toString();
	}

	static String GetBaseVoltages() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?vnom WHERE {"+
																	" ?lev r:type c:BaseVoltage."+
																	" ?lev c:BaseVoltage.nominalVoltage ?vnom."+
																	"} ORDER BY ?vnom");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			double vnom = new Double (soln.get("?vnom").toString()).doubleValue();
			buf.append ("vnom=" + df.format(vnom) + "\n");
		}
		return buf.toString();
	}

	static String GetPhaseMatrices() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?name ?cnt ?seq ?r ?x ?b WHERE {"+
																	" ?s r:type c:PerLengthPhaseImpedance."+
																	" ?s c:IdentifiedObject.name ?name."+
																	" ?s c:PerLengthPhaseImpedance.conductorCount ?cnt."+
																	" ?elm c:PhaseImpedanceData.PhaseImpedance ?s."+
																	" ?elm c:PhaseImpedanceData.sequenceNumber ?seq."+
																	" ?elm c:PhaseImpedanceData.r ?r."+
																	" ?elm c:PhaseImpedanceData.x ?x."+
																	" ?elm c:PhaseImpedanceData.b ?b"+
																	"} ORDER BY ?name ?seq");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000000");
		while (results.hasNext()) {
			soln = results.next();
			String name = GLD_Name (soln.get("?name").toString(), false);
			int cnt = new Integer (soln.get("?cnt").toString()).intValue();
			int seq = new Integer (soln.get("?seq").toString()).intValue();
			double r = new Double (soln.get("?r").toString()).doubleValue();
			double x = new Double (soln.get("?x").toString()).doubleValue();
			double b = new Double (soln.get("?b").toString()).doubleValue();
			buf.append (name + " " + Integer.toString(seq) + ":" + Integer.toString(cnt) + " r=" + df.format(r) + " x=" + df.format(x) + " b=" + df.format(b) + "\n");
		}
		return buf.toString();
	}

	static String GetSequenceMatrices() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?name ?r1 ?x1 ?b1 ?r0 ?x0 ?b0 WHERE {"+
																	" ?s r:type c:PerLengthSequenceImpedance."+
																	" ?s c:IdentifiedObject.name ?name."+
																	" ?s c:PerLengthSequenceImpedance.r ?r."+
																	" ?s c:PerLengthSequenceImpedance.x ?x."+
																	" ?s c:PerLengthSequenceImpedance.b ?b."+
																	" ?s c:PerLengthSequenceImpedance.r0 ?r0."+
																	" ?s c:PerLengthSequenceImpedance.x0 ?x0."+
																	" ?s c:PerLengthSequenceImpedance.b0 ?b0"+
																	"} ORDER BY ?name");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String name = GLD_Name (soln.get("?name").toString(), false);
			double r1 = new Double (soln.get("?r").toString()).doubleValue();
			double x1 = new Double (soln.get("?x").toString()).doubleValue();
			double b1 = new Double (soln.get("?b").toString()).doubleValue();
			double r0 = new Double (soln.get("?r0").toString()).doubleValue();
			double x0 = new Double (soln.get("?x0").toString()).doubleValue();
			double b0 = new Double (soln.get("?b0").toString()).doubleValue();
			buf.append (name + " r1=" + df.format(r1) + " x1=" + df.format(x1) + " b1=" + df.format(b1));
			buf.append (" r0=" + df.format(r0) + " x0=" + df.format(x0) + " b0=" + df.format(b0) + "\n");
		}
		return buf.toString();
	}

	static String GetXfmrCodeRatings() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?pname ?tname ?enum ?ratedS ?ratedU ?conn ?ang ?res ?nll ?iexc WHERE {"+
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
																	" 	bind(strafter(str(?connraw),\"WindingConnection.\") as ?conn)"+
																	" ?nlt c:NoLoadTest.EnergisedEnd ?e."+
																	" ?nlt c:NoLoadTest.loss ?nll."+
																	" ?nlt c:NoLoadTest.excitingCurrent ?iexc."+
																	"} ORDER BY ?pname ?tname ?enum");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String pname = GLD_Name (soln.get("?pname").toString(), false);
			String tname = GLD_Name (soln.get("?tname").toString(), false);
			int wdg = new Integer (soln.get("?enum").toString()).intValue();
			String conn = soln.get("?conn").toString();
			int ang = new Integer (soln.get("?ang").toString()).intValue();
			double S = new Double (soln.get("?ratedS").toString()).doubleValue();
			double U = new Double (soln.get("?ratedU").toString()).doubleValue();
			double r = new Double (soln.get("?res").toString()).doubleValue();
			double nll = new Double (soln.get("?nll").toString()).doubleValue();
			buf.append (pname + ":" + tname + " wdg=" + Integer.toString(wdg) + " conn=" + conn + " ang=" + Integer.toString(ang));
			buf.append (" U=" + df.format(U) + " S=" + df.format(S) + " r=" + df.format(r) + " NLL=" + df.format(nll) + "\n");
		}
		return buf.toString();
	}

	static String GetXfmrCodeSCTests() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

	static String GetPowerXfmrCore() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

	static String GetPowerXfmrMesh() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

	static String GetOverheadWires() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

	static String GetTapeShieldCables() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

	static String GetConcentricNeutralCables() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

	static String GetLineSpacings() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

	static String GetSwitches() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?name (group_concat(distinct ?bus;separator=\"\\n\") as ?buses) (group_concat(distinct ?phs;separator=\"\\n\") as ?phases) ?open WHERE {"+
																	" ?s r:type c:LoadBreakSwitch."+
																	" ?s c:IdentifiedObject.name ?name."+
																	" ?s c:Switch.normalOpen ?open."+
																	" ?t c:Terminal.ConductingEquipment ?s."+
																	" ?t c:Terminal.ConnectivityNode ?cn."+
																	" ?cn c:IdentifiedObject.name ?bus"+
																	" OPTIONAL {?swp c:SwitchPhase.Switch ?s."+
																	" ?swp c:SwitchPhase.phaseSide1 ?phsraw."+
																	" 	bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
																  "}"+
																  " GROUP BY ?name ?open"+
																  " ORDER BY ?name");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String name = GLD_Name (soln.get("?name").toString(), false);
			String[] buses = soln.get("?buses").toString().split("\\n");
			String bus1 = GLD_Name(buses[0], true); 
			String bus2 = GLD_Name(buses[1], true); 
			String phases = OptionalString (soln, "?phases", "ABC");
			String open = soln.get("?open").toString();
			buf.append (name + " from " + bus1 + " to " + bus2 + " phases=" + phases + " open=" + open + "\n");
		}
		return buf.toString();
	}

	static String GetLinesInstanceZ() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

	static String GetLinesCodeZ() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?name (group_concat(distinct ?bus;separator=\"\\n\") as ?buses) (group_concat(distinct ?phs;separator=\"\\n\") as ?phases) ?len ?lname WHERE {"+
																	" ?s r:type c:ACLineSegment."+
																	" ?s c:IdentifiedObject.name ?name."+
																	" ?s c:Conductor.length ?len."+
																	" ?s c:ACLineSegment.PerLengthImpedance ?lcode."+
																	" ?lcode c:IdentifiedObject.name ?lname."+
																	" ?t c:Terminal.ConductingEquipment ?s."+
																	" ?t c:Terminal.ConnectivityNode ?cn."+
																	" ?cn c:IdentifiedObject.name ?bus"+
																	" OPTIONAL {?acp c:ACLineSegmentPhase.ACLineSegment ?s."+
																	" ?acp c:ACLineSegmentPhase.phase ?phsraw."+
																	" 	bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
																	"}"+
																	" GROUP BY ?name ?len ?lname"+
																	" ORDER BY ?name");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String name = GLD_Name (soln.get("?name").toString(), false);
			String[] buses = soln.get("?buses").toString().split("\\n");
			String bus1 = GLD_Name(buses[0], true); 
			String bus2 = GLD_Name(buses[1], true); 
			String phases = OptionalString (soln, "?phases", "ABC");
			phases = phases.replace ('\n', ':');
			double len = new Double (soln.get("?len").toString()).doubleValue();
			String lname = soln.get("?lname").toString();
			buf.append (name + " from " + bus1 + " to " + bus2 + " phases=" + phases + " len=" + df.format(len)  + " linecode=" + lname + "\n");
		}
		return buf.toString();
	}

	static String GetLinesSpacingZ() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

	static String GetRegulators() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

	static String GetXfmrTanks() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

	static String GetPowerXfmrWindings() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

	static String GetCoordinates() {
		StringBuilder buf = new StringBuilder ("");
		return buf.toString();
	}

  public static void main (String args[]) throws UnsupportedEncodingException, FileNotFoundException {
//		System.out.println(GetBaseVoltages());

//		System.out.println(GetPhaseMatrices());
//		System.out.println(GetSequenceMatrices());
		System.out.println(GetXfmrCodeRatings());
		System.out.println(GetXfmrCodeSCTests());
		System.out.println(GetPowerXfmrCore());
		System.out.println(GetPowerXfmrMesh());
		System.out.println(GetOverheadWires());
		System.out.println(GetTapeShieldCables());
		System.out.println(GetConcentricNeutralCables());
		System.out.println(GetLineSpacings());

//		System.out.println(GetSubstation());
//		System.out.println(GetCapacitors());
//		System.out.println(GetLoads());
//		System.out.println(GetSwitches());
		System.out.println(GetLinesInstanceZ());
//		System.out.println(GetLinesCodeZ());
		System.out.println(GetLinesSpacingZ());
		System.out.println(GetRegulators());
		System.out.println(GetXfmrTanks());
		System.out.println(GetPowerXfmrWindings());

		System.out.println(GetCoordinates());
	}
}

