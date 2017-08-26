//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

// package gov.pnnl.gridlabd.cim;

import java.io.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.commons.math3.complex.Complex;

public abstract class DistComponent {
	static final String szEND = "http://localhost:9999/blazegraph/namespace/kb/sparql";
	static final String nsCIM = "http://iec.ch/TC57/2012/CIM-schema-cim16#";
	static final String nsRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	static final String nsXSD = "http://www.w3.org/2001/XMLSchema#";

	static final double gFREQ = 60.0;
	static final double gOMEGA = 377.0;
	static final double gMperMILE = 1609.344;
	static final double gFTperM = 3.2809;

	static ResultSet RunQuery(String szQuery) {
		String qPrefix = "PREFIX r: <" + nsRDF + "> PREFIX c: <" + nsCIM + "> PREFIX xsd:<" + nsXSD + "> ";
		Query query = QueryFactory.create (qPrefix + szQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService (szEND, query);
		return qexec.execSelect();
	}

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
 	 *	@param arg the root bus name, aka CIM name
 	 *	@return nd_arg
 	 */
 	static String GldPrefixedNodeName (String arg) {
 		return "nd_" + arg;
 	}

 	/** 
 	 *	convert a CIM name to GridLAB-D name, replacing unallowed characters and prefixing for a bus/node
 	 *	@param arg the root bus or component name, aka CIM name
 	 *	@param bus to flag whether `nd_` should be prepended
 	 *	@return the compatible name for GridLAB-D
 	 */  
 	static String GLD_Name (String arg, boolean bus) {			// GLD conversion
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
 		if (bus) return GldPrefixedNodeName (s);
 		return s;
 	}

 	static String GLMClassPrefix (String t) {  // GLD conversion
 		if (t.equals("LinearShuntCompensator")) return "cap";
 		if (t.equals("ACLineSegment")) return "line"; // assumes we prefix both overhead and underground with line_
 		if (t.equals("EnergyConsumer")) return "";	// TODO should we name load:?
 		if (t.equals("PowerTransformer")) return "xf";
 		return "##UNKNOWN##";
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

