import json
#import numpy as np
import matplotlib.pyplot as plt 
import networkx as nx
import sys
#import math 

if __name__ == '__main__':
	print ('usage: python3 plot_feeder.py fdr_name nodes')
	print ('  fdr_name like R5-12.47-5')
	print ('  nodes is 1 to plot labels, 0 not to')
	feedername = 'R5-12.47-5'
	plotLabels = False
	if len(sys.argv) > 1:
		feedername = sys.argv[1]
		if len(sys.argv) > 2:
			if int(sys.argv[2]) > 0:
				plotLabels = True

	lp = open ('new_' + feedername + '.json').read()
	feeder = json.loads(lp)
#	G = nx.readwrite.json_graph.node_link_graph(feeder)
	G = nx.readwrite.json_graph.node_link_data(feeder)
	nbus = G.number_of_nodes()
	nbranch = G.number_of_edges()
	print ('read graph with', nbus, 'nodes and', nbranch, 'edges')
	print (G.edges())

	# extract the XY coordinates available for plotting
	xy = {}
	lblNode = {}
	plotnodes = set()
	for n in G.nodes():
		ndata = G.nodes()[n]['ndata']
		if 'x' in ndata:
			busx = float(ndata['x'])
			busy = float(ndata['y'])
			xy[n] = [busx, busy]
			plotnodes.add(n)
			lblNode[n] = n[11:] # skip the first 11 characters, e.g., R5-12-47-5_

	# only plot the edges that have XY coordinates at both ends
	plotedges = set()
	lst3ph = []
	lst2ph = []
	lst1ph = []
	w3ph = []
	w2ph = []
	w1ph = []
	for n1, n2, data in G.edges():
		if 'eclass' in data:
			print(n1, n2, data['eclass'], data['edata'])
	for n1, n2, data in G.edges(data=True):
		bFound = False
		if n1 in xy:
			if n2 in xy:
				bFound = True
				# try to identify the phases
				if bFound:
					phs = 'ABC'
					phs = data['edata']['phases']
					if 'ABC' in phs:
						nph = 3
					elif 'S' in phs:
						nph = 1
					else:
						nph = len(phs)
					if nph == 3:
						lst3ph.append ((n1, n2))
						w3ph.append (2.0)
					elif nph == 2:
						lst2ph.append ((n1, n2))
						w2ph.append (1.5)
					elif nph == 1:
						lst1ph.append ((n1, n2))
						w1ph.append (1.0)
		if not bFound:
			print ('unable to plot', data['ename'])

	nx.draw_networkx_nodes (G, xy, nodelist=list(plotnodes), node_size=1, node_color='r')
#	nx.draw_networkx_edges (G, xy, edgelist=list(plotedges), edge_color='b')
	nx.draw_networkx_edges (G, xy, edgelist=lst3ph, edge_color='b', width=w3ph, alpha=0.8)
	nx.draw_networkx_edges (G, xy, edgelist=lst2ph, edge_color='b', width=w2ph, alpha=0.8)
	nx.draw_networkx_edges (G, xy, edgelist=lst2ph, edge_color='b', width=w1ph, alpha=0.8)
	if plotLabels:
		nx.draw_networkx_labels (G, xy, lblNode, font_size=6, font_color='k')
	plt.title ('Layout of Feeder Power Components for ' + feedername)
	plt.xlabel ('X coordinate [?]')
	plt.ylabel ('Y coordinate [?]')
	plt.grid(linestyle='dotted')
	plt.show()

