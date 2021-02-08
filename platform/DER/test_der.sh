source envars.sh

java -cp $CIMHUB_PATH $CIMHUB_PROG -s=_503D6E20-F499-4CC7-8051-971E23D0BF79 -o=both -l=1.0 -i=1 test/transactive_der

cd test
# rm *_s.csv
opendsscmd check_der.dss
gridlabd -D WANT_VI_DUMP=1 transactive_der_run.glm >transactive_der.log

tail *_s.csv
tail *.log

