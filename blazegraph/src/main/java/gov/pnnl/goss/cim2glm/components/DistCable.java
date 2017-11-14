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
}

