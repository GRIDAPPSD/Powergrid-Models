package gov.pnnl.goss.cim2glm.dto;

import java.util.ArrayList;
import java.util.List;

public class ModelState {
	List<SyncMachine> synchronousmachines = new ArrayList<SyncMachine>();
	List<Switch> switches = new ArrayList<Switch>();

	public ModelState(){

	}

	public ModelState(List<SyncMachine> synchronousmachines, List<Switch> switches) {
		this.synchronousmachines = synchronousmachines;
		this.switches = switches;
	}

	public List<SyncMachine> getSynchronousmachines() {
		return synchronousmachines;
	}

	public void setSynchronousmachines(List<SyncMachine> synchronousmachines) {
		this.synchronousmachines = synchronousmachines;
	}

	public List<Switch> getSwitches() {
		return switches;
	}

	public void setSwitches(List<Switch> switches) {
		this.switches = switches;
	}


}
