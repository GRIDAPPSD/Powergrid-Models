package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;

public abstract class DistCable extends DistWire {
	public double dcore;
	public double djacket;
	public double dins;
	public double dscreen;
	public boolean sheathNeutral;

	protected void AppendCableDisplay (StringBuilder buf) {
		AppendWireDisplay (buf);
		DecimalFormat df = new DecimalFormat("#0.000000");
		buf.append (" dcore=" + df.format(dcore) + " djacket=" + df.format(djacket) + " dins=" + df.format(dins)); 
		buf.append (" dscreen=" + df.format(dscreen) + " sheathNeutral=" + Boolean.toString(sheathNeutral)); 
	}

	protected void AppendDSSCableAttributes (StringBuilder buf) {
		AppendDSSWireAttributes (buf);
		double dEps = 2.3; // TODO - should be a setting
		DecimalFormat df = new DecimalFormat("#0.000000");
		DecimalFormat dfEps = new DecimalFormat("#0.00");
		buf.append ("\n~ EpsR=" + df.format(dEps) + " Ins=" + df.format(insthick) +
								" DiaIns=" + df.format(dins) + " DiaCable=" + df.format(djacket));
	}
}

