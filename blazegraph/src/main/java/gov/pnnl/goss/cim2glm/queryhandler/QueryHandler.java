package gov.pnnl.goss.cim2glm.queryhandler;

import org.apache.jena.query.ResultSet;

public interface QueryHandler {
	public ResultSet query(String szQuery);
	public boolean addFeederSelection (String mRID); // TODO: support more than one, return False if not present
	public boolean clearFeederSelections ();
}
