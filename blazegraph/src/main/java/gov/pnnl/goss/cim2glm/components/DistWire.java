package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;

public abstract class DistWire extends DistComponent {
	public String name;
	public String id;
	public double rad;
	public double gmr;
	public double rdc;
	public double r25;
	public double r50;
	public double r75;
	public double corerad; 
	public double amps;
	public double insthick;
	public boolean ins;
	public String insmat;

	protected void AppendWireDisplay (StringBuilder buf) {
		DecimalFormat df6 = new DecimalFormat("#0.000000");
		DecimalFormat df1 = new DecimalFormat("#0.0");
		buf.append (name + " rad=" + df6.format(rad) + " gmr=" + df6.format(gmr) + " rdc=" + df6.format(rdc)); 
		buf.append (" r25=" + df6.format(r25) + " r50=" + df6.format(r50) + " r75=" + df6.format(r75)); 
		buf.append (" corerad=" + df6.format(corerad) + " amps=" + df1.format(amps)); 
		buf.append (" ins=" + Boolean.toString(ins) + " insmat=" + insmat + " insthick=" + df6.format(insthick));
	}

	protected void AppendDSSWireAttributes (StringBuilder buf) {
		DecimalFormat df1 = new DecimalFormat("#0.0");
		if (gmr < 1.0e-6 || rad < 1.0e-6 || r25 < 1.0e-6 || rdc < 1.0e-6) {
			DecimalFormat df12 = new DecimalFormat("#0.000000000000");
			buf.append(name + " gmr=" + df12.format(gmr) + " radius=" + df12.format(rad) + " rac=" + df12.format(r25));
			buf.append (" rdc=" + df12.format(rdc) + " normamps=" + df1.format(amps) + " Runits=m Radunits=m gmrunits=m");
		} else {
			DecimalFormat df6 = new DecimalFormat("#0.000000");
			buf.append(name + " gmr=" + df6.format(gmr) + " radius=" + df6.format(rad) + " rac=" + df6.format(r25));
			buf.append (" rdc=" + df6.format(rdc) + " normamps=" + df1.format(amps) + " Runits=m Radunits=m gmrunits=m");
		}
	}
}

