//	----------------------------------------------------------
//	Copyright (c) 2017, Battelle Memorial Institute
//	All rights reserved.
//	----------------------------------------------------------

package gov.pnnl.gridlabd.cim ;

import java.io.*;
import java.util.HashMap;
import java.text.DecimalFormat;

import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexFormat;

/**
 * <p>This class runs an example SQARQL query against CIM XML</p> 
 *  
 * <p> Future versions of GridAPPS-D  
 * will rely more heavily on SPARQL queries to do the selection and filtering, as the preferred 
 * pattern for developers working with CIM. This example uses several triples to execute
 * a query on LinearShuntCompensators (aka capacitors).</p> 
 *  
 * <p>Invoke as a console-mode program</p> 
 *  
 * @see SPARQLcimTest#main 
 *  
 * @author Tom McDermott 
 * @version 1.0 
 *  
 */

public class SPARQLcimTest extends Object {
	/** 
	 *  namespace for CIM; should match the CIM version used to generate the RDF
	 */
	static final String nsCIM = "http://iec.ch/TC57/2012/CIM-schema-cim16#";

	/** 
	 *  namespace for RDF
	 */
	static final String nsRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	/** 
	 *  identifies gridlabd
	 */
	static final String baseURI = "http://gridlabd";

	/** 
	 *  convert a CIM name to GridLAB-D name, replacing unallowed characters
	 */  
	static String GLD_Name (String arg, boolean bus) {	// GLD conversion
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

	/** 
	  * Reads command-line input for the converter 
	  * @param args will be SPARQLcimTest [options] input.xml
	  * <p>Options: -e={u|i}  encoding; UTF-8 or ISO-8859-1; choose u if input.xml came from OpenDSS</p>
	  * @returns text to stdout
	*/
	public static void main (String args[]) throws UnsupportedEncodingException, FileNotFoundException {

		String fName = "", fEnc = "";
		int fInFile = 0;

		if (args.length < 2) {
			System.out.println ("Usage: sparql [options] input.xml");
			System.out.println (" 	-e={u|i}  // encoding; UTF-8 or ISO-8859-1");
		}
		int i = 0;
		while (i < args.length) {
			if (args[i].charAt(0) == '-') {
				char opt = args[i].charAt(1);
				String optVal = args[i].substring(3);
				if (opt=='e') {
					if (optVal.charAt(0) == 'u') {
						fEnc = "UTF8";
					} else {
						fEnc = "ISO-8859-1";
					}
				}
			} else if (fInFile < 1) {
				fInFile = 1;
				fName = args[i];
			}
			++i;
		}

		Model model = ModelFactory.createOntologyModel (OntModelSpec.OWL_DL_MEM);
			 
		InputStream in = FileManager.get().open(fName);
		if (in == null) {
			throw new IllegalArgumentException( "File: " + fName + " not found");
		}
				
		model.read(new InputStreamReader(in, fEnc), baseURI, "RDF/XML");

		System.out.println ("***** XML has been read *****");
				
		String qPrefix = "PREFIX r: <" + nsRDF + "> PREFIX c: <" + nsCIM + "> ";
		Query query;
		QueryExecution qexec;
		ResultSet results;
		QuerySolution soln;
		DecimalFormat df = new DecimalFormat("#.00");

		// LinearShuntCompensator ==> Capacitor
		query = QueryFactory.create (qPrefix + 
							"SELECT ?s ?name ?nomu ?bsection ?bus WHERE {" + 
					    " ?s r:type c:LinearShuntCompensator." +
					    " ?s c:IdentifiedObject.name ?name." +
					    " ?s c:ShuntCompensator.nomU ?nomu." + 
					    " ?s c:LinearShuntCompensator.bPerSection ?bsection." + 
					    " ?t c:Terminal.ConductingEquipment ?s." + 
					    " ?t c:Terminal.ConnectivityNode ?cn." + 
					    " ?cn c:IdentifiedObject.name ?bus" + 
					    "}");
		qexec = QueryExecutionFactory.create (query, model);
		results=qexec.execSelect();
		while (results.hasNext()) {
			soln = results.next();

			String id = soln.get("?s").toString();
			String name = soln.get("?name").toString();
			String bus = soln.get("?bus").toString();
			double nomu = new Double (soln.get("?nomu").toString()).doubleValue();
			double bsection = new Double (soln.get("?bsection").toString()).doubleValue();
			double kvar = nomu * nomu * bsection / 1000.0;

			System.out.println (id + ": " + name + " @ " + bus + "  " + 
				df.format(nomu/1000.0) + " [kV] " + df.format(kvar) + " [kvar]");
		}
	}
}

