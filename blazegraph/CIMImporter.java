//			----------------------------------------------------------
//			Copyright (c) 2017, Battelle Memorial Institute
//			All rights reserved.
//			----------------------------------------------------------

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

	static int OptionalInt (QuerySolution soln, String parm, int def) {
		RDFNode nd = soln.get(parm);
		if (nd != null) {
			String str = nd.toString();
			if (str.length() > 0) {
				return Integer.parseInt (str);
			}
		}
		return def;
	}

	static double OptionalDouble (QuerySolution soln, String parm, double def) {
		RDFNode nd = soln.get(parm);
		if (nd != null) {
			String str = nd.toString();
			if (str.length() > 0) {
				return Double.parseDouble (str);
			}
		}
		return def;
	}

	static boolean OptionalBoolean (QuerySolution soln, String parm, boolean def) {
		RDFNode nd = soln.get(parm);
		if (nd != null) {
			String str = nd.toString();
			if (str.length() > 0) {
				return Boolean.parseBoolean (str);
			}
		}
		return def;
	}

	/** prefix all bus names with `nd_` for GridLAB-D, so they "should" be unique
	 *	@param arg the root bus name, aka CIM name
	 *	@return nd_arg
	 */
	static String GldPrefixedNodeName (String arg) {
		return "nd_" + arg;
	}

	/** 
	 *	convert a CIM name to GridLAB-D name, replacing unallowed characters and prefixing for a bus/node
	 *	@param arg the root bus or component name, aka CIM name
	 *	@param bus to flag whether `nd_` should be prepended
	 *	@return the compatible name for GridLAB-D
	 */  
	static String GLD_Name (String arg, boolean bus) {			// GLD conversion
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
				buf.append("	control mode=" + mode + " set=" + df.format(setpoint) + " bandwidth=" + df.format(deadband));
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
																	" 			bind(strafter(str(?connraw),\"PhaseShuntConnectionKind.\") as ?conn)"+
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
																	" 			bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
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
		ResultSet results = RunQuery ("SELECT ?pname ?tname ?enum ?ratedS ?ratedU ?conn ?ang ?res WHERE {"+
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
																	" 			bind(strafter(str(?connraw),\"WindingConnection.\") as ?conn)"+
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
			buf.append (pname + ":" + tname + " wdg=" + Integer.toString(wdg) + " conn=" + conn + " ang=" + Integer.toString(ang));
			buf.append (" U=" + df.format(U) + " S=" + df.format(S) + " r=" + df.format(r) + "\n");
		}
		return buf.toString();
	}

	static String GetXfmrCodeOCTests() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?pname ?tname ?nll ?iexc WHERE {"+
																	" ?p r:type c:PowerTransformerInfo."+
																	" ?p c:IdentifiedObject.name ?pname."+
																	" ?t c:TransformerTankInfo.PowerTransformerInfo ?p."+
																	" ?t c:IdentifiedObject.name ?tname."+
																	" ?e c:TransformerEndInfo.TransformerTankInfo ?t."+
																	" ?nlt c:NoLoadTest.EnergisedEnd ?e."+
																	" ?nlt c:NoLoadTest.loss ?nll."+
																	" ?nlt c:NoLoadTest.excitingCurrent ?iexc."+
																	"} ORDER BY ?pname ?tname");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String pname = GLD_Name (soln.get("?pname").toString(), false);
			String tname = GLD_Name (soln.get("?tname").toString(), false);
			double nll = new Double (soln.get("?nll").toString()).doubleValue();
			double iexc = new Double (soln.get("?iexc").toString()).doubleValue();
			buf.append (pname + ":" + tname + " NLL=" + df.format(nll) + " iexc=" + df.format(iexc) + "\n");
		}
		return buf.toString();
	}

	static String GetXfmrCodeSCTests() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?pname ?tname ?enum ?gnum ?z ?ll WHERE {"+
																	" ?p r:type c:PowerTransformerInfo."+
																	" ?p c:IdentifiedObject.name ?pname."+
																	" ?t c:TransformerTankInfo.PowerTransformerInfo ?p."+
																	" ?t c:IdentifiedObject.name ?tname."+
																	" ?e c:TransformerEndInfo.TransformerTankInfo ?t."+
																	" ?e c:TransformerEndInfo.endNumber ?enum."+
																	" ?sct c:ShortCircuitTest.EnergisedEnd ?e."+
																	" ?sct c:ShortCircuitTest.leakageImpedance ?z."+
																	" ?sct c:ShortCircuitTest.loss ?ll."+
																	" ?sct c:ShortCircuitTest.GroundedEnds ?grnd."+
																	" ?grnd c:TransformerEndInfo.endNumber ?gnum."+
																	"} ORDER BY ?pname ?tname ?enum ?gnum");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String pname = GLD_Name (soln.get("?pname").toString(), false);
			String tname = GLD_Name (soln.get("?tname").toString(), false);
			int fwdg = new Integer (soln.get("?enum").toString()).intValue();
			int twdg = new Integer (soln.get("?gnum").toString()).intValue();
			double z = new Double (soln.get("?z").toString()).doubleValue();
			double ll = new Double (soln.get("?ll").toString()).doubleValue();
			buf.append (pname + ":" + tname + " fwdg=" + Integer.toString(fwdg) + " twdg=" + Integer.toString(twdg) +
									" z=" + df.format(z) + " LL=" + df.format(ll) + "\n");
		}
		return buf.toString();
	}

	static String GetPowerXfmrCore() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?pname ?enum ?b ?g WHERE {"+
																	" ?p r:type c:PowerTransformer."+
																	" ?p c:IdentifiedObject.name ?pname."+
																	" ?end c:PowerTransformerEnd.PowerTransformer ?p."+
																	" ?adm c:TransformerCoreAdmittance.TransformerEnd ?end."+
																	" ?end c:TransformerEnd.endNumber ?enum."+
																	" ?adm c:TransformerCoreAdmittance.b ?b."+
																	" ?adm c:TransformerCoreAdmittance.g ?g."+
																	"} ORDER BY ?pname");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String pname = GLD_Name (soln.get("?pname").toString(), false);
			int wdg = new Integer (soln.get("?enum").toString()).intValue();
			double b = new Double (soln.get("?b").toString()).doubleValue();
			double g = new Double (soln.get("?g").toString()).doubleValue();
			buf.append (pname + " wdg=" + Integer.toString(wdg) + " g=" + df.format(g) + " b=" + df.format(b) + "\n");
		}
		return buf.toString();
	}

	static String GetPowerXfmrMesh() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?pname ?fnum ?tnum ?r ?x WHERE {"+
																	" ?p r:type c:PowerTransformer."+
																	" ?p c:IdentifiedObject.name ?pname."+
																	" ?from c:PowerTransformerEnd.PowerTransformer ?p."+
																	" ?imp c:TransformerMeshImpedance.FromTransformerEnd ?from."+
																	" ?imp c:TransformerMeshImpedance.ToTransformerEnd ?to."+
																	" ?imp c:TransformerMeshImpedance.r ?r."+
																	" ?imp c:TransformerMeshImpedance.x ?x."+
																	" ?from c:TransformerEnd.endNumber ?fnum."+
																	" ?to c:TransformerEnd.endNumber ?tnum."+
																	"} ORDER BY ?pname ?fnum ?tnum");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String pname = GLD_Name (soln.get("?pname").toString(), false);
			int fwdg = new Integer (soln.get("?fnum").toString()).intValue();
			int twdg = new Integer (soln.get("?tnum").toString()).intValue();
			double r = new Double (soln.get("?r").toString()).doubleValue();
			double x = new Double (soln.get("?x").toString()).doubleValue();
			buf.append (pname + " fwdg=" + Integer.toString(fwdg) + " twdg=" + Integer.toString(twdg) + 
									" r=" + df.format(r) + " x=" + df.format(x) + "\n");
		}
		return buf.toString();
	}

	static String GetOverheadWires() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?name ?rad ?corerad ?gmr ?rdc ?r25 ?r50 ?r75 ?amps ?ins ?insmat ?insthick WHERE {"+
																	" ?w r:type c:OverheadWireInfo."+
																	" ?w c:IdentifiedObject.name ?name."+
																	" ?w c:WireInfo.radius ?rad."+
																	" ?w c:WireInfo.gmr ?gmr."+
																	" OPTIONAL {?w c:WireInfo.rDC20 ?rdc.}"+
																	" OPTIONAL {?w c:WireInfo.rAC25 ?r25.}"+
																	" OPTIONAL {?w c:WireInfo.rAC50 ?r50.}"+
																	" OPTIONAL {?w c:WireInfo.rAC75 ?r75.}"+
																	" OPTIONAL {?w c:WireInfo.coreRadius ?corerad.}"+
																	" OPTIONAL {?w c:WireInfo.ratedCurrent ?amps.}"+
																	" OPTIONAL {?w c:WireInfo.insulationMaterial ?insraw."+
																	" 	bind(strafter(str(?insraw),\"WireInsulationKind.\") as ?insmat)}"+
																	" OPTIONAL {?w c:WireInfo.insulated ?ins.}"+
																	" OPTIONAL {?w c:WireInfo.insulationThickness ?insthick.}"+
																	"} ORDER BY ?name");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String name = GLD_Name (soln.get("?name").toString(), false);
			double rad = new Double (soln.get("?rad").toString()).doubleValue();
			double gmr = new Double (soln.get("?gmr").toString()).doubleValue();
			double rdc = OptionalDouble (soln, "?rdc", 0.0);
			double r25 = OptionalDouble (soln, "?r25", 0.0);
			double r50 = OptionalDouble (soln, "?r50", 0.0);
			double r75 = OptionalDouble (soln, "?r75", 0.0);
			double corerad = OptionalDouble (soln, "?corerad", 0.0);
			double amps = OptionalDouble (soln, "?amps", 0.0);
			double insthick = OptionalDouble (soln, "?insthick", 0.0);
			boolean ins = OptionalBoolean (soln, "?ins", false);
			String insmat = OptionalString (soln, "?insmat", "N/A");
			buf.append (name + " rad=" + df.format(rad) + " gmr=" + df.format(gmr) + " rdc=" + df.format(rdc)); 
			buf.append (" r25=" + df.format(r25) + " r50=" + df.format(r50) + " r75=" + df.format(r75)); 
			buf.append (" corerad=" + df.format(corerad) + " amps=" + df.format(amps)); 
			buf.append (" ins=" + Boolean.toString(ins) + " insmat=" + insmat + " insthick=" + df.format(insthick)); 
			buf.append ("\n");
		}
		return buf.toString();
	}

	static String GetTapeShieldCables() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?name ?rad ?corerad ?gmr ?rdc ?r25 ?r50 ?r75 ?amps ?ins ?insmat"+
																	" ?insthick ?diacore ?diains ?diascreen ?diajacket ?sheathneutral"+
																	" ?tapelap ?tapethickness WHERE {"+
																	" ?w r:type c:TapeShieldCableInfo."+
																	" ?w c:IdentifiedObject.name ?name."+
																	" ?w c:WireInfo.radius ?rad."+
																	" ?w c:WireInfo.gmr ?gmr."+
																	" OPTIONAL {?w c:WireInfo.rDC20 ?rdc.}"+
																	" OPTIONAL {?w c:WireInfo.rAC25 ?r25.}"+
																	" OPTIONAL {?w c:WireInfo.rAC50 ?r50.}"+
																	" OPTIONAL {?w c:WireInfo.rAC75 ?r75.}"+
																	" OPTIONAL {?w c:WireInfo.coreRadius ?corerad.}"+
																	" OPTIONAL {?w c:WireInfo.ratedCurrent ?amps.}"+
																	" OPTIONAL {?w c:WireInfo.insulationMaterial ?insraw."+
																	" 		bind(strafter(str(?insraw),\"WireInsulationKind.\") as ?insmat)}"+
																	" OPTIONAL {?w c:WireInfo.insulated ?ins.}"+
																	" OPTIONAL {?w c:WireInfo.insulationThickness ?insthick.}"+
																	" OPTIONAL {?w c:CableInfo.diameterOverCore ?diacore.}"+
																	" OPTIONAL {?w c:CableInfo.diameterOverJacket ?diajacket.}"+
																	" OPTIONAL {?w c:CableInfo.diameterOverInsulation ?diains.}"+
																	" OPTIONAL {?w c:CableInfo.diameterOverScreen ?diascreen.}"+
																	" OPTIONAL {?w c:CableInfo.sheathAsNeutral ?sheathneutral.}"+
																	" OPTIONAL {?w c:TapeShieldCableInfo.tapeLap ?tapelap.}"+
																	" OPTIONAL {?w c:TapeShieldCableInfo.tapeThickness ?tapethickness.}"+
																	"} ORDER BY ?name");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String name = GLD_Name (soln.get("?name").toString(), false);
			double rad = new Double (soln.get("?rad").toString()).doubleValue();
			double gmr = new Double (soln.get("?gmr").toString()).doubleValue();
			double rdc = OptionalDouble (soln, "?rdc", 0.0);
			double r25 = OptionalDouble (soln, "?r25", 0.0);
			double r50 = OptionalDouble (soln, "?r50", 0.0);
			double r75 = OptionalDouble (soln, "?r75", 0.0);
			double corerad = OptionalDouble (soln, "?corerad", 0.0);
			double amps = OptionalDouble (soln, "?amps", 0.0);
			double insthick = OptionalDouble (soln, "?insthick", 0.0);
			boolean ins = OptionalBoolean (soln, "?ins", false);
			String insmat = OptionalString (soln, "?insmat", "N/A");
			double dcore = OptionalDouble (soln, "?diacore", 0.0);
			double djacket = OptionalDouble (soln, "?diajacket", 0.0);
			double dins = OptionalDouble (soln, "?diains", 0.0);
			double dscreen = OptionalDouble (soln, "?diascreen", 0.0);
			boolean sheathNeutral = OptionalBoolean (soln, "?sheathneutral", false);
			double tlap = OptionalDouble (soln, "?tapelap", 0.0);
			double tthick = OptionalDouble (soln, "?tapethickness", 0.0);
			buf.append (name + " rad=" + df.format(rad) + " gmr=" + df.format(gmr) + " rdc=" + df.format(rdc)); 
			buf.append (" r25=" + df.format(r25) + " r50=" + df.format(r50) + " r75=" + df.format(r75)); 
			buf.append (" corerad=" + df.format(corerad) + " amps=" + df.format(amps)); 
			buf.append (" ins=" + Boolean.toString(ins) + " insmat=" + insmat + " insthick=" + df.format(insthick));
			buf.append (" dcore=" + df.format(dcore) + " djacket=" + df.format(djacket) + " dins=" + df.format(dins)); 
			buf.append (" dscreen=" + df.format(dscreen) + " sheathNeutral=" + Boolean.toString(sheathNeutral)); 
			buf.append (" tlap=" + df.format(tlap) + " tthick=" + df.format(tthick));
			buf.append ("\n");
		}
		return buf.toString();
	}

	static String GetConcentricNeutralCables() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?name ?rad ?corerad ?gmr ?rdc ?r25 ?r50 ?r75 ?amps ?ins ?insmat"+
																	" ?insthick ?diacore ?diains ?diascreen ?diajacket ?sheathneutral"+
																	" ?strand_cnt ?strand_rad ?strand_gmr ?strand_rdc WHERE {"+
																	" ?w r:type c:ConcentricNeutralCableInfo."+
																	" ?w c:IdentifiedObject.name ?name."+
																	" ?w c:WireInfo.radius ?rad."+
																	" ?w c:WireInfo.gmr ?gmr."+
																	" OPTIONAL {?w c:WireInfo.rDC20 ?rdc.}"+
																	" OPTIONAL {?w c:WireInfo.rAC25 ?r25.}"+
																	" OPTIONAL {?w c:WireInfo.rAC50 ?r50.}"+
																	" OPTIONAL {?w c:WireInfo.rAC75 ?r75.}"+
																	" OPTIONAL {?w c:WireInfo.coreRadius ?corerad.}"+
																	" OPTIONAL {?w c:WireInfo.ratedCurrent ?amps.}"+
																	" OPTIONAL {?w c:WireInfo.insulationMaterial ?insraw."+
																	" 		bind(strafter(str(?insraw),\"WireInsulationKind.\") as ?insmat)}"+
																	" OPTIONAL {?w c:WireInfo.insulated ?ins.}"+
																	" OPTIONAL {?w c:WireInfo.insulationThickness ?insthick.}"+
																	" OPTIONAL {?w c:CableInfo.diameterOverCore ?diacore.}"+
																	" OPTIONAL {?w c:CableInfo.diameterOverJacket ?diajacket.}"+
																	" OPTIONAL {?w c:CableInfo.diameterOverInsulation ?diains.}"+
																	" OPTIONAL {?w c:CableInfo.diameterOverScreen ?diascreen.}"+
																	" OPTIONAL {?w c:CableInfo.sheathAsNeutral ?sheathneutral.}"+
																	" OPTIONAL {?w c:ConcentricNeutralCableInfo.diameterOverNeutral ?dianeut.}"+
																	" OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandCount ?strand_cnt.}"+
																	" OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandGmr ?strand_gmr.}"+
																	" OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandRadius ?strand_rad.}"+
																	" OPTIONAL {?w c:ConcentricNeutralCableInfo.neutralStrandRDC20 ?strand_rdc.}"+
																	"} ORDER BY ?name");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String name = GLD_Name (soln.get("?name").toString(), false);
			double rad = new Double (soln.get("?rad").toString()).doubleValue();
			double gmr = new Double (soln.get("?gmr").toString()).doubleValue();
			double rdc = OptionalDouble (soln, "?rdc", 0.0);
			double r25 = OptionalDouble (soln, "?r25", 0.0);
			double r50 = OptionalDouble (soln, "?r50", 0.0);
			double r75 = OptionalDouble (soln, "?r75", 0.0);
			double corerad = OptionalDouble (soln, "?corerad", 0.0);
			double amps = OptionalDouble (soln, "?amps", 0.0);
			double insthick = OptionalDouble (soln, "?insthick", 0.0);
			boolean ins = OptionalBoolean (soln, "?ins", false);
			String insmat = OptionalString (soln, "?insmat", "N/A");
			double dcore = OptionalDouble (soln, "?diacore", 0.0);
			double djacket = OptionalDouble (soln, "?diajacket", 0.0);
			double dins = OptionalDouble (soln, "?diains", 0.0);
			double dscreen = OptionalDouble (soln, "?diascreen", 0.0);
			boolean sheathNeutral = OptionalBoolean (soln, "?sheathneutral", false);
			double tlap = OptionalDouble (soln, "?tapelap", 0.0);
			double tthick = OptionalDouble (soln, "?tapethickness", 0.0);
			double dneut = OptionalDouble (soln, "?dianeut", 0.0);
			int strand_cnt = OptionalInt (soln, "?strand_cnt", 0);
			double strand_gmr = OptionalDouble (soln, "?strand_gmr", 0.0);
			double strand_rad = OptionalDouble (soln, "?strand_rad", 0.0);
			double strand_rdc = OptionalDouble (soln, "?strand_rdc", 0.0);
			buf.append (name + " rad=" + df.format(rad) + " gmr=" + df.format(gmr) + " rdc=" + df.format(rdc)); 
			buf.append (" r25=" + df.format(r25) + " r50=" + df.format(r50) + " r75=" + df.format(r75)); 
			buf.append (" corerad=" + df.format(corerad) + " amps=" + df.format(amps)); 
			buf.append (" ins=" + Boolean.toString(ins) + " insmat=" + insmat + " insthick=" + df.format(insthick));
			buf.append (" dcore=" + df.format(dcore) + " djacket=" + df.format(djacket) + " dins=" + df.format(dins)); 
			buf.append (" dscreen=" + df.format(dscreen) + " sheathNeutral=" + Boolean.toString(sheathNeutral)); 
			buf.append (" dneut=" + df.format(tlap) + " strand_cnt=" + Integer.toString(strand_cnt));
			buf.append (" strand_gmr=" + df.format(strand_gmr) + " strand_rad=" + df.format(strand_rad) + " strand_rdc=" + df.format(strand_rdc));
			buf.append ("\n");
		}
		return buf.toString();
	}

	static String GetLineSpacings() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?name ?cable ?usage ?bundle_count ?bundle_sep"+
																	" (group_concat(?phs;separator=\"\\n\") as ?phases)"+
																	" (group_concat(?x;separator=\"\\n\") as ?xarray)"+
																	" (group_concat(?y;separator=\"\\n\") as ?yarray) WHERE {"+
																	" ?w r:type c:WireSpacingInfo."+
																	" ?w c:IdentifiedObject.name ?name."+
																	" ?pos c:WirePosition.WireSpacingInfo ?w."+
																	" ?pos c:WirePosition.xCoord ?x."+
																	" ?pos c:WirePosition.yCoord ?y."+
																	" ?pos c:WirePosition.phase ?phsraw."+
																	" 	bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs)"+ 
																	" OPTIONAL {?w c:WireSacingInfo.isCable ?cable.}"+
																	" OPTIONAL {?w c:WireSpacingInfo.phaseWireCount ?bundle_count.}"+
																	" OPTIONAL {?w c:WireSpacingInfo.phaseWireSpacing ?bundle_sep.}"+
																	" OPTIONAL {?w c:WireSpacingInfo.usage ?useraw."+
																	" 	bind(strafter(str(?useraw),\"WireUsageKind.\") as ?usage)}"+
																	"} GROUP BY ?name ?cable ?usage ?bundle_count ?bundle_sep ORDER BY ?name");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String name = GLD_Name (soln.get("?name").toString(), false);
			String[] phases = soln.get("?phases").toString().split("\\n");
			String[] xarray = soln.get("?xarray").toString().split("\\n");
			String[] yarray = soln.get("?yarray").toString().split("\\n");
			int nwires = phases.length;
			boolean cable = OptionalBoolean (soln, "?cable", false);
			String usage = OptionalString (soln, "?usage", "distribution");
			double b_sep = OptionalDouble (soln, "?bundle_sep", 0.0);
			int b_cnt = OptionalInt (soln, "?bundle_count", 0);
			buf.append (name + " nwires=" + Integer.toString(nwires) + " cable=" + Boolean.toString(cable) + " usage=" + usage); 
			buf.append (" b_cnt=" + Integer.toString(b_cnt) + " b_sep=" + df.format(b_sep));
			buf.append("\n");
			for (int i = 0; i < nwires; i++) {
					buf.append ("  phs=" + phases[i] + " x=" + xarray[i] + " y=" + yarray[i] + "\n");
			}
		}
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
																	" 			bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
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
		ResultSet results = RunQuery ("SELECT ?name (group_concat(distinct ?bus;separator=\"\\n\") as ?buses) ?len ?r ?x ?b ?r0 ?x0 ?b0 WHERE {"+
																	" ?s r:type c:ACLineSegment."+
																	" ?s c:IdentifiedObject.name ?name."+
																	" ?s c:Conductor.length ?len."+
																	" ?s c:ACLineSegment.r ?r."+
																	" ?s c:ACLineSegment.x ?x."+
																	" OPTIONAL {?s c:ACLineSegment.b ?b.}"+
																	" OPTIONAL {?s c:ACLineSegment.r0 ?r0.}"+
																	" OPTIONAL {?s c:ACLineSegment.x0 ?x0.}"+
																	" OPTIONAL {?s c:ACLineSegment.b0 ?b0.}"+
																	" ?t c:Terminal.ConductingEquipment ?s."+
																	" ?t c:Terminal.ConnectivityNode ?cn. "+
																	" ?cn c:IdentifiedObject.name ?bus"+
																	"}"+
																	" GROUP BY ?name ?len ?r ?x ?b ?r0 ?x0 ?b0"+
																	" ORDER BY ?name");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String name = GLD_Name (soln.get("?name").toString(), false);
			String[] buses = soln.get("?buses").toString().split("\\n");
			String bus1 = GLD_Name(buses[0], true); 
			String bus2 = GLD_Name(buses[1], true); 
			String phases = "ABC";
			double len = new Double (soln.get("?len").toString()).doubleValue();
			double r1 = new Double (soln.get("?r").toString()).doubleValue();
			double x1 = new Double (soln.get("?x").toString()).doubleValue();
			double b1 = OptionalDouble (soln, "?b", 0.0);
			double r0 = OptionalDouble (soln, "?r0", 0.0);
			double x0 = OptionalDouble (soln, "?x0", 0.0);
			double b0 = OptionalDouble (soln, "?b0", 0.0);
			buf.append (name + " from " + bus1 + " to " + bus2 + " phases=" + phases + " len=" + df.format(len));
			buf.append (" r1=" + df.format(r1) + " x1=" + df.format(x1) + " b1=" + df.format(b1));
			buf.append (" r0=" + df.format(r0) + " x0=" + df.format(x0) + " b0=" + df.format(b0) + "\n");
		}
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
																	" 			bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
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
		ResultSet results = RunQuery ("SELECT ?name (group_concat(distinct ?bus;separator=\"\\n\") as ?buses)"+
																	" 	(group_concat(distinct ?phs;separator=\"\\n\") as ?phases)"+
																	" 	?len ?spacing ?wname ?wclass"+
																	" 	(group_concat(distinct ?phname;separator=\"\\n\") as ?phwires)"+
																	" 	(group_concat(distinct ?phclass;separator=\"\\n\") as ?phclasses) WHERE {"+
																	" ?s r:type c:ACLineSegment."+
																	" ?s c:IdentifiedObject.name ?name."+
																	" ?s c:Conductor.length ?len."+
																	" ?asset c:Asset.PowerSystemResources ?s."+
																	" ?asset c:Asset.AssetInfo ?inf."+
																	" ?inf c:IdentifiedObject.name ?spacing."+
																	" ?inf a c:WireSpacingInfo."+
																	" OPTIONAL {"+
																	" 	?wasset c:Asset.PowerSystemResources ?s."+
																	" 	?wasset c:Asset.AssetInfo ?winf."+
																	" 	?winf c:WireInfo.radius ?rad."+
																	" 	?winf c:IdentifiedObject.name ?wname."+
																	" 	?winf a ?classraw."+
																	" 		bind(strafter(str(?classraw),\"cim16#\") as ?wclass)"+
																	" }"+
																	" ?t c:Terminal.ConductingEquipment ?s."+
																	" ?t c:Terminal.ConnectivityNode ?cn."+
																	" ?cn c:IdentifiedObject.name ?bus"+
																	" OPTIONAL {"+
																	" 	?acp c:ACLineSegmentPhase.ACLineSegment ?s."+
																	" 	?acp c:ACLineSegmentPhase.phase ?phsraw."+
																	" 		bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs)"+
																	" 	OPTIONAL {"+
																	" 		?phasset c:Asset.PowerSystemResources ?acp."+
																	" 		?phasset c:Asset.AssetInfo ?phinf."+
																	" 		?phinf c:WireInfo.radius ?phrad."+
																	" 		?phinf c:IdentifiedObject.name ?phname."+
																	" 		?phinf a ?phclassraw."+
																	" 			bind(strafter(str(?phclassraw),\"cim16#\") as ?phclass)"+
																	" 		}"+
																	" 	}"+
																	" }"+
																	" GROUP BY ?name ?len ?spacing ?wname ?wclass"+
																	" ORDER BY ?name");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String name = GLD_Name (soln.get("?name").toString(), false);
			String[] buses = soln.get("?buses").toString().split("\\n");
			String bus1 = GLD_Name(buses[0], true); 
			String bus2 = GLD_Name(buses[1], true); 
			double len = new Double (soln.get("?len").toString()).doubleValue();
			String spacing = soln.get("?spacing").toString();
			String wname = soln.get("?wname").toString();
			String wclass = soln.get("?wclass").toString();
			buf.append (name + " from " + bus1 + " to " + bus2 + " len=" + df.format(len) + " spacing=" + spacing);
			buf.append (" wname=" + wname + "wclass=" + wclass + "\n");
			String phases = OptionalString (soln, "?phases", "");
			String phwires = OptionalString (soln, "?phwires", "");
			String phclasses = OptionalString (soln, "?phclasses", "");
			if (phases.length() > 0) {
				String[] phs = phases.split("\\n");
				String[] phwire = phwires.split("\\n");
				String[] phclass = phclasses.split("\\n");
				String lastWire = phwire[0];
				String lastClass = phclass[0];
				for (int i = 0; i < phs.length; i++) {
					buf.append ("  phs=" + phs[i]);
					if (i < phwire.length) {
						lastWire = phwire[i];
					}
					if (i < phclass.length) {
						lastClass = phclass[i];
					}
					buf.append(" wire=" + lastWire + " class=" + lastClass + "\n");
				}
      }
		}
		return buf.toString();
	}

	static String GetRegulators() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?rname ?pname ?wnum ?phs ?incr ?mode ?enabled ?highStep ?lowStep ?neutralStep"+
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
																	" ORDER BY ?rname ?wnum");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String rname = GLD_Name (soln.get("?rname").toString(), false);
			String pname = GLD_Name (soln.get("?pname").toString(), false);
			String phs = soln.get("?phs").toString();
			String monphs = soln.get("?monphs").toString();
			String mode = soln.get("?mode").toString();
			String ctlmode = soln.get("?ctlmode").toString();
			int wnum = Integer.parseInt (soln.get("?wnum").toString());
			int highStep = Integer.parseInt (soln.get("?highStep").toString());
			int lowStep = Integer.parseInt (soln.get("?lowStep").toString());
			int neutralStep = Integer.parseInt (soln.get("?neutralStep").toString());
			int normalStep = Integer.parseInt (soln.get("?normalStep").toString());
			boolean enabled = Boolean.parseBoolean (soln.get("?enabled").toString());
			boolean ldc = Boolean.parseBoolean (soln.get("?ldc").toString());
			boolean ltc = Boolean.parseBoolean (soln.get("?ltc").toString());
			boolean discrete = Boolean.parseBoolean (soln.get("?discrete").toString());
			boolean ctl_enabled = Boolean.parseBoolean (soln.get("?ctl_enabled").toString());
			double incr = new Double (soln.get("?incr").toString()).doubleValue();
			double neutralU = new Double (soln.get("?neutralU").toString()).doubleValue();
			double step = new Double (soln.get("?step").toString()).doubleValue();
			double initDelay = new Double (soln.get("?initDelay").toString()).doubleValue();
			double subDelay = new Double (soln.get("?subDelay").toString()).doubleValue();
			double vlim = new Double (soln.get("?vlim").toString()).doubleValue();
			double vset = new Double (soln.get("?vset").toString()).doubleValue();
			double vbw = new Double (soln.get("?vbw").toString()).doubleValue();
			double fwdR = new Double (soln.get("?fwdR").toString()).doubleValue();
			double fwdX = new Double (soln.get("?fwdX").toString()).doubleValue();
			double revR = new Double (soln.get("?revR").toString()).doubleValue();
			double revX = new Double (soln.get("?revX").toString()).doubleValue();
			double ctRating = new Double (soln.get("?ctRating").toString()).doubleValue();
			double ctRatio = new Double (soln.get("?ctRatio").toString()).doubleValue();
			double ptRatio = new Double (soln.get("?ptRatio").toString()).doubleValue();

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
		}
		return buf.toString();
	}

	static String GetXfmrTanks() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?pname ?tname ?xfmrcode ?vgrp ?enum ?bus ?phs ?grounded ?rground ?xground WHERE {"+
																	" ?p r:type c:PowerTransformer."+
																	" ?p c:IdentifiedObject.name ?pname."+
																	" ?p c:PowerTransformer.vectorGroup ?vgrp."+
																	" ?t c:TransformerTank.PowerTransformer ?p."+
																	" ?t c:IdentifiedObject.name ?tname."+
																	" ?asset c:Asset.PowerSystemResources ?t."+
																	" ?asset c:Asset.AssetInfo ?inf."+
																	" ?inf c:IdentifiedObject.name ?xfmrcode."+
																	" ?end c:TransformerTankEnd.TransformerTank ?t."+
																	" ?end c:TransformerTankEnd.phases ?phsraw."+
																	"  bind(strafter(str(?phsraw),\"PhaseCode.\") as ?phs)"+
																	" ?end c:TransformerEnd.endNumber ?enum."+
																	" ?end c:TransformerEnd.grounded ?grounded."+
																	" OPTIONAL {?end c:TransformerEnd.rground ?rground.}"+
																	" OPTIONAL {?end c:TransformerEnd.xground ?xground.}"+
																	" ?end c:TransformerEnd.Terminal ?trm."+
																	" ?trm c:Terminal.ConnectivityNode ?cn."+ 
																	" ?cn c:IdentifiedObject.name ?bus"+
																	"}"+
																	" ORDER BY ?pname ?tname ?enum");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String pname = GLD_Name (soln.get("?pname").toString(), false);
			String tname = GLD_Name (soln.get("?tname").toString(), false);
			String bus = GLD_Name (soln.get("?bus").toString(), true);
			String phs = soln.get("?phs").toString();
			String vgrp = soln.get("?vgrp").toString();
			String tankinfo = GLD_Name (soln.get("?xfmrcode").toString(), false);
			double rg = OptionalDouble (soln, "?rground", 0.0);
			double xg = OptionalDouble (soln, "?xground", 0.0);
			int wdg = Integer.parseInt (soln.get("?enum").toString());
			boolean grounded = Boolean.parseBoolean (soln.get("?grounded").toString());
			buf.append (pname + ":" + tname + ":" + Integer.toString(wdg) + " on " + bus + ":" + phs);
			buf.append (" vgrp=" + vgrp + " tankinfo=" + tankinfo);
			buf.append (" grounded=" + Boolean.toString(grounded) + " rg=" + df.format(rg) + " xg=" + df.format(xg) + "\n");
		}
		return buf.toString();
	}

	static String GetPowerXfmrWindings() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?pname ?vgrp ?enum ?bus ?conn ?ratedS ?ratedU ?r ?ang ?grounded ?rground ?xground WHERE {"+
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
																	" ?cn c:IdentifiedObject.name ?bus"+
																	"}"+
																	" ORDER BY ?pname ?enum");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String pname = GLD_Name (soln.get("?pname").toString(), false);
			String bus = GLD_Name (soln.get("?bus").toString(), true);
			String phs = "ABC";
			String vgrp = soln.get("?vgrp").toString();
			String conn = soln.get("?conn").toString();
			double ratedU = Double.parseDouble (soln.get("?ratedU").toString());
			double ratedS = Double.parseDouble (soln.get("?ratedS").toString());
			double r = Double.parseDouble (soln.get("?r").toString());
			int wdg = Integer.parseInt (soln.get("?enum").toString());
			int ang = Integer.parseInt (soln.get("?ang").toString());
			boolean grounded = Boolean.parseBoolean (soln.get("?grounded").toString());
			double rg = OptionalDouble (soln, "?rground", 0.0);
			double xg = OptionalDouble (soln, "?xground", 0.0);
			buf.append (pname + ":" + Integer.toString(wdg) + " on " + bus + ":" + phs);
			buf.append (" vgrp=" + vgrp + " conn=" + conn + " ang=" + Integer.toString(ang));
			buf.append (" U=" + df.format(ratedU) + " S=" + df.format(ratedS) + " r=" + df.format(r));
			buf.append (" grounded=" + Boolean.toString(grounded) + " rg=" + df.format(rg) + " xg=" + df.format(xg) + "\n");
		}
		return buf.toString();
	}

	static String GetCoordinates() {
		StringBuilder buf = new StringBuilder ("");
		ResultSet results = RunQuery ("SELECT ?class ?name ?seq ?x ?y WHERE {"+
																	" ?eq c:PowerSystemResource.Location ?loc."+
																	" ?eq c:IdentifiedObject.name ?name."+
																	" ?eq a ?classraw."+
																	"  bind(strafter(str(?classraw),\"cim16#\") as ?class)"+
																	" ?pt c:PositionPoint.Location ?loc."+
																	" ?pt c:PositionPoint.xPosition ?x."+
																	" ?pt c:PositionPoint.yPosition ?y."+
																	" ?pt c:PositionPoint.sequenceNumber ?seq."+
																	" FILTER (!regex(?class, \"Phase\"))."+
																	" FILTER (!regex(?class, \"TapChanger\"))."+
																	" FILTER (!regex(?class, \"Tank\"))."+
																	" FILTER (!regex(?class, \"RegulatingControl\"))."+
																	"}"+
																	" ORDER BY ?class ?name ?seq ?x ?y");
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.0000");
		while (results.hasNext()) {
			soln = results.next();
			String name = GLD_Name (soln.get("?name").toString(), false);
			double x = Double.parseDouble (soln.get("?x").toString());
			double y = Double.parseDouble (soln.get("?y").toString());
			int seq = Integer.parseInt (soln.get("?seq").toString());
			String cname = soln.get("?class").toString();
			buf.append (cname + ":" + name + ":" + Integer.toString(seq) + " x=" + df.format(x) + " y=" + df.format(y) + "\n");
		}
		return buf.toString();
	}

	public static void main (String args[]) throws UnsupportedEncodingException, FileNotFoundException {
		System.out.println(GetBaseVoltages());

		System.out.println(GetPhaseMatrices());
		System.out.println(GetSequenceMatrices());
		System.out.println(GetXfmrCodeRatings());
		System.out.println(GetXfmrCodeOCTests());
		System.out.println(GetXfmrCodeSCTests());
		System.out.println(GetPowerXfmrCore());
		System.out.println(GetPowerXfmrMesh());
		System.out.println(GetOverheadWires());
		System.out.println(GetTapeShieldCables());
		System.out.println(GetConcentricNeutralCables());
		System.out.println(GetLineSpacings());

		System.out.println(GetSubstation());
		System.out.println(GetCapacitors());
		System.out.println(GetLoads());
		System.out.println(GetSwitches());
		System.out.println(GetLinesInstanceZ());
		System.out.println(GetLinesCodeZ());
		System.out.println(GetLinesSpacingZ());
		System.out.println(GetRegulators());
		System.out.println(GetXfmrTanks());
		System.out.println(GetPowerXfmrWindings());

		System.out.println(GetCoordinates());
	}
}

