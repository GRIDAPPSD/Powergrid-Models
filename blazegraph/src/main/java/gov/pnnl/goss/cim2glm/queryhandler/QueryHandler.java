package gov.pnnl.goss.cim2glm.queryhandler;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetCloseable;

import gov.pnnl.goss.cim2glm.components.DistComponent;

public interface QueryHandler {
	public final String Q_PREFIX = "PREFIX r: <" + DistComponent.nsRDF + "> PREFIX c: <" + DistComponent.nsCIM + "> PREFIX rdf: <" + DistComponent.nsRDF + "> PREFIX cim: <" + DistComponent.nsCIM + "> PREFIX xsd:<" + DistComponent.nsXSD + "> ";
	
	public ResultSetCloseable query(String szQuery);
	public ResultSet construct(String szQuery);
	public boolean addFeederSelection (String mRID); // TODO: support more than one, return False if not present
	public boolean clearFeederSelections ();
	public String getFeederSelection ();
}
