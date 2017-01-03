package gov.pnnl.adms.osprrey.cim.parser;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.PathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseCIMToSQL {

	public static final String CIM_NS = "http://iec.ch/TC57/2012/CIM-schema-cim16#";
	public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String ID_ATTRIBUTE = "ID";
	public static final String RESOURCE_ATTRIBUTE = "resource";
	public static final String MODEL_COMPONENT_TABLE = "ModelComponents";
	public static void main(String[] args){
		
		
		
		if(args.length<4){
			System.out.println("Usage: <model location> <database url> <database user> <database password>");
			System.exit(1);
		}

		String dataLocation = args[0];
		String db = args[1];
		String user = args[2];
		String pw = args[3];
		
		
		String cimXMLFile = dataLocation+File.separator+"IEEE8500_CDPSM_Combined.XML";
		String cimXML2File = dataLocation+File.separator+"IEEE13Nodeckt_CDPSM_Combined.XML";
		String dbDropFile = dataLocation+File.separator+"Drop_RC1.sql";
		String dbCreateFile = dataLocation+File.separator+"RC1.sql";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(db, user, pw);
			ParseCIMToSQL parse = new ParseCIMToSQL();
			parse.resetDB(dbDropFile, dbCreateFile, conn);
			parse.doParse(cimXMLFile, conn);
//			parse.doParse(cimXML2File, conn);
		
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(conn!=null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public void doParse(String cimXMLFile, Connection conn) throws IOException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		String modelMRID = UUID.randomUUID().toString();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File( cimXMLFile ));
			Element root = document.getDocumentElement();
			NodeList nodes = root.getChildNodes();
			for(int entryNum=0;entryNum<nodes.getLength();entryNum++){
				Node node = nodes.item(entryNum);
				String entryName = node.getLocalName();
				if(entryName!=null && !entryName.startsWith("#") && node.getNamespaceURI().equals(CIM_NS)){
					Node entryId = node.getAttributes().getNamedItemNS(RDF_NS, ID_ATTRIBUTE);//("rdf:"+ID_ATTRIBUTE);
					if(entryId!=null){
						HashMap<String, List<SimpleEntry<String, Object>>> tableEntries = new HashMap<String, List<AbstractMap.SimpleEntry<String,Object>>>();
						NodeList fields = node.getChildNodes();
						for(int fieldNum=0; fieldNum<fields.getLength();fieldNum++){
							Node field = fields.item(fieldNum);
							
							String fieldName = field.getLocalName();
							if(fieldName!=null){
								String[] fieldNameSplit = StringUtils.split(fieldName, ".");
//								String tableName = fieldNameSplit[0];
								String tableName = entryName;
								if(!fieldName.startsWith("#") && field.getNamespaceURI().equals(CIM_NS)){
									if(!tableEntries.containsKey(tableName)){
										tableEntries.put(tableName, new ArrayList<AbstractMap.SimpleEntry<String,Object>>());
									}
								
									if(field.getAttributes().getNamedItemNS(RDF_NS, RESOURCE_ATTRIBUTE)!=null){
										String resourceID = field.getAttributes().getNamedItemNS(RDF_NS, RESOURCE_ATTRIBUTE).getNodeValue();
										if(resourceID.startsWith("#")){
											resourceID = resourceID.substring(1);
										} else if(resourceID.startsWith(CIM_NS)){
											resourceID = resourceID.substring(CIM_NS.length());
											if(resourceID.contains(".")){
												resourceID = StringUtils.split(resourceID, ".")[1];
											}
										}
										tableEntries.get(tableName).add(new SimpleEntry<String, Object>(fieldNameSplit[1], resourceID));
									} else {
										
										tableEntries.get(tableName).add(new SimpleEntry<String, Object>(fieldNameSplit[1], field.getTextContent()));
									}
									
									
								}
							}
						}
						
						//Perform SQL inserts for the current entry
						for(String table: tableEntries.keySet()){
							ArrayList<String> fieldNames = new ArrayList<String>();

							List<SimpleEntry<String, Object>> fieldValues = tableEntries.get(table);
							
							String fieldsStr = "";
							String valuesStr = "";
							for(SimpleEntry<String, Object> entry: fieldValues){
								
								
								if(!fieldNames.contains(entry.getKey().toString()) && !entry.getKey().toString().equals("mRID")){
									fieldsStr += ""+entry.getKey()+",";
									fieldNames.add(entry.getKey());
									if(isNumeric(entry.getValue().toString()) || isBoolean(entry.getValue().toString())){
										valuesStr += ""+entry.getValue()+",";
									} else {
										valuesStr += "'"+entry.getValue()+"',";	
									}
								}
								
								
							}
							
//							if(!fieldNames.contains("mRID")){
							fieldsStr += "mRID";
							valuesStr += "'"+entryId.getNodeValue()+"'";
//							} else {
//								fieldsStr = fieldsStr.substring(0, fieldsStr.length()-1);
//								valuesStr = valuesStr.substring(0, valuesStr.length()-1);
//							}
							
							
							String insertStmtStr = "INSERT INTO "+table+"("+fieldsStr+") VALUES ("+valuesStr+")";
							System.out.println(insertStmtStr);
							Statement insertStmt = conn.createStatement();
							int result = insertStmt.executeUpdate(insertStmtStr);
							System.out.println("Result "+result);
							
							
							insertStmtStr = "INSERT INTO "+MODEL_COMPONENT_TABLE+"(mRID, componentMRID, tableName) VALUES ('"+modelMRID+"',+'"+entryId.getNodeValue()+"',+'"+table+"')";
							System.out.println(insertStmtStr);
							insertStmt = conn.createStatement();
							result = insertStmt.executeUpdate(insertStmtStr);
							System.out.println("Result "+result);
							
						
						
						}
					}
					
				}
				
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
	}
	
	
	protected void resetDB(String dbDropFile, String dbCreateFile, Connection conn) throws IOException, SQLException {

		
		ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();    
		rdp.addScript(new PathResource(dbDropFile));
		rdp.addScript(new PathResource(dbCreateFile));
		rdp.populate(conn);

	}
	
	
	
	protected long hashString(String value){
		long result = 0;
		int MAX_LENGTH = 40;
		for (int i = 0; i < value.length(); i++) {
		   result += (long)Math.pow(27, MAX_LENGTH - i - 1)*(1 + value.charAt(i) - 'a');
		}
		
		return result;
	}
	
	protected boolean isNumeric(String value){
		try {
			new Double(value);
			return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}
	protected boolean isBoolean(String value){
			return "true".equals(value.toLowerCase()) || "false".equals(value.toLowerCase());
	}
}
