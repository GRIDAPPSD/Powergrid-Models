package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2018, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistStorage extends DistComponent {
	public static final String szQUERY = 
	 	"SELECT ?name ?bus ?ratedS ?ratedU ?ipu ?ratedE ?storedE ?state ?p ?q ?id ?fdrid (group_concat(distinct ?phs;separator=\"\\n\") as ?phases) "+
		"WHERE {"+
	 	" ?s r:type c:BatteryUnit."+
		"	?s c:IdentifiedObject.name ?name."+
		"	?pec c:PowerElectronicsConnection.PowerElectronicsUnit ?s."+
		" ?pec c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
		"	?pec c:PowerElectronicsConnection.ratedS ?ratedS."+
		"	?pec c:PowerElectronicsConnection.ratedU ?ratedU."+
		"	?pec c:PowerElectronicsConnection.p ?p."+
		"	?pec c:PowerElectronicsConnection.q ?q."+
		" ?pec c:PowerElectronicsConnection.maxIFault ?ipu."+
		" ?s c:BatteryUnit.ratedE ?ratedE."+
		" ?s c:BatteryUnit.storedE ?storedE."+
		" ?s c:BatteryUnit.batteryState ?stateraw."+
		" 	bind(strafter(str(?stateraw),\"BatteryState.\") as ?state)"+
		"	OPTIONAL {?pecp c:PowerElectronicsConnectionPhase.PowerElectronicsConnection ?pec."+
		"	?pecp c:PowerElectronicsConnectionPhase.phase ?phsraw."+
		"		bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
		" bind(strafter(str(?s),\"#\") as ?id)."+
		"	?t c:Terminal.ConductingEquipment ?pec."+
		"	?t c:Terminal.ConnectivityNode ?cn."+ 
		"	?cn c:IdentifiedObject.name ?bus"+
	 	"} "+
		"GROUP by ?name ?bus ?ratedS ?ratedU ?ipu ?ratedE ?storedE ?state ?p ?q ?id ?fdrid "+
		"ORDER BY ?name";

	public String id;
	public String name;
	public String bus;
	public String phases;
	public String state;
	public double p;
	public double q;
	public double ratedU;
	public double ratedS;
	public double ratedE;
	public double storedE;
	public double maxIFault;
	public boolean bDelta;

	public String GetJSONEntry () {
		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"mRID\":\"" + id +"\"");
		buf.append (",\"CN1\":\"" + bus + "\"");
		buf.append (",\"phases\":\"" + phases + "\"");
		buf.append (",\"ratedS\":" + df1.format(ratedS));
		buf.append (",\"ratedU\":" + df1.format(ratedU));
		buf.append (",\"p\":" + df1.format(p));
		buf.append (",\"q\":" + df1.format(q));
		buf.append (",\"ratedE\":" + df1.format(ratedE));
		buf.append (",\"storedE\":" + df1.format(storedE));
		buf.append (",\"batteryState\":\"" + state + "\"");
		buf.append (",\"maxIFault\":" + df3.format(maxIFault));
		buf.append ("}");
		return buf.toString();
	}

	private String DSSBatteryState (String s) {
		if (s.equals("Charging")) return "charging";
		if (s.equals("Discharging")) return "discharging";
		if (s.equals("Waiting")) return "idling";
		if (s.equals("Full")) return "idling";
		if (s.equals("Empty")) return "idling";
		return "idling";
	}

	public DistStorage (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			bus = SafeName (soln.get("?bus").toString());
			phases = OptionalString (soln, "?phases", "ABC");
			phases = phases.replace ('\n', ':');
			p = Double.parseDouble (soln.get("?p").toString());
			q = Double.parseDouble (soln.get("?q").toString());
			ratedU = Double.parseDouble (soln.get("?ratedU").toString());
			ratedS = Double.parseDouble (soln.get("?ratedS").toString());
			maxIFault = Double.parseDouble (soln.get("?ipu").toString());
			bDelta = false;
			ratedE = Double.parseDouble (soln.get("?ratedE").toString());
			storedE = Double.parseDouble (soln.get("?storedE").toString());
			state = soln.get("?state").toString();
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " @ " + bus + " phases=" + phases);
		buf.append (" vnom=" + df4.format(ratedU) + " vanom=" + df4.format(ratedS));
		buf.append (" kw=" + df4.format(0.001 * p) + " kvar=" + df4.format(0.001 * q));
		buf.append (" capacity=" + df4.format(0.001 * ratedE) + " stored=" + df4.format(0.001 * storedE));
		buf.append (" " + DSSBatteryState (state) + " ilimit=" + df4.format(maxIFault));
		return buf.toString();
	}

	public String GetJSONSymbols(HashMap<String,DistCoordinates> map, HashMap<String,DistXfmrTank> mapTank) {
		DistCoordinates pt = map.get("BatteryUnit:" + name + ":1");

		StringBuilder buf = new StringBuilder ();

		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"parent\":\"" + bus +"\"");
		buf.append (",\"phases\":\"" + phases +"\"");
		buf.append (",\"kva\":" + df1.format(0.001 * ratedS));
		buf.append (",\"x1\":" + Double.toString(pt.x));
		buf.append (",\"y1\":" + Double.toString(pt.y));
		buf.append ("}");
		return buf.toString();
	}

	public String GetGLM() {
		StringBuilder buf = new StringBuilder ("object inverter {\n");

		buf.append ("  name \"inv_" + name + "\";\n");
		buf.append ("  parent \"" + bus + "_stmtr\";\n");
		if (bDelta && !phases.contains("D")) {
			buf.append ("  phases " + phases + "D;\n");
		} else if (!phases.contains("S") && !phases.contains("N")) {
			buf.append ("  phases " + phases + "N;\n");
		} else {
			buf.append ("  phases " + phases + ";\n");
		}
		buf.append ("  generator_status ONLINE;\n");
		buf.append ("  generator_mode CONSTANT_PQ;\n");
		buf.append ("  inverter_type FOUR_QUADRANT;\n");
		buf.append ("  four_quadrant_control_mode CONSTANT_PQ; // LOAD_FOLLOWING;\n");
		buf.append ("  charge_lockout_time 1;\n");
		buf.append ("  discharge_lockout_time 1;\n");
		buf.append ("  sense_object \"" + bus + "_stmtr\";\n");
		buf.append ("  charge_on_threshold " + df3.format (-0.02 * ratedS) + ";\n");
		buf.append ("  charge_off_threshold " + df3.format (0.0 * ratedS) + ";\n");
		buf.append ("  discharge_off_threshold " + df3.format (0.4 * ratedS) + ";\n");
		buf.append ("  discharge_on_threshold " + df3.format (0.6 * ratedS) + ";\n");
		buf.append ("  inverter_efficiency 0.975;\n");
		buf.append ("  V_base " + df3.format (ratedU) + ";\n");
		buf.append ("  rated_power " + df3.format (ratedS) + ";\n");
		buf.append ("  max_charge_rate " + df3.format (ratedS) + ";\n");
		buf.append ("  max_discharge_rate " + df3.format (ratedS) + ";\n");
		buf.append ("  P_Out " + df3.format (p) + ";\n");
		buf.append ("  Q_Out " + df3.format (q) + ";\n");
		buf.append ("  object battery {\n");
		buf.append ("    name \"bat_" + name + "\";\n");
		buf.append ("    nominal_voltage 48;\n");
		buf.append ("    battery_capacity " + df1.format (ratedE) + ";\n");
		buf.append ("    state_of_charge " + df4.format (storedE / ratedE) + ";\n");
		buf.append ("    use_internal_battery_model true;\n");
		buf.append ("    generator_mode CONSTANT_PQ;\n");
		buf.append ("    generator_status ONLINE;\n");
		buf.append ("    battery_type LI_ION;\n");
		buf.append ("    round_trip_efficiency 0.86;\n");
		buf.append ("    rated_power " + df3.format (ratedS) + ";\n");
		buf.append ("  };\n");
		buf.append ("}\n");

		return buf.toString();
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder ("new Storage." + name);

		int nphases = DSSPhaseCount(phases, bDelta);
		double kv = 0.001 * ratedU;
		double kva = 0.001 * ratedS;
		if (nphases < 2) { // 2-phase wye load should be line-line for secondary?
			kv /= Math.sqrt(3.0);
		}
		double pf = p / Math.sqrt(p*p + q*q);
		if (p*q < 0.0) {
			pf *= -1.0;
		}

		buf.append (" phases=" + Integer.toString(nphases) + " bus1=" + DSSShuntPhases (bus, phases, bDelta) + 
								" conn=" + DSSConn(bDelta) + " kva=" + df3.format(kva) + " kv=" + df3.format(kv) +
								" kwhrated=" + df3.format(0.001 * ratedE) + 
								" kwhstored=" + df3.format(0.001 * storedE) + " state=" + DSSBatteryState(state) +
								" vminpu=" + df4.format(1/maxIFault) + " LimitCurrent=yes kw=" + df2.format(p/1000.0));
		buf.append("\n");

		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

