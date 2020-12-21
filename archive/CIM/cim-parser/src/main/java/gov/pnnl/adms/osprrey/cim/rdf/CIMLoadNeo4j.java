package gov.pnnl.adms.osprrey.cim.rdf;

import org.eclipse.rdf4j.repository.RepositoryConnection;

public class CIMLoadNeo4j {
	public static final String CIM_NS = "http://iec.ch/TC57/2012/CIM-schema-cim16#";
	public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	
	
	public static void main(String[] args) {
		
//		// Create the sail graph database
//		EmbeddedGraphDatabase graphDb = new EmbeddedGraphDatabase("var/flights");
//		LuceneIndexService indexService = new LuceneIndexService(graphDb);
//		SimpleFulltextIndex fulltextIndex = new SimpleFulltextIndex(graphDb, new File("var/flights/lucene-fulltext"));
//		VerboseQuadStore rdfStore = new VerboseQuadStore(graphDb, indexService, null, fulltextIndex);
//		GraphDatabaseSail sail = new GraphDatabaseSail(graphDb, rdfStore);
//
//		// Initialize the sail store
//		sail.initialize();
//
//		// Get the sail repository connection
//		RepositoryConnection connection = new SailRepository(sail).getConnection();
	}
}
