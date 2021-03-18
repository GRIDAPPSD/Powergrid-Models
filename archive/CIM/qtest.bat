set JENA_HOME=c:\apache-jena-3.1.0
set CLASSPATH=.;c:/apache-jena-3.1.0/lib/*;c:/commons-math3-3.6.1/*

javac SPARQLcimTest.java

java SPARQLcimTest -e=u ieee13.xml
java SPARQLcimTest -e=u ieee8500.xml

