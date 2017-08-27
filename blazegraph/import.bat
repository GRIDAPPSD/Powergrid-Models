set JENA_HOME=c:\apache-jena-3.1.0
set CLASSPATH=.;c:/apache-jena-3.1.0/lib/*;c:/commons-math3-3.6.1/*

javac GldNode.java

javac DistBaseVoltage.java
javac DistCapacitor.java
javac DistComponent.java
javac DistConcentricNeutralCable.java
javac DistCoordinates.java
javac DistLinesCodeZ.java
javac DistLinesInstanceZ.java
javac DistLineSegment.java
javac DistLineSpacing.java
javac DistLinesSpacingZ.java
javac DistLoad.java
javac DistOverheadWire.java
javac DistPhaseMatrix.java
javac DistPowerXfmrCore.java
javac DistPowerXfmrMesh.java
javac DistPowerXfmrWinding.java
javac DistRegulator.java
javac DistSequenceMatrix.java
javac DistSubstation.java
javac DistSwitch.java
javac DistTapeShieldCable.java
javac DistXfmrCodeOCTest.java
javac DistXfmrCodeRating.java
javac DistXfmrCodeSCTest.java
javac DistXfmrTank.java

javac CIMImporter.java

java CIMImporter -l=1 -i=1 test

