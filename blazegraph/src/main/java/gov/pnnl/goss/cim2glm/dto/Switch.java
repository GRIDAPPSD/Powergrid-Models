package gov.pnnl.goss.cim2glm.dto;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import com.google.gson.Gson;

public class Switch {


	public String id;
	public String name;
	public boolean open;


	public Switch() {
		this.name = null;
		this.open = false;
	}

	public Switch (String name, boolean open) {
		this.name = SafeName (name);
		this.open = open;
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

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static Switch parse(String jsonString) {
		Gson gson = new Gson();
		Switch obj = gson.fromJson(jsonString, Switch.class);
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

