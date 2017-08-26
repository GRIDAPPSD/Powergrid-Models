//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import java.text.DecimalFormat;

public class DistLoad extends DistComponent {
	static final String szQUERY = 
			"SELECT ?name ?bus ?basev ?p ?q ?conn ?pz ?qz ?pi ?qi ?pp ?qp ?pe ?qe ?phs WHERE {"+
			" ?s r:type c:EnergyConsumer."+
			" ?s c:IdentifiedObject.name ?name."+
		  " ?s c:ConductingEquipment.BaseVoltage ?bv."+
		  " ?bv c:BaseVoltage.nominalVoltage ?basev."+
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
			"} ORDER BY ?name ?phs";

	public String name;
	public String bus;
	public String phs;
	public String conn;
	public double basev;
	public double p;
	public double q;
	public double pz;
	public double qz;
	public double pi;
	public double qi;
	public double pp;
	public double qp;
	public double pe;
	public double qe;

	public DistLoad (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = GLD_Name (soln.get("?name").toString(), false);
			bus = GLD_Name (soln.get("?bus").toString(), true);
			basev = Double.parseDouble (soln.get("?basev").toString());
			phs = OptionalString (soln, "?phs", "ABC");
			conn = soln.get("?conn").toString();
			p = 0.001 * Double.parseDouble (soln.get("?p").toString());
			q = 0.001 * Double.parseDouble (soln.get("?q").toString());
			pz = Double.parseDouble (soln.get("?pz").toString());
			qz = Double.parseDouble (soln.get("?qz").toString());
			pi = Double.parseDouble (soln.get("?pi").toString());
			qi = Double.parseDouble (soln.get("?qi").toString());
			pp = Double.parseDouble (soln.get("?pp").toString());
			qp = Double.parseDouble (soln.get("?qp").toString());
			pe = Double.parseDouble (soln.get("?pe").toString());
			qe = Double.parseDouble (soln.get("?qe").toString());
		}		
	}

	public String DisplayString() {
		DecimalFormat df = new DecimalFormat("#.0000");
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " @ " + bus + " basev=" + df.format (basev) + " phs=" + phs + " conn=" + conn);
		buf.append (" kw=" + df.format(p) + " kvar=" + df.format(q));
		buf.append (" Real ZIP=" + df.format(pz) + ":" + df.format(pi) + ":" + df.format(pp));
		buf.append (" Reactive ZIP=" + df.format(qz) + ":" + df.format(qi) + ":" + df.format(qp));
		buf.append (" Exponents=" + df.format(pe) + ":" + df.format(qe));
		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

