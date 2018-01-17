package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*;
import java.util.HashMap;

public class DistXfmrBank extends DistComponent {
	public static final String szQUERY =
		"SELECT ?pname ?vgrp ?tname WHERE {"+
		" ?p r:type c:PowerTransformer."+
		" ?p c:IdentifiedObject.name ?pname."+
		" ?p c:PowerTransformer.vectorGroup ?vgrp."+
		" ?t c:TransformerTank.PowerTransformer ?p."+
		" ?t c:IdentifiedObject.name ?tname"+
		"} ORDER BY ?pname ?tname";

	public static final String szCountQUERY =
		"SELECT ?key (count(?tank) as ?count) WHERE {"+
		" ?tank c:TransformerTank.PowerTransformer ?pxf."+
		" ?pxf c:IdentifiedObject.name ?key"+
		"} GROUP BY ?key ORDER BY ?key";

	public String pname;
	public String vgrp;
	public String[] tname;

	public int size;

	private void SetSize (int val) {
		size = val;
		tname = new String[size];
	}

	public DistXfmrBank (ResultSet results, HashMap<String,Integer> map) {
		if (results.hasNext()) {
			QuerySolution soln = results.next();
			pname = SafeName (soln.get("?pname").toString());
			vgrp = soln.get("?vgrp").toString();
			SetSize (map.get(pname));
			for (int i = 0; i < size; i++) {
				tname[i] = SafeName (soln.get("?tname").toString());
				if ((i + 1) < size) {
					soln = results.next();
				}
			}
		}		
	}

	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (pname + " vgrp=" + vgrp);
		for (int i = 0; i < size; i++) {
			buf.append ("\n  tname=" + tname[i]);
		}
		return buf.toString();
	}

	public String GetKey() {
		return pname;
	}
}

