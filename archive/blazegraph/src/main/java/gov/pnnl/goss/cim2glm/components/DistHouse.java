package gov.pnnl.goss.cim2glm.components;

import java.util.EnumMap;
import java.util.Random;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class DistHouse extends DistComponent {
	public static enum HouseCooling{none,electric,heatPump};
	public static enum HouseHeating{none,gas,heatPump,resistance};
	public static enum HouseThermalIntegrity{unknown,veryLittle,normal,aboveNormal,belowNormal,good,veryGood,little};

	public static EnumMap<HouseCooling, String> gldHouseCooling = new EnumMap<HouseCooling, String>(HouseCooling.class);
	public static EnumMap<HouseHeating, String> gldHouseHeating = new EnumMap<HouseHeating, String>(HouseHeating.class);
	public static EnumMap<HouseThermalIntegrity, String> gldHouseThermalIntegrity = new EnumMap<HouseThermalIntegrity, String>(HouseThermalIntegrity.class);
	static {
		gldHouseCooling.put (HouseCooling.none, "NONE");
		gldHouseCooling.put (HouseCooling.electric, "ELECTRIC");
		gldHouseCooling.put (HouseCooling.heatPump, "HEAT_PUMP");
		gldHouseHeating.put (HouseHeating.none, "NONE");
		gldHouseHeating.put (HouseHeating.gas, "GAS");
		gldHouseHeating.put (HouseHeating.heatPump, "HEAT_PUMP");
		gldHouseHeating.put (HouseHeating.resistance, "RESISTANCE");
		gldHouseThermalIntegrity.put (HouseThermalIntegrity.unknown, "UNKNOWN");
		gldHouseThermalIntegrity.put (HouseThermalIntegrity.veryLittle, "VERY_LITTLE");
		gldHouseThermalIntegrity.put (HouseThermalIntegrity.normal, "NORMAL");
		gldHouseThermalIntegrity.put (HouseThermalIntegrity.aboveNormal, "ABOVE_NORMAL");
		gldHouseThermalIntegrity.put (HouseThermalIntegrity.belowNormal, "BELOW_NORMAL");
		gldHouseThermalIntegrity.put (HouseThermalIntegrity.good, "GOOD");
		gldHouseThermalIntegrity.put (HouseThermalIntegrity.veryGood, "VERY_GOOD");
		gldHouseThermalIntegrity.put (HouseThermalIntegrity.little, "LITTLE");
	}

	public static final String szQUERY = 
		"SELECT ?name ?parent ?coolingSetpoint ?coolingSystem ?floorArea ?heatingSetpoint ?heatingSystem ?hvacPowerFactor ?numberOfStories ?thermalIntegrity ?id ?fdrid WHERE {" + 
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
				"?fdr c:IdentifiedObject.mRID ?fdrid. " +
				"?econ c:Equipment.EquipmentContainer ?fdr. " +
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
		buf.append (" cooling setpoint=" + df3.format(coolingSetpoint) + " cooling system=" + coolingSystem.toString());
		buf.append (" floor area=" + df2.format(floorArea) + " heating setpoint=" + df3.format(heatingSetpoint));
		buf.append (" heating system=" + heatingSystem.toString() + " hvac power factor=" + df4.format(hvacPowerFactor));
		buf.append (" number of stories=" + String.valueOf(numberOfStories));
		buf.append (" thermal integrity=" + thermalIntegrity.toString());
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
	
	public String GetGLM(Random r) {
		// we must have heatingSetpoint < (coolingSetpoint - deadband) where deadband defaults to 2.0
		double localHeatSet = heatingSetpoint;
		double skew_value = 2700.0 * r.nextDouble();
		double scalar1 = 324.9 * Math.pow(floorArea, 0.442) / 8907.0;
		double scalar2 = 0.8 + 0.4 * r.nextDouble();
		double scalar3 = 0.8 + 0.4 * r.nextDouble();
		double resp_scalar = scalar1 * scalar2;
		double unresp_scalar = scalar1 * scalar3;
		double[] techdata = {0.9,1.0,0.9,1.0,0.0,1.0,0.0};
		StringBuilder buf = new StringBuilder("object house {\n");
		buf.append("  name \"" + name + "\";\n");
		buf.append("  parent \"ld_" + parent + "_ldmtr\";\n");
		buf.append("  floor_area " +  df2.format(floorArea) + ";\n");
		buf.append("  number_of_stories " + String.valueOf(numberOfStories) + ";\n");
		buf.append("  thermal_integrity_level " + gldHouseThermalIntegrity.get(thermalIntegrity) + ";\n");
		buf.append("  cooling_system_type " + gldHouseCooling.get(coolingSystem) + ";\n");
		 {
			buf.append("  cooling_setpoint " + df3.format(coolingSetpoint) + ";\n");
			if (localHeatSet > (coolingSetpoint - 2.1)) {
				localHeatSet = coolingSetpoint - 2.1;
			}
		}
		buf.append("  heating_system_type " + gldHouseHeating.get(heatingSystem) + ";\n");
		if (!heatingSystem.equals(HouseHeating.none)) {
			buf.append("  heating_setpoint " + df3.format(localHeatSet) + ";\n");
		} else if (!coolingSystem.equals(HouseCooling.none)) {
			buf.append("  heating_setpoint " + df3.format(localHeatSet) + "; // because GridLAB-D will override to RESISTANCE heating\n");
		}
		if (!heatingSystem.equals(HouseHeating.none) || !coolingSystem.equals(HouseCooling.none)) {
			buf.append("  hvac_power_factor " + df4.format(hvacPowerFactor) + ";\n");
		}
		buf.append("  object ZIPload { // responsive\n");
		buf.append("    schedule_skew " + df2.format(skew_value) + ";\n");
		buf.append("    base_power responsive_loads*" + df2.format(resp_scalar) + ";\n");
		buf.append("    heatgain_fraction " + df2.format(techdata[0]) + ";\n");
		buf.append("    impedance_pf " + df2.format(techdata[1]) + ";\n");
		buf.append("    current_pf " + df2.format(techdata[2]) + ";\n");
		buf.append("    power_pf " + df2.format(techdata[3]) + ";\n");
		buf.append("    impedance_fraction " + df2.format(techdata[4]) + ";\n");
		buf.append("    current_fraction " + df2.format(techdata[5]) + ";\n");
		buf.append("    power_fraction " + df2.format(techdata[6]) + ";\n");
		buf.append("  };\n");
		buf.append("  object ZIPload { // unresponsive\n");
		buf.append("    schedule_skew " + df2.format(skew_value) + ";\n");
		buf.append("    base_power unresponsive_loads*" + df2.format(unresp_scalar) + ";\n");
		buf.append("    heatgain_fraction " + df2.format(techdata[0]) + ";\n");
		buf.append("    impedance_pf " + df2.format(techdata[1]) + ";\n");
		buf.append("    current_pf " + df2.format(techdata[2]) + ";\n");
		buf.append("    power_pf " + df2.format(techdata[3]) + ";\n");
		buf.append("    impedance_fraction " + df2.format(techdata[4]) + ";\n");
		buf.append("    current_fraction " + df2.format(techdata[5]) + ";\n");
		buf.append("    power_fraction " + df2.format(techdata[6]) + ";\n");
		buf.append("  };\n");
		buf.append("}\n");
		return buf.toString();
	}
}
