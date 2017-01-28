# file: glm_to_json.py
# for now, manually delete comma after the last array entry
ip = open ("ieee8500_base.glm", "r")
op = open ("ieee8500_base.json", "w")
print("{\"feeder\":[", file=op)
print("{\"capacitors\":[", file=op)
inCaps = False
for line in ip:
	lst = line.split()
	if len(lst) > 1:
		if lst[1] == "capacitor":
			inCaps = True
		if lst[1] == "transformer_configuration":
			inCaps = False
		if inCaps == True:
			if lst[0] == "name":
				name = lst[1].strip(";")
			if lst[0] == "parent":
				nd1 = lst[1].strip(";")
			if lst[0] == "phases":
				phs = lst[1].strip(" ").strip(";")
				print("{\"name\":", name, ",\"parent\":", nd1, ",\"phases\":\"", phs, "\"},", sep="", file=op)
print("]}", file=op)
print("]}", file=op)
ip.close()
op.close()
