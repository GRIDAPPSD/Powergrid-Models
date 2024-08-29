ieee-118-bus.raw from https://github.com/ITI/models/tree/master/electric-grid/physical/reference/ieee-118bus/models

ReadRaw_IEEE118:	
1. need to modify raw file to ensure right format
	add "BEGIN BUS DATA"
	add space to "0 / END OF SWITCHED SHUNT DATA"
2. no column names
3. "BRANCH": different column names as WECC240
4. "TRANSFORMER": 0 nomv1, 0 nomv2
5. "FIXED SHUNT" exists, but "SWITCH SHUNT" not

RawToCIM_IEEE118:
1. haven't write "FIXED SHUNT"
2. haven't write "TRANSFORMER" as zbase = 0
3. assume all generators are "Hydro"