package gov.pnnl.goss.cim2glm.queryhandler;

import org.apache.jena.query.ResultSet;

public interface QueryHandler {
	public ResultSet query(String szQuery);
}
