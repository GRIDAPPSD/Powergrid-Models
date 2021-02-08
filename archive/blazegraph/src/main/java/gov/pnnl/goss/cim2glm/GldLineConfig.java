package gov.pnnl.goss.cim2glm;
//	----------------------------------------------------------
//	Copyright (c) 2018, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

/** 
 Helper class to accumulate spacings and conductors. 
*/
public class GldLineConfig {
	public final String name;
	public String spacing;
	public String conductor_A;
	public String conductor_B;
	public String conductor_C;
	public String conductor_N;
//	public boolean bCable;
//	public boolean bTriplex;

	public GldLineConfig (String name) {
		this.name = name;
		spacing = "";
		conductor_A = "";
		conductor_B = "";
		conductor_C = "";
		conductor_N = "";
//		bCable = false;
//		bTriplex = false;
	}

	static String GetMatchWire (String wclass, String name) {
		if (wclass.equals("OverheadWireInfo")) {
			return "wire_" + name;
		} else if (wclass.equals("ConcentricNeutralCableInfo")) {
			return "cncab_" + name;
		} else if (wclass.equals("TapeShieldCableInfo")) {
			return "tscab_" + name;
		}
		return "unknown_" + name;
	}

	public String GetGLM () {
		StringBuilder buf = new StringBuilder ();
		buf.append ("object line_configuration {\n");
		buf.append ("  name \"" + name + "\";\n");
		buf.append ("  spacing \"" + spacing + "\";\n");
		if (conductor_A.length() > 0) {
			buf.append ("  conductor_A \"" + conductor_A + "\";\n");
		}
		if (conductor_B.length() > 0) {
			buf.append ("  conductor_B \"" + conductor_B + "\";\n");
		}
		if (conductor_C.length() > 0) {
			buf.append ("  conductor_C \"" + conductor_C + "\";\n");
		}
		if (conductor_N.length() > 0) {
			buf.append ("  conductor_N \"" + conductor_N + "\";\n");
		}
		buf.append("}\n");
		return buf.toString();
	}
}

