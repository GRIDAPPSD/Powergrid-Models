package gov.pnnl.goss.cim2glm;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import java.text.DecimalFormat;
import org.apache.commons.math3.complex.Complex;

/** 
 Helper class to accumulate nodes and loads. 
 </p>All EnergyConsumer data will be attached to node objects, then written as load objects. This preserves the input ConnectivityNode names</p> 
 <p>TODO - another option is to leave all nodes un-loaded, and attach all loads to 
 parent nodes, closer to what OpenDSS does</p>  
*/
public class GldNode {
	/** 
	 *  Rotates a phasor +120 degrees by multiplication
		 */
	static final Complex pos120 = new Complex (-0.5, 0.5 * Math.sqrt(3.0));

	/** 
	 *  Rotates a phasor -120 degrees by multiplication
	 */
	static final Complex neg120 = new Complex (-0.5, -0.5 * Math.sqrt(3.0));

	/** 
	 *  @param c complex number
	 *  @return formatted string for GridLAB-D input files with 'j' at the end
		 */
	static String CFormat (Complex c) {
		String sgn;
		if (c.getImaginary() < 0.0)  {
			sgn = "-";
		} else {
			sgn = "+";
		}
		return String.format("%6g", c.getReal()) + sgn + String.format("%6g", Math.abs(c.getImaginary())) + "j";
	}

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
		phases = buf.toString();
		return true;
	}

	/** @return phasing string for GridLAB-D with appropriate D, S or N suffix */
	public String GetPhases() {
		if (bDelta && !bSecondary) return phases + "D";
		if (bSecondary) return phases + "S";
		return phases + "N";
	}

	/** Distributes a total load (pL+jqL) among the phases (phs) present on GridLAB-D node
	@param phs phases actually present at the node 
	@param pL total real power 
	@param qL total reactive power 
	@param Pv real power voltage exponent from a CIM LoadResponseCharacteristic 
	@param Qv reactive power voltage exponent from a CIM LoadResponseCharacteristic 
	@param Pz real power constant-impedance percentage from a CIM LoadResponseCharacteristic 
	@param Qz reactive power constant-impedance percentage from a CIM LoadResponseCharacteristic 
	@param Pi real power constant-current percentage from a CIM LoadResponseCharacteristic 
	@param Qi reactive power constant-current percentage from a CIM LoadResponseCharacteristic 
	@param Pp real power constant-power percentage from a CIM LoadResponseCharacteristic 
	@param Qp reactive power constant-power percentage from a CIM LoadResponseCharacteristic 
	@return void */ 
	public void AccumulateLoads (String phs, double pL, double qL, double Pv, double Qv,
															 double Pz, double Pi, double Pp, double Qz, double Qi, double Qp) {
		double fa = 0.0, fb = 0.0, fc = 0.0, denom = 0.0;
		if (phs.contains("A") || phs.contains("s")) {
			fa = 1.0;
			denom += 1.0;
		}
		if (phs.contains("B") || phs.contains("s")) {  // TODO - allow for s1 and s2
			fb = 1.0;
			denom += 1.0;
		}
		if (phs.contains("C")) {
			fc = 1.0;
			denom += 1.0;
		}
		if (fa > 0.0) fa /= denom;
		if (fb > 0.0) fb /= denom;
		if (fc > 0.0) fc /= denom;

		// we also have to divide the total pL and qL among constant ZIP components
		double fpz = 0.0, fqz = 0.0, fpi = 0.0, fqi = 0.0, fpp = 0.0, fqp = 0.0;
		denom = Pz + Pi + Pp;
		if (denom > 0.0) {
			fpz = Pz / denom;
			fpi = Pi / denom;
			fpp = Pp / denom;
		} else {
			if (Pv > 0.9 && Pv < 1.1)  {
				fpi = 1.0;
			} else if (Pv > 1.9 && Pv < 2.1) {
				fpz = 1.0;
			} else {
				fpp = 1.0;
			}
		}
		denom = Qz + Qi + Qp;
		if (denom > 0.0) {
			fqz = Qz / denom;
			fqi = Qi / denom;
			fqp = Qp / denom;
		} else {
			if (Qv > 0.9 && Qv < 1.1)  {
				fqi = 1.0;
			} else if (Qv > 1.9 && Qv < 2.1) {
				fqz = 1.0;
			} else {
				fqp = 1.0;
			}
		}

		// now update the node phases and phase loads
		pL *= 1000.0;
		qL *= 1000.0;
		AddPhases(phs);
		pa_z += fa * pL * fpz;
		pb_z += fb * pL * fpz;
		pc_z += fc * pL * fpz;
		qa_z += fa * qL * fqz;
		qb_z += fb * qL * fqz;
		qc_z += fc * qL * fqz;
		pa_i += fa * pL * fpi;
		pb_i += fb * pL * fpi;
		pc_i += fc * pL * fpi;
		qa_i += fa * qL * fqi;
		qb_i += fb * qL * fqi;
		qc_i += fc * qL * fqi;
		pa_p += fa * pL * fpp;
		pb_p += fb * pL * fpp;
		pc_p += fc * pL * fpp;
		qa_p += fa * qL * fqp;
		qb_p += fb * qL * fqp;
		qc_p += fc * qL * fqp;
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

	public String GetGLM (double load_scale, boolean bWantSched, String fSched, boolean bWantZIP, double Zcoeff, double Icoeff, double Pcoeff) {
		StringBuilder buf = new StringBuilder();
		DecimalFormat df2 = new DecimalFormat("#0.00");

		if (bSwing) {
			buf.append ("object substation {\n");
			buf.append ("  name \"" + name + "\";\n");
			buf.append ("  bustype SWING;\n");
			buf.append ("  phases " + GetPhases() + ";\n");
			buf.append ("  nominal_voltage " + df2.format(nomvln) + ";\n");
			buf.append ("  base_power 12MVA;\n");
			buf.append ("  power_convergence_value 100VA;\n");
			buf.append ("  positive_sequence_voltage ${VSOURCE};\n");
			buf.append ("}\n");
		} else if (HasLoad()) {
			RescaleLoad(load_scale);
			if (bWantZIP) {
				ApplyZIP (Zcoeff, Icoeff, Pcoeff);
			}

			Complex va = new Complex (nomvln);
			Complex vb = va.multiply (neg120);
			Complex vc = va.multiply (pos120);
			Complex amps;
			Complex vmagsq = new Complex (nomvln * nomvln);
			if (bSecondary) {
				buf.append ("object triplex_load {\n");
				buf.append ("  name \"" + name + "\";\n");
				buf.append ("  phases " + GetPhases() + ";\n");
				buf.append ("  nominal_voltage " + df2.format(nomvln) + ";\n");
				Complex base1 = new Complex (pa_z + pa_i + pa_p, qa_z + qa_i + qa_p);
				Complex base2 = new Complex (pb_z + pb_i + pb_p, qb_z + qb_i + qb_p);
				if (bWantSched) {
					buf.append ("  base_power_1 " + fSched + ".value*" + df2.format(base1.abs()) + ";\n");
					buf.append ("  base_power_2 " + fSched + ".value*" + df2.format(base2.abs()) + ";\n");
				} else {
					buf.append ("  base_power_1 " + df2.format(base1.abs()) + ";\n");
					buf.append ("  base_power_2 " + df2.format(base2.abs()) + ";\n");
				}
				if (pa_p > 0.0) {
					Complex base = new Complex(pa_p, qa_p);
					buf.append ("  power_pf_1 " + df2.format(pa_p / base.abs()) + ";\n");
					buf.append ("  power_fraction_1 " + df2.format(pa_p / base1.getReal()) + ";\n");
				}
				if (pb_p > 0.0) {
					Complex base = new Complex(pb_p, qb_p);
					buf.append ("  power_pf_2 " + df2.format(pb_p / base.abs()) + ";\n");
					buf.append ("  power_fraction_2 " + df2.format(pb_p / base2.getReal()) + ";\n");
				}
				if (pa_i > 0.0) {
					Complex base = new Complex(pa_i, qa_i);
					buf.append ("  current_pf_1 " + df2.format(pa_i / base.abs()) + ";\n");
					buf.append ("  current_fraction_1 " + df2.format(pa_i / base1.getReal()) + ";\n");
				}
				if (pb_i > 0.0) {
					Complex base = new Complex(pb_i, qb_i);
					buf.append ("  current_pf_2 " + df2.format(pb_i / base.abs()) + ";\n");
					buf.append ("  current_fraction_2 " + df2.format(pb_i / base2.getReal()) + ";\n");
				}
				if (pa_z > 0.0) {
					Complex base = new Complex(pa_z, qa_z);
					buf.append ("  impedance_pf_1 " + df2.format(pa_z / base.abs()) + ";\n");
					buf.append ("  impedance_fraction_1 " + df2.format(pa_z / base1.getReal()) + ";\n");
				}
				if (pb_z > 0.0) {
					Complex base = new Complex(pb_z, qb_z);
					buf.append ("  impedance_pf_2 " + df2.format(pb_z / base.abs()) + ";\n");
					buf.append ("  impedance_fraction_2 " + df2.format(pb_z / base2.getReal()) + ";\n");
				}
				buf.append ("}\n");
			} else {
				buf.append ("object load {\n");
				buf.append ("  name \"" + name + "\";\n");
				buf.append ("  phases " + GetPhases() + ";\n");
				buf.append ("  nominal_voltage " + df2.format(nomvln) + ";\n");
				if (pa_p > 0.0 || qa_p != 0.0)	{
					buf.append ("  constant_power_A " + CFormat(new Complex(pa_p, qa_p)) + ";\n");
				}
				if (pb_p > 0.0 || qb_p != 0.0)	{
					buf.append ("  constant_power_B " + CFormat(new Complex(pb_p, qb_p)) + ";\n");
				}
				if (pc_p > 0.0 || qc_p != 0.0)	{
					buf.append ("  constant_power_C " + CFormat(new Complex(pc_p, qc_p)) + ";\n");
				}
				if (pa_z > 0.0 || qa_z != 0.0) {
					Complex s = new Complex(pa_z, qa_z);
					Complex z = vmagsq.divide(s.conjugate());
					buf.append ("  constant_impedance_A " + CFormat(z) + ";\n");
				}
				if (pb_z > 0.0 || qb_z != 0.0) {
					Complex s = new Complex(pb_z, qb_z);
					Complex z = vmagsq.divide(s.conjugate());
					buf.append ("  constant_impedance_B " + CFormat(z) + ";\n");
				}
				if (pc_z > 0.0 || qc_z != 0.0) {
					Complex s = new Complex(pc_z, qc_z);
					Complex z = vmagsq.divide(s.conjugate());
					buf.append ("  constant_impedance_C " + CFormat(z) + ";\n");
				}
				if (pa_i > 0.0 || qa_i != 0.0) {
					Complex s = new Complex(pa_i, qa_i);
					amps = s.divide(va).conjugate();
					buf.append ("  constant_current_A " + CFormat(amps) + ";\n");
				}
				if (pb_i > 0.0 || qb_i != 0.0) {
					Complex s = new Complex(pb_i, qb_i);
					amps = s.divide(va.multiply(neg120)).conjugate();
					buf.append ("  constant_current_B " + CFormat(amps) + ";\n");
				}
				if (pc_i > 0.0 || qc_i != 0.0) {
					Complex s = new Complex(pc_i, qc_i);
					amps = s.divide(va.multiply(pos120)).conjugate();
					buf.append ("  constant_current_C " + CFormat(amps) + ";\n");
				}
				buf.append ("}\n");
			}
		} else {
			if (bSecondary) {
				buf.append ("object triplex_node {\n");
			} else {
				buf.append ("object node {\n");
			}
			buf.append ("  name \"" + name + "\";\n");
			buf.append ("  phases " + GetPhases() + ";\n");
			buf.append ("  nominal_voltage " + df2.format(nomvln) + ";\n");
			buf.append ("}\n");
		}
		return buf.toString();
	}
}

