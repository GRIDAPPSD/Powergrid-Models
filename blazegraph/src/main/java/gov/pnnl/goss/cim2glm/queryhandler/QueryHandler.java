package gov.pnnl.goss.cim2glm.queryhandler;

import org.apache.jena.query.ResultSetCloseable;

public interface QueryHandler {
	public ResultSetCloseable query(String szQuery);
	public boolean addFeederSelection (String mRID); // TODO: support more than one, return False if not present
	public boolean clearFeederSelections ();
	public String getFeederSelection ();
}
