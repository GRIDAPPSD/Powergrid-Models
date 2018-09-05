package gov.pnnl.goss.cim2glm.components;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import gov.pnnl.goss.cim2glm.components.DistHouse.HouseCooling;
import gov.pnnl.goss.cim2glm.components.DistHouse.HouseHeating;
import gov.pnnl.goss.cim2glm.components.DistHouse.HouseThermalIntegrity;

import org.apache.jena.query.*;

public class DistThermostat extends DistComponent{
	public static enum ThermostatControlMode{COOLING,HEATING};
	public static ThermostatControlMode[] thermostatControlModeList = ThermostatControlMode.values();
	public static final String szQUERY = 
	 	"SELECT ?name ?aggregatorName ?baseSetpoint ?controlMode ?priceCap ?rampHigh ?rampLow ?rangeHigh ?rangeLow ?useOverride ?usePredictive ?id ?fdrid "+
		"WHERE {"+
	 	" ?s r:type c:EnergyConsumer."+
		" ?s c:Equipment.EquipmentContainer ?fdr."+
		" ?fdr c:IdentifiedObject.mRID ?fdrid."+
	 	" ?s c:IdentifiedObject.name ?name."+
	 	" ?s c:IdentifiedObject.mRID ?id."+
	   " ?s c:Thermostat.aggregatorName ?aggregatorName."+
	   " ?bv c:Thermostat.baseSetpoint ?baseSetpoint."+
	 	" ?s c:Thermostat.controlMode ?controlMode."+
	 	" ?s c:Thermostat.priceCap ?priceCap."+
	 	" ?s c:Thermostat.rampHigh ?rampHigh."+
	 	" ?s c:Thermostat.rampLow ?rampLow."+
	 	" ?lr c:Thermostat.rangeHigh ?rangeHigh."+
	 	" ?lr c:Thermostat.rangeLow ?rangeLow."+
	 	" ?lr c:Thermostat.useOverride ?useOverride."+
	 	" ?lr c:Thermostat.usePredictive ?usePredictive."+
	 	" ?lr c:LoadResponseCharacteristic.pConstantPower ?pp."+
	 	" ?lr c:LoadResponseCharacteristic.qConstantPower ?qp."+
	 	" ?lr c:LoadResponseCharacteristic.pVoltageExponent ?pe."+
	 	" ?lr c:LoadResponseCharacteristic.qVoltageExponent ?qe."+
	 	" OPTIONAL {?ecp c:EnergyConsumerPhase.EnergyConsumer ?s"+
	 	"} "+
		"GROUP BY ?name ?aggregatorName ?baseSetpoint ?controlMode ?priceCap ?rampHigh ?rampLow ?rangeHigh ?rangeLow ?useOverride ?usePredictive ?id ?fdrid "+
		"ORDER BY ?name";
	public String id;
	public String name;
	public String aggregatorName;
	public double baseSetpoint;
	public ThermostatControlMode controlMode;
	public double priceCap;
	public double rampHigh;
	public double rampLow;
	public double rangeHigh;
	public double rangeLow;
	public boolean useOverride;
	public boolean usePredictive;
	
	
	public DistThermostat (ResultSet result) {
		if(result.hasNext()) {
			QuerySolution soln = result.next();
			name = SafeName (soln.get("?name").toString());
			id = soln.get("?id").toString();
			aggregatorName = SafeName (soln.get("?aggregatorName").toString());
			baseSetpoint = Double.parseDouble(soln.get("?baseSetpoint").toString());
			controlMode = thermostatControlModeList[Integer.parseInt(soln.get("?controlMode").toString())];
			priceCap = Double.parseDouble(soln.get("?priceCap").toString());
			rampHigh = Double.parseDouble(soln.get("?rampHigh").toString());
			rampLow = Double.parseDouble(soln.get("?rampLow").toString());
			rangeHigh = Double.parseDouble(soln.get("?rangeHigh").toString());
			rangeLow = Double.parseDouble(soln.get("?rangeLow").toString());
			useOverride = Boolean.parseBoolean(soln.get("?useOverride").toString());
			usePredictive = Boolean.parseBoolean(soln.get("?usePredictive").toString());
		}
	}


	@Override
	public String DisplayString() {
		StringBuilder buf = new StringBuilder ("");
		buf.append (name + " aggregatorName=" + aggregatorName + " base setpoint=" + df4.format(baseSetpoint));
		buf.append (" control mode=" + controlMode.toString() + " price capacity=" + df4.format(priceCap));
		buf.append (" ramp high=" + df4.format(rampHigh) + " ramp low=" + df4.format(rampLow));
		buf.append(" range high=" + df4.format(rangeHigh) + " range low=" + df4.format(rangeLow));
		buf.append (" use override=" + String.valueOf(useOverride) + " use predictive=" + String.valueOf(usePredictive));
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
}
