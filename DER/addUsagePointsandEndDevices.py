import uuid
from Meas import constants
from SPARQLWrapper import SPARQLWrapper2
import xml.etree.ElementTree as ET
from lxml import etree, objectify
from xml.etree.ElementTree import Comment
from xml.dom import minidom
import sys
from os import path


'''
since I query from outside the docker container
'''
blazegraph_url = "http://localhost:8889/bigdata/sparql"
usagePoints = {}
endDevices = {}
equipments = {}
usagePendDDict = {}
usedUsagePoints = {}
usedEndDevices = {}
usedEquipments = {}
usedUsagePendDDict = {}


class UsagePoint(object):
    '''
    class represent a usage point
    '''

    def __init__(self, name=None, mrid=None):
        self.name = name
        if mrid:
            self.mrid = mrid
        else:
            self.mrid = uuid.uuid4()


class EndDevice(object):
    '''
    class represent an end device
    '''

    def __init__(self, name=None, mrid=None, isSmartInverter=None, usagePoint=None, upID=None):
        self.name = name
        if mrid:
            self.mrid = mrid
        else:
            self.mrid = uuid.uuid4()
        self.isSmartInverter = isSmartInverter
        self.usagePoint = usagePoint
        self.upID = upID


class Equipment(object):
    '''
    class represent a power electronics connection
    '''

    def __init__(self, mrid=None, usagePoint=None, upID=None, tp=None):
        if mrid:
            self.mrid = mrid
        else:
            self.mrid = uuid.uuid4()
        self.usagePoint = usagePoint
        self.upID = upID
        self.type = tp


def getPowerElectronicsConnection(sparql):
    '''
    retrieve all power electronics connection from the blazegraph
    :param sparql:
    :return:
    '''
    query = \
        (constants.prefix +
         "SELECT ?pec ?name ?mrid WHERE {"
         "?pec r:type c:PowerElectronicsConnection ."
         "?pec c:IdentifiedObject.name ?name ."
         "?pec c:IdentifiedObject.mRID ?mrid ."
         "}"
         )

    # Set and execute the query.
    sparql.setQuery(query)
    return sparql.query()


def getSynchronousMachine(sparql):
    '''
    retrieve all synchronous machine from the blazegraph
    :param sparql:
    :return:
    '''
    query = \
        (constants.prefix +
         "SELECT ?syncMachine ?name ?mrid WHERE {"
         "?syncMachine r:type c:SynchronousMachine ."
         "?syncMachine c:IdentifiedObject.name ?name ."
         "?syncMachine c:IdentifiedObject.mRID ?mrid ."
         "}"
         )
    # Set and execute the query.
    sparql.setQuery(query)
    return sparql.query()


def insertUPsAndEDs(pecs, machines):
    '''
    loop through each power electronics connection and synchronous machine to generate RDF triples for the blazegraph
    :param pecs:
    :param machines:
    :return:
    '''
    for pec in pecs.bindings:
        # print(pec['pec'].value)
        # a = pec['name'].value
        # print(a)
        eqid = pec['mrid'].value
        # print(b)
        if eqid in equipments:
            usage = equipments[eqid].usagePoint
            usedUsagePoints[usage.mrid] = usage
            usedEquipments[eqid] = equipments[eqid]
            if usage in usagePendDDict:
                enddevice = usagePendDDict[usage]
                usedEndDevices[enddevice.mrid] = enddevice
                usedUsagePendDDict[usage] = enddevice
        else:
            usage = UsagePoint(name='usagePoint_' + pec['name'].value)
            usedUsagePoints[usage.mrid] = usage
            usedEquipments[eqid] = Equipment(mrid=eqid, usagePoint=usage, upID=usage.mrid, tp="pec")
            enddevice = EndDevice(name='endDevice_' + pec['name'].value)
            enddevice.isSmartInverter = True
            usedEndDevices[enddevice.mrid] = enddevice
            usedUsagePendDDict[usage] = enddevice
        insertUPandED(pec['pec'].value, usage, enddevice)
    for machine in machines.bindings:
        eqid = machine['mrid'].value
        # print(machine['syncMachine'].value)
        if eqid in equipments:
            usage = equipments[eqid].usagePoint
            usedUsagePoints[usage.mrid] = usage
            usedEquipments[eqid] = equipments[eqid]
            if usage in usagePendDDict:
                enddevice = usagePendDDict[usage]
                usedEndDevices[enddevice.mrid] = enddevice
                usedUsagePendDDict[usage] = enddevice
        else:
            usage = UsagePoint(name='usagePoint_' + machine['name'].value)
            usedUsagePoints[usage.mrid] = usage
            usedEquipments[eqid] = Equipment(mrid=eqid, usagePoint=usage, upID=usage.mrid, tp="syn")
            enddevice = EndDevice(name='endDevice_' + machine['name'].value)
            enddevice.isSmartInverter = False
            usedEndDevices[enddevice.mrid] = enddevice
            usedUsagePendDDict[usage] = enddevice
        insertUPandED(machine['syncMachine'].value, usage, enddevice)


def insertUPandED(equipment, usagePoint, enddevice):
    '''
    insert RDF triples for each equipment
    :param equipment: the url of the equipment in the blazegraph
    :param usagePoint: UsagePoint object related to this equipment
    :param enddevice: EndDevice object related to this equipment
    :return:
    '''
    q = (constants.prefix + 'INSERT DATA { ')
    upmRIDStr = str(usagePoint.mrid)
    if upmRIDStr[0] != '_':
        upmRIDStr = '_' + upmRIDStr
    edmRIDStr = str(enddevice.mrid)
    if edmRIDStr[0] != '_':
        edmRIDStr = '_' + edmRIDStr
    equipment = '<' + equipment + '>'
    uPoint = '<' + blazegraph_url + '#' + upmRIDStr + '>'
    eDevice = '<' + blazegraph_url + '#' + edmRIDStr + '>'

    # usage point property triples
    q += \
        uPoint + ' a c:UsagePoint . ' \
        + uPoint + ' c:IdentifiedObject.mRID \"' + upmRIDStr + '\" . ' \
        + uPoint + ' c:IdentifiedObject.name \"' + usagePoint.name + '\" . '

    # end device property triples
    q += \
        eDevice + ' a c:EndDevice . ' \
        + eDevice + ' c:IdentifiedObject.mRID \"' + edmRIDStr + '\" . ' \
        + eDevice + ' c:IdentifiedObject.name \"' + enddevice.name + '\" . ' \
        + eDevice + ' c:EndDevice.isSmartInverter \"' + str(enddevice.isSmartInverter) + '\" . ' \
        + eDevice + ' c:EndDevice.UsagePoint ' + uPoint + ' . ' \

    # equipment property triples
    q += equipment + ' c:Equipment.UsagePoint ' + uPoint + ' .'
    # Update query
    q += '}'

    # Make update in triplestore.
    sparql.setQuery(q)
    sparql.method = 'POST'
    ret = sparql.query()
    print(ret)


def exportCIMXML(pecs, machines):
    '''generate xml files that can be uploaded to the blazegraph via blazegraph workbench
    loop through each equipment: i.e., power electronics connection and synchronous machine
    create EndDevice and UsagePoint object for each equipment, call addAEquipmentToCIMXML to generate the actual xml element
    command in terminal is:
    curl -s -D- -H 'Content-Type: application/xml' --upload-file "path/to/endDevicesAndUsagePoints.xml" -X POST "http://localhost:8889/bigdata/sparql"

    :param pecs:
    :param machines:
    :return:
    '''
    root = ET.Element('rdf:RDF', attrib={'xmlns:cim': 'http://iec.ch/TC57/CIM100#', 'xmlns:rdf': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'})

    # # comment = Comment('un-comment this line to enable validation\n')
    # # root.append(comment)
    #
    # for pec in pecs.bindings:
    #     print(pec['pec'].value)
    #     usage = UsagePoint(name='usagePoint_' + pec['name'].value)
    #     enddevice = EndDevice(name='endDevice_' + pec['name'].value)
    #     enddevice.isSmartInverter = True
    #     addAEquipmentToCIMXML("cim:PowerElectronicsConnection", pec['mrid'].value, usage, enddevice, root)
    # for machine in machines.bindings:
    #     print(machine['syncMachine'].value)
    #     usage = UsagePoint(name='usagePoint_' + machine['name'].value)
    #     enddevice = EndDevice(name='endDevice_' + machine['name'].value)
    #     enddevice.isSmartInverter = False
    #     addAEquipmentToCIMXML("cim:SynchronousMachine", machine['mrid'].value, usage, enddevice, root)
    for eqID, equipment in usedEquipments.items():
        usage = equipment.usagePoint
        enddevice = usedUsagePendDDict[usage]
        if equipment.type == "pec":
            addAEquipmentToCIMXML("cim:PowerElectronicsConnection", eqID, usage, enddevice, root)
        if equipment.type == "syn":
            addAEquipmentToCIMXML("cim:SynchronousMachine", eqID, usage, enddevice, root)

    rough_string = ET.tostring(root, 'utf-8')
    reparsed = minidom.parseString(rough_string)
    with open("endDevicesAndUsagePoints.xml", "w") as f:
        f.write(reparsed.toprettyxml(indent="  "))


def addAEquipmentToCIMXML(elementName, equipmentID, usagePoint, enddevice, root):
    '''
generate xml elements for each equipment
    :param elementName: the name of the xml element that represent the equipment object in xml,
            either cim:PowerElectronicsConnection for power electronics connection
            or cim:SynchronousMachine for synchronous machine,
    :param equipmentID: the mRID of the equipment,
    :param usagePoint: the UsagePoint object related to this equipment
    :param enddevice: the EndDevice object related to this equipment
    :param root: the root of the xml element
    :return:
    '''
    upmRIDStr = str(usagePoint.mrid)
    if upmRIDStr[0] != '_':
        upmRIDStr = '_' + upmRIDStr
    edmRIDStr = str(enddevice.mrid)
    if edmRIDStr[0] != '_':
        edmRIDStr = '_' + edmRIDStr

    thisUsagePoint = ET.SubElement(root, 'cim:UsagePoint', attrib={'rdf:ID': upmRIDStr})
    ET.SubElement(thisUsagePoint, "cim:IdentifiedObject.mRID").text = upmRIDStr
    ET.SubElement(thisUsagePoint, "cim:IdentifiedObject.name").text = usagePoint.name

    thisEndDevice = ET.SubElement(root, 'cim:EndDevice', attrib={'rdf:ID': edmRIDStr})
    ET.SubElement(thisEndDevice, "cim:IdentifiedObject.mRID").text = edmRIDStr
    ET.SubElement(thisEndDevice, "cim:IdentifiedObject.name").text = enddevice.name
    ET.SubElement(thisEndDevice, "cim:EndDevice.isSmartInverter").text = str(enddevice.isSmartInverter)
    ET.SubElement(thisEndDevice, "cim:EndDevice.UsagePoint", attrib={'rdf:resource': "#" + upmRIDStr})

    thisEquipment = ET.SubElement(root, elementName, attrib={'rdf:ID': equipmentID})
    ET.SubElement(thisEquipment, "cim:Equipment.UsagePoint", attrib={'rdf:resource': "#" + upmRIDStr})


def usage():
    print("usage: python addUsagePointsandEndDevices.py <input xml file full path>")
    print("input xml file should have usage point with end device.")


def parseUsagePoint(e):
    for se in e:
        st = str(se.tag).split('}')
        if len(st) == 2 and st[1] == "IdentifiedObject.name":
            name = se.text
        if len(st) == 2 and st[1] == "IdentifiedObject.mRID":
            mrid = se.text
    return UsagePoint(name=name, mrid=mrid)


def parseEndDevice(e):
    for se in e:
        st = str(se.tag).split('}')
        if len(st) == 2 and st[1] == "IdentifiedObject.name":
            name = se.text
        if len(st) == 2 and st[1] == "IdentifiedObject.mRID":
            mrid = se.text
        if len(st) == 2 and st[1] == "EndDevice.isSmartInverter":
            isSmart = se.text
        if len(st) == 2 and st[1] == "EndDevice.UsagePoint":
            up = get_id(se.attrib)
    return EndDevice(name=name, mrid=mrid, isSmartInverter=isSmart, upID=up)


def parseEquipment(e):
    for se in e:
        st = str(se.tag).split('}')
        if len(st) == 2 and st[1] == "Equipment.UsagePoint":
            return get_id(se.attrib)
    return None


def readxmlFile(f):
    tree = ET.parse(f)
    root = tree.getroot()
    for e in root:
        t = str(e.tag).split('}')
        if len(t) == 2:
            if t[1] == "UsagePoint":
                node = parseUsagePoint(e)
                usagePoints[node.mrid] = node
            elif t[1] == "EndDevice":
                node = parseEndDevice(e)
                endDevices[node.mrid] = node
            elif t[1] == "SynchronousMachine":
                id = get_id(e.attrib)
                eq = Equipment(mrid=id, tp="syn")
                eq.upID = parseEquipment(e)
                equipments[eq.mrid] = eq
            elif t[1] == "PowerElectronicsConnection":
                id = get_id(e.attrib)
                eq = Equipment(mrid=id, tp="pec")
                eq.upID = parseEquipment(e)
                equipments[eq.mrid] = eq
            else:
                pass
    for k, v in endDevices.items():
        if v.upID in usagePoints:
            v.usagePoint = usagePoints[v.upID]
            usagePendDDict[v.usagePoint] = v
    for k, v in equipments.items():
        if v.upID in usagePoints:
            v.usagePoint = usagePoints[v.upID]


def get_id(attrib):
    for k, v in attrib.items():
        if v.startswith("#"):
            return v[1:]
        elif v.startswith("_"):
            return v
    return None


if __name__ == "__main__":
    if len(sys.argv) != 2:
        usage()
        sys.exit()
    inputFile = sys.argv[1]
    if path.isfile(inputFile):
        readxmlFile(inputFile)
    sparql = SPARQLWrapper2(blazegraph_url)
    pecs = getPowerElectronicsConnection(sparql)
    machines = getSynchronousMachine(sparql)
    print(pecs)
    print(machines)

    insertUPsAndEDs(pecs, machines)
    exportCIMXML(pecs, machines)