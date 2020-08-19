package gov.pnnl.goss.cim2glm.queryhandler.impl;

import java.util.Date;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetCloseable;

import gov.pnnl.goss.cim2glm.components.DistComponent;
import gov.pnnl.goss.cim2glm.queryhandler.QueryHandler;

public class HTTPBlazegraphQueryHandler implements QueryHandler {
	
	
	String endpoint;
	String mRID;
	boolean use_mRID;
	boolean bTiming;
	
	public HTTPBlazegraphQueryHandler(String endpoint) {
		this.endpoint = endpoint;
		this.use_mRID = false;
		this.bTiming = false;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public String getFeederSelection () {
		return mRID;
	}

	@Override
	public ResultSetCloseable query(String szQuery, String szTag) { 
		long t1 = System.nanoTime(); 
		Query query;
		if (use_mRID) { // try to insert a VALUES block for the feeder mRID of interest
			String insertion_point = "WHERE {";
			int idx = szQuery.lastIndexOf (insertion_point);
			if (idx >= 0) {
//				System.out.println ("\n***");
//				System.out.println (szQuery);
//				System.out.println ("***");
				StringBuilder buf = new StringBuilder (Q_PREFIX + szQuery.substring (0, idx) + insertion_point + " VALUES ?fdrid {\"");
				buf.append (mRID + "\"} " + szQuery.substring (idx + insertion_point.length()));
//				System.out.println ("Sending " + buf.toString());
				query = QueryFactory.create (buf.toString());
			} else {
				query = QueryFactory.create (Q_PREFIX + szQuery);
			}
		} else {
			query = QueryFactory.create (Q_PREFIX + szQuery);
		}
		QueryExecution qexec = QueryExecutionFactory.sparqlService (endpoint, query);
		ResultSetCloseable rs=  ResultSetCloseable.closeableResultSet(qexec);
		long t2 = System.nanoTime();
		if (bTiming) {
			System.out.format("SPARQL Query Time:   %7.4f for %s\n", (double)(t2 - t1) / 1.0e9, szTag);
		}
		return rs;
	}
	
	@Override
	public ResultSet construct(String szQuery) {
		Query query;
		if (use_mRID) { // try to insert a VALUES block for the feeder mRID of interest
			String insertion_point = "WHERE {";
			int idx = szQuery.lastIndexOf (insertion_point);
			if (idx >= 0) {
//				System.out.println ("\n***");
//				System.out.println (szQuery);
//				System.out.println ("***");
				StringBuilder buf = new StringBuilder (Q_PREFIX + szQuery.substring (0, idx) + insertion_point + " VALUES ?fdrid {\"");
				buf.append (mRID + "\"} " + szQuery.substring (idx + insertion_point.length()));
//				System.out.println ("Sending " + buf.toString());
				query = QueryFactory.create (buf.toString());
			} else {
				query = QueryFactory.create (Q_PREFIX + szQuery);
			}
		} else {
			query = QueryFactory.create (Q_PREFIX + szQuery);
		}
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
//		qexec.
		ResultSet rs=  qexec.execSelect();
		return rs;
	}
	public boolean addFeederSelection (String mRID) {
		this.mRID = mRID;
		use_mRID = true;
		return use_mRID;
	}
	public boolean clearFeederSelections () {
		use_mRID = false;
		return use_mRID;
	}
	public void setTiming (boolean val) {
		bTiming = val;
	}
}
