package gov.pnnl.goss.cim2glm.queryhandler.impl;

import java.io.File;
import java.util.Properties;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
//import org.openrdf.query.MalformedQueryException;
//import org.openrdf.query.QueryLanguage;
//import org.openrdf.query.TupleQuery;
//import org.openrdf.query.TupleQueryResult;
//import org.openrdf.repository.Repository;
//import org.openrdf.repository.RepositoryConnection;
//import org.openrdf.repository.RepositoryException;
//import org.openrdf.rio.RDFFormat;
//
//import com.bigdata.rdf.sail.BigdataSail;
//import com.bigdata.rdf.sail.BigdataSailRepository;

import gov.pnnl.goss.cim2glm.queryhandler.QueryHandler;

public class InternalBlazegraphQueryHandler implements QueryHandler{
//	Repository repo;
//	RepositoryConnection con;
	
	public InternalBlazegraphQueryHandler(){
		File ieee8500 = new File("ieee13.xml");
        
		try {
//			File journal = File.createTempFile("bigdata", ".jnl");
//			final String CIF = "http://iec.ch/TC57/2012/CIM-schema-cim16#";
//			final String modelID = "_676B0EA4-162F-4D4A-8FDD-B5FB7C2B7270";
//	        journal.deleteOnExit();
//	        final Properties properties = new Properties();
//	        properties.setProperty(BigdataSail.Options.FILE, journal
//	                .getAbsolutePath());
//	        
//	        // instantiate a sail
//	        BigdataSail sail = new BigdataSail(properties);
//	        repo = new BigdataSailRepository(sail);
//	        repo.initialize();
//
//	        con = repo.getConnection();
//	        
//	        String idQuery = "SELECT ?x WHERE {<"+CIF+modelID+"> ?p ?x}";
//	        TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, idQuery);
//	        TupleQueryResult result = tupleQuery.evaluate();
//	        if(!result.hasNext()) {
//	        	 System.out.println("ABOUT TO LOAD");
//	        	 con.add(ieee8500, CIF, RDFFormat.RDFXML);
//	 	        System.out.println("LOADED");
//	        }
//	        
//	        con.close();
        
        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	@Override
	public ResultSet query(String szQuery) {
//		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, szQuery);
//		TupleQueryResult result = tupleQuery.evaluate();
//		return result;
		return null;
	}
	
	
}
