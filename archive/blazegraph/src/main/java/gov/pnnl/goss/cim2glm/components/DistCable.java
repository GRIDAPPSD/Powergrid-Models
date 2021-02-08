package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import java.io.*;
import java.util.HashMap;

public abstract class DistCable extends DistWire {
	public double dcore;
	public double djacket;
	public double dins;
	public double dscreen;
	public boolean sheathNeutral;

	protected void AppendCableDisplay (StringBuilder buf) {
		AppendWireDisplay (buf);
		buf.append (" dcore=" + df6.format(dcore) + " djacket=" + df6.format(djacket) + " dins=" + df6.format(dins)); 
		buf.append (" dscreen=" + df6.format(dscreen) + " sheathNeutral=" + Boolean.toString(sheathNeutral)); 
	}

	protected void AppendDSSCableAttributes (StringBuilder buf) {
		AppendDSSWireAttributes (buf);
		double dEps = 2.3; // TODO - should be a setting
		buf.append ("\n~ EpsR=" + df2.format(dEps) + " Ins=" + df6.format(insthick) +
								" DiaIns=" + df6.format(dins) + " DiaCable=" + df6.format(djacket));
	}

	protected void AppendGLMCableAttributes (StringBuilder buf) {
		AppendGLMWireAttributes (buf);
		double dEps = 2.3; // TODO - should be a setting
		buf.append ("  conductor_gmr " + df6.format (gmr * gFTperM) + ";\n");
		buf.append ("  conductor_diameter " + df6.format (2.0 * rad * gFTperM * 12.0) + ";\n");
		buf.append ("  conductor_resistance " + df6.format (r50 * gMperMILE) + ";\n");
		buf.append ("  outer_diameter " + df6.format (djacket * gFTperM * 12.0) + ";\n");
		buf.append ("  insulation_relative_permitivitty " + df2.format (dEps) + ";\n");
	}
}

