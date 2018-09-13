package gov.pnnl.goss.cim2glm.components;

import org.apache.jena.query.*;

public class DistHouse extends DistComponent {
	public static enum HouseCooling{none,electric,heatPump};
	public static enum HouseThermalIntegrity{unknown,veryLittle,normal,aboveNormal,belowNormal,good,veryGood,little};
	public static enum HouseHeating{none,gas,heatPump,resistance};
	public static final String szQUERY = 
		"SELECT ?name ?parent ?coolingSetpoint ?coolingSystem ?floorArea ?heatingSetpoint ?heatingSystem ?hvacPowerFactor ?numberOfStories ?thermalIntegrity ?id ?fdrid WHERE { VALUES ?fdrid {\"_4F76A5F9-271D-9EB8-5E31-AA362D86F2C3\"}  \n" + 
				"?h r:type c:House. " + 
				"?h c:IdentifiedObject.name ?name. " + 
				"?h c:IdentifiedObject.mRID ?id. " + 
				"?h c:House.floorArea ?floorArea. " + 
				"?h c:House.numberOfStories ?numberOfStories. " + 
				"OPTIONAL{?h c:House.coolingSetpoint ?coolingSetpoint.} " + 
				"OPTIONAL{?h c:House.heatingSetpoint ?heatingSetpoint.} " + 
				"OPTIONAL{?h c:House.hvacPowerFactor ?hvacPowerFactor.} " + 
				"?h c:House.coolingSystem ?coolingSystemRaw. " + 
					"bind(strafter(str(?coolingSystemRaw),\"HouseCooling.\") as ?coolingSystem) " + 
				"?h c:House.heatingSystem ?heatingSystemRaw. " + 
					"bind(strafter(str(?heatingSystemRaw),\"HouseHeating.\") as ?heatingSystem) " + 
				"?h c:House.thermalIntegrity ?thermalIntegrityRaw " + 
					"bind(strafter(str(?thermalIntegrityRaw),\"HouseThermalIntegrity.\") as ?thermalIntegrity) " + 
				"?h c:House.EnergyConsumer ?econ. " + 
				"?econ c:IdentifiedObject.name ?parent. " +
		"} ORDER BY ?name";
	public String id;
	public String name;
	public String parent;
	public double coolingSetpoint;
	public HouseCooling coolingSystem;
	public double floorArea;
	public double heatingSetpoint;
	public HouseHeating heatingSystem;
	public double hvacPowerFactor;
	public int numberOfStories;
	public HouseThermalIntegrity thermalIntegrity;
	
	
	public DistHouse (ResultSet result) {
		if(result.hasNext()) {
			QuerySolution soln = result.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			parent = SafeName (soln.get("?parent").toString());
			coolingSetpoint = Double.parseDouble(OptionalString(soln, "?coolingSetpoint", "200.0"));
			coolingSystem = HouseCooling.valueOf(soln.get("?coolingSystem").toString());
			floorArea = Double.parseDouble(soln.get("?floorArea").toString());
			heatingSetpoint = Double.parseDouble(OptionalString(soln, "?heatingSetpoint", "-100.0"));
			heatingSystem = HouseHeating.valueOf(soln.get("?heatingSystem").toString());
			hvacPowerFactor = Double.parseDouble(OptionalString(soln, "?hvacPowerFactor", "1.0"));
			try {
				numberOfStories = Integer.parseInt(soln.get("?numberOfStories").toString());
			} catch (NumberFormatException e){ 
				numberOfStories = (int)Double.parseDouble(soln.get("?numberOfStories").toString());
			}
			thermalIntegrity = HouseThermalIntegrity.valueOf(soln.get("?thermalIntegrity").toString());
		}
	}


	@Override
	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " @ " + parent);
		buf.append (" cooling setpoint=" + df4.format(coolingSetpoint) + " cooling system=" + coolingSystem.toString());
		buf.append (" floor area=" + df4.format(floorArea) + " heating setpoint=" + df4.format(heatingSetpoint));
		buf.append(" heating system=" + heatingSystem.toString() + " hvac power factor=" + df4.format(hvacPowerFactor));
		buf.append (" number of stories=" + String.valueOf(numberOfStories));
		buf.append(" thermal integrity=" + thermalIntegrity.toString());
		return buf.toString();
	}


	@Override
	public String GetKey() {
		return name;
	}


	@Override
	public String GetJSONEntry() {
		StringBuilder buf = new StringBuilder ();
		buf.append ("{\"name\":\"" + name +"\"");
		buf.append (",\"mRID\":\"" + id +"\"");
		buf.append ("}");
		return buf.toString();
	}
	
	
	public String GetGLM() {
		StringBuilder buf = new StringBuilder("object house {\n");
		buf.append("  name \"" + name + "\";\n");
		buf.append("  parent \"" + parent + "\";\n");
		if (!coolingSystem.equals(HouseCooling.none)) {
			buf.append("  cooling_setpoint " + String.valueOf(coolingSetpoint) + "\";\n");
		}
		buf.append("  cooling_system_type " + coolingSystem.toString() + "\";\n");
		buf.append("  floor_area " + String.valueOf(floorArea) + "\";\n");
		if (!heatingSystem.equals(HouseHeating.none)) {
			buf.append("  heating_setpoint " + String.valueOf(heatingSetpoint) + "\";\n");
		}
		buf.append("  heating_system_type " + heatingSystem.toString() + "\";\n");
		if (!heatingSystem.equals(HouseHeating.none) || !coolingSystem.equals(HouseCooling.none)) {
			buf.append("  hvac_power_factor " + String.valueOf(hvacPowerFactor) + "\";\n");
		}
		buf.append("  number_of_stories " + String.valueOf(numberOfStories) + "\";\n");
		buf.append("  thermal_integrity_level " + thermalIntegrity.toString() + "\";\n");
		buf.append("}\n");
		return buf.toString();
	}
}
