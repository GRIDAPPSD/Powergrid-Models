//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

/** 
 Helper class to accumulate nodes and loads. 
 </p>All EnergyConsumer data will be attached to node objects, then written as load objects. This preserves the input ConnectivityNode names</p> 
 <p>TODO - another option is to leave all nodes un-loaded, and attach all loads to 
 parent nodes, closer to what OpenDSS does</p>  
*/
public class GldNode {
	/** root name of the node (or load), will have `nd_` prepended */
	public final String name;
	/** ABC allowed */
	public String phases;
	/** this nominal voltage is always line-to-neutral */
	public double nomvln;
	/** real power on phase A or s1, constant impedance portion */
	public double pa_z;
	/** real power on phase B or s2, constant impedance portion */
	public double pb_z;
	/** real power on phase C, constant impedance portion */
	public double pc_z;
	/** reactive power on phase A or s1, constant impedance portion */
	public double qa_z;
	/** reactive power on phase B or s2, constant impedance portion */
	public double qb_z;
	/** reactive power on phase C, constant impedance portion */
	public double qc_z;
	/** real power on phase A or s1, constant current portion */
	public double pa_i;
	/** real power on phase B or s2, constant current portion */
	public double pb_i;
	/** real power on phase C, constant current portion */
	public double pc_i;
	/** reactive power on phase A or s1, constant current portion */
	public double qa_i;
	/** reactive power on phase B or s2, constant current portion */
	public double qb_i;
	/** reactive power on phase C, constant current portion */
	public double qc_i;
	/** real power on phase A or s1, constant power portion */
	public double pa_p;
	/** real power on phase B or s2, constant power portion */
	public double pb_p;
	/** real power on phase C, constant power portion */
	public double pc_p;
	/** reactive power on phase A or s1, constant power portion */
	public double qa_p;
	/** reactive power on phase B or s2, constant power portion */
	public double qb_p;
	/** reactive power on phase C, constant power portion */
	public double qc_p;
	/** will add N or D phasing, if not S */
	public boolean bDelta;	
	/** denotes the SWING bus, aka substation source bus */
	public boolean bSwing;  

 /** if bSecondary true, the member variables for phase A and B 
  * loads actually correspond to secondary phases 1 and 2. For 
  * GridLAB-D, these are written to phase AS, BS or CS, depending
  * on the primary phase, which we find from the service 
  * transformer or triplex. 
  */ 

	public boolean bSecondary; 

	/** constructor defaults to zero load and zero phases present
	 *  @param name CIM name of the bus */
	public GldNode(String name) {
		this.name = name;
		nomvln = -1.0;
		phases = "";
		pa_z = pb_z = pc_z = qa_z = qb_z = qc_z = 0.0;
		pa_i = pb_i = pc_i = qa_i = qb_i = qc_i = 0.0;
		pa_p = pb_p = pc_p = qa_p = qb_p = qc_p = 0.0;
		bDelta = false;
		bSwing = false;
		bSecondary = false;
	}

	/** accumulates phases present
	 *  @param phs phases to add, may contain ABCDSs
	 *  @return always true */
	public boolean AddPhases(String phs) {
		StringBuilder buf = new StringBuilder("");
		if (phases.contains("A") || phs.contains("A")) buf.append("A");
		if (phases.contains("B") || phs.contains("B")) buf.append("B");
		if (phases.contains("C") || phs.contains("C")) buf.append("C");
		if (phs.contains("s")) bSecondary = true;
		if (phs.contains("S")) bSecondary = true;
		if (phs.contains("D")) bDelta = true;
		phases = buf.toString();
		return true;
	}

	/** @return phasing string for GridLAB-D with appropriate D, S or N suffix */
	public String GetPhases() {
		if (bDelta && !bSecondary) return phases + "D";
		if (bSecondary) return phases + "S";
		return phases + "N";
	}

	/** reapportion loads according to constant power (Z/sum), constant current (I/sum) and constant power (P/sum)
	 *  @param Z portion of constant-impedance load
	 *  @param I portion of constant-current load
	 *  @param P portion of constant-power load */
	public void ApplyZIP(double Z, double I, double P) {
		double total = Z + I + P;
		Z = Z / total;
		I = I / total;
		P = P / total;

		total = pa_z + pa_i + pa_p;
		pa_z = total * Z;
		pa_i = total * I;
		pa_p = total * P;
		total = qa_z + qa_i + qa_p;
		qa_z = total * Z;
		qa_i = total * I;
		qa_p = total * P;

		total = pb_z + pb_i + pb_p;
		pb_z = total * Z;
		pb_i = total * I;
		pb_p = total * P;
		total = qb_z + qb_i + qb_p;
		qb_z = total * Z;
		qb_i = total * I;
		qb_p = total * P;

		total = pc_z + pc_i + pc_p;
		pc_z = total * Z;
		pc_i = total * I;
		pc_p = total * P;
		total = qc_z + qc_i + qc_p;
		qc_z = total * Z;
		qc_i = total * I;
		qc_p = total * P;
	}

	/** scales the load by a factor that probably came from the command line's -l option
	 *  @param scale multiplying factor on all of the load components */
	public void RescaleLoad(double scale) {
		pa_z *= scale;
		pb_z *= scale;
		pc_z *= scale;
		qa_z *= scale;
		qb_z *= scale;
		qc_z *= scale;
		pa_i *= scale;
		pb_i *= scale;
		pc_i *= scale;
		qa_i *= scale;
		qb_i *= scale;
		qc_i *= scale;
		pa_p *= scale;
		pb_p *= scale;
		pc_p *= scale;
		qa_p *= scale;
		qb_p *= scale;
		qc_p *= scale;
	}

	/** @return true if a non-zero real or reactive load on any phase */
	public boolean HasLoad() {
		if (pa_z != 0.0) return true;
		if (pb_z != 0.0) return true;
		if (pc_z != 0.0) return true;
		if (qa_z != 0.0) return true;
		if (qb_z != 0.0) return true;
		if (qc_z != 0.0) return true;
		if (pa_i != 0.0) return true;
		if (pb_i != 0.0) return true;
		if (pc_i != 0.0) return true;
		if (qa_i != 0.0) return true;
		if (qb_i != 0.0) return true;
		if (qc_i != 0.0) return true;
		if (pa_p != 0.0) return true;
		if (pb_p != 0.0) return true;
		if (pc_p != 0.0) return true;
		if (qa_p != 0.0) return true;
		if (qb_p != 0.0) return true;
		if (qc_p != 0.0) return true;
		return false;
	}
}

