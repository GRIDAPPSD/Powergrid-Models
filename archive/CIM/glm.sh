javac -classpath "/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CDPSM_to_GLM.java

java -classpath ".:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CDPSM_to_GLM -l=1 -e=u -i=1 ieee8500.xml ieee8500
#java -classpath ".:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CDPSM_to_GLM -l=0.92 -e=u ieee8500.xml ieee8500
#java -classpath ".:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CDPSM_to_GLM -l=0.8 -e=u -n=zipload_schedule IEEE8500.XML ieee8500

#java -classpath ".:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CDPSM_to_GLM -l=0.2 -e=u -n=zipload_schedule -z=0.3 -i=0.3 -p=0.4 IEEE8500.XML ieee8500zip

#java -classpath ".:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CDPSM_to_GLM -l=1 -e=u oneline.xml oneline
java -classpath ".:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CDPSM_to_GLM -l=1 -e=u ieee13.xml ieee13
java -classpath ".:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CDPSM_to_GLM -l=1 -e=u ieee13_assets.xml ieee13_assets
#java -classpath ".:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CDPSM_to_GLM -l=0.2 -e=u ieee8500u.xml ieee8500u

#java -classpath ".:/Users/mcde601/src/apache-jena-3.1.0/lib/*:/Users/mcde601/src/commons-math3-3.6.1/*" CDPSM_to_GLM -e=u IEEE13NodecktAssets_CDPSM_Combined.XML ieee13assets

# 4BusYYbal_CDPSM_Combined.XML ieee4yy
# 4busDYBal_CDPSM_Combined.XML ieee4dy
# 4busOYODBal_CDPSM_Combined.XML ieee4oyod
# ieee4OYOD-ubal_CDPSM_Combined.XML ieee4oyodu
# 4busYDBal_CDPSM_Combined.XML ieee4yd
# DGProtFdr_CDPSM_Combined.XML ieeeDG
# IEEE_30_CDPSM_Combined.XML ieee30
# NEV_CDPSM_Combined.XML ieeeNEV
# ieee123_CDPSM_Combined.XML ieee123
# ieee34-2_CDPSM_Combined.XML ieee34
# ieee37_CDPSM_Combined.XML ieee37
# ckt5_CDPSM_Combined.XML epri5
# ckt7_CDPSM_Combined.XML epri7
# ckt24_CDPSM_Combined.XML epri24

