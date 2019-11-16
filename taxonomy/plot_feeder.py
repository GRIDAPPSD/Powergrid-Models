import json
import matplotlib.pyplot as plt 
import matplotlib.lines as lines
import networkx as nx
import sys

edgeTypes = {'overhead_line':   {'color':'blue',       'tag':'OHD'},
            'underground_line': {'color':'orange',     'tag':'UG'}, 
            'transformer':      {'color':'gray',       'tag':'XFM'}, 
            'switch':           {'color':'green',      'tag':'SWT'},
            'fuse':             {'color':'lightcoral', 'tag':'FUSE'}, 
            'regulator':        {'color':'magenta',    'tag':'REG'}, 
            'recloser':         {'color':'red',        'tag':'REC'}, 
            'sectionalizer':    {'color':'darkred',    'tag':'SEC'}, 
            'triplex_line':     {'color':'tan',        'tag':'TPX'}, 
            'series_reactor':   {'color':'sienna',     'tag':'RCT'},
            'unknown':          {'color':'black',      'tag':'UNK'}}

def get_edge_width(nphs):
    if nphs == 1:
        return 1.0
    if nph == 2:
        return 2.0
    return 3.0

def get_edge_color(eclass):
    if eclass in edgeTypes:
        return edgeTypes[eclass]['color']
    return edgeTypes['unknown']['color']

def get_edge_mnemonic(eclass):
    if eclass in edgeTypes:
        return edgeTypes[eclass]['tag']
    return edgeTypes['unknown']['tag']

if __name__ == '__main__':
    print ('usage: python3 plot_feeder.py fdr_name nodes')
    print ('  fdr_name like R5-12.47-5')
    print ('  nodes is 1 to plot labels, 0 not to')
    feedername = 'GC-12.47-1'
    feedername = 'R1-12.47-3'
    plotLabels = False
    if len(sys.argv) > 1:
        feedername = sys.argv[1]
        if len(sys.argv) > 2:
            if int(sys.argv[2]) > 0:
                plotLabels = True

    lp = open ('new_' + feedername + '.json').read()
    feeder = json.loads(lp)
    G = nx.readwrite.json_graph.node_link_graph(feeder)
    nbus = G.number_of_nodes()
    nbranch = G.number_of_edges()
    print ('read graph with', nbus, 'nodes and', nbranch, 'edges')
#    print (G.nodes())
#    print (G.edges())

    # extract the XY coordinates available for plotting
    xy = {}
    lblNode = {}
    plotNodes = []
    for n in G.nodes():
        ndata = G.nodes()[n]['ndata']
        if 'x' in ndata:
            busx = float(ndata['x'])
            busy = float(ndata['y'])
            xy[n] = [busx, busy]
            plotNodes.append(n)
            lblNode[n] = n[11:] # skip the first 11 characters, e.g., R5-12-47-5_

    # only plot the edges that have XY coordinates at both ends
    plotEdges = []
    edgeWidths = []
    edgeColors = []
    for n1, n2, data in G.edges(data=True):
        bFound = False
        if n1 in xy:
            if n2 in xy:
                bFound = True
                nph = 3
                # try to identify the phases
                if 'edata' in data:
                    if 'phases' in data['edata']:
                        phs = data['edata']['phases']
                        if 'ABC' in phs:
                            nph = 3
                        elif 'S' in phs:
                            nph = 1
                        else:
                            nph = len(phs)
                            if 'N' in phs:
                                nph -= 1
                plotEdges.append ((n1, n2))
                edgeWidths.append (get_edge_width(nph))
                edgeColors.append (get_edge_color(data['eclass']))
        if not bFound:
            print ('unable to plot', data['ename'])

    nx.draw_networkx_nodes (G, xy, nodelist=plotNodes, node_size=3, node_color='r')
    nx.draw_networkx_edges (G, xy, edgelist=plotEdges, edge_color=edgeColors, width=edgeWidths, alpha=0.8)
    if plotLabels:
        nx.draw_networkx_labels (G, xy, lblNode, font_size=8, font_color='k')
    plt.title ('Layout of Feeder Power Components for ' + feedername)
    plt.xlabel ('X coordinate [?]')
    plt.ylabel ('Y coordinate [?]')
    plt.grid(linestyle='dotted')
    eclasses = ['overhead_line', 'underground_line', 'transformer', 'switch',
                'fuse', 'regulator', 'recloser', 'sectionalizer', 'triplex_line', 'series_reactor']
    xdata = [0, 1]
    ydata = [1, 0]
    lns = [lines.Line2D(xdata, ydata, color=get_edge_color(e)) for e in edgeTypes]
    labs = [get_edge_mnemonic (e) for e in edgeTypes]
    plt.legend(lns, labs, loc='best')
    plt.show()

