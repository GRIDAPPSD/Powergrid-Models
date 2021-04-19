gridlabd -D WANT_VI_DUMP=1 ieee8500_house_run.glm >ieee8500_house.log
gridlabd -D WANT_VI_DUMP=1 ieee9500_house_run.glm >ieee9500_house.log
gridlabd -D WANT_VI_DUMP=1 r2_12_47_2_house_run.glm >r2_12_47_2_house.log
gridlabd -D WANT_VI_DUMP=1 ieee123pv_house_run.glm >ieee123pv_house.log
gridlabd -D WANT_VI_DUMP=1 transactive_house_run.glm >transactive_house.log

tail *.log

