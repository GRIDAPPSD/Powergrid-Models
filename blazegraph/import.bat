set JENA_HOME=c:\apache-jena-3.1.0
set CLASSPATH=.;c:/apache-jena-3.1.0/lib/*;c:/commons-math3-3.6.1/*

rem javac GldNode.java

rem javac DistBaseVoltage.java
rem javac DistCapacitor.java
rem javac DistComponent.java
rem javac DistConcentricNeutralCable.java
rem javac DistCoordinates.java
rem javac DistLinesCodeZ.java
rem javac DistLinesInstanceZ.java
rem javac DistLineSpacing.java
rem javac DistLinesSpacingZ.java
rem javac DistLoad.java
rem javac DistOverheadWire.java
rem javac DistPhaseMatrix.java
rem javac DistPowerXfmrCore.java
rem javac DistPowerXfmrMesh.java
rem javac DistPowerXfmrWinding.java
rem javac DistRegulator.java
rem javac DistSequenceMatrix.java
rem javac DistSubstation.java
rem javac DistSwitch.java
rem javac DistTapeShieldCable.java
rem javac DistXfmrCodeOCTest.java
rem javac DistXfmrCodeRating.java
rem javac DistXfmrCodeSCTest.java
rem javac DistXfmrTank.java

javac CIMImporter.java

java CIMImporter

