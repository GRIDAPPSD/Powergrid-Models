package gov.pnnl.goss.cim2glm.queryhandler.impl;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;

import gov.pnnl.goss.cim2glm.components.DistComponent;
import gov.pnnl.goss.cim2glm.queryhandler.QueryHandler;

public class HTTPBlazegraphQueryHandler implements QueryHandler {
	String endpoint;
	String mRID;
	boolean use_mRID;
	
	public HTTPBlazegraphQueryHandler(String endpoint) {
		this.endpoint = endpoint;
		this.use_mRID = false;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}


	@Override
	public ResultSet query(String szQuery) { 
		String qPrefix = "PREFIX r: <" + DistComponent.nsRDF + "> PREFIX c: <" + DistComponent.nsCIM + "> PREFIX xsd:<" + DistComponent.nsXSD + "> ";
		Query query = QueryFactory.create (qPrefix + szQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService (endpoint, query);
		return qexec.execSelect();
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
}
