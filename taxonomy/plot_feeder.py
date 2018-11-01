import json
#import numpy as np
import matplotlib.pyplot as plt 
import networkx as nx
#import math 

if __name__ == '__main__':
	feedername = 'R5-12.47-5'
	lp = open ('new_' + feedername + '.json').read()
	feeder = json.loads(lp)
	G = nx.readwrite.json_graph.node_link_graph(feeder)
	nbus = G.number_of_nodes()
	nbranch = G.number_of_edges()
	print ('read graph with', nbus, 'nodes and', nbranch, 'edges')

	# extract the XY coordinates available for plotting
	xy = {}
	plotnodes = set()
	for n in G.nodes():
		ndata = G.nodes()[n]['ndata']
		if 'x' in ndata:
			busx = float(ndata['x'])
			busy = float(ndata['y'])
			xy[n] = [busx, busy]
			plotnodes.add(n)

	# only plot the edges that have XY coordinates at both ends
	plotedges = set()
	for e in G.edges():
		bFound = False
		if e[0] in xy:
			if e[1] in xy:
				plotedges.add(e)
				bFound = True
		if not bFound:
			print ('unable to plot', e)

	nx.draw_networkx_nodes (G, xy, nodelist=list(plotnodes), node_size=1, node_color='r')
	nx.draw_networkx_edges (G, xy, edgelist=list(plotedges), edge_color='b')
	plt.title ('Layout of Feeder Power Components for ' + feedername)
	plt.xlabel ('X coordinate [?]')
	plt.ylabel ('Y coordinate [?]')
	plt.grid(linestyle='dotted')
	plt.show()

