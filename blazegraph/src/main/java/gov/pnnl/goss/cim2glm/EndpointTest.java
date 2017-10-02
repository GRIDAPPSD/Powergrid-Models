package gov.pnnl.goss.cim2glm;
//      ----------------------------------------------------------
//      Copyright (c) 2017, Battelle Memorial Institute
//      All rights reserved.
//      ----------------------------------------------------------

// package gov.pnnl.gridlabd.cim ;

import java.io.*;
import java.text.DecimalFormat;

import org.apache.jena.query.*;

/**
 * <p>This class runs an example SQARQL query against Blazegraph triple-store</p> 
 *  
 * <p>Invoke as a console-mode program</p> 
 *  
 * @see EndpointTest#main 
 *  
 * @author Tom McDermott 
 * @version 1.0 
 *  
 */

public class EndpointTest extends Object {
  static final String szEND = "http://localhost:9999/blazegraph/namespace/kb/sparql";
  static final String nsCIM = "http://iec.ch/TC57/2012/CIM-schema-cim16#";
  static final String nsRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  public static void main (String args[]) throws UnsupportedEncodingException, FileNotFoundException {
       
    String qPrefix = "PREFIX r: <" + nsRDF + "> PREFIX c: <" + nsCIM + "> ";
    Query query;
    QueryExecution qexec;
    ResultSet results;
    QuerySolution soln;
    DecimalFormat df = new DecimalFormat("#0.00");

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
    qexec = QueryExecutionFactory.sparqlService (szEND, query);
    results=qexec.execSelect();
    while (results.hasNext()) {
      soln = results.next();

//      String id = soln.get("?s").toString();
      String name = soln.get("?name").toString();
      String bus = soln.get("?bus").toString();
      double nomu = new Double (soln.get("?nomu").toString()).doubleValue();
      double bsection = new Double (soln.get("?bsection").toString()).doubleValue();
      double kvar = nomu * nomu * bsection / 1000.0;

      System.out.println (name + " @ " + bus + "  " + 
        df.format(nomu/1000.0) + " [kV] " + df.format(kvar) + " [kvar]");
    }
  }
}

