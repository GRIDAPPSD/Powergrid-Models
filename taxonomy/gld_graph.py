"""
"""
import re
import math
import sys
import matplotlib.pyplot as plt
import networkx as nx

def obj(parent,model,line,itr,oidh,octr):
	'''
	Store an object in the model structure
	Inputs:
		parent: name of parent object (used for nested object defs)
		model: dictionary model structure
		line: glm line containing the object definition
		itr: iterator over the list of lines
		oidh: hash of object id's to object names
		octr: object counter
	'''
	octr += 1
	# Identify the object type
	m = re.search('object ([^:{\s]+)[:{\s]',line,re.IGNORECASE)
	type = m.group(1)
	# If the object has an id number, store it
	n = re.search('object ([^:]+:[^{\s]+)',line,re.IGNORECASE)
	if n:
		oid = n.group(1)
	line = next(itr)
	# Collect parameters
	oend = 0
	oname = None
	params = {}
	if parent is not None:
		params['parent'] = parent
	while not oend:
		m = re.match('\s*(\S+) ([^;\s]+)[;\s]',line)
		if m:
			# found a parameter
			param = m.group(1)
			val = m.group(2)
			if param == 'name':
				oname = val
			elif param == 'object':
				# found a nested object
				if oname is None:
					print('ERROR: nested object defined before parent name')
					quit()
				line,octr = obj(oname,model,line,itr,oidh,octr)
			elif val == 'object':
				# found an inline object
				line,octr = obj(None,model,line,itr,oidh,octr)
				params[param] = 'OBJECT_'+str(octr)
			else:
				params[param] = val
		if re.search('}',line):
			oend = 1
		else:
			line = next(itr)
	# If undefined, use a default name
	if oname is None:
		oname = 'OBJECT_'+str(octr)
	oidh[oname] = oname
	# Hash an object identifier to the object name
	if n:
		oidh[oid] = oname
	# Add the object to the model
	if type not in model:
		# New object type
		model[type] = {}
	model[type][oname] = {}
	for param in params:
		model[type][oname][param] = params[param]
	# Return the 
	return line,octr

def is_node_class(s):
	if s == 'node':
		return True
	if s == 'load':
		return True
	if s == 'meter':
		return True
	if s == 'triplex_node':
		return True
	if s == 'triplex_meter':
		return True
	return False

def is_edge_class(s):
	if s == 'switch':
		return True
	if s == 'fuse':
		return True
	if s == 'regulator':
		return True
	if s == 'transformer':
		return True
	if s == 'overhead_line':
		return True
	if s == 'underground_line':
		return True
	if s == 'triplex_line':
		return True
	return False

def print_model(model,h):
	for t in model:
		print(t+':')
		for o in model[t]:
			print('\t'+o+':')
			for p in model[t][o]:
				if ':' in model[t][o][p]:
					print('\t\t'+p+'\t-->\t'+h[model[t][o][p]])
				else:
					print('\t\t'+p+'\t-->\t'+model[t][o][p])

#----------------------------
# Read file from command line
#----------------------------
arg = sys.argv[1]
inf = open(arg,'r')

#-----------------------
# Pull Model Into Memory
#-----------------------
lines = []
line = inf.readline()
while line is not '':
	while re.match('\s*//',line) or re.match('\s+$',line):
		# skip comments and white space
		line = inf.readline()
	lines.append(line)
	line = inf.readline()
inf.close()

#--------------------------
# Build the model structure
#--------------------------
octr = 0;
model = {}
h = {}		# OID hash
itr = iter(lines)
for line in itr:
	if re.search('object',line):
		line,octr = obj(None,model,line,itr,h,octr)

inf.close()
# print_model(model,h)

# construct a graph of the model, starting with known links
G = nx.Graph()
for t in model:
	if is_edge_class(t):
		for o in model[t]:
			n1 = model[t][o]['from']
			n2 = model[t][o]['to']
			G.add_edge(n1,n2,eclass=t,edata=model[t][o])

# add the parent-child node links
for t in model:
	if is_node_class(t):
		for o in model[t]:
			if 'parent' in model[t][o]:
				p = model[t][o]['parent']
				G.add_edge(o,p,eclass='parent',edata={})

# now we backfill node attributes
for t in model:
	if is_node_class(t):
		for o in model[t]:
			if o in G.nodes():
				G.nodes()[o]['nclass'] = t
				G.nodes()[o]['ndata'] = model[t][o]
			else:
				print('orphaned node', t, o)

for n1, data in G.nodes(data=True):
	if 'nclass' in data:
		print(n1, data['nclass'], data['ndata'])

for n1, n2, data in G.edges(data=True):
	if 'eclass' in data:
		print(n1, n2, data['eclass'], data['edata'])

nx.draw(G, node_size=5)
plt.show()

sub_graphs = nx.connected_component_subgraphs(G)
for sg in sub_graphs:
	print (sg.number_of_nodes())
	if sg.number_of_nodes() < 10:
		print(sg.nodes)
		print(sg.edges)

#nx.drawing.nx_agraph.write_dot(G, 'sample.dot')
print (nx.shortest_path(G, 'R3-12-47-2_load_62', 'R3-12-47-2_node_267'))
