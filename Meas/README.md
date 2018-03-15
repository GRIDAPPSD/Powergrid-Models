Under powergrid-models/Meas, there are three sample Python files that implement this function:

1) "python ListFeeders.py" produces an index of feeders in the triple-store. Find the mRID of the feeder you want to instrument.

2) "python ListMeasureables.py _4F76A5F9-271D-9EB8-5E31-AA362D86F2C3 rawlist.txt" produces a list of ConductingEquipment instances that can have sensors attached, written to rawlist.txt.  The example mRID corresponds to the IEEE 8500-node feeder, circa March 2018.

3) hand-prune or script-prune rawlist.txt so that it has only the sensors that should be part of the simulation, and save this to, for example, "ieee8500_measurements.txt". In step 2, rawlist.txt has 8455 lines for candidate measurements, but ieee8500_measurements.txt has only 54 lines to support the VVO app circa RC1.  

For voltage measurements, the type is PNV and you need to find an ACLineSegment, LoadBreakSwitch, LinearShuntCompensator, PowerTransformer+PowerTransformerEnd or EnergyConsumer that's connected to the ConnectivityNode (aka bus) of interest.

For current and power measurements, the type is A or VA, respectively, and the sign convention is positive into the ConnectivityNode (aka bus) of interest.  Similar the case of PNV, you have to find the ConductingEquipment instance that carries the current or power you wish to measure.

For tap measurements, the type is POS and you find the PowerTransformer+RatioTapChanger of interest. For switch state measurements, the type is POS and you'll find the LinearShuntCompensator or LoadBreakSwitch of interest.

Through this process, you would remove lines from rawlist.txt, but don't add or change any lines.  Those types of changes won't be understood in the next step.

4) "python InsertMeasurements.py ieee8500_measurements.txt" will insert those measurements into the triple-store.  This Python code makes some assumptions (e.g. each capacitor, aka LinearShuntCompensator, in the file will get a PNV, VA and POS measurement).  The battery and photovoltaic measurements are not implemented.  You can modify this Python file to implement your own measurement strategies.

5) from a browser interface, the following SPARQL query lists the measurement instances, 78 of them, created in step 4:

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

6) If you need to re-do step 4, try this SPARQL query, but note that it doesn't select by feeder.  You can implement feeder selection by following other examples in ../blazegraph/queries.txt

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

7) whenever you create a new feeder model, it will also create a JSON file that describes the sensors added in step 4
