# file: xy_to_json.py
# for now, manually delete comma after the last array entry
ip = open ("oneline_busxy.glm", "r")
op = open ("oneline_xy.json", "w")
print("{\"coordinates\":[", file=op)
for line in ip:
	lst = line.split(",")
	if len(lst) > 1:
		if float(lst[1]) != 0 or float(lst[2]) != 0:
			print("{\"node\":", lst[0], ",\"x\":", float(lst[1]), ",\"y\":", float(lst[2]), "},", sep="", file=op)
ip.close()
print("]}", file=op)
op.close()
