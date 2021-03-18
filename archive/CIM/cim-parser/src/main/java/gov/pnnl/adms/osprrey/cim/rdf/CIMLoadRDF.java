package gov.pnnl.adms.osprrey.cim.rdf;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

public class CIMLoadRDF {
	public static final String CIM_NS = "http://iec.ch/TC57/2012/CIM-schema-cim16#";
	public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	
	
	public static void main(String[] args) {
//		String cimXMLFile = "C:\\Users\\tara\\Documents\\CIM\\Powergrid-Models\\CIM\\IEEE8500_CDPSM_Combined.XML";
//		String storeDirectory = "D:\\installs\\ADMS\\rdf4jTestStore\\Server\\repositories\\ADMS";
		if(args.length<2){
			System.out.println("Usage: <input file> <rdf db file location>");
			System.exit(1);
		}
		String cimXMLFile = args[0];
		String storeDirectory = args[1];
		
		Repository repository = new SailRepository(new NativeStore(new File(storeDirectory)));
		repository.initialize();
		
		// Open a connection to this repository
		RepositoryConnection repositoryConnection = repository.getConnection();

		File fileToUpload = new File(cimXMLFile);
		try {
			repositoryConnection.add(fileToUpload, RDF_NS, RDFFormat.RDFXML);
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			repositoryConnection.close();
		}
	}

}
