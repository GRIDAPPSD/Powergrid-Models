Under powergrid-models/Meas, there are three sample Python files that implement this function:

1. "python ListFeeders.py" produces an index of feeders in the triple-store.  Find the mRID of the feeder you want to instrument.  

2. "python ListMeasureables.py _4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 
rawlist.txt" produces a list of ConductingEquipment instances that can 
have sensors attached, written to rawlist.txt.  The example mRID 
corresponds to the IEEE 8500-node feeder, circa March 2018.  

3. edit rawlist.txt so that it has only the sensors 
that should be part of the simulation, and save this to, for example, 
"ieee8500_measurements.txt".  In step 2, rawlist.txt has 12094 lines for 
candidate measurements, but ieee8500_measurements.txt has only 54 lines to 
support the VVO app circa RC1.  

    - For voltage measurements, the type is PNV and you need to find an ACLineSegment, LoadBreakSwitch, LinearShuntCompensator, EnergyConsumer, PowerTransformer+PowerTransformerEnd or PowerElectronicsConnection that's connected to the ConnectivityNode (aka bus) of interest.  

    - For current and power measurements, the type is A or VA, respectively, and 
the sign convention is positive into the ConnectivityNode (aka bus) of 
interest.  Similar the case of PNV, you have to find the 
ConductingEquipment instance that carries the current or power you wish to 
measure.  

    - For tap measurements, the type is POS and you find the 
PowerTransformer+RatioTapChanger of interest.  For switch state 
measurements, the type is POS and you'll find the LinearShuntCompensator 
or LoadBreakSwitch of interest.  

4. Through this process, you would mostly remove lines from rawlist.txt. The other valid changes are:
     - In ACLineSegment or LoadBreakSwitch, the second token may be changed to:
         - v1 to measure PNV at bus1
         - v2 to measure PNV at bus2
         - s1 to measure VA into bus1
         - s2 to measure VA into bus2
         - i1 to measure A into bus1
         - i2 to measure A into bus2
     - In PowerTransformer+PowerTransformerEnd, the third token may be changed to:
         - v to measure PNV on that winding
         - s to measure VA into that winding
         - i to measure A into that winding
     - Note that each line specifies a separate phase measurement; three lines are needed to measure a three-phase quantity.
     - The only valid use case for adding lines to rawlist.txt is to copy-and-paste ACLineSegment, LoadBreakSwitch or PowerTransformer lines to request additional quantities, eg., v2 or s 

5. "python InsertMeasurements.py ieee8500_measurements.txt" will insert 
those measurements into the triple-store.  This Python code makes some 
assumptions (e.g.  each capacitor, aka LinearShuntCompensator, in the file 
will get a PNV, VA and POS measurement).  The LoadBreakSwitch POS 
measurements are not implemented.  You can modify this Python file to 
implement your own measurement strategies.  

6. from a browser interface, the following SPARQL query lists the 
measurement instances, 78 of them, created in step 5: 
```
# list all measurements, with buses and equipments
PREFIX r: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX c: <http://iec.ch/TC57/2012/CIM-schema-cim17#>
SELECT ?class ?type ?name ?bus ?phases ?eqtype ?eqname ?eqid ?trmid ?id WHERE {
{ ?s r:type c:Discrete. bind ("Discrete" as ?class)}
  UNION
{ ?s r:type c:Analog. bind ("Analog" as ?class)}
 ?s c:IdentifiedObject.name ?name .
 ?s c:IdentifiedObject.mRID ?id .
 ?s c:Measurement.PowerSystemResource ?eq .
 ?s c:Measurement.Terminal ?trm .
 ?s c:Measurement.measurementType ?type .
 ?trm c:IdentifiedObject.mRID ?trmid.
 ?eq c:IdentifiedObject.mRID ?eqid.
 ?eq c:IdentifiedObject.name ?eqname.
 ?eq r:type ?typeraw.
  bind(strafter(str(?typeraw),"#") as ?eqtype)
 ?trm c:Terminal.ConnectivityNode ?cn.
 ?cn c:IdentifiedObject.name ?bus.
 ?s c:Measurement.phases ?phsraw .
   {bind(strafter(str(?phsraw),"PhaseCode.") as ?phases)}
} ORDER BY ?class ?type ?name
```

7. If you need to re-do step 5, try this SPARQL query, but note that it 
doesn't select by feeder.  You can implement feeder selection by following 
other examples in ../blazegraph/queries.txt 
```
# delete all measurements
PREFIX r: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX c: <http://iec.ch/TC57/2012/CIM-schema-cim17#>
DELETE {
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:Measurement.PowerSystemResource ?psr.
 ?m c:Measurement.Terminal ?trm.
 ?m c:Measurement.phases ?phases.
 ?m c:Measurement.measurementType ?type.
} WHERE {
 VALUES ?class {c:Analog c:Discrete}
 ?m a ?class.
 ?m c:IdentifiedObject.mRID ?uuid.
 ?m c:IdentifiedObject.name ?name.
 ?m c:Measurement.PowerSystemResource ?psr.
 ?m c:Measurement.Terminal ?trm.
 ?m c:Measurement.phases ?phases.
 ?m c:Measurement.measurementType ?type.
}
```

8. whenever you create a new feeder model, it will also create a JSON file 
that describes the sensors added in step 5 
