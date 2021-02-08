import json

# samples to look up wires and cables by name
wires=json.load(open('overhead.json'))
print (wires['2AL']['gmr'])

cables=json.load(open('underground.json'))
print (cables['4/0CN15'])

# reverse lookup wire names by mrid
wirenames = {}
for name in wires.keys():
	wirenames[wires[name]['mrid']] = name

print (wirenames)
