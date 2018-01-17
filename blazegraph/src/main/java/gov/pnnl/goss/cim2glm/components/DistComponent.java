package gov.pnnl.goss.cim2glm.components;
//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

import org.apache.jena.query.*; 
import org.apache.jena.rdf.model.RDFNode;
import org.apache.commons.math3.complex.Complex;
import java.text.DecimalFormat;

public abstract class DistComponent {
	public static final String nsCIM = "http://iec.ch/TC57/2012/CIM-schema-cim16#";
	public static final String nsRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String nsXSD = "http://www.w3.org/2001/XMLSchema#";

	static final double gFREQ = 60.0;
	static final double gOMEGA = 377.0;
	static final double gMperMILE = 1609.344;
	static final double gFTperM = 3.2809;

	static final DecimalFormat df1 = new DecimalFormat("#0.0");
	static final DecimalFormat df2 = new DecimalFormat("#0.00");
	static final DecimalFormat df3 = new DecimalFormat("#0.000");
	static final DecimalFormat df4 = new DecimalFormat("#0.0000");
	static final DecimalFormat df5 = new DecimalFormat("#0.00000");
	static final DecimalFormat df6 = new DecimalFormat("#0.000000");
	static final DecimalFormat df12 = new DecimalFormat("#0.000000000000");

//	public static ResultSet RunQuery (String szQuery) {
//		String qPrefix = "PREFIX r: <" + nsRDF + "> PREFIX c: <" + nsCIM + "> PREFIX xsd:<" + nsXSD + "> ";
//		Query query = QueryFactory.create (qPrefix + szQuery);
//		QueryExecution qexec = QueryExecutionFactory.sparqlService (szEND, query);
//		return qexec.execSelect();
//	}

	static String OptionalString (QuerySolution soln, String parm, String def) {
		RDFNode nd = soln.get(parm);
		if (nd != null) {
			String str = nd.toString();
			if (str.length() > 0) {
				return str;
			}
		}
		return def;
	}

	static int OptionalInt (QuerySolution soln, String parm, int def) {
		RDFNode nd = soln.get(parm);
		if (nd != null) {
			String str = nd.toString();
			if (str.length() > 0) {
				return Integer.parseInt (str);
			}
		}
		return def;
	}

	static double OptionalDouble (QuerySolution soln, String parm, double def) {
		RDFNode nd = soln.get(parm);
		if (nd != null) {
			String str = nd.toString();
			if (str.length() > 0) {
				return Double.parseDouble (str);
			}
		}
		return def;
	}

	static boolean OptionalBoolean (QuerySolution soln, String parm, boolean def) {
		RDFNode nd = soln.get(parm);
		if (nd != null) {
			String str = nd.toString();
			if (str.length() > 0) {
				return Boolean.parseBoolean (str);
			}
		}
		return def;
	}

 	/** prefix all bus names with `nd_` for GridLAB-D, so they "should" be unique
 	 *	@param arg the root bus name
 	 *	@return nd_arg
 	 */
 	static String GldBusName (String arg) {
 		return "nd_" + arg;
 	}

 	/** 
 	 *	convert a CIM name to simulator name, replacing unallowed characters
 	 *	@param arg the root bus or component name, aka CIM name
 	 *	@return the compatible name for GridLAB-D or OpenDSS
 	 */  
 	public static String SafeName (String arg) {			// GLD conversion
 		String s = arg.replace (' ', '_');
 		s = s.replace ('.', '_');
 		s = s.replace ('=', '_');
 		s = s.replace ('+', '_');
 		s = s.replace ('^', '_');
 		s = s.replace ('$', '_');
 		s = s.replace ('*', '_');
 		s = s.replace ('|', '_');
 		s = s.replace ('[', '_');
 		s = s.replace (']', '_');
 		s = s.replace ('{', '_');
 		s = s.replace ('}', '_');
 		s = s.replace ('(', '_');
 		s = s.replace (')', '_');
 		return s;
 	}

 	static String GLMClassPrefix (String t) {  // GLD conversion
 		if (t.equals("LinearShuntCompensator")) return "cap";
 		if (t.equals("ACLineSegment")) return "line"; // assumes we prefix both overhead and underground with line_
 		if (t.equals("EnergyConsumer")) return "";	// TODO should we name load:?
 		if (t.equals("PowerTransformer")) return "xf";
 		return "##UNKNOWN##";
 	}

	static String DSSClassPrefix (String t) {  // DSS conversion
		if (t.equals("LinearShuntCompensator")) return "capacitor";
		if (t.equals("ACLineSegment")) return "line";
		if (t.equals("EnergyConsumer")) return "load";
		if (t.equals("PowerTransformer")) return "transformer";
		return "##UNKNOWN##";
	}

	static String FirstDSSPhase (String phs) {
		if (phs.contains ("A")) return "1";
		if (phs.contains ("B")) return "2";
		return "3";
	}

	static int DSSPhaseCount (String phs, boolean bDelta) {
		int nphases = 0;
		if (phs.contains ("A")) nphases += 1;
		if (phs.contains ("B")) nphases += 1;
		if (phs.contains ("C")) nphases += 1;
		if (phs.contains ("s1")) nphases += 1;
		if (phs.contains ("s2")) nphases += 1;
		if ((nphases < 3) && bDelta) {
			nphases = 1;
		}
//		System.out.println (phs + "," + Boolean.toString(bDelta) + "," + Integer.toString(nphases));
		return nphases;
	}

	static String DSSConn (boolean bDelta) {
		if (bDelta) {
			return "d";
		}
		return "w";
	}

	static String DSSShuntPhases (String bus, String phs, boolean bDelta) {
		if (phs.contains ("ABC")) {
			return bus + ".1.2.3";
		}
		if (!bDelta) {
			return DSSBusPhases(bus, phs);
		}
//		if (phs_cnt == 1) {
			if (phs.contains ("A")) {
				return bus + ".1.2";
			} else if (phs.contains ("B")) {
				return bus + ".2.3";
			} else if (phs.contains ("C")) {
				return bus + ".3.1";
			}
//		}
		// TODO - can we have two-phase delta in the CIM?
//		if (phs.contains ("AB")) {
//			return ".1.2.3";
//		} else if (phs.contains ("AC")) {
//			return ".3.1.2";
//		}
		// phs.contains ("BC")) for 2-phase delta
		return bus;// ".2.3.1";
	}

	static String DSSBusPhases (String bus, String phs) {
    if (phs.contains ("ABC")) {
      return bus + ".1.2.3";
    } else if (phs.contains ("AB") || phs.contains ("A:B")) {
      return bus + ".1.2";
		} else if (phs.contains ("12")) {
			return bus + ".1.2";
    } else if (phs.contains ("AC") || phs.contains ("A:C")) {
      return bus + ".1.3";
    } else if (phs.contains ("BC") || phs.contains ("B:C")) {
      return bus + ".2.3";
		} else if (phs.contains ("B:A")) {
			return bus + ".2.1";
		} else if (phs.contains ("C:A")) {
			return bus + ".3.1";
		} else if (phs.contains ("C:B")) {
			return bus + ".3.2";
		} else if (phs.contains ("s1:s2")) {
			return bus + ".1.2";
		} else if (phs.contains ("s2:s1")) {
			return bus + ".2.1";
		} else if (phs.contains ("s1")) {
			return bus + ".1";
		} else if (phs.contains ("s2")) {
			return bus + ".2";
    } else if (phs.contains ("A")) {
      return bus + ".1";
    } else if (phs.contains ("B")) {
      return bus + ".2";
    } else if (phs.contains ("C")) {
      return bus + ".3";
		} else if (phs.contains ("1")) {
			return bus + ".1";
		} else if (phs.contains ("2")) {
			return bus + ".2";
    } else {
      return bus;  // defaults to 3 phases
    }
  }

	static String DSSXfmrBusPhases (String bus, String phs) {
		if (phs.contains("s2")) return (bus + ".0.2");
		if (phs.contains("s1")) return (bus + ".1.0");
		return DSSBusPhases (bus, phs);
	}

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

	/** <p>Map CIM connectionKind to GridLAB-D winding connections. TODO: some of the returnable types aren't actually supported in GridLAB-D</p>
	@param wye array of CIM connectionKind attributes per winding 
	@param nwdg number of transformer windings, also the size of wye 
	@return the GridLAB-D winding connection. This may be something not supported in GridLAB-D, which should be treated as a feature request 
	*/
	static String GetGldTransformerConnection(String [] conn, int nwdg) {
		if (nwdg == 3) {
			if (conn[0].equals("I") && conn[1].equals("I") && conn[1].equals("I")) return "SINGLE_PHASE_CENTER_TAPPED"; // supported in GridLAB-D
		}
		if (conn[0].equals("D"))
		{
			if (conn[1].equals("D"))  {
				return "DELTA_DELTA";  // supported in GridLAB-D
			} else if (conn[1].equals("Y")) {
				return "DELTA_GWYE"; // supported in GridLAB-D
			} else if (conn[1].equals("Z")) {
				return "D_Z";
			} else if (conn[1].equals("Yn")) {
				return "DELTA_GWYE"; // supported in GridLAB-D
			} else if (conn[1].equals("Zn")) {
				return "D_Zn";
			} else if (conn[1].equals("A")) {
				return "D_A";
			} else if (conn[1].equals("I")) {
				return "D_I";
			}
		} else if (conn[0].equals("Y")) {
			if (conn[1].equals("D"))  {
				return "Y_D";  // TODO - flip to DELTA_GWYE and reverse the node order?
			} else if (conn[1].equals("Y")) {
				return "WYE_WYE"; // supported in GridLAB-D
			} else if (conn[1].equals("Z")) {
				return "Y_Z";
			} else if (conn[1].equals("Yn")) {
				return "WYE_WYE"; // supported in GridLAB-D
			} else if (conn[1].equals("Zn")) {
				return "Y_Z";
			} else if (conn[1].equals("A")) {
				return "WYE_WYE";   // supported in GridLAB-D // TODO - approximately correct
			} else if (conn[1].equals("I")) {
				return "Y_I";
			}
		} else if (conn[0].equals("Z")) {
			if (conn[1].equals("D"))  {
				return "Z_D";
			} else if (conn[1].equals("Y")) {
				return "Z_Y";
			} else if (conn[1].equals("Z")) {
				return "Z_Z";
			} else if (conn[1].equals("Yn")) {
				return "Z_Yn";
			} else if (conn[1].equals("Zn")) {
				return "Z_Zn";
			} else if (conn[1].equals("A")) {
				return "Z_A";
			} else if (conn[1].equals("I")) {
				return "Z_I";
			}
		} else if (conn[0].equals("Yn")) {
			if (conn[1].equals("D"))  {
				return "Yn_D";
			} else if (conn[1].equals("Y")) {
				return "WYE_WYE"; // supported in GridLAB-D
			} else if (conn[1].equals("Z")) {
				return "Yn_Z";
			} else if (conn[1].equals("Yn")) {
				return "WYE_WYE"; // supported in GridLAB-D
			} else if (conn[1].equals("Zn")) {
				return "Yn_Zn";
			} else if (conn[1].equals("A")) {
				return "WYE_WYE";  // supported in GridLAB-D // TODO - approximately correct
			} else if (conn[1].equals("I")) {
				return "Yn_I";
			}
		} else if (conn[0].equals("Zn")) {
			if (conn[1].equals("D"))  {
				return "Zn_D";
			} else if (conn[1].equals("Y")) {
				return "Zn_Y";
			} else if (conn[1].equals("Z")) {
				return "Zn_Z";
			} else if (conn[1].equals("Yn")) {
				return "Zn_Yn";
			} else if (conn[1].equals("Zn")) {
				return "Zn_Zn";
			} else if (conn[1].equals("A")) {
				return "Zn_A";
			} else if (conn[1].equals("I")) {
				return "Zn_I";
			}
		} else if (conn[0].equals("A")) {
			if (conn[1].equals("D"))  {
				return "A_D";
			} else if (conn[1].equals("Y")) {
				return "WYE_WYE";  // supported in GridLAB-D // TODO - approximately correct
			} else if (conn[1].equals("Z")) {
				return "A_Z";
			} else if (conn[1].equals("Yn")) {
				return "WYE_WYE";  // supported in GridLAB-D // TODO - approximately correct
			} else if (conn[1].equals("Zn")) {
				return "A_Zn";
			} else if (conn[1].equals("A")) {
				return "WYE_WYE";  // supported in GridLAB-D // TODO - approximately correct
			} else if (conn[1].equals("I")) {
				return "A_I";
			}
		} else if (conn[0].equals("I")) {
			if (conn[1].equals("D"))  {
				return "I_D";
			} else if (conn[1].equals("Y")) {
				return "I_Y";
			} else if (conn[1].equals("Z")) {
				return "I_Z";
			} else if (conn[1].equals("Yn")) {
				return "I_Yn";
			} else if (conn[1].equals("Zn")) {
				return "I_Zn";
			} else if (conn[1].equals("A")) {
				return "I_A";
			} else if (conn[1].equals("I")) {
				return "SINGLE_PHASE"; // supported in GridLAB-D
			}
		}
		return "** Unsupported **";  // TODO - this could be solvable as UNKNOWN in some cases
	}

 	public abstract String DisplayString();
 	public abstract String GetKey();
}

