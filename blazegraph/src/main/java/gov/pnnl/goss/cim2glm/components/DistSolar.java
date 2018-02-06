package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2018, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistSolar extends DistComponent {
	public static final String szQUERY = 
	 	"SELECT ?name ?bus ?ratedS ?ratedU ?p ?q ?id (group_concat(distinct ?phs;separator=\"\\n\") as ?phases) "+
		"WHERE {"+
	 	" ?s r:type c:PhotovoltaicUnit."+
		"	?s c:IdentifiedObject.name ?name."+
		"	?pec c:PowerElectronicsConnection.PowerElectronicsUnit ?s."+
		"	?pec c:PowerElectronicsConnection.ratedS ?ratedS."+
		"	?pec c:PowerElectronicsConnection.ratedU ?ratedU."+
		"	?pec c:PowerElectronicsConnection.p ?p."+
		"	?pec c:PowerElectronicsConnection.q ?q."+
		"	OPTIONAL {?pecp c:PowerElectronicsConnectionPhase.PowerElectronicsConnection ?pec."+
		"	?pecp c:PowerElectronicsConnectionPhase.phase ?phsraw."+
		"		bind(strafter(str(?phsraw),\"SinglePhaseKind.\") as ?phs) }"+
		" bind(strafter(str(?s),\"#_\") as ?id)."+
		"	?t c:Terminal.ConductingEquipment ?pec."+
		"	?t c:Terminal.ConnectivityNode ?cn."+ 
		"	?cn c:IdentifiedObject.name ?bus"+
	 	"} "+
		"GROUP by ?name ?bus ?ratedS ?ratedU ?p ?q ?id "+
		"ORDER BY ?name";

	public String id;
	public String name;
	public String bus;
	public String phases;
	public double p;
	public double q;
	public double ratedU;
	public double ratedS;
	public boolean bDelta;

	public DistSolar (ResultSet results) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			bus = SafeName (soln.get("?bus").toString());
			phases = OptionalString (soln, "?phases", "ABC");
			phases = phases.replace ('\n', ':');
			p = 0.001 * Double.parseDouble (soln.get("?p").toString());
			q = 0.001 * Double.parseDouble (soln.get("?q").toString());
			ratedU = Double.parseDouble (soln.get("?ratedU").toString());
			ratedS = Double.parseDouble (soln.get("?ratedS").toString());
			bDelta = false;
		}		
//		System.out.println (DisplayString());
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " @ " + bus + " phases=" + phases);
		buf.append (" vnom=" + df4.format(ratedU) + " vanom=" + df4.format(ratedS));
		buf.append (" kw=" + df4.format(p) + " kvar=" + df4.format(q));
		return buf.toString();
	}

	public String GetJSONSymbols(HashMap<String,DistCoordinates> map) {
		DistCoordinates pt = map.get("PowerElectronicsConnection:" + name + ":1");

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
		StringBuilder buf = new StringBuilder ("object solar {\n");

		buf.append ("  name \"pv_" + name + "\";\n");
		buf.append ("  parent \"" + bus + "\";\n");
		if (bDelta) {
			buf.append ("  phases " + phases + "D;\n");
			buf.append ("  phases_connected " + phases + "D;\n");
		} else {
			buf.append ("  phases " + phases + "N;\n");
			buf.append ("  phases_connected " + phases + "N;\n");
		}  // TODO: rating and inverter parameters
		buf.append("}\n");

		return buf.toString();
	}

	public String GetDSS() {
		StringBuilder buf = new StringBuilder ("new PVSystem." + name);

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
								" pmpp=" + df3.format(kva) + " irrad=" + df3.format(p/kva) + " pf=" + df4.format(pf));
		buf.append("\n");

		return buf.toString();
	}

	public String GetKey() {
		return name;
	}
}

