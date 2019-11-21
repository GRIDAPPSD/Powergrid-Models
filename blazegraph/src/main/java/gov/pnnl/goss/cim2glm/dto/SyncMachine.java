package gov.pnnl.goss.cim2glm.dto;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import com.google.gson.Gson;

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

	public SyncMachine() {
		name = null;
		p = 0;
		q = 0;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getP() {
		return p;
	}

	public void setP(double p) {
		this.p = p;
	}

	public double getQ() {
		return q;
	}

	public void setQ(double q) {
		this.q = q;
	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static SyncMachine parse(String jsonString) {
		Gson gson = new Gson();
		SyncMachine obj = gson.fromJson(jsonString, SyncMachine.class);
		return obj;
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

