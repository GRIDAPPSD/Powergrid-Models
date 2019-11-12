package gov.pnnl.goss.cim2glm.dto;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

public class SyncMachine {

	public String id;
	public String name;
	public double p;
	public double q;

	public SyncMachine (String name, Double p, Double q) {
		this.name = SafeName (name);
		this.p = p;
		this.q = q;
	}


/**
	 * convert a CIM name to simulator name, replacing unallowed characters
	 * 
	 * @param arg
	 *            the root bus or component name, aka CIM name
	 * @return the compatible name for GridLAB-D or OpenDSS
	 */
	public static String SafeName(String arg) { // GLD conversion
		String s = arg.replace(' ', '_');
		s = s.replace('.', '_');
		s = s.replace('=', '_');
		s = s.replace('+', '_');
		s = s.replace('^', '_');
		s = s.replace('$', '_');
		s = s.replace('*', '_');
		s = s.replace('|', '_');
		s = s.replace('[', '_');
		s = s.replace(']', '_');
		s = s.replace('{', '_');
		s = s.replace('}', '_');
		s = s.replace('(', '_');
		s = s.replace(')', '_');
		return s;
	}
}

