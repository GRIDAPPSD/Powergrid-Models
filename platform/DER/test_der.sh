source envars.sh

java -cp $CIMHUB_PATH $CIMHUB_PROG -s=_503D6E20-F499-4CC7-8051-971E23D0BF79 -o=both -l=1.0 -i=1 test/transactive_der

cd test
opendsscmd transactive_der_base.dss
gridlabd -D WANT_VI_DUMP=1 transactive_der_run.glm >transactive_der.log

tail *.log

