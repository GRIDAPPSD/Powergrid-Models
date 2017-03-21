set JENA_HOME=c:\apache-jena-3.1.0
set CLASSPATH=.;c:/apache-jena-3.1.0/lib/*;c:/commons-math3-3.6.1/*

javac CDPSM_to_GLM.java

rem java CDPSM_to_GLM -p=c -e=u -f=60 -v=0.001 -s=0.001 -q=y IEEE13.XML ieee13
rem java CDPSM_to_GLM -p=c -e=u -f=60 -v=0.001 -s=0.001 -q=y IEEE13_Assets.XML ieee13assets
rem java CDPSM_to_GLM -p=c -e=u -f=60 -v=0.001 -s=0.001 -q=y IEEE8500u.XML ieee8500u
rem java CDPSM_to_GLM -l=0.2 -t=y -e=u -f=60 -v=1 -s=1 -q=y IEEE8500.XML ieee8500
rem java CDPSM_to_GLM -l=0.2 -t=y -e=u -f=60 -v=1 -s=1 -q=y -n=zipload_schedule IEEE8500.XML ieee8500zip
java CDPSM_to_GLM -l=0.2 -t=y -e=u -f=60 -v=1 -s=1 -q=y -n=zipload_schedule -z=0.3 -i=0.3 -p=0.4 IEEE8500.XML ieee8500zip

