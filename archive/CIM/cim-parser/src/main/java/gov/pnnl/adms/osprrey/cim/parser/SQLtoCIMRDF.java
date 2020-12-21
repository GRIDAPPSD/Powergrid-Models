package gov.pnnl.adms.osprrey.cim.parser;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class SQLtoCIMRDF {
	public static final String CIM_NS = "http://iec.ch/TC57/2012/CIM-schema-cim16#";
	public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String CIM_PREFIX = "cim:";
	public static final String RDF_PREFIX = "rdf:";
	public static final String ID_ATTRIBUTE = "ID";
	public static final String RESOURCE_ATTRIBUTE = "resource";
	static HashMap<String, String> fieldNameMap = new HashMap<String, String>();
	static HashMap<String, String> referenceMap = new HashMap<String, String>();

	
	
	public static void main(String[] args) {
		if(args.length<4){
			System.out.println("Usage: <output file> <database url> <database user> <database password>");
			System.exit(1);
		}
		Connection conn = null;
		OutputStream out = null;
		String dataLocation = args[0];
		String db = args[1];
		String user = args[2];
		String pw = args[3];
		
		
		try {
			conn = DriverManager.getConnection(db, user, pw);
			SQLtoCIMRDF parse = new SQLtoCIMRDF();
			out = new FileOutputStream(dataLocation);
			parse.outputModel("ieee8500", new BufferedWriter(new OutputStreamWriter(out)), conn);
		
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(conn!=null)
					conn.close();
				if(out!=null){
					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
		
		
		
	public void outputModel(String lineName, BufferedWriter out, Connection conn) throws IOException, SQLException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			doc.setXmlStandalone(true);
			doc.setDocumentURI(RDF_NS);
			Element rootElement = doc.createElementNS(RDF_NS, RDF_PREFIX+"RDF");
	        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:cim", CIM_NS);
	        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:rdf", RDF_NS);
	 
			doc.appendChild(rootElement);
			
			ArrayList<String> notFound = new ArrayList<String>();
		
			//All components that belong in the same model as the line
			String lineLookup = "SELECT distinct mc2.componentMRID, mc2.tableName"
						+ "	FROM ModelComponents mc1, Line l, ModelComponents mc2"
						+ " where mc1.componentMRID=l.mRID and l.name='"+lineName+"' and mc1.mRID=mc2.mRID ";
			
			Statement lookupStmt = conn.createStatement();
			ResultSet results = lookupStmt.executeQuery(lineLookup);
			//For each result
			while(results.next()){
				String tableName = results.getString("tableName");
				String mrid = results.getString("componentMRID");
				Element next = doc.createElementNS(CIM_NS, CIM_PREFIX+results.getString("tableName"));
				next.setAttributeNS(RDF_NS, RDF_PREFIX+ID_ATTRIBUTE, mrid);
				rootElement.appendChild(next);
//				out.write(results.getString("componentMRID")+" "+results.getString("tableName"));
//				out.newLine();
				String tableLookup = "SELECT * from "+tableName+" where mRID='"+mrid+"'";
				Statement tableLookupStmt = conn.createStatement();
				ResultSet tableResults = tableLookupStmt.executeQuery(tableLookup);
				tableResults.next();
				ResultSetMetaData metadata = tableResults.getMetaData();
				for(int i=1;i<=tableResults.getMetaData().getColumnCount();i++){
					//create element with the table name and rdf:ID of the mRID
					//add element for each field that it has content for, do a lookup by name and table name to see what it should be written out as
					String column = tableResults.getMetaData().getColumnName(i);
					String fullColumn = tableName+"."+column;
//					System.out.println(fullColumn);
					
					String value = tableResults.getString(i);
					if(value!=null){
						if(fieldNameMap.containsKey(tableName+"."+column)){
							fullColumn = fieldNameMap.get(tableName+"."+column);
						} else {
							if(!notFound.contains(fullColumn)){
								notFound.add(fullColumn);
							}
						}
						
						
						
						Element field = doc.createElementNS(CIM_NS, CIM_PREFIX+fullColumn);
						if(referenceMap.containsKey(column)){
							field.setAttributeNS(RDF_NS, RDF_PREFIX+RESOURCE_ATTRIBUTE, CIM_NS+referenceMap.get(column)+"."+value);
						} else if(value.startsWith("_") && !column.equals("mRID")&& !column.equals("name")){
							field.setAttributeNS(RDF_NS, RDF_PREFIX+RESOURCE_ATTRIBUTE, "#"+value);
						} else {
							field.setTextContent(value);
						}
						next.appendChild(field);
					}
							
				}
			}
		
			
			// Use a Transformer for output
		    TransformerFactory tFactory = TransformerFactory.newInstance();
		    Transformer transformer = tFactory.newTransformer();
		    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(doc);
		    StreamResult result = new StreamResult(out);
		    transformer.transform(source, result);
		    
		    
//		    System.out.println();
//		    System.out.println();
//		    for(String str: notFound){
//		    	System.out.println(str);
//		    }
		}catch(ParserConfigurationException e){
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		out.flush();
	}
		
	
	static {
		fieldNameMap.put("IEC61970CIMVersion.version", "IEC61970CIMVersion.version");
		fieldNameMap.put("IEC61970CIMVersion.date", "IEC61970CIMVersion.date");
		fieldNameMap.put("CoordinateSystem.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("CoordinateSystem.name", "IdentifiedObject.name");
		fieldNameMap.put("GeographicalRegion.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("GeographicalRegion.name", "IdentifiedObject.name");
		fieldNameMap.put("SubGeographicalRegion.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("SubGeographicalRegion.name", "IdentifiedObject.name");
		fieldNameMap.put("Location.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("Location.name", "IdentifiedObject.name");
		fieldNameMap.put("Line.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("Line.name", "IdentifiedObject.name");
		fieldNameMap.put("Line.Location", "PowerSystemResource.Location");
		fieldNameMap.put("TopologicalNode.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("TopologicalNode.name", "IdentifiedObject.name");
		fieldNameMap.put("ConnectivityNode.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("ConnectivityNode.name", "IdentifiedObject.name");		
		fieldNameMap.put("TopologicalIsland.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("TopologicalIsland.name", "IdentifiedObject.name");
		fieldNameMap.put("BaseVoltage.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("BaseVoltage.name", "IdentifiedObject.name");
		fieldNameMap.put("EnergySource.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("EnergySource.name", "IdentifiedObject.name");
		fieldNameMap.put("EnergySource.BaseVoltage", "ConductingEquipment.BaseVoltage");
		fieldNameMap.put("EnergySource.EquipmentContainer", "Equipment.EquipmentContainer");
		fieldNameMap.put("EnergySource.Location", "PowerSystemResource.Location");
		fieldNameMap.put("Terminal.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("Terminal.name", "IdentifiedObject.name");
		fieldNameMap.put("PositionPoint.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("PositionPoint.name", "IdentifiedObject.name");
		fieldNameMap.put("LinearShuntCompensator.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("LinearShuntCompensator.name", "IdentifiedObject.name");
		fieldNameMap.put("LinearShuntCompensator.EquipmentContainer", "Equipment.EquipmentContainer");
		fieldNameMap.put("LinearShuntCompensator.BaseVoltage", "ConductingEquipment.BaseVoltage");
		fieldNameMap.put("LinearShuntCompensator.nomU", "ShuntCompensator.nomU");
		fieldNameMap.put("LinearShuntCompensator.aVRDelay", "ShuntCompensator.aVRDelay");
		fieldNameMap.put("LinearShuntCompensator.phaseConnection", "ShuntCompensator.phaseConnection");
		fieldNameMap.put("LinearShuntCompensator.grounded", "ShuntCompensator.grounded");
		fieldNameMap.put("LinearShuntCompensator.normalSections", "ShuntCompensator.normalSections");
		fieldNameMap.put("LinearShuntCompensator.maximumSections", "ShuntCompensator.maximumSections");
		fieldNameMap.put("LinearShuntCompensator.Location", "PowerSystemResource.Location");
		fieldNameMap.put("LinearShuntCompensatorPhase.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("LinearShuntCompensatorPhase.name", "IdentifiedObject.name");
		fieldNameMap.put("LinearShuntCompensatorPhase.phase", "ShuntCompensatorPhase.phase");
		fieldNameMap.put("LinearShuntCompensatorPhase.normalSections", "ShuntCompensatorPhase.normalSections");
		fieldNameMap.put("LinearShuntCompensatorPhase.maximumSections", "ShuntCompensatorPhase.maximumSections");
		fieldNameMap.put("LinearShuntCompensatorPhase.ShuntCompensator", "ShuntCompensatorPhase.ShuntCompensator");
		fieldNameMap.put("LinearShuntCompensatorPhase.Location", "PowerSystemResource.Location");
		fieldNameMap.put("RegulatingControl.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("RegulatingControl.name", "IdentifiedObject.name");
		fieldNameMap.put("RegulatingControl.Location", "PowerSystemResource.Location");
		fieldNameMap.put("PowerTransformerInfo.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("PowerTransformerInfo.name", "IdentifiedObject.name");
		fieldNameMap.put("TransformerTankInfo.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("TransformerTankInfo.name", "IdentifiedObject.name");
		fieldNameMap.put("TransformerEndInfo.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("TransformerEndInfo.name", "IdentifiedObject.name");
		fieldNameMap.put("NoLoadTest.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("NoLoadTest.name", "IdentifiedObject.name");
		fieldNameMap.put("NoLoadTest.basePower", "TransformerTest.basePower");
		fieldNameMap.put("NoLoadTest.temperature", "TransformerTest.temperature");
		fieldNameMap.put("ShortCircuitTest.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("ShortCircuitTest.name", "IdentifiedObject.name");
		fieldNameMap.put("ShortCircuitTest.basePower", "TransformerTest.basePower");
		fieldNameMap.put("ShortCircuitTest.temperature", "TransformerTest.temperature");
		fieldNameMap.put("Asset.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("Asset.name", "IdentifiedObject.name");
		fieldNameMap.put("TapChangerInfo.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("TapChangerInfo.name", "IdentifiedObject.name");
		fieldNameMap.put("TapChangerControl.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("TapChangerControl.name", "IdentifiedObject.name");
		fieldNameMap.put("TapChangerControl.mode", "RegulatingControl.mode");
		fieldNameMap.put("TapChangerControl.Terminal", "RegulatingControl.Terminal");
		fieldNameMap.put("TapChangerControl.monitoredPhase", "RegulatingControl.monitoredPhase");
		fieldNameMap.put("TapChangerControl.enabled", "RegulatingControl.enabled");
		fieldNameMap.put("TapChangerControl.discrete", "RegulatingControl.discrete");
		fieldNameMap.put("TapChangerControl.targetValue", "RegulatingControl.targetValue");
		fieldNameMap.put("TapChangerControl.targetDeadband", "RegulatingControl.targetDeadband");
		fieldNameMap.put("TapChangerControl.Location", "PowerSystemResource.Location");
		fieldNameMap.put("RatioTapChanger.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("RatioTapChanger.name", "IdentifiedObject.name");
		fieldNameMap.put("RatioTapChanger.TapChangerControl", "TapChanger.TapChangerControl");
		fieldNameMap.put("RatioTapChanger.highStep", "TapChanger.highStep");
		fieldNameMap.put("RatioTapChanger.lowStep", "TapChanger.lowStep");
		fieldNameMap.put("RatioTapChanger.neutralStep", "TapChanger.neutralStep");
		fieldNameMap.put("RatioTapChanger.normalStep", "TapChanger.normalStep");
		fieldNameMap.put("RatioTapChanger.neutralU", "TapChanger.neutralU");
		fieldNameMap.put("RatioTapChanger.initialDelay", "TapChanger.initialDelay");
		fieldNameMap.put("RatioTapChanger.subsequentDelay", "TapChanger.subsequentDelay");
		fieldNameMap.put("RatioTapChanger.ltcFlag", "TapChanger.ltcFlag");
		fieldNameMap.put("RatioTapChanger.controlEnabled", "TapChanger.controlEnabled");
		fieldNameMap.put("RatioTapChanger.step", "TapChanger.step");
		fieldNameMap.put("RatioTapChanger.Location", "PowerSystemResource.Location");
		fieldNameMap.put("ACLineSegment.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("ACLineSegment.name", "IdentifiedObject.name");
		fieldNameMap.put("ACLineSegment.EquipmentContainer", "Equipment.EquipmentContaine");
		fieldNameMap.put("ACLineSegment.BaseVoltage", "ConductingEquipment.BaseVoltage");
		fieldNameMap.put("ACLineSegment.Location", "PowerSystemResource.Location");
		fieldNameMap.put("ACLineSegment.length", "Conductor.length");
		fieldNameMap.put("ACLineSegment.Location", "PowerSystemResource.Location");
		fieldNameMap.put("ACLineSegmentPhase.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("ACLineSegmentPhase.name", "IdentifiedObject.name");
		fieldNameMap.put("ACLineSegmentPhase.Location", "PowerSystemResource.Location");
		fieldNameMap.put("LoadBreakSwitch.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("LoadBreakSwitch.name", "IdentifiedObject.name");
		fieldNameMap.put("LoadBreakSwitch.EquipmentContainer", "Equipment.EquipmentContainer");
		fieldNameMap.put("LoadBreakSwitch.BaseVoltage", "ConductingEquipment.BaseVoltage");
		fieldNameMap.put("LoadBreakSwitch.breakingCapacity", "ProtectedSwitch.breakingCapacity");
		fieldNameMap.put("LoadBreakSwitch.ratedCurrent", "Switch.ratedCurrent");
		fieldNameMap.put("LoadBreakSwitch.normalOpen", "Switch.normalOpen");
		fieldNameMap.put("LoadBreakSwitch.open", "Switch.open");
		fieldNameMap.put("LoadBreakSwitch.retained", "Switch.retained");
		fieldNameMap.put("LoadBreakSwitch.Location", "PowerSystemResource.Location");
		fieldNameMap.put("SwitchPhase.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("SwitchPhase.name", "IdentifiedObject.name");
		fieldNameMap.put("SwitchPhase.Location", "PowerSystemResource.Location");
		fieldNameMap.put("LoadResponseCharacteristic.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("LoadResponseCharacteristic.name", "IdentifiedObject.name");
		fieldNameMap.put("EnergyConsumer.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("EnergyConsumer.name", "IdentifiedObject.name");
		fieldNameMap.put("EnergyConsumer.EquipmentContainer", "Equipment.EquipmentContainer");
		fieldNameMap.put("EnergyConsumer.BaseVoltage", "ConductingEquipment.BaseVoltage");
		fieldNameMap.put("EnergyConsumer.Location", "PowerSystemResource.Location");
		fieldNameMap.put("EnergyConsumerPhase.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("EnergyConsumerPhase.name", "IdentifiedObject.name");
		fieldNameMap.put("EnergyConsumerPhase.Location", "PowerSystemResource.Location");
		fieldNameMap.put("PerLengthPhaseImpedance.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("PerLengthPhaseImpedance.name", "IdentifiedObject.name");
		fieldNameMap.put("PerLengthSequenceImpedance.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("PerLengthSequenceImpedance.name", "IdentifiedObject.name");
		fieldNameMap.put("TransformerCoreAdmittance.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("TransformerCoreAdmittance.name", "IdentifiedObject.name");
		fieldNameMap.put("TransformerMeshImpedance.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("TransformerMeshImpedance.name", "IdentifiedObject.name");
		fieldNameMap.put("PowerTransformerEnd.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("PowerTransformerEnd.name", "IdentifiedObject.name");
		fieldNameMap.put("PowerTransformerEnd.endNumber", "TransformerEnd.endNumber");
		fieldNameMap.put("PowerTransformerEnd.grounded", "TransformerEnd.grounded");
		fieldNameMap.put("PowerTransformerEnd.Terminal", "TransformerEnd.Terminal");
		fieldNameMap.put("PowerTransformerEnd.BaseVoltage", "TransformerEnd.BaseVoltage");
		fieldNameMap.put("TransformerTank.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("TransformerTank.name", "IdentifiedObject.name");
		fieldNameMap.put("TransformerTank.EquipmentContainer", "Equipment.EquipmentContainer");
		fieldNameMap.put("TransformerTank.Location", "PowerSystemResource.Location");
		fieldNameMap.put("TransformerTankEnd.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("TransformerTankEnd.name", "IdentifiedObject.name");
		fieldNameMap.put("TransformerTankEnd.endNumber", "TransformerEnd.endNumber");
		fieldNameMap.put("TransformerTankEnd.grounded", "TransformerEnd.grounded");
		fieldNameMap.put("TransformerTankEnd.Terminal", "TransformerEnd.Terminal");
		fieldNameMap.put("TransformerTankEnd.BaseVoltage", "TransformerEnd.BaseVoltage");
		fieldNameMap.put("PowerTransformer.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("PowerTransformer.name", "IdentifiedObject.name");
		fieldNameMap.put("PowerTransformer.EquipmentContainer", "Equipment.EquipmentContainer");
		fieldNameMap.put("PowerTransformer.Location", "PowerSystemResource.Location");
		fieldNameMap.put("PhaseImpedanceData.mRID", "IdentifiedObject.mRID");
		fieldNameMap.put("PhaseImpedanceData.name", "IdentifiedObject.name");
		
		referenceMap.put("phaseConnection","PhaseShuntConnectionKind");
		referenceMap.put("phase","SinglePhaseKind");
		referenceMap.put("phaseSide1","SinglePhaseKind");
		referenceMap.put("phaseSide2","SinglePhaseKind");
		referenceMap.put("connectionKind","WindingConnection");
		referenceMap.put("phases","PhaseCode");
		referenceMap.put("mode","PhaseCode");
		referenceMap.put("monitoredPhase","RegulatingControlModeKind");
		referenceMap.put("tculControlMode","TransformerControlMode");
//		ShuntCompensator.phaseConnection	PhaseShuntConnectionKind
//		EnergyConsumer.phaseConnection	PhaseShuntConnectionKind
//		EnergyConsumer.phase	SinglePhaseKind
//		ShuntCompensatorPhase.phase	SinglePhaseKind
//		ACLineSegmentPhase.phase	SinglePhaseKind
//		TransformerEndInfo.connectionKind  WindingConnection
//		PowerTransformerEnd.connectionKind   WindingConnection
//
//		TransformerTankEnd.phases   PhaseCode
//		RegulatingControl.mode	RegulatingControlModeKind
//		RegulatingControl.monitoredPhase	PhaseCode
//		RatioTapChanger.tculControlMode	TransformerControlMode
	}

}
