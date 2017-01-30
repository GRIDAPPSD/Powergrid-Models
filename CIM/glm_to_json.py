# file: glm_to_json.py
# for now, manually delete comma after the last array entry
ip = open ("ieee8500_base.glm", "r")
op = open ("ieee8500_base.json", "w")
print("{\"feeder\":[", file=op)

print("{\"swing_nodes\":[", file=op)
inNodes = False
for line in ip:
	lst = line.split()
	if len(lst) > 1:
		if lst[1] == "node":
			inNodes = True
			inSwing = False
		if inNodes == True:
			if lst[0] == "name":
				name = lst[1].strip(";")
			if lst[0] == "bustype":
				if "SWING" in lst[1]:
					inSwing = True
			if lst[0] == "phases":
				phs = lst[1].strip(" ").strip(";")
			if lst[0] == "nominal_voltage":
				voltage = lst[1].strip(" ").strip(";")
				if inSwing:
					print("{\"name\":", name, ",\"phases\":\"", phs, "\",\"nominal_voltage\":", voltage, "},", sep="", file=op)
				inNodes = False
				inSwing = False
print("]},", file=op)

ip.seek(0,0)
print("{\"capacitors\":[", file=op)
inCaps = False
for line in ip:
	lst = line.split()
	if len(lst) > 1:
		if lst[1] == "capacitor":
			inCaps = True
			kvar_A = 0.0
			kvar_B = 0.0
			kvar_C = 0.0
		if inCaps == True:
			if lst[0] == "name":
				name = lst[1].strip(";")
			if lst[0] == "parent":
				nd1 = lst[1].strip(";")
			if lst[0] == "phases":
				phs = lst[1].strip(" ").strip(";")
			if lst[0] == "capacitor_A":
				kvar_A = float(lst[1].strip(" ").strip(";")) * 0.001
			if lst[0] == "capacitor_B":
				kvar_B = float(lst[1].strip(" ").strip(";")) * 0.001
			if lst[0] == "capacitor_C":
				kvar_C = float(lst[1].strip(" ").strip(";")) * 0.001
	elif len(lst) == 1:
		if inCaps and lst[0] == "}":
			print("{\"name\":", name, ",\"parent\":", nd1, ",\"phases\":\"", phs, "\",\"kvar_A\":", kvar_A, ",\"kvar_B\":", kvar_B, ",\"kvar_C\":", kvar_C, "},", sep="", file=op)
			inCaps = False
print("]},", file=op)

ip.seek(0,0)
print("{\"overhead_lines\":[", file=op)
inLines = False
for line in ip:
	lst = line.split()
	if len(lst) > 1:
		if lst[1] == "overhead_line":
			inLines = True
		if inLines == True:
			if lst[0] == "name":
				name = lst[1].strip(";")
			if lst[0] == "from":
				nd1 = lst[1].strip(";")
			if lst[0] == "to":
				nd2 = lst[1].strip(";")
			if lst[0] == "phases":
				phs = lst[1].strip(" ").strip(";")
			if lst[0] == "length":
				length = lst[1].strip(" ").strip(";")
			if lst[0] == "configuration":
				config = lst[1].strip(" ").strip(";")
				print("{\"name\":", name, ",\"from\":", nd1, ",\"to\":", nd2, ",\"phases\":\"", phs, "\",\"length\":", length, ",\"configuration\":", config, "},", sep="", file=op)
				inLines = False
print("]},", file=op)

ip.seek(0,0)
print("{\"transformers\":[", file=op)
inXfmr = False
for line in ip:
	lst = line.split()
	if len(lst) > 1:
		if lst[1] == "transformer":
			inXfmr = True
		if inXfmr == True:
			if lst[0] == "name":
				name = lst[1].strip(";")
			if lst[0] == "from":
				nd1 = lst[1].strip(";")
			if lst[0] == "to":
				nd2 = lst[1].strip(";")
			if lst[0] == "phases":
				phs = lst[1].strip(" ").strip(";")
			if lst[0] == "configuration":
				config = lst[1].strip(" ").strip(";")
				if "S" not in phs:
					print("{\"name\":", name, ",\"from\":", nd1, ",\"to\":", nd2, ",\"phases\":\"", phs, "\",\"configuration\":", config, "},", sep="", file=op)
				inXfmr = False
print("]},", file=op)

ip.seek(0,0)
print("{\"regulators\":[", file=op)
inReg = False
for line in ip:
	lst = line.split()
	if len(lst) > 1:
		if lst[1] == "regulator":
			inReg = True
		if inReg == True:
			if lst[0] == "name":
				name = lst[1].strip(";")
			if lst[0] == "from":
				nd1 = lst[1].strip(";")
			if lst[0] == "to":
				nd2 = lst[1].strip(";")
			if lst[0] == "phases":
				phs = lst[1].strip(" ").strip(";")
			if lst[0] == "configuration":
				config = lst[1].strip(" ").strip(";")
				print("{\"name\":", name, ",\"from\":", nd1, ",\"to\":", nd2, ",\"phases\":\"", phs, "\",\"configuration\":", config, "},", sep="", file=op)
				inReg = False
print("]}", file=op)

ip.close()
print("]}", file=op)
op.close()
