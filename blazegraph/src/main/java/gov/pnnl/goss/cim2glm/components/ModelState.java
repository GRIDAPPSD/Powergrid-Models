package gov.pnnl.goss.cim2glm.components;

import java.util.ArrayList;
import java.util.List;

public class ModelState {
	List<DistSyncMachine> synchronousmachines = new ArrayList<DistSyncMachine>();
	List<DistSwitch> switches = new ArrayList<DistSwitch>();
	
	public ModelState(){
		
	}
	public ModelState(List<DistSyncMachine> synchronousmachines, List<DistSwitch> switches){
		this.synchronousmachines = synchronousmachines;
		this.switches = switches;
	}
	
	public List<DistSyncMachine> getSynchronousmachines() {
		return synchronousmachines;
	}
	public void setSynchronousmachines(List<DistSyncMachine> synchronousmachines) {
		this.synchronousmachines = synchronousmachines;
	}
	public List<DistSwitch> getSwitches() {
		return switches;
	}
	public void setSwitches(List<DistSwitch> switches) {
		this.switches = switches;
	}
	
	
}
