rm *.csv
gridlabd ieee8500_run.glm
cat vreg*.csv | grep "00:00:01"
./showvolts.sh
